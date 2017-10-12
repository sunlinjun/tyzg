package com.gimis.gateway.kafka;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.util.Config;
import com.gimis.util.SerializeObjUtil;
import com.gimis.util.SourceMessage;

public class SendDataTokafka extends Thread{
	
	private BlockingQueue<SourceMessage> sendQueue; 
	
    private Producer<String, byte[]> producer;
     
    private Config config;
	
    private static final Logger logger = LoggerFactory.getLogger(SendDataTokafka.class);
 
	public SendDataTokafka(Config config,BlockingQueue<SourceMessage> queue){
		this.config=config;
		this.sendQueue=queue;
		Properties props = new Properties();
		props.put("bootstrap.servers", config.getKafkaServer());
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
	    producer = new KafkaProducer<String, byte[]>(props);
	}
 
	public void run() {		
		while (true) {
			try {
				SourceMessage message= sendQueue.take();				
				byte[] data =  SerializeObjUtil.SerializeMessage(message); 
				if(data!=null){
					sendMessageDataToKafka(config.getSourcecodeTopic(),message.getAddress(),data);	
				}
							 														            
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendMessageDataToKafka(String topicName,String address,byte[] content){
		ProducerRecord<String, byte[]> myrecord = new ProducerRecord<String, byte[]>(topicName,
				address , content);
 
		producer.send(myrecord, new Callback() {				
			public void onCompletion(RecordMetadata metadata, Exception e) {
 
				if (e != null){
					logger.error(e.toString());	
				}
			}
		});
	}
	
	public void shutdown(){
		producer.close();
	}

 

}

