package com.gimis.dataserver.command;

import com.gimis.ps.msg.DefaultHeader;
import com.gimis.ps.msg.DefaultMessage;
import com.gimis.ps.parser.SendCommandParse;
import com.gimis.redis.RedisUtil;
import com.gimis.util.C3P0DBUtil;
import com.gimis.util.GlobalCache;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import redis.clients.jedis.JedisPool;

public class CommandThread extends Thread {
	private RedisUtil redisUtil;
	private int MAXID = 0;
	// 原始命令列表
	private List<Command> oriCommandList;
	
	private long lastCommandListTime;
	private long lastRefrushInvalidTime;
	private RepeatSendCommandList sendCommandList;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public CommandThread(RepeatSendCommandList sendCommandList) {
		JedisPool jedisPool = GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
		redisUtil = new RedisUtil(jedisPool);

		oriCommandList = new LinkedList<Command>();
		this.sendCommandList = sendCommandList;

		SendCommandThread repeatSendCommandThread = new SendCommandThread(sendCommandList);
		repeatSendCommandThread.setDaemon(true);
		repeatSendCommandThread.start();

		SaveResultToDBThread saveResultToDBThread = new SaveResultToDBThread();
		saveResultToDBThread.setDaemon(true);
		saveResultToDBThread.start();
	}

