package com.gimis.util;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.gimis.dataserver.can.CANHashBody;
import com.gimis.dataserver.command.ResultCommand;
import com.gimis.dataserver.gps.GPSPositionChs;
import com.gimis.dataserver.terminal.GPSDevice;
import com.gimis.ps.msg.DefaultMessage;
import com.gimis.ps.msg.FaultBody;
import com.gimis.ps.msg.GPSBody;
import com.gimis.redis.RedisPoolFactory;
 

public class GlobalCache {
	
    private static GlobalCache instance = new GlobalCache();
     
    private RedisPoolFactory redisPoolFactory=new RedisPoolFactory();
 
	//下发命令的队列
	private BlockingQueue<SourceMessage> cmdQueue=new LinkedBlockingQueue<SourceMessage>();

	//GPS数据队列
	private BlockingQueue<GPSBody> gpsBodyQueue=new LinkedBlockingQueue<GPSBody>();
	
	//CAN数据队列
	private BlockingQueue<DefaultMessage> canBodyQueue=new LinkedBlockingQueue<DefaultMessage>();	
	
	//报警数据队列
	private BlockingQueue<FaultBody> faultBodyQueue=new LinkedBlockingQueue<FaultBody>();	
	
	//解析后的CAN Hash数据体
	private BlockingQueue<CANHashBody> canHasBodyQueue=new LinkedBlockingQueue<CANHashBody>();	
 
    
    /*
     * GPS设备对应的地址
     * key为gpsId,value为地址
     */
	private Map<String, String> gpsIpAddressMap = new Hashtable<String, String>();
	
    /*
     * GPS设备对应的地址
     * key为gpsId,value为 时间
     */
	private Map<String, Date> gpsIpAddressTimeMap = new Hashtable<String, Date>();
		
    /**
     * device UNID与 gpsId对应关系
     * key为设备 UNID,value为gpsId
     */
	private Map<String, String> deviceUnidMap = new Hashtable<String, String>();
	
    /**
     * key为设备 gpsId,value为GPSDevice
     */
	private Map<String, GPSDevice> deviceMap = new Hashtable<String, GPSDevice>();
	
    /**
     * 中文位置gps
     * key为gps编号,value为车辆编号
     */
    private Map<String, GPSPositionChs> gpsChinese = new Hashtable<String, GPSPositionChs>();
  
    /*
     * 收到的GPS,CAN消息队列
     */
    private BlockingQueue<DefaultMessage> defaultMessageQueue=new LinkedBlockingQueue<DefaultMessage>();
    
    /*
     * 收到的GPS应答队列
     */
    private BlockingQueue<ResultCommand> terminalResponseQueue=new LinkedBlockingQueue<ResultCommand>();
    
    /*
     * 发送到分析服务器的队列
     */
    private BlockingQueue<DefaultMessage> sendAnalysisServerQueue=new LinkedBlockingQueue<DefaultMessage>();
    
   
    /*
     * GPS中文解析队列
     */
    private BlockingQueue<GPSBody> gpsChsQueue=new LinkedBlockingQueue<GPSBody>();
 
    /*
     * 最后一次CAN数据时间
     */
    private Hashtable<String,Long> lastCanTimes=new Hashtable<String,Long>(); 
    
    /*
     * 设备应答序列号列表
     * key 为 gpsId   value ：   应答的序列号
     */
    private Map<String, Short> responseSEQ = new Hashtable<String, Short>();
        
    //全局的命令自增值，每下发一次命令给终端，就增加一次 
    public static short seqNumber;
    
    /*
     * 全局配置文件
     */
    private Config config=new Config();
    
    private GlobalCache()
    {

    }
 
    /**
     * 获取实例的静态方法
     * @return
     */
    public static GlobalCache getInstance()
    {
        return instance;
    }
 
	public BlockingQueue<SourceMessage> getCmdQueue() {
		return cmdQueue;
	}

	public void setCmdQueue(BlockingQueue<SourceMessage> cmdQueue) {
		this.cmdQueue = cmdQueue;
	}

	public Map<String, GPSPositionChs> getGpsChinese() {
		return gpsChinese;
	}

