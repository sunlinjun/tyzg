package com.gimis.dataserver.fault;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.ps.msg.FaultBody;
import com.gimis.ps.msg.FaultData;
import com.gimis.redis.RedisUtil;
import com.gimis.util.DBConnection;
import com.gimis.util.GlobalCache;
import com.mysql.jdbc.Connection;

import redis.clients.jedis.JedisPool;


/*
 * 保存车机报警数据
 */
public class SaveFaultThread extends Thread{
	
	private static final Logger logger = LoggerFactory.getLogger(SaveFaultThread.class);
	
	//建立一个Mysql连接
	private Connection connection=null;
	 
	private ArrayList<String> faultCodeList=new ArrayList<String>();
	
	private SimpleDateFormat sdf;
 
	//redis工具
	private RedisUtil redisUtil;
	
	public SaveFaultThread(){
		sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	JedisPool jedisPool= GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
    	redisUtil = new RedisUtil(jedisPool);		
	}
	
	private void createConnection(){
		try {
			connection=DBConnection.getConnection();
			connection.setAutoCommit(false);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error("连接mysql出错!"+e1.toString());
		}
	}
	
	private void initDBConnection(){
		
		if(connection==null){
			createConnection();	
		} else{
			try {
				if(connection.isClosed()){
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
		
		while(true){
			try {
				FaultBody faultBody=GlobalCache.getInstance().getFaultBodyQueue().take();
 
				//保存到数据库和redis中
				saveToMySQL(faultBody); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
	    }
	}

	//结束报警
	private String updateFaultSlave(String device_unid,String beginDT,FaultData faultData){
		StringBuilder sb=new StringBuilder();
 
		sb.append(" update  FAULT_DEVICE_LOG set ");
		sb.append(" FAULT_TIME_ET='").append( sdf.format(faultData.getFaultTime()) ).append("'");
		sb.append(" where  DEVICE_UNID='").append(device_unid).append("'");
		sb.append(" and FAULT_TIME_BT='").append( beginDT ).append("'");

		return sb.toString();		
	}
	
	//新增
	private String addFaultSlave(String device_unid,FaultData faultData){
		StringBuilder sb=new StringBuilder();
		
		sb.append("insert into FAULT_DEVICE_LOG set ");
		sb.append("DEVICE_UNID='").append(device_unid).append("',");
		sb.append("FAULT_CODE=").append(faultData.getFaultCode()).append(",");
		sb.append("FAULT_TIME_BT='").append( sdf.format(faultData.getFaultTime()) ).append("'");
 
		return sb.toString();		
	}
	
	//新增前，结束原来的报警
	private String updateFaultSlave2(String device_unid,FaultData faultData){
		StringBuilder sb=new StringBuilder();
 
		sb.append(" update  FAULT_DEVICE_LOG set ");
		sb.append(" FAULT_TIME_ET='").append( sdf.format(faultData.getFaultTime()) ).append("'");
		sb.append(" where  DEVICE_UNID='").append(device_unid).append("'");
		sb.append(" and FAULT_TIME_ET=null");

		return sb.toString();		
	}	
	
	public void saveToMySQL(FaultBody faultBody){
		initDBConnection();
		
		Statement st = null;		
		try {
			
			faultCodeList.clear(); 
			
			if (faultBody.getList()==null){
				return;
			}
			
			st = connection.createStatement();		

			for(int i=0;i<faultBody.getList().size();i++){
				FaultData faultData= faultBody.getList().get(i);
				
				//查看是开始报警，还是结束报警
				String key="0_"+Long.toString(faultData.getFaultCode());				
				String faultTime= redisUtil.getField("FAULT", faultBody.getDevice_unid(),key );
																
				//开始报警
				if( faultData.getFaultStatus()==1){
					
					//如果上次结束，那么将 新增 ， 否则不处理
					if(faultTime==null){
						
						 //新增
						 faultCodeList.add("1;"+key+";"+sdf.format(faultData.getFaultTime()));
						 
						 //String sqlstr1 = addFaultMaster(faultBody.getDevice_unid(),faultData);
		        		 //st.executeUpdate(sqlstr1);
						 
						 //将原来的没有结束的报警结束
						 String sqlstr1 = updateFaultSlave2(faultBody.getDevice_unid(),faultData);
		        		 st.executeUpdate(sqlstr1);

						 String sqlstr2 = addFaultSlave(faultBody.getDevice_unid(),faultData);
						 st.executeUpdate(sqlstr2);						 
					}
				}				
				else{  //结束报警
					
					//如果没有开始，就结束，那么 开始时间=结束时间
					if(faultTime==null){
						faultTime= sdf.format(faultData.getFaultTime());
					}
					
					//删除报警表
					//String sqlstr1 = deleteFaultMaster(faultBody.getDevice_unid(),faultData);
					//st.executeUpdate(sqlstr1);
 
					//修改报警日志表
					String  sqlstr2 = updateFaultSlave(faultBody.getDevice_unid(),faultTime,faultData);
					st.executeUpdate(sqlstr2);
					
					  //删除
					faultCodeList.add("2;"+key+";"+faultTime);
				}
 
			}
			
 
			//提交
			connection.commit();
			
			//提交成功后，修改报警状态
			for(String status: faultCodeList){
				
				String[] array=status.split(";");
				
				if(array[0].equals("1")){
					redisUtil.setField("FAULT", faultBody.getDevice_unid(), array[1], array[2]);
				}else {
					redisUtil.removeField("FAULT", faultBody.getDevice_unid(), array[1]);
				}				
			}

 
 
		} catch (Exception e) {
			//logger.error(e.toString()+" "+sb.toString());
			
			try {
				//回滚
				connection.rollback();
				st.close();
				connection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}				
			connection=null;
		}finally{
			try {
				st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
		
	}
 
	

}
