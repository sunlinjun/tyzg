package com.gimis.dataserver.gps;

import BaiduMap.AddressComponent;
import BaiduMap.BaiduMap;
import com.gimis.ps.msg.GPSBody;
import com.gimis.redis.RedisUtil;
import com.gimis.util.Config;
import com.gimis.util.DBConnection;
import com.gimis.util.GlobalCache;
import com.mysql.jdbc.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class SaveChsThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(SaveChsThread.class);

	private Connection connection = null;
	private RedisUtil redisUtil;
	private Config config;
	private DecimalFormat floatFormat = new DecimalFormat("0.000000");

	private DecimalFormat simplefloatFormat = new DecimalFormat("0.0");

	private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Map<String, Long> saveDBList = new Hashtable<String, Long>();

	private Map<String, Long> vehicleLastTime = new Hashtable<String, Long>();

	private Map<String, GPSPositionChs> lnglatList = new Hashtable<String, GPSPositionChs>();

	public SaveChsThread() {
		JedisPool jedisPool = GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
		this.redisUtil = new RedisUtil(jedisPool);

		this.config = GlobalCache.getInstance().getConfig();
	}

	public void run() {
		while (true)
			try {
				GPSBody gpsBody = (GPSBody) GlobalCache.getInstance().getGpsChsQueue().take();

				long temp = gpsBody.getGpsTime().getTime() - System.currentTimeMillis();

				if (temp > 86400000L) {
					logger.error(gpsBody.getGps_id() + " gpsTime is error : "
							+ this.dateformat.format(gpsBody.getGpsTime()));
				} else {
					boolean isDebug = false;
					if (this.config.getDebugGPSID().indexOf(gpsBody.getGps_id()) > -1) {
						isDebug = true;
					}

					if (gpsBody.getUploadType() == 0) {
						if (isDebug) {
							System.out.println(gpsBody.getGps_id() + " gpsTime: "
									+ this.dateformat.format(gpsBody.getGpsTime()) + " getUploadType==0 ");
						}

						boolean bSave = false;
						Long lastTime = (Long) this.vehicleLastTime.get(gpsBody.getGps_id());
						if (lastTime == null) {
							this.vehicleLastTime.put(gpsBody.getGps_id(), Long.valueOf(gpsBody.getGpsTime().getTime()));
							bSave = true;
						} else {
							long diff = gpsBody.getGpsTime().getTime() - lastTime.longValue();
							if (diff > 0L) {
								this.vehicleLastTime.put(gpsBody.getGps_id(),
										Long.valueOf(gpsBody.getGpsTime().getTime()));
								bSave = true;
							}

							if (isDebug) {
								System.out.println(gpsBody.getGps_id() + " " + gpsBody.getGpsTime().getTime() + "-"
										+ lastTime + "=" + diff);
							}
						}

						if (bSave) {
							if (isDebug) {
								System.out.println(gpsBody.getGps_id() + " gpsTime: "
										+ this.dateformat.format(gpsBody.getGpsTime()) + " save ");
							}

							refrushChinseLocation(gpsBody);

							HashMap<String, String> map = gpsBody.ToMap(this.floatFormat, this.dateformat);
							this.redisUtil.add("GPSPosition", gpsBody.getDevice_unid(), map);

							refrushDB(gpsBody);
						} else if (isDebug) {
							System.out.println(gpsBody.getGps_id() + " gpsTime: "
									+ this.dateformat.format(gpsBody.getGpsTime()) + " not save ");
						}

					} else if (isDebug) {
						System.out.println(gpsBody.getGps_id() + " gpsTime: "
								+ this.dateformat.format(gpsBody.getGpsTime()) + " getUploadType==1 ");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.toString());
			}
	}

	private void createConnection() {
		try {
			this.connection = DBConnection.getConnection();
		} catch (Exception e1) {
			logger.error("连接mysql出错!" + e1.toString());
		}
	}

	private void initDBConnection() {
		if (this.connection == null)
			createConnection();
		else
			try {
				if (this.connection.isClosed())
					createConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	private void refrushChinseLocation(GPSBody gpsBody) throws InterruptedException {
		GPSPositionChs chs = (GPSPositionChs) this.lnglatList.get(gpsBody.getGps_id());

		if (chs == null) {
			chs = new GPSPositionChs();
			chs.setGpsId(gpsBody.getGps_id());
			chs.setUnid(gpsBody.getDevice_unid());
			chs.setLocationtime(new Date());
			if (gpsBody.getLocationStatus() == 1) {
				chs.setLongitude(gpsBody.getLongitude());
				chs.setLatitude(gpsBody.getLatitude());

				saveBaiduMapLocation(chs);
			} else {
				getPostionFromRedis(gpsBody, chs);
			}
			this.lnglatList.put(gpsBody.getGps_id(), chs);
		} else {
			long diff = (System.currentTimeMillis() - chs.getLocationtime().getTime()) / 1000L;

			if ((diff >= 60L) && (gpsBody.getLocationStatus() == 1)) {
				chs.setLocationtime(new Date());

				double distance = BaiduMap.calPosDistance(chs.getLongitude().doubleValue(),
						chs.getLatitude().doubleValue(), gpsBody.getLongitude().doubleValue(),
						gpsBody.getLatitude().doubleValue());

				if ((distance >= 10000.0D) || (diff > 86400L)
						|| (((chs.getProvince() == null) || (chs.getProvince().equals(""))) && (diff > 1800L))) {
					chs.setLatitude(gpsBody.getLatitude());
					chs.setLongitude(gpsBody.getLongitude());
					saveBaiduMapLocation(chs);
				}

				this.lnglatList.put(gpsBody.getGps_id(), chs);
			}

		}

		if ((gpsBody.getLocationStatus() != 1) && (chs.getLatitude() != null) && (chs.getLongitude() != null)) {
			Double latitude = Double.valueOf(Math.floor(chs.getLatitude().doubleValue() * 1000000.0D));
			Double longitude = Double.valueOf(Math.floor(chs.getLongitude().doubleValue() * 1000000.0D));
			gpsBody.setLatitude(Integer.valueOf(latitude.intValue()));
			gpsBody.setLongitude(Integer.valueOf(longitude.intValue()));
		}
	}

	private void saveBaiduMapLocation(GPSPositionChs chs) {
		try {
			if ((chs.getLongitude().doubleValue() >= 73.659999999999997D)
					&& (chs.getLongitude().doubleValue() <= 135.05000000000001D)
					&& (chs.getLatitude().doubleValue() >= 16.736000000000001D)
					&& (chs.getLatitude().doubleValue() <= 53.728999999999999D)) {
				Thread.sleep(500L);
				AddressComponent address = BaiduMap.getChinePosition(this.config.getBaiduMapKey(),
						chs.getLatitude().toString(), chs.getLongitude().toString());

				if (address != null) {
					chs.setProvince(address.getProvince());
					chs.setCity(address.getCity());
					chs.setArea(address.getDistrict());

					System.out.println(this.dateformat.format(new Date()) + " " + chs.getGpsId() + " "
							+ chs.getProvince() + " " + chs.getCity() + " " + chs.getArea());

					this.redisUtil.add("GPSPosition", chs.getUnid(), chs.ToMap(this.dateformat));

					SaveChineseLocationToDB(chs, true);

					return;
				}
				System.out.println(this.dateformat.format(new Date()) + " " + chs.getGpsId() + " not get address");
			}

		} catch (Exception e) {
			logger.error(e.toString());
		}

		this.redisUtil.add("GPSPosition", chs.getUnid(), chs.ToMapEx(this.dateformat));

		SaveChineseLocationToDB(chs, false);
	}

	private void getPostionFromRedis(GPSBody gpsBody, GPSPositionChs chs) {
		String strlat = this.redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "latitude");
		String strlng = this.redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "longitude");

		String province = this.redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "province");
		String city = this.redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "city");
		String area = this.redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "area");
		if ((strlat != null) && (strlng != null))
			try {
				Double lat = Double.valueOf(Double.parseDouble(strlat));
				Double lng = Double.valueOf(Double.parseDouble(strlng));

				chs.setLongitude(lng);
				chs.setLatitude(lat);
				if (province != null) {
					chs.setProvince(province);
				}

				if (city != null) {
					chs.setCity(city);
				}

				if (area != null) {
					chs.setArea(area);
				}

				Double latitude = Double.valueOf(Math.floor(lat.doubleValue() * 1000000.0D));
				Double longitude = Double.valueOf(Math.floor(lng.doubleValue() * 1000000.0D));

				gpsBody.setLatitude(Integer.valueOf(latitude.intValue()));
				gpsBody.setLongitude(Integer.valueOf(longitude.intValue()));
			} catch (Exception localException) {
			}
	}

	private void SaveChineseLocationToDB(GPSPositionChs chs, boolean saveChs) {
		initDBConnection();

		Statement st = null;
		StringBuilder sb = new StringBuilder();
		try {
			st = this.connection.createStatement();

			sb.append("insert into gpsposition set ");
			sb.append(getSub2SQL(chs, saveChs));
			sb.append(" on duplicate key update ");
			sb.append(getSub2SQL(chs, saveChs));
			st.execute(sb.toString());
		} catch (Exception e) {
			logger.error(e.toString() + " " + sb.toString());
			try {
				this.connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			this.connection = null;
			try {
				st.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private String getSubSQL(GPSBody gpsBody) {
		StringBuilder sb = new StringBuilder();

		sb.append(" gps_id='").append(gpsBody.getGps_id()).append("'");
		sb.append(",").append("latitude=").append(this.floatFormat.format(gpsBody.getLatitude()));
		sb.append(",").append("longitude=").append(this.floatFormat.format(gpsBody.getLongitude()));
		sb.append(",").append("speed=").append(this.simplefloatFormat.format(gpsBody.getSpeed()));
		sb.append(",").append("distance=").append(gpsBody.getDistance());

		sb.append(",").append("direction=").append(gpsBody.getDirection());
		sb.append(",").append("height=").append(gpsBody.getHeight());

		sb.append(",").append("softversion='").append(gpsBody.getSoftVersion()).append("'");
		sb.append(",").append("hardwareversion='").append(gpsBody.getHardwareVersion()).append("'");
		sb.append(",").append("accstatus=").append(gpsBody.getAccStatus());

		sb.append(",").append("locationstatus=").append(gpsBody.getLocationStatus());
		sb.append(",").append("gpsmodelstatus=").append(gpsBody.getGpsModelStatus());
		sb.append(",").append("powerbatterystatus=").append(gpsBody.getPowerBatteryStatus());

		sb.append(",").append("satellitecount=").append(gpsBody.getSatelliteCount());

		sb.append(",").append("gpstime='").append(this.dateformat.format(gpsBody.getGpsTime())).append("'");
		sb.append(",").append("servertime='").append(gpsBody.getServerTime()).append("'");

		return sb.toString();
	}

	private String getSub2SQL(GPSPositionChs chs, boolean saveChs) {
		StringBuilder sb = new StringBuilder();
		sb.append(" gps_id='").append(chs.getGpsId()).append("'");

		if (saveChs) {
			sb.append(",").append("province='").append(chs.getProvince()).append("'");
			sb.append(",").append("city='").append(chs.getCity()).append("'");
			sb.append(",").append("area='").append(chs.getArea()).append("'");
		}
		sb.append(",").append("locationtime='").append(this.dateformat.format(chs.getLocationtime())).append("'");
		return sb.toString();
	}

	private void refrushDB(GPSBody gpsBody) {
		Long lastTime = (Long) this.saveDBList.get(gpsBody.getGps_id());
		if (lastTime != null) {
			long diff = (System.currentTimeMillis() - lastTime.longValue()) / 1000L;
			if (diff >= this.config.getSaveMysqlInterval()) {
				this.saveDBList.put(gpsBody.getGps_id(), Long.valueOf(System.currentTimeMillis()));
				SaveToDB(gpsBody);
			}
		} else {
			this.saveDBList.put(gpsBody.getGps_id(), Long.valueOf(System.currentTimeMillis()));
			SaveToDB(gpsBody);
		}
	}

	private void SaveToDB(GPSBody gpsBody) {
		initDBConnection();

		Statement st = null;
		StringBuilder sb = new StringBuilder();
		try {
			st = this.connection.createStatement();

			sb.append("insert into gpsposition set ");
			sb.append(getSubSQL(gpsBody));
			sb.append(" on duplicate key update ");
			sb.append(getSubSQL(gpsBody));

			st.execute(sb.toString());
		} catch (Exception e) {
			logger.error(e.toString() + " " + sb.toString());
			try {
				st.close();
				this.connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			this.connection = null;
			try {
				st.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		} finally {
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}