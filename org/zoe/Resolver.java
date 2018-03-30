package org.zoe;

import org.json.*;
/**
 * This class defines the resolution of incoming intents
 * @author danoloan10
 *
 */
public abstract class Resolver {
	private String name;
	
	public Resolver(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
	/**
	 * This methods takes the intent of type {@code name} and should return the resolution. The resolved intent must NOT include the "data" field.
	 * @param intent Intent to be resolved
	 * @param full Full received message
	 * @return Resolved intent
	 * @throws IntentErrorException
	 */
	public abstract JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException;
	/**
	 * This method takes the error occurred during intent resolving and should return the error object to be included along the error message
	 * @param ex Exception occurred
	 * @return Error object to be appended
	 */
	public abstract JSONObject getErrorObject(IntentErrorException ex);
}
