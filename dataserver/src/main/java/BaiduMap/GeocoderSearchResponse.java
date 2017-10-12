package BaiduMap;

public class GeocoderSearchResponse {
	private int status;
	
	private GeocoderSearchResponseResult result;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public GeocoderSearchResponseResult getResult() {
		return result;
	}

	public void setResult(GeocoderSearchResponseResult result) {
		this.result = result;
	}
	

}
