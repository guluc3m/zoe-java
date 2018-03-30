package org.zoe.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;
import org.zoe.Agent;
import org.zoe.Intent;
import org.zoe.IntentErrorException;
import org.zoe.Resolver;

public class DemoAgent {
	public static void main(String args[]){
		Agent agent;
		agent = new Agent("a"){
			public void onReception(ConsumerRecord<String, byte[]> record){
				System.out.println("Recieved: "+new String(record.value()));
			}
			@Override
			public void onSending(JSONObject json){
				System.out.println("Sent: "+json);
			}
		};
		agent.addResolver(new Resolver("a"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				return new JSONObject();
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				return new JSONObject();
			}
			
		});
		agent.start();
	}
}
