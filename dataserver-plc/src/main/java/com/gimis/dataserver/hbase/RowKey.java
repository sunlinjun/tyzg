package com.gimis.dataserver.hbase;

import org.apache.hadoop.hbase.util.Bytes;

public class RowKey {
	
	public static byte[] makeRowKey(String unid, long timestamp)
	{ 
		byte[] b2 = Bytes.toBytes( timestamp );
		return Bytes.add( (unid + "-").getBytes(), b2 );
	}
 
}
