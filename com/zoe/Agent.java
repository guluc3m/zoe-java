package com.zoe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import com.rabbitmq.client.*;

/**
 * This class defines an Agent, the core of the Zoe library.
 * 
 * To create an Agent:
 *  - First, create an object of type this class (i.e. <code>{@link Agent}</code>)
 *  - Add resolvers (i.e., classes implementing <code>{@link Resolver}</code>) via the method <code>{@link Agent#addResolver(Resolver)}</code>)
 *  - Start the agent through <code>{@link Agent#start()}</code> in the main thread
 * 
 * @author danoloan10
 * @version v0.1
 * @since 25th October, 2017
 */

public class Agent{
	private String name = "agent";
	private Publisher publisher = new Publisher(this);
	private Map<String, Resolver> resolvers = new HashMap<String, Resolver>();
	private Consumer con;
	private boolean online = true;

	private RabbitMQClient rabbitClient;
	
	/**
	 * Default constructor
	 * 
	 * In this constructor, both the <code>{@link RabbitMQClient}</code> and the <code>Consumer</code> are created. This object can be created without connecting to a RabbitMQ serve (see forceOnline below)
	 * 
	 * @param name The name of the <code>{@link Agent}</code>, what identifies it. It should be assigned in the extension of this class.
	 * @param forceOnline If the connection with the bus server could not be established, the creation of this agent will throw a IOException if this parameter is true; if it is false, it will create a dummy agent.
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public Agent(String name, boolean forceOnline) throws IOException, TimeoutException{
		try{
			rabbitClient = new RabbitMQClient();
		}catch(IOException ex){
			if(!forceOnline){
				online = false;
				System.out.println("WARNING: Agent "+name+" running in offline mode. There is no connection to a RabbitMQ bus");
			}else{
				throw new IOException(ex.getMessage() + " (agent running in 'forced online mode')");
			}
		}
		this.name = name;
		if(online){
			con = new DefaultConsumer(rabbitClient.getChannel()){
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
	
					handler(consumerTag, envelope, properties, body);
	
					JSONObject message = Util.bytesToJSON(body);
					try{
						publish(intentResolver(message));
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}		
			};
		}
	}
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
		resolved = resolve(new Intent(json), main);
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
	 * Start the Agent, i.e., start listening
	 * @throws IOException
	 */
	public void start() throws IOException{
		if(online)
			rabbitClient.startConsuming(con);
	}
	
	/**
	 * This is the first method called by the <code>Consumer.handleDelivery()</code> method; it is originally declared to do nothing.
	 * 
	 * The parameters of <code>handleDelivery()</code> are directly passed to this method. In case that something should be done before any delivery,
	 * this method should be overrided.
	 *  
	 * @param consumerTag
	 * @param envelope
	 * @param properties
	 * @param body
	 * @throws IOException
	 */
	public void handler(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException{
		System.out.println("Received: "+new String(body));
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
	 * Publishes in the <code>rabbitmq</code> queue the parameter given.
	 * 
	 * @param json Message to be delivered
	 */
	public void publish(JSONObject json){
		publisher.queueMessage(json.toString().getBytes());
	}
	
	//Getters
	public String getName(){
		return name;
	}
	public RabbitMQClient getClient(){
		return rabbitClient;
	}
	
	public void finalize(){
		try {
			rabbitClient.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
		resolvers.clear();
	}
}
