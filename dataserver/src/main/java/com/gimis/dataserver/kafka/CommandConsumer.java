package com.gimis.dataserver.kafka;

import com.gimis.dataserver.command.RepeatSendCommandList;
import com.gimis.dataserver.terminal.GPSDeviceManager;
import com.gimis.util.Config;
import com.gimis.util.GlobalCache;
import com.gimis.util.SourceMessage;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandConsumer {
	private BlockingQueue<SourceMessage> cmdQueue;
	private KafkaConsumer<String, String> consumer;
	private Properties props = new Properties();
	private Config config;
	private static final Logger logger = LoggerFactory.getLogger(CommandConsumer.class);
	private ExecutorService executor;
	private GPSDeviceManager gpsDeviceManager;
	private RepeatSendCommandList sendCommandList;

	public CommandConsumer(BlockingQueue<SourceMessage> cmdQueue, GPSDeviceManager gpsDeviceManager,
			RepeatSendCommandList sendCommandList) {
		this.config = GlobalCache.getInstance().getConfig();
		this.cmdQueue = cmdQueue;
		this.gpsDeviceManager = gpsDeviceManager;
		this.sendCommandList = sendCommandList;

		this.props.put("bootstrap.servers", this.config.getKafkaServer());
		this.props.put("group.id", this.config.getKafkaGroupID());
		this.props.put("enable.auto.commit", "true");
		this.props.put("auto.commit.interval.ms", "1000");
		this.props.put("session.timeout.ms", "30000");
		this.props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		this.props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		this.consumer = new KafkaConsumer<String, String>(this.props);
		this.consumer.subscribe(Arrays.asList(new String[] { this.config.getSourcecodeTopic() }));
	}

	public void run() {
		try {
			this.executor = Executors.newFixedThreadPool(1);
			this.executor.submit(new CommandConsumerThread(this.consumer, this.cmdQueue, this.gpsDeviceManager,
					this.sendCommandList));
		} catch (Exception e) {
			logger.error("接收kafka消息异常!" + e.toString());
		}
	}
}
