package com.gimis.gateway.network.coder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

 

public class PackageEncoder implements ProtocolEncoder {

	public void dispose(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	public void encode(IoSession session, Object object,
			ProtocolEncoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		if (object instanceof byte[]){
			byte[] data = (byte[])object;
			IoBuffer buffer = IoBuffer.wrap(data);			
			out.write(buffer);
		}
	}

}

