package com.zoe;

import org.json.JSONObject;
/**
 * Some static utilities to manipulate intents, JSON files and byte streams.
 * 
 * @author danoloan10
 * @version v0.1
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
	public static String[] sortAlphabetically(String[] arr){
		//Insertion sort
		String[] ordered = new String[arr.length];
		for(int i = 1; i < arr.length; i++){
			int j = i-1;
			while(j >= 0 && arr[i].compareTo(arr[j]) < 0)
				j--;
			String aux = arr[i];
			for(int k = j+1; k < i; k++){
				aux = arr[k+1];
				arr[k+1] = arr[k];
			}
			arr[j+1] = aux;
		}
		for(int i = 0; i < arr.length; i++){
			ordered[i] = arr[i];
		}
		return ordered;
	}
}
