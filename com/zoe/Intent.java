package com.zoe;
import java.util.Iterator;

import org.json.JSONObject;
/**
 * JSONObject wrapper to define the intent model of Zoe.
 * 
 * This class only adds to the JSONObject the need of having a field named 'intent', which will be the name of the intent.
 * If there is no such field in the JSON, the name will be null, declaring thus that the JSON provided should not be an intent.
 * 
 * @author danoloan10
 * @version v0.0.1
 * @since 25th October, 2017
 */
public class Intent extends JSONObject{
	public String name;
	/**
	 * Creates an <code>{@link Intent}</code> from a given JSON.
	 * 
	 * If the JSON provided does not have an 'intent' field, null is assigned to the name, as a flag indicating
	 * it is not an intent.
	 * 
	 * @param json the JSON to be used to create the intent
	 * @throws NotAnIntentException 
	 */
	public Intent(JSONObject json) throws NotAnIntentException{
		try{
			name = json.getString("intent");
		}catch(Exception ex){
			throw new NotAnIntentException();
		}
		Iterator<String> i = json.keys();
		while(i.hasNext()){
			String key = i.next();
			super.put(key, json.get(key));
		}
	}
}
