/**
 * 
 */
package com.gimis.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;



/**
 * [包尾解析]<p>
 * [功能详细描述]<p>
 * @author zjm
 * @version 1.0, 2011-11-30
 * @see
 * @since gms-v100
 */
public class MessageTail
{

    /**
     * 检验包尾是否正确
     * @param data
     * @return
     */
    public static boolean parseMessageTail(byte[] data)
    {
        return fiveMessageTail(data);
    }

    @SuppressWarnings("unused")
	private static boolean csrMessageTail(byte[] data)
    {
        byte[] dataTail =parseCRCMessageTailSCR(data);
        if(data[data.length-1] == dataTail[0])
        {
            return true;
        }
        return false;
    }

    private static byte[] parseCRCMessageTailSCR(byte[] data)
    {
        int temp  =  0x33;
        for(int i=1; i<data.length - 1; i++)
        {
            short x = data[i];
            if(data[i] < 0)
            {
                x = (short)(256-Math.abs(data[i]));
            }
            temp = temp ^ x;
        }
        ByteBuffer bf = ByteBuffer.allocate(1);
        bf.put((byte)temp);
        return bf.array();
    }

    private static boolean fiveMessageTail(byte[] data)
    {
        //获取包尾
        byte[] dataTail =parseCRCMessageTail(data);
        if(data[data.length-2] == dataTail[0] && data[data.length-1] == dataTail[1])
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static byte[] parseCRCMessageTail(byte[] data)
    {
        int temp  =  0xFFFF;
         for (int i = 0; i < data.length - 2; i++) 
            {
                short x = data[i];
                if(data[i]<0)
                    x = (short)(256-Math.abs(data[i]));
                temp = temp ^ x;
                for(int j = 0; j < 8; j++)
                {
                    if((temp & 0x0001) == 1)
                    {
                        temp = temp >> 1;
                        temp = temp ^ 0x1021;
                    }
                    else
                        temp = temp >> 1;
                }
            }
         ByteBuffer bf = ByteBuffer.allocate(2);
         bf.order(ByteOrder.LITTLE_ENDIAN);
         bf.putShort((short) temp);
         return bf.array();
        
    }
    
    /**
     * 生成CRC包尾
     * @param data
     * @return
     */
    public static byte[] parseCRCMessageTailShort(byte[] data)
    {
        int temp  =  0xFFFF;
         for (int i = 0; i < data.length; i++) 
            {
                short x = data[i];
                if(data[i]<0)
                    x = (short)(256-Math.abs(data[i]));
                temp = temp ^ x;
                for(int j = 0; j < 8; j++)
                {
                    if((temp & 0x0001) == 1)
                    {
                        temp = temp >> 1;
                        temp = temp ^ 0x1021;
                    }
                    else
                        temp = temp >> 1;
                }
            }
         ByteBuffer bf = ByteBuffer.allocate(2);
         bf.order(ByteOrder.LITTLE_ENDIAN);
         bf.putShort((short) temp);
         return bf.array();
        
    }
    
    /**
     * 组装包尾
     * @param dataBuffer
     * @return
     */
    public static ByteBuffer assemblyTail(ByteBuffer dataBuffer)
    {
        //获取消息尾
        byte[] dataTail =parseCRCMessageTail(dataBuffer.array());
        dataBuffer.put(dataTail);
        return dataBuffer;
    }
}
