package com.gimis.gateway.network;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Calendar;
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
import com.gimis.util.PlcBody;
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
		
		ssc.setMaxReadBufferSize(4096);
		
		tcpAcceptor.setHandler(this);
		

		try {						
			tcpAcceptor.bind(new InetSocketAddress(config.getTerminalTCPPort()));
			logger.info("bind plc-terminal tcp port success ! port:" + config.getTerminalTCPPort());		
			System.out.println("bind plc-terminal tcp port success ! port:" + config.getTerminalTCPPort());
		} catch (Exception e) {
			logger.error("bind plc-terminal tcp port error ! port:" + config.getTerminalTCPPort());
			return false;
		}
		return true;
	}
	
	
	//心跳应答
	private void heartReponse(IoSession session,byte[] data){		
		try{
			byte[] sendData=new byte[11];
			sendData[0]=(byte)0x81;
			sendData[1]=8;
			sendData[2]=0;
			System.arraycopy(data,3, sendData, 3, 8);
			session.write(sendData);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
 
	//数据应答
	private void dataResponse(IoSession session,byte[] data){
		
		try{
			byte[] sendData=new byte[11];
			sendData[0]=(byte)0x84;
			sendData[1]=8;
			sendData[2]=0;
			System.arraycopy(data,3, sendData, 3, 8);
			session.write(sendData);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//校时应答
	private void timeResponse(IoSession session,byte[] data){
		try{
			byte[] sendData=new byte[21];
			sendData[0]=(byte)0x83;
			sendData[1]=18;
			sendData[2]=0;
			System.arraycopy(data,3, sendData, 3, 8);
			
			sendData[11]=1;
			sendData[12]=0;
			
			sendData[13]=6;
			sendData[14]=0;	
			
			
			Calendar now = Calendar.getInstance();  
			
	 	
			sendData[15]=(byte)(now.get(Calendar.YEAR)-2000);
			sendData[16]=(byte)(now.get(Calendar.MONTH) + 1);
			sendData[17]=(byte)(now.get(Calendar.DAY_OF_MONTH));
			
			sendData[18]=(byte)(now.get(Calendar.HOUR_OF_DAY));
			sendData[19]=(byte)(now.get(Calendar.MINUTE));
			sendData[20]=(byte)(now.get(Calendar.SECOND));
			
 
			session.write(sendData);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 接收网络数据
	 */
	public void messageReceived(IoSession session, Object message) {
 
		try{	
			byte[] data = (byte[])message;
		    if(data==null){	
		    	sessionManager.removeSession(session);
		    	session.closeOnFlush(); 	
		    }else{
 		
				sessionManager.addSession(session.getRemoteAddress().toString(), session);
				if(data!=null){	 
 
					int cmdId=data[0];	
					
					//心跳指令
					if(cmdId==1){					
						heartReponse(session,data);
					}
					//校准时间
					else if(cmdId==3){
						timeResponse(session,data);
					}
					
					//设备主动上传数据
					else if(cmdId==4){
						dataResponse(session,data);
					}
					
					SourceMessage sourceMessage=new SourceMessage(config.getGateWayID()
							,session.getRemoteAddress().toString()
							,data);		
					
 
					if(config.getIsDebug()==1){		
																	
						PlcBody plcBody = parser(data);
										
						if(config.getDebugID().indexOf(plcBody.getPlcId())>-1){
							logger.info("recv data: "+session.getRemoteAddress().toString()
									+" "+sourceMessage.toString());
							
							String content ="";
							if(config.getShowDebugDetail()==1){
								try {
									if(plcBody.getData()!=null){
										 content = "\r\n"+new String(plcBody.getData(), "ISO8859-1");	
									}						
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 	
							}

							logger.info(plcBody.toString()+" "+content);						
						}
						

					}else{
						//String id=bytesToAscii(data,5,6);
						//logger.info(" id:"+id);	
					}
					
					try {
						sendQueue.put(sourceMessage);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("add queue exception! "+e.toString());
					}			
				}
		    	
		    }

			
		}catch(Exception e){	
			if(session!=null){
				sessionManager.removeSession(session);
				session.closeOnFlush();				
			}
		}
 
	}

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		//logger.error("GatewayServer error! :"+session.getRemoteAddress().toString(), cause);
	}

	public void sessionClosed(IoSession session) {
		if(session!=null){
			sessionManager.removeSession(session);
		}	
	}

	public void sessionIdle(IoSession session, IdleStatus status) {
		
		try{
			if (status == IdleStatus.READER_IDLE || status == IdleStatus.WRITER_IDLE) {						
				session.closeOnFlush();
			} 	
		}catch(Exception e){
			
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
		
	public static String bytesToAscii(byte[] bytes) {  
        return bytesToAscii(bytes, 0, bytes.length);  
    } 
	
	public static String bytesToAscii(byte[] bytes, int offset, int dateLen) {  
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dateLen <= 0)) {  
            return null;  
        }  
        if ((offset >= bytes.length) || (bytes.length - offset < dateLen)) {  
            return null;  
        }  
  
        String asciiStr = null;  
        byte[] data = new byte[dateLen];  
        System.arraycopy(bytes, offset, data, 0, dateLen);  
        try {  
            asciiStr = new String(data, "ISO8859-1");  
        } catch (Exception e) {  
        }  
        return asciiStr;  
    } 
	
	
	private PlcBody parser(byte[] data){
		
		try{
			PlcBody plcBody=new PlcBody();
			plcBody.setCmdNo(data[0]);
			
			int len=(data[2] & 0xFF)*256+(data[1] & 0xFF)+3;
			plcBody.setSize(len);
			
			int seqNo=(data[4] & 0xFF)*256+(data[3] & 0xFF);
			
			plcBody.setSeqNo(seqNo);
			
			String id=bytesToAscii(data,5,6);
			plcBody.setPlcId(id);
			
			
			if(len>=15){
				int dataType=(data[12] & 0xFF)*256+(data[11] & 0xFF);					
				plcBody.setDataType(dataType);
				
				int dataLen=(data[14] & 0xFF)*256+(data[13] & 0xFF);
	 
				if(dataLen>0){
					byte[] sourceData=new byte[dataLen];
					System.arraycopy(data, 15, sourceData, 0, dataLen);
					plcBody.setData(sourceData);
				}				
			}
			 
			return plcBody;			
		}catch(Exception e){
			System.out.println(e.toString());
			return null;
		}

	}

}
