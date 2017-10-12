package com.gimis.dataserver.redis;

import java.io.IOException;
import java.util.Properties;

import com.gimis.util.PropertiesTools;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPoolFactory {
	
	private JedisPool jedisPool; 
	 
	public RedisPoolFactory(){
		 Properties prop = new Properties();
		 
		 try {
			prop = PropertiesTools.loadProperties("cfg.properties", System.getProperty("user.dir"));
	
			String addr=prop.getProperty("redis.addr").trim();
			String auth=prop.getProperty("redis.auth").trim();
			int port=Integer.parseInt(prop.getProperty("redis.port").trim());
			
			int maxIdle=Integer.parseInt(prop.getProperty("redis.maxIdle").trim());
			int maxTotal=Integer.parseInt(prop.getProperty("redis.maxTotal").trim());
			int maxWaitMillis=Integer.parseInt(prop.getProperty("redis.maxWaitMillis").trim());
			int timeout=Integer.parseInt(prop.getProperty("redis.timeout").trim());

			JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
	        jedisPoolConfig.setMaxIdle(maxIdle);
	        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
	        jedisPoolConfig.setMaxTotal(maxTotal);
	        jedisPoolConfig.setTestOnBorrow(true);
	        
	        if(auth!=null  && auth.equals("")==false){
	        	//需要用户名，密码登录
	        	jedisPool = new JedisPool(jedisPoolConfig, addr, port, timeout, auth);
	        }else{
		        //无需用户名，密码登录
		        jedisPool = new JedisPool(jedisPoolConfig, addr, port);
	        }
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public JedisPool redisPoolFactory() {        
        return jedisPool;
    }


	public JedisPool getJedisPool() {
		return jedisPool;
	}

}
