package com.gimis.gateway.network.coder;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class CommandProtocolCodecFactory implements ProtocolCodecFactory {

	private ProtocolEncoder encoder;

	private ProtocolDecoder decoder;

	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		return decoder;
	}

	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		return encoder;
	}

	public CommandProtocolCodecFactory(ProtocolEncoder encoder,
			ProtocolDecoder decoder) {
		this.encoder = encoder;
		this.decoder = decoder;
	}

}

