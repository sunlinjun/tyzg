package com.gimis.dataserver.command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.gimis.dataserver.redis.RedisUtil;
import com.gimis.util.C3P0DBUtil;
import com.gimis.util.GlobalCache;

import redis.clients.jedis.JedisPool;

public class CommandThread extends Thread {

	// redis工具
	private RedisUtil redisUtil;

	// MAXID
	private int MAXID = 0;

	// 原始命令列表
	private List<Command> oriCommandList;

	private long lastCommandListTime;

	// 最后一次更新过期时间
	private long lastRefrushInvalidTime;

	private RepeatSendCommandList sendCommandList;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public CommandThread(RepeatSendCommandList sendCommandList) {
		JedisPool jedisPool = GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
		redisUtil = new RedisUtil(jedisPool);

		oriCommandList = new LinkedList<Command>();
		this.sendCommandList = sendCommandList;

		// 重复发送命令
		SendCommandThread repeatSendCommandThread = new SendCommandThread(sendCommandList);
		repeatSendCommandThread.setDaemon(true);
		repeatSendCommandThread.start();

		// 保存应答数据库
		SaveResultToDBThread saveResultToDBThread = new SaveResultToDBThread();
		saveResultToDBThread.setDaemon(true);
		saveResultToDBThread.start();

	}

	// 更新命令过期数据
	private void refrushInvalidData() {
		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			conn = C3P0DBUtil.getConnection();

			st = conn.createStatement();

			String CurrentDT = sdf.format(new Date());
			String sqlStr = "update command_log set result_status=3 where id in( "
					+ " select COMMAND_LOG_ID from command_plc_temp where VALIDTIME<'" + CurrentDT + "')";

			st.execute(sqlStr);

			sqlStr = "delete from command_plc_temp where validtime<'" + CurrentDT + "'";
			st.execute(sqlStr);

		} catch (Exception e) {
			
			System.out.println(sdf.format(new Date())+" 更新命令过期数据关系异常!");
			e.printStackTrace();
		} finally {
			C3P0DBUtil.attemptClose(rs);
			C3P0DBUtil.attemptClose(st);
			C3P0DBUtil.attemptClose(conn);
		}
	}

	// 刷新COMMAND_TEMP表
	private void refrushCommandTempTable() {

		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			conn = C3P0DBUtil.getConnection();

			st = conn.createStatement();

			// 获取最新发送的命令
			String sqlStr = " select * from command_plc_temp where id>" + Integer.toString(MAXID) + " order by id ";
			rs = st.executeQuery(sqlStr);

			while (rs.next()) {

				try {
					int ID = rs.getInt("ID");
					MAXID = ID;

					int commandLogID = rs.getInt("COMMAND_LOG_ID");
					int commandID = rs.getInt("COMMAND_ID");
					String cmdContent = "";
					if (rs.getString("COMMAND_CONTENT") != null) {
						cmdContent = rs.getString("COMMAND_CONTENT");
					}

					String deviceUNID = rs.getString("DEVICE_UNID");					 
					long validTime = rs.getTimestamp("VALIDTIME").getTime();
 
					String gpsID = redisUtil.getField("DEVICE", deviceUNID, "gps_id");
					if (gpsID != null) {
						Command command = new Command();
						command.setID(ID);
						command.setCommandLogID(commandLogID);
						command.setCommandID(commandID);
						command.setCmdContent(cmdContent);
						command.setValidTime(validTime);
						command.setGpsID(gpsID);
						command.setDeviceUNID(deviceUNID);
						oriCommandList.add(command);

						System.out.println(sdf.format(new Date())+" 获取发送命令 " + command.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			System.out.println(sdf.format(new Date())+" 更新command_temp表异常!");
			e.printStackTrace();
		} finally {
			C3P0DBUtil.attemptClose(rs);
			C3P0DBUtil.attemptClose(st);
			C3P0DBUtil.attemptClose(conn);
		}

	}

	// 查看设备是否在线 30秒内表示在线
	private boolean onLine(String UNID) {
		String strDT = redisUtil.getField("GPSPosition", UNID, "serverTime");
		if (strDT != null) {
			try {
				Date dt = sdf.parse(strDT);
				long onlines = (System.currentTimeMillis() - dt.getTime()) / 1000;
				// 在线,发送数据
				if (onlines <= 60) {
					return true;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}
 
	private SendCommandData getSendCommandData(Command command) {

		String address = GlobalCache.getInstance().getGpsIpAddressMap().get(command.getGpsID());
		if (address == null) {
			address = "";
		}

		// 通过URL传过来时, 将 , 转化成 .
		String content = command.getCmdContent().replace(',', '.');

		
		// 自增序列号
		GlobalCache.seqNumber++;
		if (GlobalCache.seqNumber > 65535) {
			GlobalCache.seqNumber = 1;
		}
		
		int seqNo=GlobalCache.seqNumber;
		
		// 解析命令
		byte[] sendData = SendCommandParse.parse(command.getGpsID(), 
				command.getCommandID(), seqNo,content); 
		
		if(sendData!=null){
			SendCommandData data = new SendCommandData();
			data.setCount(1);
			data.setCommanLogID(command.getCommandLogID());
			data.setData(sendData);
			data.setDt((new Date()).getTime());
			data.setGpsID(command.getGpsID());
			data.setSeqNO(seqNo);

			return data;			
		}else{
			return null;
		}

	}
 
	public void run() {

		while (true) {

			try {

				long currentDT = (new Date()).getTime();

				long diff = (currentDT - lastRefrushInvalidTime) / 1000;
				if (diff >= 3600) {
					lastRefrushInvalidTime = currentDT;
					refrushInvalidData();
				}

				// 获取发送命令队列
				diff = (currentDT - lastCommandListTime) / 1000;
				// N秒更新一次数据库
				if (diff >= 120) {
					lastCommandListTime = currentDT;
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
								iter.remove();
																
								// 获取发送
								SendCommandData sendCommandData = getSendCommandData(data);								
								if(sendCommandData!=null){
									sendCommandList.put(sendCommandData);								
									System.out.println(sdf.format(new Date())+" 放入发送命令缓存区!"+sendCommandData.toString());									
								}else{
									System.out.println(sdf.format(new Date())+" 命令内容出错!"+data.toString());									
								}
							}
						}
					}
				}

				Thread.sleep(1000);

			} catch (Exception e) {				
				System.out.println(sdf.format(new Date())+" CommandThread Exception !"+e.toString());
			}

		}

	}
}

