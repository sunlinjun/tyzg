package BaiduMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
 
/*
 * 封装百度地图的解析类
 */
public class BaiduMap {
	
	private static final Logger logger = LoggerFactory.getLogger(BaiduMap.class);

	private static String BaseURL = "http://api.map.baidu.com/geocoder/v2/";

	// 根据经纬度，获取中文地理位置 省 市 区
	public static AddressComponent getChinePosition(String baiduKey, String lat, String lng) {

		StringBuilder sb = new StringBuilder();
		sb.append(BaseURL);
		sb.append("?ak=").append(baiduKey);
		 //sb.append("?ak=84a882c3f905a3722a525766e8a3eebd");
		sb.append("&callback=renderReverse");
		sb.append("&coordtype=wgs84ll");
		sb.append("&location=").append(lat).append(",").append(lng);
		sb.append("&output=json");
		sb.append("&output=json");

		try {
			URL url = new URL(sb.toString());
			HttpURLConnection urlConnectin = (HttpURLConnection) url.openConnection();
			urlConnectin.setRequestProperty("Content-type", "text/html");
			urlConnectin.setRequestProperty("Connection", "close");
			urlConnectin.setUseCaches(true);
			urlConnectin.setConnectTimeout(5000);
			urlConnectin.setReadTimeout(5000);
			urlConnectin.setDoOutput(true);
			urlConnectin.setDoInput(true);
						
			BufferedReader in;

			in = new BufferedReader(new InputStreamReader(urlConnectin.getInputStream(), "utf-8"));

			try {

				String temp = "";
				sb.setLength(0);
				while ((temp = in.readLine()) != null) {
					sb.append(temp);
				}
				temp = sb.toString();

				int pos = temp.indexOf("{");
				temp = temp.substring(pos);
				temp = temp.substring(0, temp.length() - 1);

				// json反序列化
				Gson gson = new Gson();
				GeocoderSearchResponse response = gson.fromJson(temp, GeocoderSearchResponse.class);
				
				//ObjectMapper mapper = new ObjectMapper();
				//GeocoderSearchResponse response = mapper.readValue(temp, GeocoderSearchResponse.class);
				return response.getResult().getAddressComponent();

			} catch (Exception e) {
				logger.error(" 1: "+e.toString());
				System.out.print(" 1: "+e.toString());
			} finally {
				in.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			logger.error(" 2: "+e1.toString());
			System.out.print(" 2: "+e1.toString()); 	
		}

		return null;

	}
	
	private static double rad(double d) {
		return d * 3.1415926535898 / 180.0;
	}
	
	// 计算两点间的距离，传入 经度1 纬度1 经度2 纬度2 单位为度 返回值:米
	public static double calPosDistance(double JD1, double WD1, double JD2,
			double WD2) {
		double radLat1 = rad(WD1);
		double radLat2 = rad(WD2);
		double a = radLat1 - radLat2;
		double b = rad(JD1) - rad(JD2);
		double temp = Math.sin(a / 2) * Math.sin(a / 2) + Math.cos(radLat1)
				* Math.cos(radLat2) * Math.sin(b / 2) * Math.sin(b / 2);
		temp = (2 * (Math.sqrt(temp))) * 6378137; // 6378137 地球半径
		return temp;
	}

 /*
	public static void main(String[] args) throws IOException {

		// ChineseProcessor testMap=new ChineseProcessor(null);

		AddressComponent address = BaiduMap.getChinePosition("84a882c3f905a3722a525766e8a3eebd",
				"32.294", "109.788328");

		System.out.println(address.getCity()+" "+address.getProvince()+"　");
	}
 */
}
