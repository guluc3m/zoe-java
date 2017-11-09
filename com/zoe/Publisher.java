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
public class Publisher extends Thread{
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
		return messages.add(mes);
	}
	
	@Override
	public void run() {
		while(true){
			if(!messages.isEmpty()){
				try {
					byte[] message = messages.poll();
					agent.getClient().publish(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}