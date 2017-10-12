package com.gimis.util;

 
import java.sql.DriverManager;
import java.util.Properties;

import com.mysql.jdbc.Connection;

public class DBConnection {
	
	public static Connection getConnection() throws Exception{
 
		Properties cfgPro = new Properties();
		try {
			cfgPro = PropertiesTools.loadProperties("cfg.properties", System.getProperty("user.dir"));
			
			String url = cfgPro.getProperty("datasource.url");		
			String userName = cfgPro.getProperty("datasource.username");	
			String password = cfgPro.getProperty("datasource.password");	
			
			String dataClassName = cfgPro.getProperty("datasource.driver-class-name");
 
			Class.forName(dataClassName);
			return (Connection) DriverManager.getConnection(
					url
					,userName
					,password);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}

}
