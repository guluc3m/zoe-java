package org.zoe;

import org.json.*;

public abstract class Resolver {
	private String name;
	private String dataType;
	
	public Resolver(String name, String dataType){
		this.name = name;
		this.dataType = dataType;
	}
	public String getName(){
		return name;
	}
	public String getDataType(){
		return dataType;
	}
	public abstract JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException;
	public abstract JSONObject getErrorObject(IntentErrorException ex);
}
