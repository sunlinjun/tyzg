package com.gimis.util;

import java.text.DecimalFormat;

public class MessageTools {

	public static double getSixDoubleValue(double value) {
		DecimalFormat df = new DecimalFormat("#.000000");
		return Double.parseDouble(df.format(value));
	}

	/**
	 * 将short负值改为正值
	 * 
	 * @param value
	 * @return
	 */
	public static int getShortValue(short value) {
		// TODO Auto-generated method stub
		int relayValue = value;
		if (value < 0) {
			relayValue = 65536 - Math.abs(value);
		}
		return relayValue;
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

}
