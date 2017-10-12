package com.gimis.dataserver.kafka;

import com.gimis.ps.msg.CANBody;
import com.gimis.ps.msg.DefaultMessage;
import com.gimis.ps.msg.FaultBody;
import com.gimis.ps.msg.GPSBody;
import com.gimis.ps.msg.ReportBody;
import com.gimis.util.Config;
import com.gimis.util.GlobalCache;
import com.gimis.util.SerializeObjUtil;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendDefaultMessageToKafkaThread extends Thread {
	private BlockingQueue<DefaultMessage> sendQueue;
	private Producer<String, byte[]> producer;
	private Config config;
	private static final Logger logger = LoggerFactory.getLogger(SendDefaultMessageToKafkaThread.class);

	public SendDefaultMessageToKafkaThread(BlockingQueue<DefaultMessage> queue) {
		this.config = GlobalCache.getInstance().getConfig();
		this.sendQueue = queue;
		Properties props = new Properties();
		props.put("bootstrap.servers", this.config.getKafkaServer());
		props.put("acks", "all");
		props.put("retries", Integer.valueOf(0));
		props.put("batch.size", Integer.valueOf(16384));
		props.put("linger.ms", Integer.valueOf(1));
		props.put("buffer.memory", Integer.valueOf(33554432));
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		this.producer = new KafkaProducer<String, byte[]>(props);
	}

	public void run() {
		while (true)
			try {
				DefaultMessage message = (DefaultMessage) this.sendQueue.take();
				try {
					boolean bSend = false;
					String gpsID = "";

					if (message.getHeader().getGpsCommandId() == 385) {
						GPSBody gpsBody = message.getGpsBody();

						if (gpsBody != null) {
							gpsID = gpsBody.getGps_id();
							bSend = true;
						}

					}

					if (message.getHeader().getAttachmentId() == 744) {
						CANBody canBody = message.getCanBody();

						if (canBody != null) {
							gpsID = canBody.getGps_id();
							bSend = true;
						}

					}

					if (message.getHeader().getAttachmentId() == 1155) {
						FaultBody faultBody = message.getFaultBody();

						if (faultBody != null) {
							gpsID = faultBody.getGps_id();
							bSend = true;
						}

					}

					if ((message.getHeader().getAttachmentId() == 747)
							|| (message.getHeader().getGpsCommandId() == 747)) {
						ReportBody reportBody = message.getReportBody();

						if (reportBody != null) {
							gpsID = reportBody.getGps_id();
							bSend = true;
						}
					}

					if (!bSend)
						continue;
					byte[] data = SerializeObjUtil.SerializeMessage(message);
					if (data == null) {
						continue;
					}
					String topicName = this.config.getAnalysisTopic();
					ProducerRecord<String, byte[]> myrecord = new ProducerRecord<String, byte[]>(topicName, gpsID, data);

					this.producer.send(myrecord, new Callback() {
						public void onCompletion(RecordMetadata metadata, Exception e) {
							if (e != null) {
								SendDefaultMessageToKafkaThread.logger.error(e.toString());
							}
						}

					});
				} catch (Exception e) {
					logger.error("发送DefaultMessage至kafka异常!" + e.toString());
				}
			} catch (Exception localException1) {
			}
	}

	public void shutdown() {
		this.producer.close();
	}
}