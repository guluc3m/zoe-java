package com.zoe;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Starts a thread that publishes messages as they are received.
 * 
 * 
 * 
 * @author danoloan10
 *
 */
public class Publisher {
	private Agent agent;
	
	//TODO Esta Queue se podría sustituir por una BlockingQueue
	private Queue<byte[]> messages = new LinkedList<byte[]>();
	
	/**
	 * Constructor
	 * 
	 * All instances of <code>{@link Publisher}</code> depend on and instance of <code>{@link Agent}</code>, that is the one handling the connections.
	 * 
	 * @param agent the agent handling the connections
	 */
	public Publisher(Agent agent){
		this.agent = agent;
	}
	
	/**
	 * Adds a message to the publishing queue.
	 * 
	 * The queued message will be sent as soon as possible through the common exchange of Zoe agents.
	 * 
	 * @param mes the message to publish, in byte stream form
	 * @return true if it could be added, false if not.
	 */
	public boolean queueMessage(byte[] mes){
		boolean b = messages.add(mes);
		if(b) commitMessages();
		return b;
	}
	private void commitMessages(){
		while(!messages.isEmpty()){
			byte[] message = messages.poll();
			try {
				agent.getClient().publish(message);
				//System.out.println("Sent: "+new String(message));
			} catch (Exception e) {
				messages.add(message);
				e.printStackTrace();
			}
		}
	}
}