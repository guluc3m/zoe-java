package com.zoe.test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.zoe.Agent;

public class TestSender {

	public static void main(String[] args) {
		Agent userAgent;
		long t = System.currentTimeMillis();
		while(true){
			if(System.currentTimeMillis() - t > 5000){
				try {
					userAgent = new Agent("sender"){
						@Override
						public void handler(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException{
							
						}
					};
					//TODO DEBUG
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
		
					try {
						userAgent.start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (TimeoutException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				t = System.currentTimeMillis();
			}
		}
	}

}
