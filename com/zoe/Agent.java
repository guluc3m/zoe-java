package com.zoe;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import com.rabbitmq.client.*;

/**
 * This class defines the basic agent model of Zoe, without specifying its behavior.
 * 
 * This class consists of attributes and two separate <code>Threads</code>: 
 * 		one handled by the <code>rabbitmq</code> library used to consume messages, 
 * 		and the other included in the class object used to publish them, of type {@link Publisher}.
 * The <code>Agent</code> is supposed to receive messages at the same time that delivers them. 
 * When a message is received, whether it is an <code>{@link Intent}</code> or some raw JSON is checked.
 * If it happens to be the first, it will resolve it as specified in <code>{@link #intentResolver(Intent)}</code>; 
 * else, it will try to process it through the method <code>{@link #pendingResolver(JSONObject)}</code>. 
 * The unresolved intents are supposed to be stored in the <code>Queue {@link #unresolved}</code>, as there is 
 * where the <code>{@link #pendingResolver(JSONObject)}</code> will look up for pending intents
 * This can be useful when trying to implement nested intent resolvers.
 * 
 * @author danoloan10
 * @version v0.0.1
 * @since 25th October, 2017
 */

public abstract class Agent{
	private String name = "agent";
	private Publisher publisher = new Publisher(this);
	private Queue<Intent> unresolved = new LinkedList<Intent>();
	private Consumer con;

	protected RabbitMQClient rabbitClient;
	
	/**
	 * Default and only constructor
	 * 
	 * In this constructor, both the <code>{@link RabbitMQClient}</code> and the <code>Consumer</code> are created.
	 * 
	 * @param name The name of the <code>{@link Agent}</code>, what identifies it. It should be assigned in the extension of this class.
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public Agent(String name) throws IOException, TimeoutException{
		rabbitClient = new RabbitMQClient();
		this.name = name;
		con = new DefaultConsumer(rabbitClient.getChannel()){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				//handler() call
				handler(consumerTag, envelope, properties, body);
				JSONObject json = null;
				try{
					//if the message is an integer, pass it to the intent resolver
					json = intentResolver(Util.bytesToIntent(body));
				}catch(NotAnIntentException ex){
					//if the message is not an intent, pass it to the pending resolver
					json = pendingResolver(new JSONObject(body));
				}
				if(json != null) {
					//Publish only in case there is something to publish
					publish(json);
				}
			}		
		};
	}
	
	public void start() throws IOException{
		rabbitClient.startConsuming(con);
		publisher.start();
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
	protected void handler(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException{}
	
	/**
	 * Returns a JSON, result of some <code>{@link Intent}</code> passed as a parameter.
	 * 
	 * When this function is specified, it should filter the intents by their name, and also should expect some defined fields in the JSON received
	 * 
	 * @param intent The <code>{@link Intent}</code> to be resolved
	 * @return
	 */
	protected abstract JSONObject intentResolver(Intent intent);
	
	/**
	 * Method called when a JSON arrives that is not an <code>{@link Intent}</code>
	 * 
	 * This method should be overrided if developing a nested-intent <code>{@link Agent}</code> 
	 * to define how the resolved inner intents should be included into the resolution of the pending intents of the agent.
	 * 
	 * @param json The non-intent JSON
	 * @return The first unresolved <code>{@link Intent}</code>
	 */
	protected JSONObject pendingResolver(JSONObject json){
		if(!unresolved.isEmpty()) return unresolved.poll();
		return null;
	}
	
	/**
	 * Adds an <code>{@link Intent}</code> to the pending list.
	 * 
	 * Intents should only be added to the pending list if they are unresolved until other intent is resolved, i.e.: if they are nested
	 * 
	 * @param intent Nested intent pending resolution
	 */
	protected void addPending(Intent intent){
		unresolved.add(intent);
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
	
	public void finalize() throws IOException, TimeoutException{
		rabbitClient.close();
	}
}
