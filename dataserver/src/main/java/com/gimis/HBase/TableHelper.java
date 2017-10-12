/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2016 Ttron Kidman. All rights reserved.
 */
package com.gimis.HBase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

/**
 * @Ttron 2015年12月3日
 */
public class TableHelper
{
	public boolean exists(String tableName, Configuration conf)
	{
		try (Connection connection = ConnectionFactory.createConnection( conf ))
		{
			try (Table table = connection.getTable( TableName.valueOf( tableName ) ))
			{
				// use table as needed, the table returned is lightweight
				return true;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
