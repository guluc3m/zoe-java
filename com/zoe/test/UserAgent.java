package com.zoe.test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.zoe.Agent;
import com.zoe.Intent;
import com.zoe.Resolver;

/**
 * Test agent. It sends some intents and resolves them.
 * 
 * It also prints all the messages that it receives (it receives also the ones that it sends)
 * 
 * @author danoloan10
 *
 */

public class UserAgent{
	
	public static void main(String[] args) throws IOException, TimeoutException{
		Agent userAgent = new Agent("user"){
			@Override
			public void handler(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException{
				 //Here, the handler can be overwritten
				System.out.println("Received: "+new String(body));
			}
		};
				
		userAgent.addResolver(new UserGet());
		userAgent.addResolver(new UserAdd());
		userAgent.start();
		
		//TODO DEBUG
		//user.get intent
		JSONObject json = new JSONObject();
		json.put("intent", "user.get");
		json.put("name", "hola");
		JSONObject json2 = new JSONObject();
		json2.put("intent", "user.get");
		json2.put("name", "adios");
		//this json has the previous intent nested inside it
		JSONObject out = new JSONObject();
		out.put("e", json);
		out.put("b", json2);
		userAgent.publish(json);
		userAgent.publish(out);
	}
	
	private static JSONObject userGet(Intent intent, JSONObject full){
		JSONObject json = new JSONObject();
		String name = (String) intent.get("name");
		json.put("data", "user");
		json.put("email", name.concat("@blah.com"));
		json.put("name", name);
		return json;
	}
	
	static class UserGet implements Resolver{
		@Override
		public String name() {
			return "user.get";
		}
		@Override
		public JSONObject resolve(Intent intent, JSONObject full) {
			return userGet(intent, full);
		}		
	}
	
	static class UserAdd implements Resolver{
		@Override
		public String name() {
			return "user.get";
		}
		@Override
		public JSONObject resolve(Intent intent, JSONObject full) {
			return userGet(intent, full);
		}	
	}
}

