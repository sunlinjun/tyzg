package com.gimis.ps.parser;

import java.nio.ByteBuffer;
import com.gimis.ps.msg.LockBody;

public class LockParse {
	private LockBody lockBody;

	public LockParse() {
		lockBody = new LockBody();
	}

	public void deCode(String gpsId, ByteBuffer buf) {

		lockBody.setLongitude(buf.getInt());
		lockBody.setLatitude(buf.getInt());
		lockBody.setLockType(buf.get());
		lockBody.setLeftTime(buf.getShort());
	}

	public LockBody getLockBody() {
		// TODO Auto-generated method stub
		return this.lockBody;
	}
}
