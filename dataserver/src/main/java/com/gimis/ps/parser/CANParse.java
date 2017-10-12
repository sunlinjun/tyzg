package com.gimis.ps.parser;

import java.nio.ByteBuffer;
import java.util.Date;

import com.gimis.ps.msg.CANBody;
import com.gimis.ps.msg.CANFrame;
import com.gimis.util.MessageTools;

/**
 * CAN数据解析
 * @author gimis
 *
 */
public class CANParse {

	private CANBody canBody;

	public CANParse() {
		canBody = new CANBody();
	}

	public CANBody getCanBody() {
		return canBody;
	}

	public void deCode(String gpsId, ByteBuffer buf) {
		canBody.setGps_id(gpsId);

		canBody.setUploadType(buf.get());

		canBody.setSeqID(buf.getInt());
		canBody.setLongitude(buf.getInt());
		canBody.setLatitude(buf.getInt());
		canBody.setSpeed(buf.getShort());
		canBody.setHeight(buf.getShort());
		canBody.setDirection(buf.getShort());

		//年月日时分秒毫秒
		byte[] temp = new byte[7];
		buf.get(temp, 0, temp.length);
		try {
			canBody.setGpsTime(MessageTools.bytesToDate(temp));
			canBody.getGpsTime().setTime(canBody.getGpsTime().getTime() + 28800000);
		} catch (Exception ex) {
			// log.error("转换日期异常：", ex);
			canBody.setGpsTime(new Date());
		}
 
		// CAN数据格式
		buf.get();

		// CAN数据包个数
		int count = buf.get();
		for (int i = 0; i < count; i++) {

			// 时间间隔
			buf.getShort();

			CANFrame canFrame = new CANFrame();
			String canID = MessageTools.bytesToCANID(buf.get(), buf.get(), buf.get(), buf.get());
			canFrame.setCANID(canID);

			// 通道
			buf.get();

			// 数据内容
			byte[] data = new byte[8];
			for (int j = 0; j < 8; j++) {
				data[j] = buf.get();
			}
			canFrame.setData(data);

			canBody.getCanlist().add(canFrame);
		}
	}
}