	private void refrushInvalidData() {
		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			conn = C3P0DBUtil.getConnection();

			st = conn.createStatement();

			String CurrentDT = this.sdf.format(new Date());
			String sqlStr = "update command_log set result_status=3 where id in(  select COMMAND_LOG_ID from COMMAND_TEMP where VALIDTIME<'"
					+ CurrentDT + "')";

			st.execute(sqlStr);

			sqlStr = "delete from command_temp where validtime<'" + CurrentDT + "'";
			st.execute(sqlStr);
		} catch (Exception e) {
			System.out.println(this.sdf.format(new Date()) + " 更新命令过期数据关系异常!");
			e.printStackTrace();
		} finally {
			C3P0DBUtil.attemptClose(rs);
			C3P0DBUtil.attemptClose(st);
			C3P0DBUtil.attemptClose(conn);
		}
	}

	private void refrushCommandTempTable() {
		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			conn = C3P0DBUtil.getConnection();

			st = conn.createStatement();

			StringBuilder sb = new StringBuilder();
			sb.append(" select self.ID,self.DEVICE_UNID,COMMAND_LOG_ID,COMMAND_ID,COMMAND_CONTENT,VALIDTIME,a.GPS_ID ");
			sb.append(" from command_temp self,device a where self.DEVICE_UNID=a.UNID ");
			sb.append(" and self.ID>").append(this.MAXID);
			sb.append(" order by self.ID ");
			rs = st.executeQuery(sb.toString());

			while (rs.next())
				try {
					int ID = rs.getInt("ID");
					this.MAXID = ID;

					int commandLogID = rs.getInt("COMMAND_LOG_ID");
					int commandID = rs.getInt("COMMAND_ID");
					String cmdContent = "";
					if (rs.getString("COMMAND_CONTENT") != null) {
						cmdContent = rs.getString("COMMAND_CONTENT");
					}

					String deviceUNID = rs.getString("DEVICE_UNID").trim();
					long validTime = rs.getTimestamp("VALIDTIME").getTime();
					String gpsID = rs.getString("GPS_ID").trim();

					Command command = new Command();
					command.setID(ID);
					command.setCommandLogID(commandLogID);
					command.setCommandID(commandID);
					command.setCmdContent(cmdContent);
					command.setValidTime(validTime);
					command.setGpsID(gpsID);
					command.setDeviceUNID(deviceUNID);
					this.oriCommandList.add(command);

					System.out.println(this.sdf.format(new Date()) + " 获取发送命令 " + command.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
		} catch (Exception e) {
			System.out.println(this.sdf.format(new Date()) + " 更新command_temp表异常!");
			e.printStackTrace();
		} finally {
			C3P0DBUtil.attemptClose(rs);
			C3P0DBUtil.attemptClose(st);
			C3P0DBUtil.attemptClose(conn);
		}
	}

	private boolean onLine(String UNID) {
		String strDT = this.redisUtil.getField("GPSPosition", UNID, "serverTime");
		if (strDT != null) {
			try {
				Date dt = this.sdf.parse(strDT);
				long onlines = (System.currentTimeMillis() - dt.getTime()) / 1000L;

				if (onlines <= 90L)
					return true;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	private SendCommandData getSendCommandData(Command command) {
		String address = (String) GlobalCache.getInstance().getGpsIpAddressMap().get(command.getGpsID());
		if (address == null) {
			address = "";
		}

		String content = command.getCmdContent().replace(',', '.');

		byte[] sendData = SendCommandParse.parse(command.getCommandID(), content);

		short commandId = SendCommandParse.commandConvert(command.getCommandID());

		DefaultHeader defaultHeader = new DefaultHeader();
		defaultHeader.setGpsId(command.getGpsID());

		GlobalCache.seqNumber = (short) (GlobalCache.seqNumber + 1);
		if (GlobalCache.seqNumber > 65535) {
			GlobalCache.seqNumber = 1;
		}
		short seqNo = GlobalCache.seqNumber;
		defaultHeader.setSequenceId(seqNo);

		DefaultMessage sendMessage = new DefaultMessage();

		if (SendCommandParse.commandType(commandId) == 0) {
			sendMessage.setSendGPSData(sendData);
			defaultHeader.setGpsCommandId(commandId);
		} else {
			sendMessage.setSendCANData(sendData);
			defaultHeader.setAttachmentId(commandId);
		}
		sendMessage.setHeader(defaultHeader);

		SendCommandData data = new SendCommandData();
		data.setCount(3);
		data.setCommanLogID(command.getCommandLogID());
		data.setData(sendMessage.getData());
		data.setDt(new Date().getTime());
		data.setGpsID(command.getGpsID());
		data.setSeqNO(seqNo);

		return data;
	}

	public void run() {
		while (true)
			try {
				long currentDT = new Date().getTime();

				long diff = (currentDT - this.lastRefrushInvalidTime) / 1000L;
				if (diff >= 3600L) {
					this.lastRefrushInvalidTime = currentDT;
					refrushInvalidData();
				}

				diff = (currentDT - this.lastCommandListTime) / 1000L;

				if (diff >= 120L) {
					this.lastCommandListTime = currentDT;
					refrushCommandTempTable();
				}
				
				
				
				// 循环发送命令列表，如果在线，就发送命令，并且改 command_lock 的status
				// 如果超期，就删除命令，并且改 command_lock 的status
				if (oriCommandList.size() > 0) {
					currentDT = (new Date()).getTime();
					Iterator<Command> iter = oriCommandList.iterator();
					while (iter.hasNext()) {
						Command data = iter.next();
 
						// 如果超期，那么就删除发送队列
						if (currentDT > data.getValidTime()) {
							iter.remove();
						} else {
							if (onLine(data.getDeviceUNID())) {
								
 
								if ((data.getCommandID() == 500) || (data.getCommandID() == 501)) {
									Long lastTimes = (Long) GlobalCache.getInstance().getLastCanTimes().get(data.getGpsID());
									if (lastTimes != null) {
										long diffTimes = System.currentTimeMillis() - lastTimes.longValue();
										if (diffTimes <= 60000L) {
											iter.remove();

											SendCommandData sendCommandData = getSendCommandData(data);
											this.sendCommandList.put(sendCommandData);

											System.out.println(
													this.sdf.format(new Date()) + " 放入发送命令缓存区!" + sendCommandData.toString());
										}

									}

								} else {
									iter.remove();

									SendCommandData sendCommandData = getSendCommandData(data);
									this.sendCommandList.put(sendCommandData);

									System.out
											.println(this.sdf.format(new Date()) + " 放入发送命令缓存区!" + sendCommandData.toString());
								}
							}
						}
					}
				}

 
 

				Thread.sleep(1000L);
			} catch (Exception e) {
				System.out.println(this.sdf.format(new Date()) + " CommandThread Exception !" + e.toString());
			}
	}
}