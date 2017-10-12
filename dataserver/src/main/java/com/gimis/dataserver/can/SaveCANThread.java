package com.gimis.dataserver.can;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.HBase.HBaseUtil;
import com.gimis.dataserver.can.DynamicParser.CANParser;
import com.gimis.ps.msg.CANBody;
import com.gimis.ps.msg.CANFrame;
import com.gimis.ps.msg.DefaultMessage;
import com.gimis.redis.RedisUtil;
import com.gimis.util.Config;
import com.gimis.util.DBConnection;
import com.gimis.util.GlobalCache;
import com.gimis.util.MessageConstants;
import com.google.gson.Gson;
import com.mysql.jdbc.Connection;

import redis.clients.jedis.JedisPool;

public class SaveCANThread extends Thread {

	private BlockingQueue<DefaultMessage> canBodyQueue;

	private static final Logger logger = LoggerFactory.getLogger(SaveCANThread.class);

	// 最后一次更新时间
	private long lastRefrushTime = 0;

	private CANParser canParser = new CANParser();

	private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// redis工具
	private RedisUtil redisUtil;

	// HBase
	private HBaseUtil hBaseUtil = new HBaseUtil();

	// 存放保存数据库的时间
	private Map<String, Long> saveDBList = new Hashtable<String, Long>();

	// 配置文件
	private Config config;

	// 建立一个Mysql连接
	private Connection connection = null;

	// 车辆最后的时间
	private Map<String, Long> vehicleLastTime = new Hashtable<String, Long>();

