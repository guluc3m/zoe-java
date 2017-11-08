package com.zoe;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.*;

/**
 * The <code>rabbitmq</code> API for the <code>{@link Agent}</code>.
 * 
 * This object is responsible for creating the connection, the channel and the queue used in the life of the <code>{@link Agent}</code> that uses it.
 * It is made so that its connections are compatible with other Zoe libraries.
 * 
 * @author danoloan10
 * @since 25th October, 2017
 * @version v0.1
 */

public class RabbitMQClient{
	//Names compatible with the python library
    @SuppressWarnings("unused")
	private final String QUEUE = "zoemessages";  //Not used... yet
    private final String ROUTING_KEY = "zoemessages";
    private final String EXCHANGE = "zoeexchange";
    
    //The URL provided by the environment
	private static String url = System.getenv("RABBITMQ");
	
	//Connection object
	private ConnectionFactory factory;
	private Connection connection;
	private Channel channel;
	private String qName;
	
	/**
	 * Full constructor.
	 * 
	 * Creates the connection, the channel and the queue to be used by the current instance of this object, and binds the new queue to the exising common 
	 * exchange of Zoe 
	 * 
	 * @param url the URL to connect to the <code>rabbitmq</code> server
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public RabbitMQClient(String url) throws IOException, TimeoutException{
		factory = new ConnectionFactory();
		connection = factory.newConnection(url);
		channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE, "fanout");
		
		qName = channel.queueDeclare().getQueue();
		channel.queueBind(qName, EXCHANGE, "");
	}
	/** 
	 * Default constructor.
	 * 
	 * Calls the full constructor with the URL provided by the environment as a parameter, thus creating the connection to the server in that URL.
	 * 
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public RabbitMQClient() throws IOException, TimeoutException{
		this(url);
	}
	
	public Channel getChannel(){
		return channel;
	}
	
	/**
	 * Calls the <code>basicConsume()</code> function.
	 * 
	 * Reads messages from the queue created for the current instance of this object.
	 * It is important to notice that this function starts up a new thread in the Java program.
	 * 
	 * @param con The <code>Consumer</code> to be used in the consuming process
	 * @throws IOException
	 */
	public void startConsuming(Consumer con) throws IOException{
		channel.basicConsume(qName, true, con);
	}
	/**
	 * Calls the <code>basicPublish()</code> function.
	 * 
	 * This action is done in the thread where it is called. It publishes the parameter in the common exchange used by Zoe agents.
	 * 
	 * @param body The byte stream to be published
	 * @throws IOException
	 */
	public void publish(byte[] body) throws IOException{
		channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
	}
	
	/**
	 * Closes all channels and connections created.
	 * 
	 * Should be called when the <code>{@link RabbitMQClient}</code> object is not anymore used.
	 * 
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public void close() throws IOException, TimeoutException{
		channel.close();
		connection.close();
	}
}
