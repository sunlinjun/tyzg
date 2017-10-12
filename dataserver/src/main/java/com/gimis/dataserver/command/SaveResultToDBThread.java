package com.gimis.dataserver.command;

import com.gimis.util.C3P0DBUtil;
import com.gimis.util.GlobalCache;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class SaveResultToDBThread extends Thread {
	private BlockingQueue<ResultCommand> queue;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public SaveResultToDBThread() {
		this.queue = GlobalCache.getInstance().getTerminalResponseQueue();
	}

	private void responseToDB(ResultCommand resultCommand) {
		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		try {
			conn = C3P0DBUtil.getConnection();

			st = conn.createStatement();

			String CurrentDT = this.sdf.format(new Date());

			String sqlStr = " update command_log set result_status=" + Integer.toString(resultCommand.getResultStatus())
					+ " ,command_time='" + CurrentDT + "' " + " where id="
					+ Integer.toString(resultCommand.getCommandLogID());
			st.execute(sqlStr);

			sqlStr = " delete from command_temp where command_log_id="
					+ Integer.toString(resultCommand.getCommandLogID());
			st.execute(sqlStr);
		} catch (Exception e) {
			System.out.println("更新应答状态到数据库异常!");
			e.printStackTrace();
		} finally {
			C3P0DBUtil.attemptClose(rs);
			C3P0DBUtil.attemptClose(st);
			C3P0DBUtil.attemptClose(conn);
		}
	}

	public void run() {
		while (true)
			try {
				ResultCommand resultCommand = (ResultCommand) this.queue.take();
				responseToDB(resultCommand);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}