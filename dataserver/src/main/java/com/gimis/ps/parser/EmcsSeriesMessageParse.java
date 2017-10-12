package com.gimis.ps.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.ps.msg.DefaultMessage;
import com.gimis.util.MessageTools;

/*
 * 南车,太重3G协议解析
 */
public class EmcsSeriesMessageParse {

    private static final Logger logger = LoggerFactory.getLogger(EmcsSeriesMessageParse.class);
    
    
    
	public DefaultMessage deCodeSimpleMessage(byte[] data){
		DefaultMessage message = new DefaultMessage();
		
		//已经是去掉包头包尾的数据
		byte[] transData=data;
		             
        //4、解析头
        EmcsSeriesHeaderParse headerParse = new EmcsSeriesHeaderParse();
        headerParse.deCode(MessageTools.wrap(transData, 0, headerParse.getLength()));
        message.setHeader(headerParse.getHeader());
        
        //5、解析GPS内容       
        GpsHeartBeatParse gpsBodyParse = null;
        //McuBodyParse mcuBodyParse = null;
        gpsBodyParse = getGpsBodyParse(message,
        		headerParse.getHeader().getGpsCommandId(), transData,
                headerParse.getLength(), message.getHeader().getGpsLength());
 
        if (null != gpsBodyParse)
        {
        	message.setGpsBody(gpsBodyParse.getGpsBody());
        }
        
        //6、解析CAN数据     
        CANParse canParse = null;
        canParse = getCANBodyParse(message,
        		headerParse.getHeader().getAttachmentId(), transData,
                headerParse.getLength(), message.getHeader().getAttachmentLength());
 
        if (null != canParse)
        {
        	message.setCanBody(canParse.getCanBody());
        }
        
        //7 解析锁车消息
        LockParse lockParse = null;
        lockParse = getLockParse(message,
        		headerParse.getHeader().getGpsCommandId()  , transData,
                headerParse.getLength(), message.getHeader().getGpsLength()  );
 
        if (null != lockParse)
        {
        	message.setLockBody(lockParse.getLockBody());
        }else{
        	//特殊处理  
            lockParse = getLockParse(message,
            		headerParse.getHeader().getAttachmentId()  , transData,
                    headerParse.getLength(), message.getHeader().getAttachmentLength()  );
            if (null != lockParse){
                message.setLockBody(lockParse.getLockBody());            	
            } 
        }
        
        //8、解析报警数据     
        FaultParse faultParse = null;
        faultParse = getFaultParse(message,
        		headerParse.getHeader().getAttachmentId(), transData,
                headerParse.getLength(), message.getHeader().getAttachmentLength());
 
        if (null != faultParse)
        {
        	message.setFaultBody(faultParse.getFaultBody() );
        }  
        
        //9、解析日报表数据
        ReportParse reportParse = null;        
        reportParse = getReportParse(message,
        		headerParse.getHeader().getGpsCommandId(), transData,
                headerParse.getLength(), message.getHeader().getGpsLength());
 
        if (null != reportParse)
        {
        	message.setReportBody(reportParse.getReportBody());  
        } 
		return message;
	}
	/*
	public DefaultMessage deCodeMessage(byte[] data){
		DefaultMessage message = new DefaultMessage();
		
		//0、判断头尾标识是否正确
        if (MessageTools.hasIdentifyTag(data)==false){
        	message.setErrorMsg("消息头或尾不正确!");
        	return message;
        }
        
        //1、去掉头尾，获取数据块内容
        byte[] pureData = getPureData(data);
        
        //2、转义还原
        byte[] transData = MessageTools.deCodeFormat(pureData);
        
        //3、验证效验码，如果验证码正确则解析            
        if (MessageTail.parseMessageTail(transData)==false){
        	message.setErrorMsg("校验码不正确!");
        	return message;
        }
            
        //4、解析头
        EmcsSeriesHeaderParse headerParse = new EmcsSeriesHeaderParse();
        headerParse.deCode(MessageTools.wrap(transData, 0, headerParse.getLength()));
        message.setHeader(headerParse.getHeader());
        
        //5、解析GPS内容       
        GpsHeartBeatParse gpsBodyParse = null;
        //McuBodyParse mcuBodyParse = null;
        gpsBodyParse = getGpsBodyParse(message,
        		headerParse.getHeader().getGpsCommandId(), transData,
                headerParse.getLength(), message.getHeader().getGpsLength());
 
        if (null != gpsBodyParse)
        {
        	message.setGpsBody(gpsBodyParse.getGpsBody());
        }
        
        //6、解析CAN数据     
        CANParse canParse = null;
        canParse = getCANBodyParse(message,
        		headerParse.getHeader().getAttachmentId(), transData,
                headerParse.getLength(), message.getHeader().getAttachmentLength());
 
        if (null != canParse)
        {
        	message.setCanBody(canParse.getCanBody());
        }
        
        //7 解析锁车消息
        LockParse lockParse = null;
        lockParse = getLockParse(message,
        		headerParse.getHeader().getGpsCommandId()  , transData,
                headerParse.getLength(), message.getHeader().getGpsLength()  );
 
        if (null != lockParse)
        {
        	message.setLockBody(lockParse.getLockBody());
        }else{
        	//特殊处理  
            lockParse = getLockParse(message,
            		headerParse.getHeader().getAttachmentId()  , transData,
                    headerParse.getLength(), message.getHeader().getAttachmentLength()  );
            if (null != lockParse){
                message.setLockBody(lockParse.getLockBody());            	
            }
 
        }
        
        //8、解析报警数据     
        FaultParse faultParse = null;
        faultParse = getFaultParse(message,
        		headerParse.getHeader().getAttachmentId(), transData,
                headerParse.getLength(), message.getHeader().getAttachmentLength());
 
        if (null != faultParse)
        {
        	message.setFaultBody(faultParse.getFaultBody() );
        }  
                
		return message;
	}
	
	*/
	
