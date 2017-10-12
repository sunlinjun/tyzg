
package com.gimis.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.CRC32;
 
public class MessageTools
{
    private static final SimpleDateFormat DEFAULT_DATE_SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 定义消息字节缓冲
     * @param length
     * @return
     */
    public static ByteBuffer allocate(int length)
    {
        ByteBuffer result = ByteBuffer.allocate(length);
        result.order(MessageConstants.DEFAULT_BYTE_ORDER);
        return result;
    }
    
    public static ByteBuffer bigAllocate(int length)
    {
        ByteBuffer result = ByteBuffer.allocate(length);
        result.order(MessageConstants.BIG_BYTE_ORDER);
        return result;
    }

    public static ByteBuffer wrap(byte[] data)
    {
        ByteBuffer result = ByteBuffer.wrap(data);
        result.order(MessageConstants.DEFAULT_BYTE_ORDER);
        return result;
    }

    public static ByteBuffer bigWrap(byte[] data , int offset , int length)
    {
        ByteBuffer result = ByteBuffer.wrap(data, offset, length);
        result.order(MessageConstants.BIG_BYTE_ORDER);
        return result;
    }
    
    public static ByteBuffer wrap(byte[] data , int offset , int length)
    {
        ByteBuffer result = ByteBuffer.wrap(data, offset, length);
        result.order(MessageConstants.DEFAULT_BYTE_ORDER);
        return result;
    }

