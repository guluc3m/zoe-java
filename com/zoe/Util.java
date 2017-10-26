package com.zoe;

import org.json.JSONObject;
/**
 * Some static utilities to manipulate intents, JSON files and byte streams.
 * 
 * @author danoloan10
 * @version v0.0.1
 * @since 25th October, 2017
 */
public class Util {
	public static JSONObject bytesToJSON(byte[] body){
		return new JSONObject(new String(body));
	}
	public static Intent jsonToIntent(JSONObject json) throws NotAnIntentException{
		return new Intent(json);
	}
	public static Intent bytesToIntent(byte[] body) throws NotAnIntentException{
		return jsonToIntent(bytesToJSON(body));
	}
	public static byte[]  intentToBytes(Intent intent){
		return intent.toString().getBytes();
	}
}