    /**
     * 获取Gps解析类,并解析
     * @param gpsMessageId
     * @param data
     * @param offset
     * @return
     */
    private GpsHeartBeatParse getGpsBodyParse(DefaultMessage message,
    		short gpsMessageId , byte[] data , int offset , short gpsLength)           
    {
    	
    	if(gpsMessageId==com.gimis.util.MessageConstants.MESSAGE_TYPE_HEART){
        	GpsHeartBeatParse gpsHeartBeatParse=new GpsHeartBeatParse();
        	try
            {    		
        		
        		
        		gpsHeartBeatParse.deCode(message.getHeader().getGpsId(),
        				MessageTools.wrap(data, offset, gpsLength));
            }
            catch (Exception ex)
            {
            	message.setErrorMsg("GPS解析错误!"+ex.toString());
            	return null;
            }
            return gpsHeartBeatParse;    		
    	}
    	return null;
    }	
	
    /**
     * 获取CAN解析类,并解析
     * @param gpsMessageId
     * @param data
     * @param offset
     * @return
     */
    private CANParse getCANBodyParse(DefaultMessage message,
    		short gpsMessageId , byte[] data , int offset , short gpsLength)           
    {
    	
    	
    	if(gpsMessageId==com.gimis.util.MessageConstants.MESSAGE_TYPE_CAN){
    		CANParse canParse=new CANParse();
        	try
            {    		
        		
        		
        		canParse.deCode(message.getHeader().getGpsId(),
        				MessageTools.wrap(data, offset, gpsLength));
            }
            catch (Exception ex)
            {
            	message.setErrorMsg("CAN解析错误!"+ex.toString());
            	return null;
            }
            return canParse;    		
    	}
    	
    	return null;
    }
    
    /**
     * 获取Lock解析类,并解析
     * @param gpsMessageId
     * @param data
     * @param offset
     * @return
     */
    private LockParse getLockParse(DefaultMessage message,
    		short gpsMessageId , byte[] data , int offset , short gpsLength)           
    {
    	
    	
    	if(gpsMessageId==com.gimis.util.MessageConstants.MESSAGE_LOCK_NOTICE){
    		LockParse lockParse=new LockParse();
        	try
            {    		
        		
        		
        		lockParse.deCode(message.getHeader().getGpsId(),
        				MessageTools.wrap(data, offset, gpsLength));
            }
            catch (Exception ex)
            {
            	message.setErrorMsg("LOCK解析错误!"+ex.toString());
            	return null;
            }
            return lockParse;    		
    	}
    	
    	return null;
    }	    
    
    /**
     * 获取报警解析类,并解析
     * @param gpsMessageId
     * @param data
     * @param offset
     * @return
     */
    private FaultParse getFaultParse(DefaultMessage message,
    		short gpsMessageId , byte[] data , int offset , short gpsLength)           
    {
    	
    	
    	if(gpsMessageId==com.gimis.util.MessageConstants.MESSAGE_FAULT){
    		FaultParse parse=new FaultParse();
        	try
            {    		
        		parse.deCode(message.getHeader().getGpsId(),
        				MessageTools.wrap(data, offset, gpsLength));
            }
            catch (Exception ex)
            {
            	logger.error("FAULT解析错误!"+ex.toString());
            	message.setErrorMsg("FAULT解析错误!"+ex.toString());
            	return null;
            }
            return parse;    		
    	}
    	
    	return null;
    }	
    
    /**
     * 获取日报表解析类,并解析
     * @param gpsMessageId
     * @param data
     * @param offset
     * @return
     */
    private ReportParse getReportParse(DefaultMessage message,
    		short gpsMessageId , byte[] data , int offset , short gpsLength)           
    {
 
    	if(gpsMessageId==com.gimis.util.MessageConstants.MESSAGE_REPORT){
    		ReportParse parse=new ReportParse();
        	try
            {    		
        		parse.deCode(message.getHeader().getGpsId(),
        				MessageTools.wrap(data, offset, gpsLength));
            }
            catch (Exception ex)
            {
            	logger.error("Report解析错误!"+ex.toString());
            	message.setErrorMsg("Report解析错误!"+ex.toString());
            	return null;
            }
            return parse;    		
    	}
    	
    	return null;
    }	
    
    /*
     * 获取数据块内容 
     * @param data
     * @return
     */
    private byte[] getPureData(byte[] data)
    {
        byte[] result = new byte[data.length - 2];
        if (result.length != 0)
        {
            System.arraycopy(data, 1, result, 0, data.length - 2);
        }
        return result;
    }	
	
}
