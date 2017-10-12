package com.gimis.dataserver.kafka;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.util.GlobalCache;
import com.gimis.util.PropertiesTools;

public class SendDataToAnalysisThread extends Thread {

	private Producer<String, String> producer;

	private String topicName;

	private static final Logger logger = LoggerFactory.getLogger(SendDataToAnalysisThread.class);

	public SendDataToAnalysisThread() {

		Properties kafkaPro = new Properties();
		try {
			kafkaPro = PropertiesTools.loadProperties("kafka.properties", System.getProperty("user.dir"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		topicName = kafkaPro.getProperty("plcAnalysisTopic");

		String kafkaServer = kafkaPro.getProperty("bootstrap.servers");

		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaServer);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producer = new KafkaProducer<String, String>(props);
	}

	public void run() {

		while (true) {

			try {

				String strMessage = GlobalCache.getInstance().getAnalysisQueue().take();

				String gpsId = strMessage.substring(0, 6);

				try {

					ProducerRecord<String, String> myrecord = new ProducerRecord<String, String>(topicName, gpsId,
							strMessage);

					producer.send(myrecord, new Callback() {

						public void onCompletion(RecordMetadata metadata, Exception e) {
							if (e != null) {
								logger.error(e.toString());
							}
						}
					});

				} catch (Exception e) {
					logger.error("发送分析 数据至Kafka异常!" + e.toString());

				}
			} catch (Exception e) {
			}

		}
	}

}
