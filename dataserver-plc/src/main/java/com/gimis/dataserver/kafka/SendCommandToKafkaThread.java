package com.gimis.dataserver.kafka;

import com.gimis.util.GlobalCache;
import com.gimis.util.PropertiesTools;
import com.gimis.util.SourceMessage;
import java.io.IOException;
import java.util.Properties;	
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendCommandToKafkaThread extends Thread {
	private Producer<String, String> producer;
	private String topicName;
	private static final Logger logger = LoggerFactory.getLogger(SendCommandToKafkaThread.class);

	public SendCommandToKafkaThread() {
		Properties kafkaPro = new Properties();
		try {
			kafkaPro = PropertiesTools.loadProperties("kafka.properties", System.getProperty("user.dir"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.topicName = kafkaPro.getProperty("plcCmdTopic");

		String kafkaServer = kafkaPro.getProperty("bootstrap.servers");

		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaServer);
		props.put("acks", "all");
		props.put("retries", Integer.valueOf(0));
		props.put("batch.size", Integer.valueOf(16384));
		props.put("linger.ms", Integer.valueOf(1));
		props.put("buffer.memory", Integer.valueOf(33554432));
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		this.producer = new KafkaProducer<String, String>(props);
	}

	public void run() {
		while (true)
			try {
				SourceMessage message = (SourceMessage) GlobalCache.getInstance().getCmdQueue().take();

				String strMessage = message.toString();
				try {
					ProducerRecord<String, String> myrecord = new ProducerRecord<String, String>(this.topicName, message.getAddress(), strMessage);

					this.producer.send(myrecord, new Callback() {
						public void onCompletion(RecordMetadata metadata, Exception e) {
							if (e != null) {
								SendCommandToKafkaThread.logger.error(e.toString());
							}
						}
					});
				} catch (Exception e) {
					logger.error("发送命令回码至kafka异常!" + e.toString());
				}
			} catch (Exception localException1) {
			}
	}

	public void shutdown() {
		this.producer.close();
	}
}