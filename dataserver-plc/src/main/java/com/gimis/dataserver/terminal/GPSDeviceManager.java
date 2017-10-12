package com.gimis.dataserver.terminal;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.dataserver.redis.RedisUtil;
import com.gimis.util.GlobalCache;
import com.gimis.util.PropertiesTools;

import redis.clients.jedis.JedisPool;

/*
 * 管理 GPS与Device对应关系
 * 定期从redis读取 device的数据字典和  gpsId unid
 * Key为gpsId
 */
public class GPSDeviceManager {
	
	private RedisUtil redisUtil;
	
	private int interval;
 	
    private Map<String, GPSDevice> hashMap;
    
    private static final Logger logger = LoggerFactory.getLogger(GPSDeviceManager.class);
    
    public GPSDeviceManager(){	
    	JedisPool jedisPool= GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
    	redisUtil = new RedisUtil(jedisPool);
    	
        interval=300;
		Properties cfgPro = new Properties();
		try {
			cfgPro = PropertiesTools.loadProperties("cfg.properties", System.getProperty("user.dir"));
			
			interval=Integer.parseInt(cfgPro.getProperty("redis.gpsDeviceInterval"));	
			
		} catch (IOException e) {
			e.printStackTrace();
		}				
 
    	hashMap=GlobalCache.getInstance().getDeviceMap();
    }
    
   
    
    /*		
     * 从 hashMap中获取对应关系，如果不存在，就去redis获取。
     * redis获取的时间间隔 N 分钟
     */
	public GPSDevice getGPSDevice(String gpsId){
 
		try{
			GPSDevice gpsDevice= hashMap.get(gpsId);
			if(gpsDevice!=null){				
				Date current=new Date();
				long diff = (current.getTime() - gpsDevice.getGetDate().getTime())/1000;
				
				//如果相差 N 分钟，就去redis读取
				if(diff>=interval){
					queryRedis(gpsDevice);
					hashMap.put(gpsId, gpsDevice);
					
					if(gpsDevice.getDevice_unid()!=null){
						GlobalCache.getInstance().getDeviceUnidMap().put(gpsDevice.getDevice_unid(), 
								gpsDevice.getGpsID());
					}					
				}
			}else{ 
				gpsDevice=new GPSDevice();
				gpsDevice.setGpsID(gpsId);
				queryRedis(gpsDevice);
				hashMap.put(gpsId, gpsDevice);
				
				if(gpsDevice.getDevice_unid()!=null){
					GlobalCache.getInstance().getDeviceUnidMap().put(gpsDevice.getDevice_unid(), 
							gpsDevice.getGpsID());
				}	
			}
			
			
			return gpsDevice;
		} catch (Exception e) {
            // TODO: handle exception
			logger.error(e.toString());
			return null;
        } 	
	}
		
	//向redis请求
	private void  queryRedis(GPSDevice gpsDevice){
		if(gpsDevice!=null && gpsDevice.getGpsID()!=null){
			Map<String, String> map = redisUtil.getAllFields("GPSDEVICE", gpsDevice.getGpsID());
 
			if(map!=null){
				if(map.get("device_unid")!=null){
					gpsDevice.setDevice_unid(map.get("device_unid"));
					gpsDevice.setFiber_unid(map.get("fiber_unid"));				
				}

			}			
		}
	}
}