	public SaveCANThread() {
		this.canBodyQueue = GlobalCache.getInstance().getCanBodyQueue();
		JedisPool jedisPool = GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
		redisUtil = new RedisUtil(jedisPool);
		config = GlobalCache.getInstance().getConfig();
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

	private void createConnection() {
		try {
			connection = DBConnection.getConnection();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error("连接mysql出错!" + e1.toString());
		}
	}

	private String getCANKey(String UNID, String CANID) {
		return UNID + "_" + CANID;
	}

	public void run() {

		// hBaseUtil.createTable(MessageConstants.TABLE_CAN_NAME) ;

		// TestQuery();
		while (true) {
			try {

				DefaultMessage message = canBodyQueue.take();
				CANBody canBody = message.getCanBody();

				canBody.setServerTime(dateformat.format(new Date()));
				HashMap<String, String> canData = new HashMap<String, String>();

				// 1: 定义刷新 CAN解析定义
				RefrushCANDef();

				// 2: 解析CAN
				parserCAN(canBody, canData);

				// 下发锁车命令
				if (canBody.getUploadType() == 0 && canBody.getCanlist() != null && canBody.getCanlist().size() > 0) {
					GlobalCache.getInstance().getLastCanTimes().put(canBody.getGps_id(), System.currentTimeMillis());
				}

				// 没有解析出数据
				if (canData.size() == 0) {
					continue;
				}

				if (canBody.getUploadType() == 0) {

					boolean bSave = false;
					Long lastTime = vehicleLastTime.get(canBody.getGps_id());
					if (lastTime == null) {
						vehicleLastTime.put(canBody.getGps_id(), canBody.getGpsTime().getTime());
						bSave = true;
					} else {
						long diff = canBody.getGpsTime().getTime() - lastTime;
						if (diff > 0) {
							vehicleLastTime.put(canBody.getGps_id(), canBody.getGpsTime().getTime());
							bSave = true;
						}
					}

					if (bSave) {
						// 转发到统计服务器
						message.getCanBody().setContentList(canData);
						GlobalCache.getInstance().getSendAnalysisServerQueue().put(message);

						// 3: 保存到redis
						redisUtil.add("CAN", canBody.getGps_id(), canData);

						// 4: 刷新数据库
						refrushDB(canBody, canData);
					}

					// 5: 保存到队列中，已便于自定义报警解析
					// SaveCANHash(canBody, canData);

				}

				// 6: 保存到HBase
				saveHBase(canBody, canData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("解析保存CAN数据异常! " + e.toString());
			}
		}

	}

	private void parserCAN(CANBody canBody, HashMap<String, String> canData) {

		ArrayList<CANFrame> list = canBody.getCanlist();
		for (CANFrame canFrame : list) {
			String key = canBody.getFiber_unid() + "_" + canFrame.getCANID();

			HashMap<String, String> map = canParser.parser(key, canFrame.getData());

			Iterator<String> it = map.keySet().iterator();
			while (it.hasNext()) {
				String fieldName = (String) it.next();
				String value = map.get(fieldName);
				canData.put(canFrame.getCANID() + "_" + fieldName, value);
			}
		}

		if (canData.size() > 0) {
			canData.put("gps_id", canBody.getGps_id());
			canData.put("device_unid", canBody.getDevice_unid());
			canData.put("gps_time", dateformat.format(canBody.getGpsTime()));
			canData.put("server_time", dateformat.format(new Date()));
		}
	}

	private void saveHBase(CANBody canBody, HashMap<String, String> canData) {

		/*
		 * StringBuilder sb=new StringBuilder(); Iterator iter =
		 * canData.entrySet().iterator(); while (iter.hasNext()) { Map.Entry
		 * entry = (Map.Entry) iter.next(); String key = (String)entry.getKey();
		 * String val = (String)entry.getValue();
		 * sb.append("key:"+key).append(" value:"+val).append(" "); }
		 * 
		 * System.out.println("save hbase "+canBody.getGps_id()+" time:"+canBody
		 * .getGpsTime().getTime() +" "+sb.toString());
		 * 
		 */
		hBaseUtil.write(MessageConstants.TABLE_CAN_NAME, canBody.getDevice_unid(), canBody.getGpsTime().getTime(),
				canData);
	}

	private void refrushDB(CANBody canBody, HashMap<String, String> canData) {

		String key = canBody.getGps_id() + "-" + canBody.getDevice_unid();

		Long lastTime = saveDBList.get(key);
		if (lastTime != null) {
			long diff = (System.currentTimeMillis() - lastTime) / 1000;
			if (diff >= config.getSaveMysqlInterval()) {
				saveDBList.put(key, System.currentTimeMillis());
				SaveToMySQLDB(canBody, canData);
			}

		} else {
			saveDBList.put(key, System.currentTimeMillis());
			SaveToMySQLDB(canBody, canData);
		}
	}

	private void SaveToMySQLDB(CANBody canBody, HashMap<String, String> canData) {
		initDBConnection();

		// 序列化json
		// {"server_time":"2016-10-19
		// 15:16:54","gps_id":"gps002","gps_time":"2016-10-19
		// 15:06:20","device_unid":"AB63F1A637D34193916F2FCBCC97715F"}
		Gson gson = new Gson();
		String strCanData = gson.toJson(canData);

		// 反序列化
		// Map<String, String> resultMap2 = gson.fromJson( jsonStr, new
		// TypeToken<Map<String, String>>() { }.getType());

		Statement st = null;
		StringBuilder sb = new StringBuilder();
		try {
			st = connection.createStatement();

			sb.append("insert into gpscan set ");
			sb.append(getSubSQL(canBody));
			sb.append(",").append("candata='").append(strCanData).append("'");

			sb.append(" on duplicate key update ");
			sb.append(getSubSQL(canBody));
			sb.append(",").append("candata='").append(strCanData).append("'");

			st.execute(sb.toString());

		} catch (Exception e) {
			logger.error(e.toString() + " " + sb.toString());
			try {
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

	// 载入CAN定义
	private ArrayList<CANDef> getCANDefFromMySQL() {
		Connection connection = null;
		ArrayList<CANDef> list = new ArrayList<CANDef>();
		try {
			connection = DBConnection.getConnection();
			java.sql.Statement st = connection.createStatement();

			ResultSet rs = st.executeQuery(getCANDefQuerySQL());
			while (rs.next() != false) {

				CANDef canDef = new CANDef();
				canDef.setFiberUNID(rs.getString("FIBER_UNID"));
				canDef.setCanID(rs.getString("CANID"));
				canDef.setByteOrder(Integer.parseInt(rs.getString("BYTEORDER")));
				canDef.setDataType(Integer.parseInt(rs.getString("DATATYPE")));
				canDef.setPropertyName(rs.getString("PROPERTYNAME"));
				canDef.setFieldName(rs.getString("FIELDNAME"));

				if (rs.getString("HIGHBYTES") != null) {
					canDef.setHighBytes(rs.getString("HIGHBYTES"));
				} else {
					canDef.setHighBytes("");
				}

				// ******这边对BIT位进行一个处理，认为3.0 就是 3*8+0 ，而不是3*8+7-0
				/*
				 * if(canDef.getDataType()==0){ String
				 * a=canDef.getFieldName().substring(0, 1); String
				 * b=canDef.getFieldName().substring(2, 3);
				 * 
				 * int lowBytes=Integer.parseInt(a)*8+Integer.parseInt(b);
				 * canDef.setLowBytes(Integer.toString(lowBytes));
				 * //canDef.setLowBytes(rs.getString("LOWBYTES")); }else{
				 * 
				 * }
				 */

				canDef.setLowBytes(rs.getString("LOWBYTES"));

				canDef.setOffset(Integer.parseInt(rs.getString("OFFSET")));

				// String outStr=canDef.getFieldName()
				// +" db:"+rs.getString("LOWBYTES")+"
				// real:"+canDef.getLowBytes();

				// System.out.println(outStr);

				if (rs.getString("RESOLUTION") != null) {
					canDef.setResolution(Double.parseDouble(rs.getString("RESOLUTION")));
				}

				canDef.setSignBit(Integer.parseInt(rs.getString("SIGNBIT")));

				list.add(canDef);
			}

			rs.close();
			st.close();

			logger.info("载入 CAN定义成功!");

		} catch (Exception e) {
			logger.error("载入 CAN定义异常!" + e.toString());
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
		return list;
	}

	// 刷新CAN定义
	private void RefrushCANDef() {
		try {
			long currentTime = System.currentTimeMillis();
			long diff = (currentTime - lastRefrushTime) / 1000;
			// 1小时执行一次
			if ((diff >= 3600)) {
				lastRefrushTime = currentTime;
				System.out.println("开始加载数据字典...");

				ArrayList<CANDef> arrayList = getCANDefFromMySQL();

				Hashtable<String, ArrayList<CANDef>> hashtable = new Hashtable<String, ArrayList<CANDef>>();
				for (CANDef canDef : arrayList) {
					String key = getCANKey(canDef.getFiberUNID(), canDef.getCanID());
					ArrayList<CANDef> list = hashtable.get(key);
					if (list == null) {
						list = new ArrayList<CANDef>();
					}
					list.add(canDef);
					hashtable.put(key, list);
				}

				HashMap<String, String> hashMap = new HashMap<String, String>();
				Iterator<String> it = hashtable.keySet().iterator();
				while (it.hasNext()) {
					String key;
					key = (String) it.next();
					ArrayList<CANDef> list = hashtable.get(key);

					Gson gson = new Gson();
					String str = gson.toJson(list);
					hashMap.put(key, str);
				}

				// 将原来clear 重新加载
				canParser.init(hashMap);
			}
		} catch (Exception e) {
			System.out.println("加载数据字典失败!");
			e.printStackTrace();
		}

	}

	private void SaveCANHash(CANBody canBody, HashMap<String, String> canData) {

		CANHashBody body = new CANHashBody();
		body.setGps_id(canBody.getGps_id());
		body.setDevice_unid(canBody.getDevice_unid());
		body.setFiber_unid(canBody.getFiber_unid());
		body.setCanData(canData);
		try {
			GlobalCache.getInstance().getCanHasBodyQueue().put(body);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getCANDefQuerySQL() {

		StringBuilder sb = new StringBuilder();

		sb.append("select FIBER_UNID,self.ID,self.FIBER_CAN_ID,a.CANID,BYTEORDER,DATATYPE");
		sb.append(" ,PROPERTYNAME,FIELDNAME,HIGHBYTES,LOWBYTES,OFFSET,RESOLUTION,SIGNBIT ");

		sb.append(" from FIBER_CAN_CONTENT self,FIBER_CAN a,FIBER_GROUP b  ");
		sb.append(" where self.FIBER_CAN_ID=a.ID and a.FIBER_GROUP_ID=b.ID ");

		return sb.toString();
	}

	private String getSubSQL(CANBody gpsBody) {
		StringBuilder sb = new StringBuilder();
		sb.append(" gps_id='").append(gpsBody.getGps_id()).append("'");

		sb.append(",").append("device_unid='").append(gpsBody.getDevice_unid()).append("'");
		;
		sb.append(",").append("latitude=").append(gpsBody.getLatitude());
		sb.append(",").append("longitude=").append(gpsBody.getLongitude());
		sb.append(",").append("speed=").append(gpsBody.getSpeed());
		sb.append(",").append("direction=").append(gpsBody.getDirection());
		sb.append(",").append("height=").append(gpsBody.getHeight());

		sb.append(",").append("gpstime='").append(dateformat.format(gpsBody.getGpsTime())).append("'");
		sb.append(",").append("servertime='").append(gpsBody.getServerTime()).append("'");

		return sb.toString();
	}

}
