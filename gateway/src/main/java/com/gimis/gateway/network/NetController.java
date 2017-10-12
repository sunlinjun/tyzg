package com.gimis.gateway.network;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.gateway.network.coder.CommandProtocolCodecFactory;
import com.gimis.gateway.network.coder.PackageDecoder;
import com.gimis.gateway.network.coder.PackageEncoder;
import com.gimis.util.Config;
import com.gimis.util.SourceMessage;
 
public class NetController  extends IoHandlerAdapter {
 
	private BlockingQueue<SourceMessage> sendQueue;
	
	private SessionManager sessionManager;
	
	private Config config;

	private PackageEncoder encoder;

	private PackageDecoder decoder;
	
	private static final Logger logger = LoggerFactory.getLogger(NetController.class);
	
	public NetController(Config config,SessionManager sessionManager, BlockingQueue<SourceMessage> queue){
		this.config=config;
		this.sessionManager=sessionManager;
		this.sendQueue=queue;
		encoder = new PackageEncoder();
		decoder = new PackageDecoder();	
	}

	public boolean start(){
		NioSocketAcceptor tcpAcceptor = new NioSocketAcceptor();
		
		//加上这句话，避免重启时提示地址被占用			
		tcpAcceptor.setReuseAddress(true);
		
		tcpAcceptor.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(new CommandProtocolCodecFactory(
						encoder, decoder)));
		
		SocketSessionConfig ssc = tcpAcceptor.getSessionConfig();
		ssc.setReuseAddress(true);
	    ssc.setWriterIdleTime(config.getWriteIdleTime());
		ssc.setReaderIdleTime(config.getReaderIdleTime());
		
		tcpAcceptor.setHandler(this);

		try {						
			tcpAcceptor.bind(new InetSocketAddress(config.getTerminalTCPPort()));
			logger.info("bind terminal tcp port success ! port:" + config.getTerminalTCPPort());		
			System.out.println("bind terminal tcp port success ! port:" + config.getTerminalTCPPort());
		} catch (Exception e) {
			logger.error("bind terminal tcp port error ! port:" + config.getTerminalTCPPort());
			return false;
		}
		return true;
	}
	
	/**
	 * 接收网络数据
	 */
	public void messageReceived(IoSession session, Object message) {
		
		byte[] data =  (byte[])message;
 
	    if(data==null){	
	    	session.closeOnFlush();   	
	    }else{
	    	
			sessionManager.addSession(session.getRemoteAddress().toString(), session);
			if(data!=null){	 				
				SourceMessage sourceMessage=new SourceMessage(config.getGateWayID()
						,session.getRemoteAddress().toString()
						,data);				
				if(config.getIsDebug()==1){
				//	logger.info("recv terminal data: "+session.getRemoteAddress().toString()
				//			+" "+sourceMessage.toString());
				}
				
				try {
					sendQueue.put(sourceMessage);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("add queue exception! "+e.toString());
				}			
			}
	    	
	    }
	}

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.error("GatewayServer error! :"+session.getRemoteAddress().toString(), cause);
	}

	public void sessionClosed(IoSession session) {
		sessionManager.removeSession(session);
	}

	public void sessionIdle(IoSession session, IdleStatus status) {		
		if (status == IdleStatus.READER_IDLE || status == IdleStatus.WRITER_IDLE) {	
			session.closeOnFlush();
		} 
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		/*
		 * Long simno = getSimnoFromSession(session); if (simno != null &&
		 * message instanceof IoBuffer) { logger.info("gateway->" + simno + "("
		 * + session.getRemoteAddress().toString().replace("/", "") + "):" +
		 * ((IoBuffer)message).getHexDump()); }
		 */
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}	

}
