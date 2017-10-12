package com.gimis.dataserver.hbase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

 

public class HBaseUtil {
	
	private static byte[] COLUMNFAMILY = "column1".getBytes();

	private Configuration config;

	private Connection writeConnection=null;
	//private final int threads;
	//private final ExecutorService internalPool;
	

	public HBaseUtil() {
		config = HBaseConfiguration.create();
		
		String fileDir=System.getProperty("user.dir");
		String fileName="hbase-site.xml";				
		File propFile = new File(fileDir + "/" + fileName);
		if (propFile.exists()) {
			config.addResource(new Path(fileDir, fileName)); 
			System.out.println("读取"+fileName+"外部配置文件成功!");
		} else {			
			config.addResource(new Path("/", fileName)); 
			System.out.println("读取"+fileName+"内部配置文件成功!");
		}
 
 
		//config.addResource(new Path("/", "hbase-site.xml"));
		//config.addResource(new Path("/", "core-site.xml"));
		try {
			writeConnection = ConnectionFactory.createConnection(config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	}
	
	private void ResetConnection(){
		try {
			writeConnection.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			writeConnection = ConnectionFactory.createConnection(config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
 
	public void createTable(String tableName) {
		Connection connection=null;
		try {
			connection = ConnectionFactory.createConnection(config);
			HBaseAdmin hBaseAdmin = (HBaseAdmin) connection.getAdmin();
			if (hBaseAdmin.tableExists(tableName) == false) {
 
				HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
				tableDescriptor.addFamily(new HColumnDescriptor(COLUMNFAMILY));
				hBaseAdmin.createTable(tableDescriptor);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			try {
				if(connection!=null){
					connection.close();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 保存数据
	public void write(String tableName, String unid, long timestamp, HashMap<String, String> record) {

		try {
			if(writeConnection!=null && writeConnection.isClosed()==false){
				Table table = writeConnection.getTable(TableName.valueOf(tableName));
				Put p = new Put(RowKey.makeRowKey(unid, timestamp));
				for (String column : record.keySet()) {
					String value = record.get(column);
					p.addColumn(COLUMNFAMILY, Bytes.toBytes(column), Bytes.toBytes(value));
				}
				table.put(p);
			}else{
				ResetConnection();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			ResetConnection();
			
		}
	}

	public void writeList(String tableName, ArrayList<HBaseData> list) {

		try {
			if(writeConnection!=null && writeConnection.isClosed()==false){
				Table table = writeConnection.getTable(TableName.valueOf(tableName));				
				ArrayList<Put> putList=new ArrayList<Put>();
 
				for(HBaseData data:list){ 		
 
					Put p = new Put(data.getRowKey());					
					HashMap<String, String> record = data.getRecord();
					for (String column :record.keySet()) {																	
						String value = record.get(column);
						p.addColumn(COLUMNFAMILY, Bytes.toBytes(column), Bytes.toBytes(value));						
					}
 
					putList.add(p);
				}			
				table.put(putList);
			}else{				
				ResetConnection();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			ResetConnection();
			
		}
	}
	
	//根据key ,时间段进行查询
	public LinkedList<HashMap<String, String>> read(String tableName, String key, ArrayList<String> codes, long dateFrom,
			long dateTo) {

		LinkedList<HashMap<String, String>> recordList = new LinkedList<HashMap<String, String>>();
		Connection connection = null;
		Table table = null;
		try {
			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(tableName));

			Scan scan = new Scan();
			
			for (String code : codes)
				scan.addColumn(COLUMNFAMILY, Bytes.toBytes(code));
			
			scan.setStartRow(RowKey.makeRowKey(key, dateFrom));
			scan.setStopRow(RowKey.makeRowKey(key, dateTo));
 
			scan.setCaching(100000);
			ResultScanner rs = table.getScanner(scan);
						

			try {
				for (Result r = rs.next(); r != null; r = rs.next()) {

					HashMap<String, String> record = new HashMap<String, String>();
					boolean bFind=false;
					for (String code : codes) {
						Cell cell = r.getColumnLatestCell(COLUMNFAMILY, Bytes.toBytes(code));
						if (cell != null) {
							String value = new String(CellUtil.cloneValue(cell));
							record.put(code, value);
							
							if(!(code.equals("server_time") || code.equals("gps_time")) ){
								bFind=true;
							}
		 
							// record.setTimestamp( cell.getTimestamp() );
						}
					}
					
					if(bFind){
						recordList.add(record);	
					}
					
				}

			} finally {
				rs.close(); // always close the ResultScanner!
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (table != null)
					table.close();
			} catch (IOException e) {
			}

			try {
				if (connection != null)
					connection.close();
			} catch (IOException e) {
			}
		}

		return recordList;
	}

	
	//模糊查找 然后过滤key
 
	public LinkedList<HashMap<String, String>> read(String tableName, String key, ArrayList<String> keys, ArrayList<String> codes, long dateFrom,
			long dateTo) {

 
		LinkedList<HashMap<String, String>> recordList = new LinkedList<HashMap<String, String>>();
		
		Connection connection = null;
		Table table = null;
		try {
			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(tableName));
			Scan scan = new Scan();

			if (null != codes) {
				for (String code : codes)
					scan.addColumn(COLUMNFAMILY, Bytes.toBytes(code));
			}
 
			scan.setStartRow(startRowKey(key,dateFrom));
			scan.setStopRow(endRowKey(key,dateTo));

 
			// 过滤查询的ids 		
			if ((null != keys) && (0 < keys.size())) {

				List<Filter> flters = new ArrayList<Filter>();
				for (String id : keys) {
					String regex =id;
					Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(regex));
					flters.add(filter);
				}
				FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE, flters);
				scan.setFilter(filterList);
			}
 
	 

			scan.setCaching(100000);

			ResultScanner rs = table.getScanner(scan);
			try {
				for (Result r = rs.next(); r != null; r = rs.next()) {

					HashMap<String, String> record = new HashMap<String, String>();
					
					
					byte[] strByte=new byte[39];
					System.arraycopy(r.getRow(), 0, strByte, 0, 39);
					
					byte[] times=new byte[8];
					System.arraycopy(r.getRow(), 39, times, 0, 8);
					
					long aa=Bytes.toLong(times);
 
					record.put("row", Bytes.toString(strByte)+Long.toString(aa));
					for (String code : codes) {
						
											
						Cell cell = r.getColumnLatestCell(COLUMNFAMILY, Bytes.toBytes(code));
						if (cell != null) {
							String value = new String(CellUtil.cloneValue(cell));
							record.put(code, value);
						}
					}
					recordList.add(record);
				}
			} finally {
				rs.close(); // always close the ResultScanner!
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (table != null)
					table.close();
			} catch (IOException e) {
			}

			try {
				if (connection != null)
					connection.close();
			} catch (IOException e) {
			}
		}

		return recordList;
	}

	// 根据时间模糊查询 
	public Collection<HashMap<String, String>> readlike2(String tableName, ArrayList<String> codes,
			long dateFrom,
			long dateTo) {

		List<HashMap<String, String>> recordList = new LinkedList<HashMap<String, String>>();
		Connection connection = null;
		Table table = null;
 
		
		byte[] rawKeyFrom= startRowKey(dateFrom);
		byte[] rawKeyTo= endRowKey(dateTo);
 
		try {
			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(tableName));

			Scan scan = new Scan();
			for (String code : codes)
				scan.addColumn(COLUMNFAMILY, Bytes.toBytes(code));
			scan.setStopRow(rawKeyFrom);
			scan.setStartRow(rawKeyTo);
			ResultScanner rs = table.getScanner(scan);
			try {
				for (Result r = rs.next(); r != null; r = rs.next()) {
					HashMap<String, String> record = new HashMap<String, String>();
					for (String code : codes) {
						
		 

						Cell cell = r.getColumnLatestCell(COLUMNFAMILY, Bytes.toBytes(code));
						if (cell != null) {
							record.put(code, new String(CellUtil.cloneValue(cell)));
							// record.setTimestamp( cell.getTimestamp() );//
							// FIXME
						}
					}
					recordList.add(record);
				}

			} finally {
				rs.close(); // always close the ResultScanner!
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (table != null)
					table.close();
			} catch (IOException e) {
			}

			try {
				if (connection != null)
					connection.close();
			} catch (IOException e) {
			}
		}

		return recordList;
	}
	
 
 
	private byte[] startRowKey(long result) {
		byte[] b2 = Bytes.toBytes( result );
		return Bytes.add( ("00000000000000000000000000000000" + "-").getBytes(), b2 );
	}

	private byte[] endRowKey(long result) {
		byte[] b2 = Bytes.toBytes( result );
		return Bytes.add( ("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + "-").getBytes(), b2 );
	}
 
	
	private byte[] startRowKey(String unid,long result) {
		byte[] b2 = Bytes.toBytes( result );
        System.out.println("start: "+unid + "-00000-"+result);		
		return Bytes.add( (unid + "-00001-").getBytes(), b2 );
	}

	private byte[] endRowKey(String unid,long result) {
		byte[] b2 = Bytes.toBytes( result );
		System.out.println("end: "+unid + "-ZZZZZ-"+result);
		return Bytes.add( (unid + "-00001-").getBytes(), b2 );
	}
	
	

}
