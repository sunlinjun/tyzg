package com.gimis.dataserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import com.gimis.dataserver.terminal.GPSDevice;
import com.gimis.dataserver.terminal.GPSDeviceManager;
import com.gimis.ps.msg.CANBody;
import com.gimis.ps.msg.DefaultMessage;
import com.gimis.ps.msg.FaultBody;
import com.gimis.ps.msg.GPSBody;
import com.gimis.ps.msg.ReportBody;
import com.gimis.redis.RedisUtil;
import com.gimis.util.GlobalCache;
import com.gimis.util.MessageConstants;

import redis.clients.jedis.JedisPool;

/*
 * 判断设备是否注册,接收消息后，发送给GPS线程,CAN线程分别处理。
 *  
 * 后期可以 增加  发送给Kafka
 */
public class MessageProcessThread extends Thread{
	
	private BlockingQueue<DefaultMessage> messageQueue;
	
	private GPSDeviceManager gpsDeviceManager;
	
	private RedisUtil redisUtil;
	
	private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public MessageProcessThread(GPSDeviceManager gpsDeviceManager){
		this.messageQueue=GlobalCache.getInstance().getDefaultMessageQueue();
		this.gpsDeviceManager=gpsDeviceManager;
    	JedisPool jedisPool= GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
    	redisUtil = new RedisUtil(jedisPool);
	}

	public void run(){
		while(true){
			try {
				DefaultMessage message=messageQueue.take();
			    String gpsId=message.getHeader().getGpsId();
			    GPSDevice gpsDevice=gpsDeviceManager.getGPSDevice(gpsId);
				if(gpsDevice!=null && gpsDevice.getDevice_unid()!=null){
					process(gpsDevice, message);
				}			 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void process(GPSDevice gpsDevice,DefaultMessage message) throws InterruptedException{
 
		//GPS数据解析
		if(message.getHeader().getGpsCommandId()==MessageConstants.MESSAGE_TYPE_HEART){
			
			GPSBody gpsBody=message.getGpsBody();
			
			if(gpsBody!=null){
				gpsBody.setDevice_unid(gpsDevice.getDevice_unid());
				
				gpsBody.setServerTime(dateformat.format(new Date()));
				
				//保存到HBase
				GlobalCache.getInstance().getGpsBodyQueue().put(gpsBody);
				//获取中文名称 保存到REDIS MYSQL
				GlobalCache.getInstance().getGpsChsQueue().put(gpsBody);
				
				if(gpsBody.getUploadType()==0){
					//转发统计分析服务器
					GlobalCache.getInstance().getSendAnalysisServerQueue().put(message);
				}
			}
 
		}	
		
		//CAN数据解析
		if(message.getHeader().getAttachmentId() ==MessageConstants.MESSAGE_TYPE_CAN){			
			CANBody canBody=message.getCanBody() ;
			
		
			if(canBody!=null){
				
				//生成GPS位置
				GPSBody gpsBody=new GPSBody();
				gpsBody.setDevice_unid(gpsDevice.getDevice_unid());
				gpsBody.setGps_id(message.getHeader().getGpsId());
				gpsBody.setLatitude((int)(canBody.getLatitude()*1000000));
				gpsBody.setLongitude((int)(canBody.getLongitude()*1000000));
				gpsBody.setSpeed((short)(canBody.getSpeed()*2));
				gpsBody.setGpsTime(canBody.getGpsTime());
				gpsBody.setHeight(canBody.getHeight());
				gpsBody.setDirection(canBody.getDirection());
				gpsBody.setUploadType(canBody.getUploadType());
				gpsBody.setIsCAN(1);
				
				gpsBody.setServerTime(dateformat.format(new Date()));
 
				//保存到HBase
				GlobalCache.getInstance().getGpsBodyQueue().put(gpsBody);
				//获取中文名称 保存到REDIS MYSQL
				GlobalCache.getInstance().getGpsChsQueue().put(gpsBody);
 				
				//生产CAN数据包				
				if(gpsDevice.getFiber_unid()!=null){
					canBody.setDevice_unid(gpsDevice.getDevice_unid());
					canBody.setFiber_unid(gpsDevice.getFiber_unid());
					GlobalCache.getInstance().getCanBodyQueue().put(message);
				}
			}
		}
		
		//报警数据上报
		if(message.getHeader().getAttachmentId() ==MessageConstants.MESSAGE_FAULT){			
			FaultBody faultBody=message.getFaultBody() ;
			if(faultBody!=null){
				faultBody.setDevice_unid(gpsDevice.getDevice_unid());

				GlobalCache.getInstance().getSendAnalysisServerQueue().put(message);
			}
		}	
				
		//报表数据上报
		if(message.getHeader().getGpsCommandId() ==MessageConstants.MESSAGE_REPORT
		|| message.getHeader().getAttachmentId() ==MessageConstants.MESSAGE_REPORT){			
		    ReportBody reportBody=message.getReportBody() ;
			if(reportBody!=null){
				reportBody.setDevice_unid(gpsDevice.getDevice_unid());			 		
				GlobalCache.getInstance().getSendAnalysisServerQueue().put(message);
			}			
		}

		
		//锁车状态上报
		if(message.getHeader().getAttachmentId() == MessageConstants.MESSAGE_LOCK_NOTICE
		   || message.getHeader().getGpsCommandId() == MessageConstants.MESSAGE_LOCK_NOTICE ){				
			int lockStatus=message.getLockBody().getLockType();		
			int lockTime=message.getLockBody().getLeftTime();
			HashMap<String, String> map=new HashMap<String,String>();
			map.put("lock_status", Integer.toString(lockStatus));	
			map.put("lock_time", Integer.toString(lockTime));	
			redisUtil.add("GPSPosition", gpsDevice.getGpsID(), map);
		}				
		//锁车应答
	}
	
}
