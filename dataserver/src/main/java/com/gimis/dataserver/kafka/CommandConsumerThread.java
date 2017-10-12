package com.gimis.dataserver.kafka;

import com.gimis.dataserver.command.RepeatSendCommandList;
import com.gimis.dataserver.terminal.GPSDevice;
import com.gimis.dataserver.terminal.GPSDeviceManager;
import com.gimis.ps.msg.DefaultHeader;
import com.gimis.ps.msg.DefaultMessage;
import com.gimis.ps.parser.EmcsSeriesMessageParse;
import com.gimis.util.Config;
import com.gimis.util.GlobalCache;
import com.gimis.util.SourceMessage;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandConsumerThread implements Runnable {
	private BlockingQueue<SourceMessage> cmdQueue;
	private KafkaConsumer<String, String> consumer;
	private GPSDeviceManager gpsDeviceManager;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	private static final Logger logger = LoggerFactory.getLogger(CommandConsumerThread.class);

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private RepeatSendCommandList sendCommandList;

	public CommandConsumerThread(KafkaConsumer<String, String> consumer, BlockingQueue<SourceMessage> cmdQueue,
			GPSDeviceManager gpsDeviceManager, RepeatSendCommandList sendCommandList) {
		this.consumer = consumer;
		this.cmdQueue = cmdQueue;
		this.gpsDeviceManager = gpsDeviceManager;
		this.sendCommandList = sendCommandList;
	}

	public void run() {
		while (!this.closed.get())
			try {
				ConsumerRecords<String, String> records = this.consumer.poll(100);
				for (ConsumerRecord<String, String> record : records) {
					if (record.value() != null) {
						SourceMessage message = new SourceMessage((String) record.value());

						if (message.getAddress() != null) {
							EmcsSeriesMessageParse messageParse = new EmcsSeriesMessageParse();
							try {
								DefaultMessage defaultmessage = messageParse.deCodeSimpleMessage(message.getData());
								if (defaultmessage.getErrorMsg().equals("")) {
									responseCmd(defaultmessage, message);
									GlobalCache.getInstance().getDefaultMessageQueue().put(defaultmessage);

									String debugGPSID = GlobalCache.getInstance().getConfig().getDebugGPSID();
									if (defaultmessage.getHeader().getGpsId().equals(debugGPSID)) {
										logger.info((String) record.value());
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
	}

	private void responseCmd(DefaultMessage defaultmessage, SourceMessage message)
			throws InterruptedException, CloneNotSupportedException {
		String gpsId = defaultmessage.getHeader().getGpsId();

		GPSDevice gpsDevice = this.gpsDeviceManager.getGPSDevice(gpsId);
		if ((gpsDevice == null) || (gpsDevice.getDevice_unid() == null)) {
			return;
		}

		if (message.getData() == null) {
			System.out.println("kafka message dt is null!");
			return;
		}
		try {
			Date date = this.sdf.parse(message.getDT());
			Date currentDT = new Date();
			long diff = currentDT.getTime() - date.getTime();

			if (diff > 120000L)
				return;
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}

		GlobalCache.getInstance().getGpsIpAddressMap().put(gpsId, message.getAddress());
		GlobalCache.getInstance().getGpsIpAddressTimeMap().put(gpsId, new Date());

		if ((defaultmessage.getHeader().getGpsCommandId() == 390)
				|| (defaultmessage.getHeader().getAttachmentId() == 390)
				|| (defaultmessage.getHeader().getGpsCommandId() == 752)
				|| (defaultmessage.getHeader().getAttachmentId() == 752)
				|| (defaultmessage.getHeader().getGpsCommandId() == 740)
				|| (defaultmessage.getHeader().getAttachmentId() == 740)
				|| (defaultmessage.getHeader().getGpsCommandId() == 1156)
				|| (defaultmessage.getHeader().getAttachmentId() == 1156)
				|| (defaultmessage.getHeader().getGpsCommandId() == 1153)
				|| (defaultmessage.getHeader().getAttachmentId() == 1153)
				|| (defaultmessage.getHeader().getAttachmentId() == -31740)) {
			GlobalCache.getInstance().getResponseSEQ().put(gpsId,
					Short.valueOf(defaultmessage.getHeader().getSequenceId()));
			this.sendCommandList.deleteData(gpsId, defaultmessage.getHeader().getSequenceId());
		}
	}
}