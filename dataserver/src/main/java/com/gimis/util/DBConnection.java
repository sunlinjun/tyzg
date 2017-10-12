package com.gimis.util;

import java.sql.DriverManager;
import com.mysql.jdbc.Connection;

public class DBConnection {
 
	public static Connection getConnection() throws Exception{
		Config config=GlobalCache.getInstance().getConfig();
		Class.forName(config.getDataSourceDriveName()).newInstance();
		return (Connection) DriverManager.getConnection(
				 config.getDataSourceURL()
				,config.getDataSourceUserName()
				,config.getDataSourcePassword());
	}	

}
