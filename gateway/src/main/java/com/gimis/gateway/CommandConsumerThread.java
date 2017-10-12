package com.gimis.gateway;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.gimis.util.Config;
import com.gimis.util.SourceMessage;

public class CommandConsumerThread implements Runnable {

	private BlockingQueue<SourceMessage> cmdQueue;

	private final AtomicBoolean closed = new AtomicBoolean(false);

	private KafkaConsumer<String, String> consumer;

	private Config config;
	
	public CommandConsumerThread(KafkaConsumer<String, String> consumer, 
			BlockingQueue<SourceMessage> cmdQueue,
			Config config) {
		this.consumer = consumer;
		this.cmdQueue = cmdQueue;
		this.config = config;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (!closed.get()) {
			try {
				ConsumerRecords<String, String> records = consumer.poll(100);
				for (ConsumerRecord<String, String> record : records) {
					SourceMessage message = new SourceMessage(record.value());					
					System.out.println("send command to terminal:" +message.toString() );
					if (message.getAddress() != null) {
						cmdQueue.put(message);
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				try {
					Thread.sleep(1000); 
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

	}

	// Shutdown hook which can be called from a separate thread
	public void shutdown() {
		System.out.println("close consumer thread!");
		closed.set(true);
		consumer.wakeup();
	}

}