	public void setGpsChinese(Map<String, GPSPositionChs> gpsChinese) {
		this.gpsChinese = gpsChinese;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public BlockingQueue<DefaultMessage> getDefaultMessageQueue() {
		return defaultMessageQueue;
	}

	public void setDefaultMessageQueue(BlockingQueue<DefaultMessage> defaultMessageQueue) {
		this.defaultMessageQueue = defaultMessageQueue;
	}
 
	public Map<String, String> getGpsIpAddressMap() {
		return gpsIpAddressMap;
	}

	public void setGpsIpAddressMap(Map<String, String> gpsIpAddressMap) {
		this.gpsIpAddressMap = gpsIpAddressMap;
	}

	public BlockingQueue<GPSBody> getGpsBodyQueue() {
		return gpsBodyQueue;
	}

	public void setGpsBodyQueue(BlockingQueue<GPSBody> gpsBodyQueue) {
		this.gpsBodyQueue = gpsBodyQueue;
	}

	public RedisPoolFactory getRedisPoolFactory() {
		return redisPoolFactory;
	}

	public void setRedisPoolFactory(RedisPoolFactory redisPoolFactory) {
		this.redisPoolFactory = redisPoolFactory;
	}

	public BlockingQueue<DefaultMessage> getCanBodyQueue() {
		return canBodyQueue;
	}

	public void setCanBodyQueue(BlockingQueue<DefaultMessage> canBodyQueue) {
		this.canBodyQueue = canBodyQueue;
	}

	public Map<String, String> getDeviceUnidMap() {
		return deviceUnidMap;
	}

	public void setDeviceUnidMap(Map<String, String> deviceUnidMap) {
		this.deviceUnidMap = deviceUnidMap;
	}

	public Map<String, GPSDevice> getDeviceMap() {
		return deviceMap;
	}

	public void setDeviceMap(Map<String, GPSDevice> deviceMap) {
		this.deviceMap = deviceMap;
	}

	public Map<String, Date> getGpsIpAddressTimeMap() {
		return gpsIpAddressTimeMap;
	}

	public void setGpsIpAddressTimeMap(Map<String, Date> gpsIpAddressTimeMap) {
		this.gpsIpAddressTimeMap = gpsIpAddressTimeMap;
	}

 

	public BlockingQueue<FaultBody> getFaultBodyQueue() {
		return faultBodyQueue;
	}

	public void setFaultBodyQueue(BlockingQueue<FaultBody> faultBodyQueue) {
		this.faultBodyQueue = faultBodyQueue;
	}

	public BlockingQueue<CANHashBody> getCanHasBodyQueue() {
		return canHasBodyQueue;
	}

	public void setCanHasBodyQueue(BlockingQueue<CANHashBody> canHasBodyQueue) {
		this.canHasBodyQueue = canHasBodyQueue;
	}

 

	public Map<String, Short> getResponseSEQ() {
		return responseSEQ;
	}

	public void setResponseSEQ(Map<String, Short> responseSEQ) {
		this.responseSEQ = responseSEQ;
	}

	public BlockingQueue<ResultCommand> getTerminalResponseQueue() {
		return terminalResponseQueue;
	}

	public void setTerminalResponseQueue(BlockingQueue<ResultCommand> terminalResponseQueue) {
		this.terminalResponseQueue = terminalResponseQueue;
	}

	public BlockingQueue<DefaultMessage> getSendAnalysisServerQueue() {
		return sendAnalysisServerQueue;
	}

	public void setSendAnalysisServerQueue(BlockingQueue<DefaultMessage> sendAnalysisServerQueue) {
		this.sendAnalysisServerQueue = sendAnalysisServerQueue;
	}

	public BlockingQueue<GPSBody> getGpsChsQueue() {
		return gpsChsQueue;
	}

	public void setGpsChsQueue(BlockingQueue<GPSBody> gpsChsQueue) {
		this.gpsChsQueue = gpsChsQueue;
	}

	public Hashtable<String,Long> getLastCanTimes() {
		return lastCanTimes;
	}

	public void setLastCanTimes(Hashtable<String,Long> lastCanTimes) {
		this.lastCanTimes = lastCanTimes;
	}

}
