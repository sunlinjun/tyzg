package com.gimis.gateway;

import com.gimis.gateway.network.SessionManager;
import com.gimis.util.Config;
import com.gimis.util.MessageTail;
import com.gimis.util.MessageTools;
import com.gimis.util.SourceMessage;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDataThread extends Thread {
	private BlockingQueue<SourceMessage> sendQueue;
	private Producer<String, String> producer;
	private Config config;
	private static final Logger logger = LoggerFactory.getLogger(ProcessDataThread.class);
	private SessionManager sessionManager;

	public ProcessDataThread(Config config, BlockingQueue<SourceMessage> queue, SessionManager sessionManager) {
		this.config = config;
		this.sendQueue = queue;
		this.sessionManager = sessionManager;

		Properties props = new Properties();
		props.put("bootstrap.servers", config.getKafkaServer());
		props.put("acks", "all");
		props.put("retries", Integer.valueOf(0));
		props.put("batch.size", Integer.valueOf(16384));
		props.put("linger.ms", Integer.valueOf(1));
		props.put("buffer.memory", Integer.valueOf(33554432));
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		this.producer = new KafkaProducer<String, String>(props);
	}

	private byte[] getPureData(byte[] data) {
		byte[] result = new byte[data.length - 2];
		if (result.length != 0) {
			System.arraycopy(data, 1, result, 0, data.length - 2);
		}
		return result;
	}

	public void run() {
		while (true)
			try {
				SourceMessage message = (SourceMessage) this.sendQueue.take();

				byte[] data = message.getData();

				if (MessageTools.hasIdentifyTag(data)) {
					byte[] pureData = getPureData(data);

					byte[] transData = MessageTools.deCodeFormat(pureData);

					if (!MessageTail.parseMessageTail(transData)) {
						System.out.println("校验码错误");
					} else {
						message.setData(transData);
						String strMessage = message.toString();

						byte[] gpsBuffer = new byte[6];
						System.arraycopy(transData, 10, gpsBuffer, 0, 6);
						String gpsId = new String(gpsBuffer).trim();

						if ((this.config.getIsDebug() == 1) && (this.config.getDebugID().indexOf(gpsId) > -1)) {
							logger.info(strMessage);
						}

						if (transData[10] == 66) {
							sendMessageDataToKafka(this.config.getSourcecodeTopic2(), message.getAddress(), strMessage);
						} else {
							sendMessageDataToKafka(this.config.getSourcecodeTopic(), message.getAddress(), strMessage);
						}

						response(gpsId, message.getAddress(), transData);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

	}

	private void response(String gpsId, String address, byte[] transData) {
		byte[] data;
		if ((transData[0] == -113) && (transData[1] == 3))
			data = new byte[28];
		else {
			data = new byte[21];
		}
		System.arraycopy(transData, 0, data, 0, 18);
		data[2] = 0;
		data[3] = 0;
		data[6] = 0;
		data[7] = 0;

		if ((transData[0] == -127) && (transData[1] == 1)) {
			data[0] = 1;
			data[1] = 1;
		} else if ((transData[0] == -125) && (transData[1] == 4)) {
			data[0] = 3;
			data[1] = 4;
		} else if ((transData[0] == -21) && (transData[1] == 2)) {
			data[0] = 107;
			data[1] = 2;
		} else if ((transData[0] == -112) && (transData[1] == 3)) {
			data[0] = 16;
			data[1] = 3;
		} else if ((transData[0] == -113) && (transData[1] == 3)) {
			data[0] = 15;
			data[1] = 3;
			data[2] = 7;
			data[3] = 0;

			char chr = gpsId.charAt(0);
			if (chr == 'B') {
				byte[] dateBuffer = MessageTools.dateToBytesEx(new Date());
				System.arraycopy(dateBuffer, 0, data, 19, 7);
			} else {
				byte[] dateBuffer = MessageTools.dateToBytes1(new Date());
				System.arraycopy(dateBuffer, 0, data, 19, 7);
			}
		} else {
			return;
		}

		byte[] endBody = MessageTail.parseCRCMessageTailShort(data, 0, data.length - 2);
		data[(data.length - 2)] = endBody[0];
		data[(data.length - 1)] = endBody[1];

		byte[] hbcTrans = MessageTools.enCodeFormat(data);

		ByteBuffer tranBuf = ByteBuffer.allocate(2 + hbcTrans.length);
		tranBuf.put((byte) 126);
		tranBuf.put(hbcTrans);
		tranBuf.put((byte) 126);

		this.sessionManager.writeSession(address, tranBuf.array());
	}

	private void sendMessageDataToKafka(String topicName, String address, String content) {
		ProducerRecord<String, String> myrecord = new ProducerRecord<String, String>(topicName, address, content);

		this.config.getIsDebug();

		this.producer.send(myrecord, new Callback() {
			public void onCompletion(RecordMetadata metadata, Exception e) {
				if (e != null) {
					ProcessDataThread.logger.error(e.toString());
				}
				ProcessDataThread.this.config.getIsDebug();
			}
		});
	}

	public void shutdown() {
		this.producer.close();
	}
}