package com.gimis.dataserver.kafka;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.gimis.util.GlobalCache;
import com.gimis.util.PropertiesTools;
import com.gimis.util.SerializeObjUtil;
import com.gimis.util.SourceMessage;

public class PlcDataConsumer {
	
	private KafkaConsumer<String, byte[]> consumer;
	
	//private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

 
	public PlcDataConsumer(){
		
		Properties kafkaPro = new Properties();
		try {
			kafkaPro = PropertiesTools.loadProperties("kafka.properties", System.getProperty("user.dir"));
		} catch (IOException e) {
			e.printStackTrace();
		}				
		String topicName = kafkaPro.getProperty("plcDataTopic");		 
		consumer = new KafkaConsumer<String, byte[]>(kafkaPro);
		consumer.subscribe(Arrays.asList(topicName)); 		
	}
	
 
	public void run(){
		while (true) {			
			try{
				ConsumerRecords<String, byte[]> records = consumer.poll(100);
				for (ConsumerRecord<String, byte[]> record : records) {	 
					SourceMessage message= (SourceMessage)SerializeObjUtil.DeserializeMessage(record.value()); 
					if(message!=null){						 
						//System.out.println(sdf.format(new Date())+" recv kafka data "+message.getDT());
						GlobalCache.getInstance().getSourceMessageQueue().add(message); 			
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
