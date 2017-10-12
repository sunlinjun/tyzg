package com.gimis.dataserver.can.DynamicParser;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CANParser {

	// 同步锁
	private ReentrantLock myLock;
	
	// CAN定义
	private HashMap<String, List<CANData>> canHash;

	// 格式化float型
	private DecimalFormat decimalFormat;

	private static int[] moveValue = { 0, 8, 16, 24, 32, 40, 48, 56 };

	public CANParser() {
		canHash = new HashMap<String, List<CANData>>();
		myLock = new ReentrantLock();
		decimalFormat = new DecimalFormat("0.00");
	}

	// 解析 json字符串
	private List<CANData> readJsonData(String jsonData) {
		Type listType = new TypeToken<List<CANData>>() {
		}.getType();
		Gson gson = new Gson();
		List<CANData> canDatas = gson.fromJson(jsonData, listType);

		return canDatas;
	}

	// 在数组中的位置
	private static int getArrayPos(int value) {
		int pos = 0;
		for (int i = 0; i < 8; i++) {
			pos = i;
			if (i < 7) {
				if (value >= moveValue[i] && value < moveValue[i + 1]) {
					break;
				}
			}
		}
		return pos;
	}

	private static void initCANContentFromJson(CANContent data, int value1, int value2) {
		int pos1 = getArrayPos(value1);
		int pos2 = getArrayPos(value2);

		value1 = value1 % 8;
		value2 = value2 % 8;

		// 第一种情况 在一个字节中
		if (pos1 == pos2) {

			// 一个完整的字节
			if (value1 == 0 && value2 == 7) {
				data.setValueType(1);
				data.setStartPos(pos1);
				data.setEndpos(pos2);
			} else { // 不完整的字节
				data.getHead().setPos(pos1);
				data.getHead().setStart(7 - value2);
				data.getHead().setEnd(8 - value1);
				data.setValueType(2);
			}

		} else {

			if (value1 == 0 && value2 == 7) {
				data.setValueType(1);
				data.setStartPos(pos1);
				data.setEndpos(pos2);
			} else {
				data.setValueType(3);
				data.getHead().setPos(pos1);
				data.getTail().setPos(pos2);
				if (value1 > 0) {
					data.getHead().setStart(0);
					data.getHead().setEnd(7 - value1 + 1);
				} else {
					data.getHead().setStart(0);
					data.getHead().setEnd(8);
				}

				if (value2 > 0) {
					data.getTail().setStart(7 - value2);
					data.getTail().setEnd(8);
				} else {
					data.getTail().setStart(0);
					data.getTail().setEnd(8);
				}
			}
		}
	}

	private boolean checkNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
	private String checkLowByteOrHighByte(String strByte){
		String[] buf = strByte.split("-");			
		
		//必须为数字型
		if(checkNumber(buf[0])==false || checkNumber(buf[buf.length-1])==false){
			return "is not number";
		}
		
		//必须0-63之间  第一个必须最小		 
		if (Integer.parseInt(buf[0]) > Integer.parseInt(buf[buf.length-1])
				|| Integer.parseInt(buf[0])<0 || Integer.parseInt(buf[0])>63
				|| Integer.parseInt(buf[buf.length-1])<0 || Integer.parseInt(buf[buf.length-1])>63					
				) {
			return "out of ranger";
		}
		
		return "";
	}

	// 有效性检测
	private String validCANData(List<CANData> canDatas) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < canDatas.size(); i++) {
			CANData data = canDatas.get(i);
			data.set_valueType(-1);
			// 无效的
			if (data.getLowBytes().equals("")) {
				sb.append(data.getPropertyName() + ":lowBytes is Empty ");
				continue;
			}
			//低位定义有效性检测
			String lowByteResult=checkLowByteOrHighByte(data.getLowBytes());
			if(lowByteResult.equals("")==false){
				sb.append(data.getPropertyName()).append(":lowBytes ").append(lowByteResult);
				continue;
			}
			//高位定义有效性检测
			if (data.getHighBytes().equals("") == false) {			
				String highByteResult=checkLowByteOrHighByte(data.getHighBytes());
				if(highByteResult.equals("")==false){
					sb.append(data.getPropertyName()).append(":highBytes ").append(highByteResult);
					continue;
				}				 								
			}
			
			if(data.getPropertyName()==null){
				sb.append(data.getPropertyName() + ":propertyName is null ");
				continue;	
			}
			
			if(data.getPropertyName().equals("")){
				sb.append(data.getPropertyName() + ":propertyName is Empty ");
				continue;	
			}			

			data.set_valueType(0);
		}
		return sb.toString();
	}

	private void processCANDef(List<CANData> canDatas) {
		for (int i = 0; i < canDatas.size(); i++) {
			CANData data = canDatas.get(i);

			// 无效的
			if (data.getLowBytes().equals("")) {
				data.set_valueType(-1);
				continue;
			}

			if (data.getLowBytes().contains("-")) {
				data.set_valueType(1);

				String[] pos = data.getLowBytes().split("-");

				int s1 = Integer.parseInt(pos[0]);
				int s2 = Integer.parseInt(pos[1]);

				initCANContentFromJson(data.getLow_content(), s1, s2);

				if (data.getHighBytes().equals("") == false) {
					if (data.getHighBytes().contains("-")) {
						String[] pos2 = data.getHighBytes().split("-");
						int s3 = Integer.parseInt(pos2[0]);
						int s4 = Integer.parseInt(pos2[1]);
						initCANContentFromJson(data.getHigh_content(), s3, s4);
					} else {
						int value = Integer.parseInt(data.getHighBytes());
						int pos1 = getArrayPos(value);
						data.getHigh_content().getHead().setPos(pos1);
						value = value % 8;
						data.getHigh_content().getHead().setStart(7 - value);
						data.getHigh_content().getHead().setEnd(7 - value + 1);
						data.getHigh_content().setValueType(2);
					}
				}

			} else { // bit型
				data.set_valueType(0);
				int value = Integer.parseInt(data.getLowBytes());
				int pos = getArrayPos(value);
				data.getLow_content().getHead().setPos(pos);
				value = value % 8;
				data.getLow_content().getHead().setStart(7 - value);
				data.getLow_content().getHead().setEnd(7 - value + 1);
			}
		}

	}

	private String parserContent(int byteOrder, CANContent data, String[] buf) {

		String lowStr = "";
		if (data.getValueType() == 0) {
			return lowStr;
		}

		StringBuilder sb = new StringBuilder();
		// CANContent 0 无效 1数组 2在一个字节中 3组合
		if (data.getValueType() == 1) {
			int startPos = data.getStartPos();
			int endPos = data.getEndpos();

			// 小端
			if (byteOrder == 0) {
				sb.setLength(0);
				for (int index = endPos; index >= startPos; index--) {
					sb.append(buf[index]);
				}
			} else { // 大端
				sb.setLength(0);
				for (int index = startPos; index <= endPos; index++) {
					sb.append(buf[index]);
				}
			}

			lowStr = sb.toString();
		} else {

			if (data.getValueType() == 2) { // 在一个字节中
				int startPos = data.getHead().getStart();
				int endPos = data.getHead().getEnd();

				int pos = data.getHead().getPos();
				lowStr = buf[pos].substring(startPos, endPos);
			} else { // 组合

				int startPos1 = data.getHead().getStart();
				int endPos1 = data.getHead().getEnd();
				int pos1 = data.getHead().getPos();

				int startPos2 = data.getTail().getStart();
				int endPos2 = data.getTail().getEnd();
				int pos2 = data.getTail().getPos();

				String head = buf[pos1].substring(startPos1, endPos1);
				String tail = buf[pos2].substring(startPos2, endPos2);

				sb.setLength(0);
				if (pos2 > pos1) {

					// 小端
					if (byteOrder == 0) {
						sb.setLength(0);
						for (int index = pos2 - 1; index >= pos1 + 1; index--) {
							sb.append(buf[index]);
						}
					} else {
						sb.setLength(0);
						for (int index = pos1 + 1; index <= pos2 - 1; index++) {
							sb.append(buf[index]);
						}
					}

				}
				String content = sb.toString();

				sb.setLength(0);

				// 小端
				if (byteOrder == 0) {
					sb.append(tail);
					sb.append(content);
					sb.append(head);
				} else {
					sb.append(head);
					sb.append(content);
					sb.append(tail);
				}
				lowStr = sb.toString();
			}
		}

		return lowStr;
	}

	// key=CANID value=CANLIST
	@SuppressWarnings("rawtypes")
	public String init(HashMap<String, String> hashMap) {
		StringBuilder sb = new StringBuilder();
		myLock.lock();
		try {
			canHash.clear();
			Iterator iter = hashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String CANID = (String) entry.getKey();
				String CANLIST = (String) entry.getValue();
				List<CANData> canDatas = readJsonData(CANLIST);

				String error = validCANData(canDatas);
				if (error.equals("")) {
					
				
					processCANDef(canDatas);
					canHash.put(CANID, canDatas);
				} else {
					sb.append(CANID).append(":");
					sb.append(error).append("\r\n");
				}
			}
		} finally {
			myLock.unlock();
		}
		return sb.toString();
	}

	/**
	 * 将CAN数据通过json格式的描述，解析成hashmap 返回值 HashMap
	 * 
	 * @author SunLinJun
	 * @version 1.0, 2016-7-18
	 * @see
	 * @since V1.0
	 */
	public HashMap<String, String> parser(String Key, byte[] buffer) {

		String[] buf = new String[8];
		for (int i = 0; i < 8; i++) {
			buf[i] = toBinaryString(buffer[i], 8);
		}

		HashMap<String, String> hashMap = new HashMap<String, String>();

		myLock.lock();
		try {
			List<CANData> list = canHash.get(Key);

			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					CANData data = list.get(i);

					if (data.get_valueType() == -1) {
						continue;
					}

					// bit
					if (data.get_valueType() == 0) {
						CANDataPos dataPos = data.getLow_content().getHead();
						String bitString = buf[dataPos.getPos()];
						bitString = bitString.substring(dataPos.getStart(), dataPos.getEnd());

						int d = Integer.parseInt(bitString, 2); // 2进制
						hashMap.put(data.getFieldName(), Integer.toString(d));
					} else {
						String str = parserContent(data.getByteOrder(), data.getLow_content(), buf);

						// 如果高字节是bit类型

						String highStr = parserContent(data.getByteOrder(), data.getHigh_content(), buf);
						str = highStr + str;

						// 当为64位，如下时，Long.parserLong转换会出错
						// str="1111111111111111111111111111111111111111111111111111111111111111";
						if (str.length() >= 64) {
							str = str.substring(str.length() - 64, str.length());
						}
						long l = Long.parseLong(str, 2);
						if(data.getSignBit()==1){							
							if(str.charAt(0)=='1'){
								str=str.substring(1);
								l = Long.parseLong(str, 2);
								l=0-l;
							}							 
						}
						
						
						l = l + data.getOffset();
						if (data.getResolution() != null && data.getResolution() != "") {

							double f = Double.parseDouble(data.getResolution());

							if (f != 1) {
								f = l * f;
								hashMap.put(data.getFieldName(), decimalFormat.format(f));
							} else {
								hashMap.put(data.getFieldName(), Long.toString(l));
							}
						} else {
							hashMap.put(data.getFieldName(), Long.toString(l));
						}
					}

				}

			}

		} finally {
			myLock.unlock();
		}
		return hashMap;

	}

	private static String toBinaryString(int value, int count) {

		String result = Integer.toBinaryString(value);
		if (result.length() < count) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < count - result.length(); i++) {
				sb.append("0");
			}
			sb.append(result);

			return sb.toString();
		} else {
			if (result.length() > count) {
				result = result.substring(result.length() - 8, result.length());
			}
			return result;
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public String hashMaptoString(HashMap<String, String> hashMap) {
		StringBuilder sb = new StringBuilder();

		Iterator<?> iter = hashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			sb.append(key + ":" + val).append("\r\n");
		}
		return sb.toString();

	}

 
	/*
	public static void main(String[] args) {

		CANParser canParser = new CANParser();
		HashMap<String, String> hashMap = new HashMap<String, String>();
		
		
		String CANLIST = "[{\"bits\":8,\"byteOrder\":0,\"fieldName\":\"2\",\"highBytes\":\"\",\"id\":1,\"lowBytes\":\"16-23\",\"offset\":0,\"propertyName\":\"Y109\",\"resolution\":1,\"unit\":\"\",\"valueType\":0},"
				+ "{\"bits\":8,\"byteOrder\":0,\"fieldName\":\"电机温度\",\"highBytes\":\"\",\"id\":2,\"lowBytes\":\"32-39\",\"offset\":-40,\"propertyName\":\"a2.1\",\"resolution\":1.0,\"unit\":\"\",\"valueType\":0},"
				+ "{\"bits\":8,\"byteOrder\":0,\"fieldName\":\"电机开\",\"highBytes\":\"\",\"id\":3,\"lowBytes\":\"41\",\"offset\":-40,\"propertyName\":\"a3\",\"resolution\":0,\"unit\":\"\",\"valueType\":0},"
				+ "{\"bits\":8,\"byteOrder\":0,\"fieldName\":\"电流\",\"highBytes\":\"\",\"id\":4,\"lowBytes\":\"40-49\",\"offset\":0,\"propertyName\":\"a4.2\",\"resolution\":1,\"unit\":\"\",\"valueType\":0},"
				+ "{\"bits\":8,\"byteOrder\":0,\"fieldName\":\"电流1\",\"highBytes\":\"\",\"id\":5,\"lowBytes\":\"61-63\",\"offset\":0,\"propertyName\":\"a5\",\"resolution\":1,\"unit\":\"\",\"valueType\":0}]";
		
		hashMap.put("03010101", CANLIST);
		String result = canParser.init(hashMap);
		// 检测输入是否有错误
		System.out.print(result);

		byte[] buffer = { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
				(byte) 0x08 };
		HashMap<String, String> resultMap = canParser.parser("03010101", buffer);
		// 输出测试结果
		System.out.println(canParser.hashMaptoString(resultMap));

 
	}
 */
 

}
