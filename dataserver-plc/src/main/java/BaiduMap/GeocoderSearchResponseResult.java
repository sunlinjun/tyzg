package BaiduMap;

import java.util.ArrayList;

public class GeocoderSearchResponseResult {
	
	private Location location;
	
	private String formatted_address;
	
	private String business;
	
	private String sematic_description;
	
	private int cityCode;
	
	private ArrayList<Object> pois;
	
	private ArrayList<Object> poiRegions;
 
	private AddressComponent addressComponent;
    
 

	public AddressComponent getAddressComponent() {
		return addressComponent;
	}

	public void setAddressComponent(AddressComponent addressComponent) {
		this.addressComponent = addressComponent;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getFormatted_address() {
		return formatted_address;
	}

	public void setFormatted_address(String formatted_address) {
		this.formatted_address = formatted_address;
	}

	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public String getSematic_description() {
		return sematic_description;
	}

	public void setSematic_description(String sematic_description) {
		this.sematic_description = sematic_description;
	}

	public int getCityCode() {
		return cityCode;
	}

	public void setCityCode(int cityCode) {
		this.cityCode = cityCode;
	}

	public ArrayList<Object> getPois() {
		return pois;
	}

	public void setPois(ArrayList<Object> pois) {
		this.pois = pois;
	}

	public ArrayList<Object> getPoiRegions() {
		return poiRegions;
	}

	public void setPoiRegions(ArrayList<Object> poiRegions) {
		this.poiRegions = poiRegions;
	}

}
