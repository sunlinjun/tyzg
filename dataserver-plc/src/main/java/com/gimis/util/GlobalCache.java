package com.gimis.util;
 
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.gimis.dataserver.command.ResultCommand;
import com.gimis.dataserver.redis.RedisPoolFactory;
import com.gimis.dataserver.terminal.GPSDevice;
 
public class GlobalCache {
	
	public static int seqNumber;
		
	private static GlobalCache instance = new GlobalCache();
	
	
	//下发命令的队列
	private BlockingQueue<SourceMessage> cmdQueue=new LinkedBlockingQueue<SourceMessage>();
	
	//下发数据至分析服务器
	private BlockingQueue<String> analysisQueue=new LinkedBlockingQueue<String>();

	/*
     * 收到的plc源码数据队列 kafka中
     */
    private ConcurrentLinkedQueue<SourceMessage> sourceMessageQueue=new ConcurrentLinkedQueue<SourceMessage>();
    
    private RedisPoolFactory redisPoolFactory=new RedisPoolFactory();
    
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
	 * 保存到redis中的plc数据
	 */
	private BlockingQueue<HashMap<String, String>> plcRedisQueue=new LinkedBlockingQueue<HashMap<String, String>>();
	
	 /*
     * 收到的plc数据队列
     */
    private ConcurrentLinkedQueue<HashMap<String,HashMap<String,String>>> plcDataQueue=new ConcurrentLinkedQueue<HashMap<String,HashMap<String,String>>>();
 
    /*
     * 收到的GPS数据
     */
	private BlockingQueue<PlcContent> gpsDataQueue=new LinkedBlockingQueue<PlcContent>();

	
    
    /*
     * 收到的GPS应答队列
     */
    private BlockingQueue<ResultCommand> terminalResponseQueue=new LinkedBlockingQueue<ResultCommand>();

    
    /*
     * GPS设备对应的地址
     * key为gpsId,value为地址
     */
	private Map<String, String> gpsIpAddressMap = new Hashtable<String, String>();
	
	/*
	 * 设备流量统计
	 */
	private Hashtable<String,Long> deviceFlow =new Hashtable<String,Long>();
	
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

 

	public RedisPoolFactory getRedisPoolFactory() {
		return redisPoolFactory;
	}

	public void setRedisPoolFactory(RedisPoolFactory redisPoolFactory) {
		this.redisPoolFactory = redisPoolFactory;
	}

	public Map<String, GPSDevice> getDeviceMap() {
		return deviceMap;
	}

	public void setDeviceMap(Map<String, GPSDevice> deviceMap) {
		this.deviceMap = deviceMap;
	}

	public Map<String, String> getDeviceUnidMap() {
		return deviceUnidMap;
	}

	public void setDeviceUnidMap(Map<String, String> deviceUnidMap) {
		this.deviceUnidMap = deviceUnidMap;
	}

	public ConcurrentLinkedQueue<SourceMessage> getSourceMessageQueue() {
		return sourceMessageQueue;
	}

	public void setSourceMessageQueue(ConcurrentLinkedQueue<SourceMessage> sourceMessageQueue) {
		this.sourceMessageQueue = sourceMessageQueue;
	}

	public BlockingQueue<HashMap<String, String>> getPlcRedisQueue() {
		return plcRedisQueue;
	}

	public void setPlcRedisQueue(BlockingQueue<HashMap<String, String>> plcRedisQueue) {
		this.plcRedisQueue = plcRedisQueue;
	}

 

	public BlockingQueue<PlcContent> getGpsDataQueue() {
		return gpsDataQueue;
	}

	public void setGpsDataQueue(BlockingQueue<PlcContent> gpsDataQueue) {
		this.gpsDataQueue = gpsDataQueue;
	}

	public ConcurrentLinkedQueue<HashMap<String,HashMap<String,String>>> getPlcDataQueue() {
		return plcDataQueue;
	}

	public void setPlcDataQueue(ConcurrentLinkedQueue<HashMap<String,HashMap<String,String>>> plcDataQueue) {
		this.plcDataQueue = plcDataQueue;
	}

	public BlockingQueue<ResultCommand> getTerminalResponseQueue() {
		return terminalResponseQueue;
	}

	public void setTerminalResponseQueue(BlockingQueue<ResultCommand> terminalResponseQueue) {
		this.terminalResponseQueue = terminalResponseQueue;
	}

	public Map<String, String> getGpsIpAddressMap() {
		return gpsIpAddressMap;
	}

	public void setGpsIpAddressMap(Map<String, String> gpsIpAddressMap) {
		this.gpsIpAddressMap = gpsIpAddressMap;
	}

	public BlockingQueue<SourceMessage> getCmdQueue() {
		return cmdQueue;
	}

	public void setCmdQueue(BlockingQueue<SourceMessage> cmdQueue) {
		this.cmdQueue = cmdQueue;
	}

	public BlockingQueue<String> getAnalysisQueue() {
		return analysisQueue;
	}

	public void setAnalysisQueue(BlockingQueue<String> analysisQueue) {
		this.analysisQueue = analysisQueue;
	}

	public Hashtable<String,Long> getDeviceFlow() {
		return deviceFlow;
	}

	public void setDeviceFlow(Hashtable<String,Long> deviceFlow) {
		this.deviceFlow = deviceFlow;
	}

 

}
