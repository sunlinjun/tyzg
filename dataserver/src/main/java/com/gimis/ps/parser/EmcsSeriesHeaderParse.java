package com.gimis.ps.parser;

import java.nio.ByteBuffer;
import com.gimis.ps.msg.DefaultHeader;
import com.gimis.util.MessageTools;
 

public class EmcsSeriesHeaderParse {
	
	private DefaultHeader header;

    public EmcsSeriesHeaderParse()
    {
        header = new DefaultHeader();
    }
    
 
    public void deCode(ByteBuffer buf)
    {
        // TODO Auto-generated method stub
 
        header.setGpsCommandId(buf.getShort());
        header.setGpsLength(buf.getShort());
        header.setAttachmentId(buf.getShort());
        header.setAttachmentLength(buf.getShort());
        header.setSequenceId(buf.getShort());
        byte[] gpsId = new byte[6];
        buf.get(gpsId, 0, gpsId.length);
        header.setGpsId(new String(gpsId).trim());
        header.setSubDeviceId(buf.get());
        header.setGpsManufacturers(buf.get());
        
        //密匙，暂时不校验
        buf.get();
    }

 
    public byte[] enCode(DefaultHeader header)
    {
        // TODO Auto-generated method stub
        this.header = (DefaultHeader)header;
        ByteBuffer buf = MessageTools.allocate(this.getLength());
        buf.putShort(this.header.getGpsCommandId());
        buf.putShort(this.header.getGpsLength());
        buf.putShort((short)this.header.getAttachmentId());
        buf.putShort(this.header.getAttachmentLength());
        buf.putShort(this.header.getSequenceId());
        buf.put(this.header.getGpsId().getBytes());
        buf.put(this.header.getSubDeviceId());
        buf.put(this.header.getGpsManufacturers());
        buf.put((byte)0);
        return buf.array();
    }

    
 
    public DefaultHeader getHeader()
    {
        // TODO Auto-generated method stub
        return header;
    }

    
 
    public short getLength()
    {
        // TODO Auto-generated method stub
        return 19;
    }
}