    public static String getIdentityString(byte subDeviceId , byte commandId)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(subDeviceId);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(commandId);
        return sb.toString();
    }
    
    public static String getIdentityString(byte subDeviceId , byte commandId , byte contend)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(subDeviceId);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(commandId);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(contend);
        return sb.toString();
    }
    
    public static String getIdentityString(byte subDeviceId , byte commandId , byte contend , byte subcontend)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(subDeviceId);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(commandId);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(contend);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(subcontend);
        return sb.toString();
    }
    
    public static String getIdentityString(String subDeviceId , byte commandId)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(subDeviceId);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(commandId);
        return sb.toString();
    }
    
    public static String getIdentityString(String canId , int serviceId)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(canId);
        sb.append(MessageConstants.FRAME_FLAG);
        sb.append(serviceId);
        return sb.toString();
    }
    
    public static String getIdentityString(String canId , int serviceId,int flag)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(canId);
        sb.append(MessageConstants.FRAME_FLAG);
        sb.append(serviceId);
        sb.append(MessageConstants.FRAME_FLAG);
        sb.append(flag);
        return sb.toString();
    }
    public static int getIdentityInteger(byte subDeviceId , byte commandId)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(subDeviceId);
        sb.append(commandId);
        return Integer.parseInt(sb.toString());
    }

    public static Date bytesToDate(byte[] b) throws Exception
    {
        if (b == null || b.length < 6)
        {
            throw new Exception();
        }
        Calendar c = Calendar.getInstance();
        c.set(2000 + b[0], b[1] - 1, b[2], b[3], b[4], b[5]);
        return c.getTime();
    }

    public static byte[] dateToBytes(Date d) throws NullPointerException
    {
        if (d == null)
        {
            throw new NullPointerException("Null Date value.");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        byte[] result = new byte[6];
        result[0] = (byte) (c.get(Calendar.YEAR) - 2000);
        result[1] = (byte) (c.get(Calendar.MONTH) + 1);
        result[2] = (byte) c.get(Calendar.DAY_OF_MONTH);
        result[3] = (byte) c.get(Calendar.HOUR_OF_DAY);
        result[4] = (byte) c.get(Calendar.MINUTE);
        result[5] = (byte) c.get(Calendar.SECOND);
        c = null;
        return result;
    }

    public static String bytesToHexString(byte[] b , int len)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++)
        {
            String hex = Integer.toHexString(b[i] >= 0 ? b[i] : b[i] + 256).toUpperCase();
            if (hex.length() == 1)
            {
                hex = "0" + hex;
            }
            sb.append(hex);
            sb.append(" ");

        }
        return sb.toString();
    }

    public static String bytesToHexString(byte[] b)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] >= 0 ? b[i] : b[i] + 256).toUpperCase();
            if (hex.length() == 1)
            {
                hex = "0" + hex;
            }
            sb.append(hex);
            sb.append(" ");
        }
        return sb.toString();
    }
    
    public static String bytesToString(byte[] b)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] >= 0 ? b[i] : b[i] + 256).toUpperCase();
            if (hex.length() == 1)
            {
                hex = "0" + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
    public static String getIdentityString(String head,String end)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(head);
        sb.append(MessageConstants.SEPERATOR);
        sb.append(end);
        return sb.toString();
    }
    
    /**
     * 返回6位小数double
     * @param value
     * @return
     */
    public static double getSixDoubleValue(double value)
    {
        DecimalFormat  df = new DecimalFormat("#.000000");
        return Double.parseDouble(df.format(value));
    }
    
    /**
     * 返回2位小数double
     * @param value
     * @return
     */
    public static double getSecDoubleValue(double value)
    {
        DecimalFormat  df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format(value));
    }
    
    /**
     * 返回1位小数double
     * @param value
     * @return
     */
    public static double getOneDoubleValue(double value)
    {
        DecimalFormat  df = new DecimalFormat("#.0");
        return Double.parseDouble(df.format(value));
    }

    /**
     * 返回3位小数double
     * @param value
     * @return
     */
    public static double getThreeDoubleValue(double value)
    {
        DecimalFormat  df = new DecimalFormat("#.000");
        return Double.parseDouble(df.format(value));
    }
    
    /**
     * 将Byte[]转成byte[]
     * @param value
     * @return
     */
    public static byte[] getByteNumber(Byte[] value)
    {
        byte[] temp = new byte[value.length];
        
        for(int i = 0 ; i < value.length ; i++)
        {
            temp[i] = value[i];
        }
        return temp;
    }
    
    /**
     * 将byte负值改为正值
     * @param value
     * @return
     */
    public static int getByteValue(byte value)
    {
        int relayValue = value;
        if(value < 0)
        {
            relayValue = 256-Math.abs(value);
        }
        return relayValue;
    }
    
    
    public static int getUnsignedByteValue(byte value)
    {
        return value & 0xff;
    }

    /**
     * 将4位负值改为正值
     * @param value
     * @return
     */
    public static int getFourBitValue(byte value)
    {
        int relayValue = value;
        if(value < 0)
        {
            relayValue = 16-Math.abs(value);
        }
        return relayValue;
    }
    
    /**
     * 将short负值改为正值
     * @param value
     * @return
     */
    public static int getShortValue(short value) {
        // TODO Auto-generated method stub
        int relayValue = value;
        if(value < 0)
        {
            relayValue = 65536-Math.abs(value);
        }
        return relayValue;
    }
    
    /**
     * 根据IP地址，获取字节码
     * @param url
     * @return
     */
    public static byte[] getServerIp(String url)
    {
        InetAddress ia = null;
        try {
            ia = InetAddress.getByName(url);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ia.getAddress();
    }
    
    /**
     * 获取IP地址
     * @param serverIp
     * @return
     */
    public static String getHostAddress(byte[] serverIp)
    {
        InetAddress ia;
        try 
        {
            ia = InetAddress.getByAddress(serverIp);
            return ia.getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 将文件转成字节数组
     * @param filename 文件路径名称
     * @return
     */
    public static byte[] downloadGpsUpgradeFile(String filename)
    {
        byte[] result = null;
        File f = new File(filename);
        result = new byte[(int) f.length()];
        try
        {
            FileInputStream fis = new FileInputStream(f);
            fis.read(result);
            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != f)
            {
                f = null;
            }
        }
        return result;
    }
    
    /**
     * 获取定长的字节数组
     * @param filecontent
     * @param pos
     * @param length
     * @return
     */
    public static byte[] addClip(byte[] filecontent , int pos , int length)
    {
//        ByteBuffer result = ByteBuffer.allocate(256);
        ByteBuffer result = ByteBuffer.allocate(length);
        result.put(filecontent, pos, length);
        return result.array();
    }
    
    /**
     * 日期字节转换   年  月  日
     * @param b
     * @return
     * @throws InvalidDataLengthException
     */
    public static Date bytesToShortDate(byte[] b) throws Exception 
    {
        if (b == null || b.length < 3) {
           throw new Exception();
        }
        Calendar c = Calendar.getInstance();
        c.set(2000 + b[0], b[1] - 1, b[2]);
        return c.getTime();
     }
    
    /**
     * 使用默认日期格式格式化时间
     * @param date 时间
     * @return 日期字符串，格式为yyyy-MM-dd
     */
    public static String formatDate(Date date)
    {
        if (null != date)
        {
            return DEFAULT_DATE_SIMPLEDATEFORMAT.format(date);
        }
        else
        {
            return "";
        }
    }
    
    /**
     * 使用默认格式（yyyy-MM-dd）将字符串转化为日期对象
     * @param s 日期字符串，要求格式必须为yyyy-MM-dd
     * @return 日期对象
     * @throws ParseException 格式化异常
     */
    public static Date parse(String s) throws ParseException
    {
        return DEFAULT_DATE_SIMPLEDATEFORMAT.parse(s);
    }
    
    public static byte[] dateShortToBytes(Date d) {
        
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        byte[] result = new byte[3];
        result[0] = (byte) (c.get(Calendar.YEAR)-2000);
        result[1] = (byte) (c.get(Calendar.MONTH) + 1);
        result[2] = (byte) c.get(Calendar.DAY_OF_MONTH);
        c = null;
        return result;
     }
    
    public static byte[] getPwd(String pswd)
    {
        byte[] tmp = new byte[3];
        if(pswd.length() == 6){
            String[] tmpPswd= new String[3];          
            tmpPswd[0]=pswd.substring(0,2);
            if(Integer.parseInt(tmpPswd[0])>=80)
            {
                tmp[0] = new BigInteger(tmpPswd[0], 16).toByteArray()[1];
            }
            else
            {
                tmp[0] = new BigInteger(tmpPswd[0], 16).toByteArray()[0];
            }
            tmpPswd[1]=pswd.substring(2,4);
            if(Integer.parseInt(tmpPswd[1])>=80)
            {
                tmp[1]= new BigInteger(tmpPswd[1], 16).toByteArray()[1];
            }
            else
            {
                tmp[1]= new BigInteger(tmpPswd[1], 16).toByteArray()[0];
            }
            tmpPswd[2]=pswd.substring(4);
            if(Integer.parseInt(tmpPswd[2])>=80)
            {
                tmp[2]= new BigInteger(tmpPswd[2], 16).toByteArray()[1];
            }
            else
            {
                tmp[2]= new BigInteger(tmpPswd[2], 16).toByteArray()[0];
            }
        }
        return tmp;

    }  
    /**
     * Integer类型转为byte数组
     * @param intValue
     * @return
     */
    public static byte[] int2Byte(int intValue)
    {
        byte[] b=new byte[4];
        for(int i=0;i<4;i++)
        {
         b[i]=(byte)(intValue>>8*(3-i) & 0xFF);
        }
        return b;
     }
    /**
     * byte[]>>long
     */
    
    /** 
     * 字节数组到long的转换. 
     */  
    public static long byteToLong(byte[] b) 
    {  
        long s = 0;  
        long s0 = b[0] & 0xff;// 最低位  
        long s1 = b[1] & 0xff;  
        long s2 = b[2] & 0xff;  
        long s3 = b[3] & 0xff;  
        long s4 = b[4] & 0xff;// 最低位  
        long s5 = b[5] & 0xff;  
        long s6 = b[6] & 0xff;  
        long s7 = b[7] & 0xff;  
  
        // s0不变  
        s1 <<= 8;  
        s2 <<= 16;  
        s3 <<= 24;  
        s4 <<= 8 * 4;  
        s5 <<= 8 * 5;  
        s6 <<= 8 * 6;  
        s7 <<= 8 * 7;  
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;  
        return s;  
    }
    
    /** 
     * long类型转成byte数组 
     */  
    public static byte[] longToByte(long number) 
    {  
        long temp = number;  
        byte[] b = new byte[8];  
        for (int i = 0; i < b.length; i++) 
        {          
            b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位 temp = temp  
                                                        // >> 8;// 向右移8位  
        }  
        return b;  
    }  
    
    /**
     * 浮点到字节转换
     * @param d
     * @return
     */
    public static byte[] doubleToByte(double d)
    {
        byte[] b=new byte[8];
        long l=Double.doubleToLongBits(d);
        for(int i=0;i < 8;i++)
        {
            b[i]=new Long(l).byteValue();
            l=l>>8;
        }
            return b;
    }
    /**
     * 字节到浮点转换
     * @param b
     * @return
     */
    public static double byteToDouble(byte[] b)
    {
        long l;
        l=b[0];
        l&=0xff;
        l|=((long)b[1]<<8);
        l&=0xffff;
        l|=((long)b[2]<<16);
        l&=0xffffff;
        l|=((long)b[3]<<24);
        l&=0xffffffffl;
        l|=((long)b[4]<<32);
        l&=0xffffffffffl;
        l|=((long)b[5]<<40);
        l&=0xffffffffffffl;
        l|=((long)b[6]<<48);
        l|=((long)b[7]<<56);
        return Double.longBitsToDouble(l);
    }
    
    /**
     * 将int负值改为正值
     * @param value
     * @return
     */
    public static Long getIntValue(int value)
    {

        long relayValue = value;
        if (value < 0)
        {
            relayValue = 4294967296L - Math.abs(value);
        }
        return relayValue;
    }
    
    /*
     *指定时间精确到毫秒 
     * @param b
     * @return
     * @throws InvalidDataLengthException
     */
    public static Timestamp bytesToDates(byte[] b)
    {

        Calendar c = Calendar.getInstance();
        c.set(2000 + b[0], b[1] - 1, b[2], b[3], b[4], b[5]);
        c.set(Calendar.MILLISECOND, b[6] * 10);
        Date d = new Date(c.getTimeInMillis());

        return new Timestamp(d.getTime());
    }
    
    /*
     *指定时间精确到毫秒 
     * @param b
     * @return
     * @throws InvalidDataLengthException
     */
    public static Date bytesToDates(byte[] b , int millisecond)
    {

        Calendar c = Calendar.getInstance();
        c.set(2000 + b[0], b[1] - 1, b[2], b[3], b[4], b[5]);
        c.set(Calendar.MILLISECOND, millisecond);

        return c.getTime();
    }
    
    /*
     * 解码转义
     * @param data
     * @return
     */
    public static byte[] deCodeFormat(byte[] data)
    {
        ByteBuffer tmp = ByteBuffer.allocate(data.length);
        for (int i = 0; i < data.length;)
        {
            if (data[i] == MessageConstants.MESSAGE_TRANS_FLAG && (i + 1) < data.length && data[i + 1] == 0x02)
            {
                tmp.put(MessageConstants.MESSAGE_FLAG);
                i += 2;
            }
            else if (data[i] == MessageConstants.MESSAGE_TRANS_FLAG && (i + 1) < data.length && data[i + 1] == 0x01)
            {
                tmp.put(MessageConstants.MESSAGE_TRANS_FLAG);
                i += 2;
            }
            else
            {
                tmp.put(data[i]);
                i++;
            }

        }
        tmp.flip();
        byte[] result = new byte[tmp.limit()];
        tmp.get(result);
        return result;
    }

    /*
     * 编码转义
     * @param data
     * @return
     */
    public static byte[] enCodeFormat(byte[] data)
    {
        ByteBuffer tmp = ByteBuffer.allocate(data.length * 2);
        for (byte b : data)
        {
            if (b == MessageConstants.MESSAGE_FLAG)
            {
                tmp.put(MessageConstants.MESSAGE_TRANS_FLAG);
                tmp.put((byte) 0x02);
            }
            else if (b == MessageConstants.MESSAGE_TRANS_FLAG)
            {
                tmp.put(MessageConstants.MESSAGE_TRANS_FLAG);
                tmp.put((byte) 0x01);
            }
            else
            {
                tmp.put(b);
            }
        }
        tmp.flip();
        byte[] result = new byte[tmp.limit()];
        tmp.get(result);
        return result;
    }

    /**
     * 检验效验码是否正确
     * @param data
     * @return
     */
    public static boolean checkValidationCode(byte[] data)
    {
        //获取包尾
        byte dataTail = enVerbCode(data);
        if (data[data.length - 1] == dataTail)
        {
            return true;
        }
        else
        {
            return false;
        }
    }



    /**
     * 生成CRC包尾
     * @param data
     * @return
     */
    public static byte enVerbCode(byte[] data)
    {

        byte y = data[0];
        for (int i = 1; i < data.length - 1; i++)
        {
            y ^= data[i];
        }
        return y;
    }
    
    
    /**
     * 字符串转字节
     * @param data
     * @return
     */
    public static byte[] hexStringToByte(String data)
    {
        String hex = data;
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++)
        {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }
    /**
     * String[]转byte[]
     * @param c
     * @return
     */
    public static Byte[] stringArr2ByteArr(String[] data)
    {
       Byte[] b = new Byte[data.length];
       for(int i=0;i<data.length;i++)
       {
           b[i] = Byte.parseByte(data[i]);
       }
        return b;
        
    }
    
    public static byte[] stringArr2byteArr(String[] data)
    {
        byte[] b = new byte[data.length];
        for(int i=0;i<data.length;i++)
        {
            b[i] = Byte.parseByte(data[i]);
        }
        return b; 
    }
    
    private static byte toByte(char c)
    {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
    
    public static byte[] hexStringToByteUpgrade(String hex, boolean flag)
    {
        byte[] result = null;
        if(flag)
        {
           String[] ver = hex.split("\\.");
           result = new byte[ver.length];
           for(int i=0; i<ver.length; i++)
           {
               result[i] = Byte.parseByte(ver[i]);
           }
        }
        else
        {
            int len = hex.length();
            result = new byte[len];
            char[] achar = hex.toCharArray();
            for (int i = 0; i < len; i++)
            {
                byte b = toByte(achar[i]);
                result[i] = b;
            }
        }
        return result;
    }
    
    /**
     * 根据子地址，延生地址，地址长度获取实际地址
     * @param subAddr
     * @param extensionAddr
     * @param len
     * @return
     */
    public static String getRealAddr(String subAddr , String extensionAddr , int len)
    {
        // TODO Auto-generated method stub
        String id = subAddr.replaceAll("0x", "");
        
        if(id.length() < len)
        {
            int length = id.length();
            for(int i = 0 ; i < (len-length); i ++)
            {
                id = extensionAddr.replaceAll("0x", "") + id;
            }
        }
        return id;
    }
   
    /**
     * 小于10的数字转成两个字符的字符串
     * @param arg
     * @return
     */
    public static String formatNum(int arg)
    {
        String s = "";
        if (arg < 10)
        {
            s = "0" + arg;
        }
        else
        {
            s = "" + arg;
        }
        return s;
    }
    
    /**
     * 时间比较（精确到日）  date1大于等于date2返回true
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareDate(Date date1,Date date2)
    {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

        String str1 = sf.format(date1);
        String str2 = sf.format(date2);
        if (str1.compareTo(str2) < 0)
        {
            return false;
        }
        return true;
    }

    /**
     * 将字符串转换成byte数组
     * @param hex
     * @param num
     * @return
     */
    public static byte[] hexStringAppendToByte(String hex , int num)
    {
        if(null != hex)
        {
            return bigAllocate(num).put(hex.getBytes()).array();
        }
        return bigAllocate(num).array();
    }
    
    public static byte[] hexStringToByteArr(String hex){
        if(hex.indexOf("0x")!=-1){
            hex = hex.substring(hex.indexOf("x")+1);
        }
        int intVal = Integer.parseInt(hex, 16);
        byte[] byteArr = new byte[4];
        byteArr[0] = (byte)intVal;
        byteArr[1] = (byte)(intVal >> 8);
        byteArr[2] = (byte)(intVal >> 16);
        byteArr[3] = (byte)(intVal >> 24);
        return byteArr;
    }
    
    public static long enCrc32(String fileName)
    {
        CRC32 crc32 = new CRC32();
        crc32.update(downloadGpsUpgradeFile(fileName));
        return crc32.getValue();
    }
    
    public static byte[] hexString2Bytes(String hex)
    {
        if(null != hex && hex.length() > 0 && hex.length()%2 == 0)
        {
            byte[] res = new byte[hex.length()/2];
            for(int i=0,j=hex.length();i<j;i+=2)
            {
                res[i/2] = (byte)(short)Short.valueOf(hex.substring(i, i+2), 16);
            }
            return res;
        }
        return null;
    }
    
    
    /*
     * 数据包标识位是否正确
     * @param bytes
     * @return
     */
    public static boolean hasIdentifyTag(byte[] data)
    {
        return identifyFiveSeriesTag(data);
    }
    
    private static boolean identifyFiveSeriesTag(byte[] data)
    {
        return (data[0] == MessageConstants.MESSAGE_FLAG && MessageConstants.MESSAGE_FLAG == data[data.length - 1]);
    }
}
