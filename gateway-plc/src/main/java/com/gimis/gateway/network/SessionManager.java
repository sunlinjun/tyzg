package com.gimis.gateway.network;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

public class SessionManager {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
	
	private ConcurrentHashMap<String, IoSession> map = new ConcurrentHashMap<String, IoSession>();
 
	public  void addSession(String address, IoSession session) {
		try{
			map.put(address, session);
			if (!session.containsAttribute("ID")) {
				session.setAttribute("ID", address);
			}	
		}catch(Exception e){
			logger.error("addSession exception!"+e.toString());
		}
	}
	
	public IoSession getSession(String address) {
		return map.get(address);
	}
	
	public  void removeSession(IoSession session) {
		
		try{
			Object o = session.getAttribute("ID");
			if (o != null && o instanceof String) {			 
				String ID = (String) o;
				map.remove(ID);							
			}			
		}catch(Exception e){
			logger.error("removeSession exception!"+e.toString());
		}		
	}
	
	public void writeSession(String address,byte[] data){		
		try{
			IoSession session=this.getSession(address);
		    if(session!=null){
			    session.write(data);
		    }
		}catch(Exception e){
			logger.error("write session exception!"+e.toString());
		}
	}

	public int getCount() {
		return map.size();
	}	
}
