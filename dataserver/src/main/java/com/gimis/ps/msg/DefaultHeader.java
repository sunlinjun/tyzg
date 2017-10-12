package com.gimis.ps.msg;

import java.io.Serializable;
 
 
public class DefaultHeader implements Serializable, Cloneable{
 

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	    private short gpsCommandId;

	    private short gpsLength;

	    private short attachmentId;

	    private short attachmentLength;

	    private short sequenceId;

	    private String gpsId;

	    private byte subDeviceId;

	    private byte gpsManufacturers;

	    private byte hostCompanies;

	    public short getGpsCommandId()
	    {
	        return gpsCommandId;
	    }

	    public void setGpsCommandId(short gpsCommandId)
	    {
	        this.gpsCommandId = gpsCommandId;
	    }

	    public short getGpsLength()
	    {
	        return gpsLength;
	    }

	    public void setGpsLength(short gpsLength)
	    {
	        this.gpsLength = gpsLength;
	    }

	    public short getAttachmentId()
	    {
	        return attachmentId;
	    }

	    public void setAttachmentId(short attachmentId)
	    {
	        this.attachmentId = attachmentId;
	    }

	    public short getAttachmentLength()
	    {
	        return attachmentLength;
	    }

	    public void setAttachmentLength(short attachmentLength)
	    {
	        this.attachmentLength = attachmentLength;
	    }

	    public short getSequenceId()
	    {
	        return sequenceId;
	    }

	    public void setSequenceId(short sequenceId)
	    {
	        this.sequenceId = sequenceId;
	    }


	    public byte getSubDeviceId()
	    {
	        return subDeviceId;
	    }

	    public void setSubDeviceId(byte subDeviceId)
	    {
	        this.subDeviceId = subDeviceId;
	    }

	    public byte getGpsManufacturers()
	    {
	        return gpsManufacturers;
	    }

	    public void setGpsManufacturers(byte gpsManufacturers)
	    {
	        this.gpsManufacturers = gpsManufacturers;
	    }

	    public byte getHostCompanies()
	    {
	        return hostCompanies;
	        }

	    public void setHostCompanies(byte hostCompanies)
	    {
	        this.hostCompanies = hostCompanies;
	    }
	    
	    
	    public String getGpsId()
	    {
	        return gpsId;
	    }

	    public void setGpsId(String gpsId)
	    {
	        this.gpsId = gpsId;
	    }

 
 
	    @Override
	    public String toString()
	    {
	        StringBuilder builder = new StringBuilder();
	        builder.append("[Can网络消息命令号/PLC命令号 : ").append(attachmentId).append(",").append("Can网络消息命令号/PLC命令长度 : ").append(attachmentLength)
	                .append(",").append("命令ID : ").append(gpsCommandId)
	                .append(",").append("终端ID : ").append(gpsId).append(",").append("消息长度 : ").append(gpsLength)
	                 .append(",").append("Gps供应商代码 : ")
	                .append(gpsManufacturers).append(",").append("消息流水号 : ").append(sequenceId).append(",")
	                .append("子设备代码 : ").append(subDeviceId).append(",").append("]");
	        return builder.toString();
	    }
	    
	    public byte[] getData(){
	    	byte[] data=new byte[19];

	    	data[0] = (byte)gpsCommandId;
	    	data[1] = (byte)(gpsCommandId >> 8);
	    	data[2] = (byte)gpsLength;
	    	data[3] = (byte)(gpsLength >> 8);	    	
	    	data[4] = (byte)attachmentId;
	    	data[5] = (byte)(attachmentId >> 8);	  
	    	data[6] = (byte)attachmentLength;
	    	data[7] = (byte)(attachmentLength >> 8);	
	    	data[8] = (byte)sequenceId;
	    	data[9] = (byte)(sequenceId >> 8);	

	    	if(gpsId.length()>=6){
	    		for(int i=0;i<6;i++){
		    		data[i+10]=(byte)gpsId.charAt(i);
		    	}	
	    	}else{
	    		for(int i=0;i<gpsId.length();i++){
		    		data[i+10]=(byte)gpsId.charAt(i);
		    	}
	    	}
	    	
	        
	    	data[16] = (byte)subDeviceId;
	    	data[17] = (byte)gpsManufacturers;
	    	data[18] = (byte)hostCompanies;
	    	
	    	return data;

	    }
	    
	    @Override 
	    protected Object clone() throws CloneNotSupportedException {  
	        return super.clone();  
	    }  
}
