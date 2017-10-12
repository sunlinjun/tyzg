package com.gimis.dataserver.command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import com.gimis.util.C3P0DBUtil;
import com.gimis.util.GlobalCache;

/*
 * 保存命令应答到COMMAND_LOG表中
 */
public class SaveResultToDBThread extends Thread{
	
	private BlockingQueue<ResultCommand> queue;
	
	private SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	
	public SaveResultToDBThread(){		
		queue=GlobalCache.getInstance().getTerminalResponseQueue();		
	}
	
	
	//更新应答状态到数据库
	private void responseToDB(ResultCommand resultCommand){		
		Connection conn = null; 
		ResultSet rs = null;
		Statement st = null;
		try
		{
			conn = C3P0DBUtil.getConnection();
			
			st = conn.createStatement();
									
			String CurrentDT=sdf.format(new Date());
			
			//修改COMMAND_LOG表
			String sqlStr=" update command_log set result_status="+Integer.toString(resultCommand.getResultStatus())
			+" ,command_time='"+CurrentDT+"' "
			+" where id="+Integer.toString(resultCommand.getCommandLogID());
			st.execute(sqlStr);	 
			
			
			//删除COMMAND_TEMP表
			sqlStr=" delete from command_temp where command_log_id="+Integer.toString(resultCommand.getCommandLogID());
			st.execute(sqlStr); 
 
		}
		catch(Exception e)
		{
			System.out.println("更新应答状态到数据库异常!");
			e.printStackTrace();
		}
		finally
		{
			C3P0DBUtil.attemptClose(rs);
			C3P0DBUtil.attemptClose(st);
			C3P0DBUtil.attemptClose(conn);
		}
		
	}
	
	public void run() {			 	
		while(true){			
			try {
				ResultCommand resultCommand=queue.take();				
				responseToDB(resultCommand); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	 

}
