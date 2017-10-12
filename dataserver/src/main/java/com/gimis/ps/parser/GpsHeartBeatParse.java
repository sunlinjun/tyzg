package com.gimis.ps.parser;

import java.nio.ByteBuffer;
import java.util.Date;

import com.gimis.ps.msg.GPSBody;
import com.gimis.util.MessageConstants;
import com.gimis.util.MessageTools;
 

public class GpsHeartBeatParse {
	
	   private GPSBody gpsHeartBeat;
	    
 
	    public GpsHeartBeatParse()
	    {
	        gpsHeartBeat = new GPSBody();
	    }

	    public void deCode(String gpsId,ByteBuffer buf)
	    {
	    	gpsHeartBeat.setGps_id(gpsId);
	        gpsHeartBeat.setUploadType(buf.get());
	        gpsHeartBeat.setLongitude(buf.getInt());
	        gpsHeartBeat.setLatitude(buf.getInt());
	        gpsHeartBeat.setSpeed(buf.getShort());
	        gpsHeartBeat.setHeight(buf.getShort());
	        gpsHeartBeat.setDirection(buf.getShort());
	        gpsHeartBeat.setDistance(buf.getInt());
	        gpsHeartBeat.setHardwareVersion(buf.getShort());
	        gpsHeartBeat.setSoftVersion(MessageTools.getByteValue(buf.get()) + "."
	                + MessageTools.getByteValue(buf.get()) + "." + MessageTools.getByteValue(buf.get()) + "."
	                + MessageTools.getByteValue(buf.get()));
	        byte[] temp = new byte[6];
	        buf.get(temp, 0, temp.length);
	        try
	        {
	        	gpsHeartBeat.setGpsTime(MessageTools.bytesToDate(temp));   
	        	gpsHeartBeat.getGpsTime().setTime(gpsHeartBeat.getGpsTime().getTime()+28800000);
	        }
	        catch (Exception ex)
	        {
	        	//log.error("转换日期异常：", ex);  
	        	gpsHeartBeat.setGpsTime(new Date());	            
	        }
	        
	        byte state=buf.get();
	        
	        gpsHeartBeat.setAccStatus((byte)(state & MessageConstants.B0));
	        gpsHeartBeat.setLocationStatus((byte)((state & MessageConstants.B1)>>1));
	        gpsHeartBeat.setGpsModelStatus((byte)((state & MessageConstants.B6)>>6));
	        gpsHeartBeat.setSatelliteCount((byte) ((state & 0x3c)>>2));
	        gpsHeartBeat.setPowerBatteryStatus((byte)((state >> 7) & MessageConstants.B0));
	    }
 
	    public GPSBody getGpsBody()
	    {
	        // TODO Auto-generated method stub
	        return this.gpsHeartBeat;
	    }

 

}
