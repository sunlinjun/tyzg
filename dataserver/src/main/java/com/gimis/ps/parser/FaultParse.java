package com.gimis.ps.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gimis.ps.msg.FaultBody;
import com.gimis.ps.msg.FaultData;
import com.gimis.util.MessageTools;
 

public class FaultParse {

	private FaultBody faultBody;

	public FaultParse() {
		faultBody = new FaultBody();
	}

	public void deCode(String gpsId, ByteBuffer buf) {
		faultBody.setGps_id(gpsId);
		
		faultBody.setStatus(buf.getShort());
		int count=buf.get();
		if(count>0){
			List<FaultData> list=new ArrayList<FaultData>();
			for(int i=0;i<count;i++){				
				FaultData faultData=new FaultData();	
				
				short a1=buf.get();
				short a2=buf.get();
				short a3=buf.get();
				short a4=buf.get();
				short a5=buf.get();
				short a6=buf.get();
				
				long faultCode=a6*0x10000000000L+a5*0x100000000L+a4*0x1000000+a3*0x10000+a2*0x100+a1;
				faultData.setFaultCode(faultCode);
				
				faultData.setFaultStatus(buf.get());
				
		        byte[] temp = new byte[6];
		        buf.get(temp, 0, temp.length);
		        try
		        {
		        	faultData.setFaultTime(MessageTools.bytesToDate(temp));
		        	faultData.getFaultTime().setTime(faultData.getFaultTime().getTime()+28800000);
		        }
		        catch (Exception ex)
		        {
		        	//log.error("转换日期异常：", ex);  
		        	faultData.setFaultTime(new Date());	            
		        }
				
				list.add(faultData);
			}
			
			faultBody.setList(list);

			
		}
	}

	public FaultBody getFaultBody() {
		// TODO Auto-generated method stub
		return this.faultBody;
	}
}
