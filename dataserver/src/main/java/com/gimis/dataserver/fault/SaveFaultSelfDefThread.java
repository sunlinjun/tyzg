package com.gimis.dataserver.fault;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.dataserver.can.CANHashBody;
import com.gimis.redis.RedisUtil;
import com.gimis.util.DBConnection;
import com.gimis.util.GlobalCache;
import com.mysql.jdbc.Connection;

import redis.clients.jedis.JedisPool;

/*
 * 保存车机 自定义 报警数据
 */
public class SaveFaultSelfDefThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(SaveFaultSelfDefThread.class);

	// 建立一个Mysql连接
	private Connection connection = null;

	private SimpleDateFormat sdf;

	// 最后一次更新时间
	private long lastRefrushTime = 0;

	// redis工具
	private RedisUtil redisUtil;

	// 读取自定义报警间隔
	private int interval;

	// 报警自定义 主表 Key为 FIBER_UNID Value 为 FAULT_DEF表的ID
	private Hashtable<String, ArrayList<String>> faultDefMasterTable;

	// 报警自定义 从表 Key为 FAULT_DEF表的ID
	private Hashtable<String, ArrayList<FaultDefBody>> faultDefSlaveTable;

	public SaveFaultSelfDefThread() {
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JedisPool jedisPool = GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
		redisUtil = new RedisUtil(jedisPool);

		interval = GlobalCache.getInstance().getConfig().getFaultDefIntervalFromMySQL();
		faultDefMasterTable = new Hashtable<String, ArrayList<String>>();
		faultDefSlaveTable = new Hashtable<String, ArrayList<FaultDefBody>>();
	}

	private void createConnection() {
		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error("连接mysql出错!" + e1.toString());
		}
	}

	private void initDBConnection() {

		if (connection == null) {
			createConnection();
		} else {
			try {
				if (connection.isClosed()) {
					createConnection();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void run() {

		initDBConnection();
		while (true) {
			try {
				CANHashBody body = GlobalCache.getInstance().getCanHasBodyQueue().take();

				// 定期读取自定义报警设置
				RefrushCANFaultDef();

				// 自定义报警判断
				Hashtable<String, String> list = parseFault(body);

				// 保存自定义报警到redis和数据库中
				saveFaultDef(body, list);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 新增 报警
	private String addFaultSlave(String device_unid, String faultID, String faultTime) {

		StringBuilder sb = new StringBuilder();
		sb.append("insert into FAULT_DEVICE_SELF_LOG set ");
		sb.append("DEVICE_UNID='").append(device_unid).append("',");
		sb.append("FAULT_DEF_ID=").append(faultID).append(",");
		sb.append("FAULT_TIME_BT='").append(faultTime).append("'");

		return sb.toString();
	}

	// 结束原来的报警 2
	private String updateFaultSlave2(String device_unid, String faultID, String faultTime) {
		StringBuilder sb = new StringBuilder();

		sb.append(" update  FAULT_DEVICE_SELF_LOG set ");
		sb.append(" FAULT_TIME_ET='").append(faultTime).append("'");
		sb.append(" where  DEVICE_UNID='").append(device_unid).append("'");
		sb.append(" and FAULT_TIME_ET=null");

		return sb.toString();
	}

	// 结束报警
	private String updateFaultSlave(String device_unid, String beginDT, String et) {
		StringBuilder sb = new StringBuilder();

		sb.append(" update  FAULT_DEVICE_SELF_LOG set ");
		sb.append(" FAULT_TIME_ET='").append(et).append("'");
		sb.append(" where  DEVICE_UNID='").append(device_unid).append("'");
		sb.append(" and FAULT_TIME_BT='").append(beginDT).append("'");

		return sb.toString();
	}

	private void addFaultToMySQL(String device_unid, String faultID, String faultTime) {

		initDBConnection();
		Statement st = null;
		try {

			st = connection.createStatement();

			// 将原来的没有结束的报警结束
			String sqlstr1 = updateFaultSlave2(device_unid, faultID, faultTime);
			st.executeUpdate(sqlstr1);

			String sqlstr2 = addFaultSlave(device_unid, faultID, faultTime);
			st.executeUpdate(sqlstr2);

			// 提交
			connection.commit();

		} catch (Exception e) {
			// logger.error(e.toString()+" "+sb.toString());

			try {
				// 回滚
				connection.rollback();
				st.close();
				connection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			connection = null;
		} finally {
			try {
				st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 保存自定义报警
	private void saveFaultDef(CANHashBody body, Hashtable<String, String> list) throws ParseException {

		// 获取报警开始
		// 生成的报警中，添加到redis
		if (list != null) {
			for (Map.Entry<String, String> entry : list.entrySet()) {
				String field = "1_" + entry.getKey();
				String faultTime = redisUtil.getField("FAULT", body.getDevice_unid(), field);
				if (faultTime == null) {
					// 报警开始
					faultTime = sdf.format(new Date());
					addFaultToMySQL(body.getDevice_unid(), entry.getKey(), faultTime);
					redisUtil.setField("FAULT", body.getDevice_unid(), field, faultTime);
				}
			}

		}

		// 获取 报警结束
		//  获取原来的自定义报警
		Map<String, String> map = redisUtil.getAllFields("FAULT", body.getDevice_unid());
		if(map!=null){
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				// 如果是设备上传的故障报警，就不处理。
				if (key.indexOf("1_") == -1) {
					continue;
				}

				String faultID = key.substring(2, key.length());

				// 如果不在现在的报警表中，表示报警消失(与上次报警时间超过2分钟)
				String faultTime=null;
				if(list != null){
					faultTime= list.get(faultID);
				}
 
				if (faultTime == null) {
					String oldFaultTime = entry.getValue();
					Date dt1 = sdf.parse(oldFaultTime);
					long diff = (System.currentTimeMillis() - dt1.getTime()) / 1000;
					if (diff > 120) {
						// 报警消失
						updateFaultSlave(body.getDevice_unid(), oldFaultTime, sdf.format(new Date()));
						redisUtil.removeField("FAULT", body.getDevice_unid(), key);

					}
				}
			}			
		}


	}

	// 刷新CAN报警自定义
	private void RefrushCANFaultDef() {
		try {
			long currentTime = System.currentTimeMillis();
			long diff = (currentTime - lastRefrushTime) / 1000;

			if ((diff >= interval)) {
				lastRefrushTime = currentTime;
				getCANFaultDefFromMySQL();
			}

		} catch (Exception e) {

		}
	}

	// 根据条件值判断 faultValue：工作参数值 ConditionValue:条件值
	// faultDefBody.getFaultCondition 0> 1>= 2< 3<= 4==
	private boolean judgeCondition(double faultValue, FaultDefBody faultDefBody) {

		if (faultDefBody.getFaultCondition() == 0) {// >
			if (faultValue > faultDefBody.getFaultConditionValue()) {
				return true;
			}
		} else if (faultDefBody.getFaultCondition() == 1) { // >=

			if (faultValue >= faultDefBody.getFaultConditionValue()) {
				return true;
			}

		} else if (faultDefBody.getFaultCondition() == 2) { // <

			if (faultValue < faultDefBody.getFaultConditionValue()) {
				return true;
			}

		} else if (faultDefBody.getFaultCondition() == 3) { // <=

			if (faultValue <= faultDefBody.getFaultConditionValue()) {
				return true;
			}
		} else { // ==
			Double diff = Math.abs(faultValue - faultDefBody.getFaultConditionValue());
			if (diff < 0.000001) {
				return true;
			}
		}

		return false;
	}

	// 是否报警
	private Hashtable<String, String> parseFault(CANHashBody body) {

		Hashtable<String, String> returnList = null;

		// 根据数据字典获取 自定义报警设置 如果没有就返回
		ArrayList<String> faultIDList = faultDefMasterTable.get(body.getFiber_unid());

		if (faultIDList == null) {
			return returnList;
		}

		// 1种数据字典有好几个报警定义
		for (String faultID : faultIDList) {

			ArrayList<FaultDefBody> faultDefBodylist = faultDefSlaveTable.get(faultID);
			if (faultDefBodylist == null) {
				continue;
			}

			boolean bFault = true;
			// 一个报警定义有几个条件 并且 都要满足才为true
			for (FaultDefBody faultDefBody : faultDefBodylist) {
				String temp = body.getCanData().get(faultDefBody.getKey());

				if (temp == null) {
					bFault = false;
					break;
				}

				try {
					Double faultValue = Double.parseDouble(temp);
					bFault = judgeCondition(faultValue, faultDefBody);
					if (bFault == false) {
						break;
					}
				} catch (Exception e) {
					bFault = true;
					break;
				}

			}

			if (bFault) {
				if (returnList == null) {
					returnList = new Hashtable<String, String>();
				}
				returnList.put(faultID, sdf.format(new Date()));
			}
		}

		return returnList;
	}

	private String getFaultDefQuerySQL() {

		StringBuilder sb = new StringBuilder();
		sb.append(
				" select FAULT_ID,CAN_CONTENT_ID, a.FIELDNAME, b.CANID, c.FIBER_UNID, FAULT_CONDITION,FAULT_CONDITION_VALUE  ");
		sb.append(" from FAULT_DEF_SLAVE self,fiber_can_content a,fiber_can b,fiber_group c ");
		sb.append(" where self.CAN_CONTENT_ID=a.ID  ");

		sb.append(" and a.FIBER_CAN_ID=b.id ");
		sb.append(" and b.FIBER_GROUP_ID=c.ID ");
		return sb.toString();
	}

	// 载入自定义报警
	private void getCANFaultDefFromMySQL() {

		Connection connection = null;
		try {
			connection = DBConnection.getConnection();
			java.sql.Statement st = connection.createStatement();

			// 清除原来的
			faultDefMasterTable.clear();
			faultDefSlaveTable.clear();

			ResultSet rs = st.executeQuery(getFaultDefQuerySQL());
			while (rs.next() != false) {

				String fiberUNID = rs.getString("FIBER_UNID");
				String faultID = rs.getString("FAULT_ID");
				String key = rs.getString("CANID") + "_" + rs.getString("FIELDNAME");
				int faultCondition = Integer.parseInt(rs.getString("FAULT_CONDITION"));
				double conditionValue = Integer.parseInt(rs.getString("FAULT_CONDITION_VALUE"));

				// 载入主表
				ArrayList<String> list = faultDefMasterTable.get(fiberUNID);
				if (list == null) {
					list = new ArrayList<String>();
				}
				list.add(faultID);
				faultDefMasterTable.put(fiberUNID, list);

				// 载入从表
				ArrayList<FaultDefBody> faultDefBodyList = faultDefSlaveTable.get(faultID);
				if (faultDefBodyList == null) {
					faultDefBodyList = new ArrayList<FaultDefBody>();
				}
				FaultDefBody faultDefBody = new FaultDefBody();
				faultDefBody.setKey(key);
				faultDefBody.setFaultCondition(faultCondition);
				faultDefBody.setFaultID(Integer.parseInt(faultID));
				faultDefBody.setFaultConditionValue(conditionValue);

				faultDefBodyList.add(faultDefBody);
				faultDefSlaveTable.put(faultID, faultDefBodyList);

			}
			rs.close();
			st.close();

			logger.error("载入 FAULT_DEF 定义成功!");

		} catch (Exception e) {
			logger.error("载入 FAULT_DEF 定义异常!" + e.toString());
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
