
package com.gimis.util;

import java.nio.ByteOrder;

public class MessageConstants
{
    public static final String SEPERATOR = ":";
    
    public static final String FRAME_FLAG = "_";

    //默认为小端模式
    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    public static final ByteOrder BIG_BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    // 最小消息字节数
    public static final byte MINMESSAGESIZE = 6;
    
    /**
     * gps长度+头尾包体长度
     */
    public static final int GPSLENGTH = 67;
    
    /**
     * mcu E系列原协议长度
     */
    public static final int MCULENGTH = 122;
    
    /**
     * 无线超长待机GPS子设备号
     */
    public static final byte GPSSUBVALUE = 0x12;

    //位比较
    public static final byte B0 = 0x01;

    public static final byte B1 = 0x02;

    public static final byte B2 = 0x04;

    public static final byte B3 = 0x08;

    public static final byte B4 = 0x10;

    public static final byte B5 = 0x20;

    public static final byte B6 = 0x40;

    public static final short B7 =  0x80;
    
    public static final short B8 =  0x0100;
    
    public static final short B9 =  0x0200;
    
    public static final short B10 =  0x0400;
    
    public static final short B11 =  0x0800;
    public static final short B12 =  0x1000;
    public static final short B13 =  0x2000;
    public static final short B14 =  0x4000;
    public static final int B15 =  0x8000;
    
    public static final byte B5432 = (byte) 0x1E;
    
    public static final byte B54321 = (byte)0x1F;
    
    public static final byte B321 = (byte) 0x07;

    public static final byte B21 = (byte) 0x03;

    public static final byte B43 = (byte) 0x0C;

    public static final byte B65 = (byte) 0x30;
    
    public static final byte B67 = (byte) 0x60;

    public static final byte B87 = (byte) 0xC0;

    public static final byte B54 = (byte) 0x18;

    public static final byte B4321 = (byte) 0x0F;

    public static final byte B8765 = (byte) 0xF0;
    
    public static final byte B654321 = (byte)0x3F;
    
    public static final byte B7654321 = (byte)0x7F;
    
    public static final byte B1110987654321 = (byte) 0x07FF;
    
    public static final short B12_1 = 0x0FFF;

    public static final byte mcuOneLock = 0x01;

    public static final byte mcuOneLockMacCode = (byte) 0xFE;

    public static final byte mcuTwoLock = 0x02;

    public static final byte mcuTwoLockMacCode = (byte) 0xFD;

    public static final byte mcuOneTwoLock = 0x03;

    public static final byte mcuOneTwoLockMacCode = (byte) 0xFC;

    public static final byte mcuUnLock = 0x00;
    
    /**
     * gps  消息体错误
     */
    public static final int GPS_BODY_ERROR = 1;
    
    /**
     * MCU  消息体错误
     */
    public static final int MCU_BODY_ERROR = 2;
    
    /**
     * 消息尾解析错误
     */
    public static final int END_BODY_ERROR = 3;

    /**
     * 协议解析类型
     */
    public static final int PARSE_TYPE_DEFAULT = 0;

    public static final int PARSE_TYPE_EXTEND = 1;
    
    public static final int PARSE_TYPE_EXTEND_2 = 2;
    
    public static final int PARSE_TYPE_EXTEND_3 = 3;
    
    public static final int PARSE_TYPE_EXTEND_4 = 4;
    
    public static final int PARSE_TYPE_EXTEND_5 = 5;
    
    
    
    public static final byte MESSAGE_FLAG = (byte)0x7e;
    
    public static final byte MESSAGE_TRANS_FLAG = (byte)0x7d;
}
