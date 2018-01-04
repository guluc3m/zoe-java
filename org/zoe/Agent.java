package org.zoe;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class defines an Agent, the core of the Zoe library.
 * TODO
 * @author danoloan10
 * @version v0.1
 * @since 25th October, 2017
 */

public class Agent extends Thread{
	@SuppressWarnings("unused")
	private String group = "default";
	private Map<String, Resolver> resolvers = new HashMap<String, Resolver>();

	private KafkaClient kafkaClient;

	public Agent(String group){
		kafkaClient = new KafkaClient(group);
		this.group = group;
	}
	
	public Agent(String name, Collection<Resolver> resolvers){
		this(name);
		for(Resolver r : resolvers){
			this.addResolver(r);
		}
	}
	
	public void run(){
		while(true){
			ConsumerRecords<String, byte[]> records = kafkaClient.consume();
			for(ConsumerRecord<String, byte[]> record : records){
				JSONObject incoming = Util.bytesToJSON(record.value());
				onReception(record);
				try{
					this.send(intentResolver(incoming));
					kafkaClient.commit(record);
				}catch(NoResolverException | NotAnIntentException | ErrorMessageException ex){
					kafkaClient.ignore(record);
				}
			}
		}
	}
	
	public void onReception(ConsumerRecord<String, byte[]> record){}
	public void onSending(JSONObject json){}
	/**
	 * This method resolves an intent and returns the resolution. The resolution is constructed by adding the data type of the resolver to the resolution. If an <code>{@link IntentErrorException}</code> has been thrown,
	 * it will return the error object plus the error message,  and add that message to the main message.
	 * @param intent Intent to resolve
	 * @param main Main message where the intent is
	 * @return Resolved intent
	 * @throws NoResolverException
	 */
	private JSONObject resolve(Intent intent, JSONObject main) throws NoResolverException{
		for(String r : resolvers.keySet()){
			if(r.equals(intent.name)){
				JSONObject resolved;
				try{
					resolved = resolvers.get(r).resolve(intent, main);
					resolved.put("data", resolvers.get(r).getDataType());
				}catch(IntentErrorException ex){
					ex.printStackTrace();
					JSONObject error = resolvers.get(r).getErrorObject(ex);
					resolved = new JSONObject();
					resolved.put("error", error);
					main.put("error", resolvers.get(r).getErrorObject(ex));
				}
				return resolved;
			}
		}	
		throw new NoResolverException();
	}
	
	/**
	 * This method will search for intents and resolve the appropriate one in the JSON json
	 * @param json The message to resolve
	 * @return Resolved message
	 * @throws NotAnIntentException
	 * @throws NoResolverException
	 * @throws ErrorMessageException 
	 */
	public final JSONObject intentResolver(JSONObject json) throws NoResolverException, NotAnIntentException, ErrorMessageException{ return intentResolver(json, json); }
	
	/**
	 * This method searches for an intent recursively, from left to right, in depth, until it reaches a valid one; and returns the main json with the intent resolved
	 * @param json The intent to resolve
	 * @param main The main message where the intent is
	 * @return The main message resolved
	 * @throws NotAnIntentException
	 * @throws NoResolverException
	 * @throws ErrorMessageException 
	 * @throws JSONException 
	 */
	private JSONObject intentResolver(JSONObject json, JSONObject main) throws NoResolverException, NotAnIntentException, ErrorMessageException{
		String[] keys = new String[1];
		keys = json.keySet().toArray(keys);
		keys = Util.sortAlphabetically(keys);
		
		for(int i = 0; i < keys.length; i++){
			String key = keys[i]; //Current key
			if(key == null)
				throw new NotAnIntentException();
			if(key.equals("error"))
				throw new ErrorMessageException(json.get(key).toString());
			//Ignore quotations
			if(key.substring(key.length()-1).equalsIgnoreCase("!"))
				continue;
			if(json.get(key) instanceof JSONObject){
				try{
					//If we find a JSONObject, we try to find there some Intents. If there are, they will be resolved
					return intentResolver(json.getJSONObject(key), main);					
				}catch(NotAnIntentException ex){
					//An exception will be thrown if we have reached and object with no more nested jsons, and that object is not an intent.
					continue;
				}
			}			
		}
		
		//We resolve the intent and substitute it with the resolution 		
		JSONObject resolved;
		Intent intent = new Intent(json);
		resolved = resolve(intent, main);
		String[] kk = new String[1];
		kk = json.keySet().toArray(kk);
		for(String k : kk){
			json.remove(k);
		}
		kk = new String[1];
		kk = resolved.keySet().toArray(kk);
		for(String k : kk){
			json.put(k, resolved.get(k));
		}
		return main;
	}
	
	/**
	 * Adds a <code>{@link Resolver}</code> to the resolver list.
	 * 
	 * Resolvers will be chosen by the API to resolve intents based on their name.
	 * This method is idempotent, i.e., already registered resolvers will not be added.
	 * 
	 * @param resolver Resolver class that implements the intent solution
	 */
	public void addResolver(Resolver resolver){
		if(!resolvers.containsKey(resolver.getName())){
			resolvers.put(resolver.getName(), resolver);
		}
	}
	
	/**
	 * Publishes in the <code>kafkamq</code> queue the parameter given.
	 * 
	 * @param json Message to be delivered
	 */
	public void send(JSONObject json){
		onSending(json);
		kafkaClient.send(json.toString().getBytes());
	}
	
	public KafkaClient getClient(){
		return kafkaClient;
	}
	
	public void finalize(){
		kafkaClient.finalize();
		resolvers.clear();
	}
}
