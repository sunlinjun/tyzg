package com.gimis.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
 
@Configuration
@PropertySource(value = "classpath:/application.properties")
@EnableCaching
public class ConfigCache extends CachingConfigurerSupport{
 
	@Value("${appKey}")
	private String appKey;

	@Bean 
	public String getAppKey(){
		return appKey;
	}
}
