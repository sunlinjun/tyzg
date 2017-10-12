package com.gimis.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializeObjUtil {
	
	//分配内存，反序列化对象  	  
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T DeserializeMessage(byte[] data){
    	    
		T message = null;
		ByteArrayInputStream ios = new ByteArrayInputStream(data);  
        ObjectInputStream ois=null;
		try {
			ois = new ObjectInputStream(ios);			
             //返回生成的新对象  
			message = (T) ois.readObject();  			

		} catch (Exception e) {
		}  finally{
			if(ois!=null){
				try {
					ois.close();
				} catch (IOException e) {
				}	
			}
			
			try {
				ios.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return message;
	}
	
	public static <T extends Serializable> byte[]  SerializeMessage(T obj){	    
        ByteArrayOutputStream out = new ByteArrayOutputStream();        
        ObjectOutputStream obs;
		try {
			obs = new ObjectOutputStream(out);
	        obs.writeObject(obj);  
	        obs.close(); 
	        return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}  		
		return null;	   
	}	

}
