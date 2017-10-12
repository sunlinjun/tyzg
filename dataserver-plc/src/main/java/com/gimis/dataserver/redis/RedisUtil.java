package com.gimis.dataserver.redis;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtil {
	
	   private static final String KEY_SYSTEM = "#TZ:";
	    
	   private JedisPool jedisPool;
		
		public RedisUtil(JedisPool jedisPool){
			this.jedisPool=jedisPool;
		}
		
	    private String getNameSpace(String nameSpace,String key)
	    {
	    	key = nameSpace+  KEY_SYSTEM + key ;
	        return key;
	    }
	    
		public void add(String nameSpace,String key, Map<String, String> value) {
			if (null != key && null != value) {   
				Jedis jedis= jedisPool.getResource();
				try{					
					jedis.hmset(getNameSpace(nameSpace,key), value);					
				}finally{
					jedis.close();
				}
			}
		}
		
		public void add(String key,String value){
			if (null != key && null != value) {   
				Jedis jedis= jedisPool.getResource();
				try{	
					jedis.set(key, value, "NX","EX",1200);				
				}finally{
					jedis.close();
				}
			}
		}
		
		public String getValue(String key){
			String value=null;
			if (null != key ) {   
				Jedis jedis= jedisPool.getResource();
				try{	
					value=jedis.get(key);			
				}finally{
					jedis.close();
				}
			}
			return value;
		}

		public void remove(String nameSpace,String key) {
			if (null != key ) {
				Jedis jedis= jedisPool.getResource();
				try{				
					jedis.del(getNameSpace(nameSpace,key));				
				}finally{
					jedis.close();
				}			 
			}
		}
			
		public void remove(String key) {
			if (null != key ) {
				Jedis jedis= jedisPool.getResource();
				try{				
					jedis.del(key);				
				}finally{
					jedis.close();
				}			 
			}
		}	
		
		public Map<String, String> getAllFields(String nameSpace,String key){
			
			Map<String, String> result=null;
			Jedis jedis= jedisPool.getResource();
			try{				
				result=jedis.hgetAll(getNameSpace(nameSpace,key));		
			}finally{
				jedis.close();
			}
	        return result;
		}
		
	    public String getField(String nameSpace,String key , String field)
	    {
	        String result = null;
			Jedis jedis= jedisPool.getResource();
			try{				
				result=jedis.hget(getNameSpace(nameSpace,key), field);	
			}finally{
				jedis.close();
			}
	        return result;
	    }

	    public void setField(String nameSpace,String key , String field,String value)
	    {
	 
			Jedis jedis= jedisPool.getResource();
			try{		
				jedis.hset(getNameSpace(nameSpace,key), field, value);
			}finally{
				jedis.close();
			}
	    }
	    
		public void removeField(String nameSpace,String key, String field) {
			if (null != key && null != field ) {
				Jedis jedis= jedisPool.getResource();
				try{				
					jedis.hdel(getNameSpace(nameSpace,key), field);		
				}finally{
					jedis.close();
				}
				 
			}
		}	

}
