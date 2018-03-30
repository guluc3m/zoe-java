package org.zoe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.*;

public class KafkaClient{

	private KafkaConsumer<String, byte[]> consumer;
	private KafkaProducer<String, byte[]> producer;

	private static String url = System.getenv("KAFKA_SERVERS");
	private static String topic = "zoe";
	
	public KafkaClient(String url, String group){
		Properties props = new Properties();
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		consumer = new KafkaConsumer<String, byte[]>(props, new StringDeserializer(), new ByteArrayDeserializer());		
		consumer.subscribe(Arrays.asList(topic));
		
		props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
		producer = new KafkaProducer<String, byte[]>(props, new StringSerializer(), new ByteArraySerializer());
	}
	public KafkaClient(String group){
		this(url, group);
	}
	
	public ConsumerRecords<String, byte[]> consume(){
		ConsumerRecords<String, byte[]> record = null;
		while(record == null)
			record = consumer.poll(100);
		return record;
	}
	
	public void commit(ConsumerRecord<String, byte[]> record){
		System.out.println("Commiting record in offset "+record.offset()+" in partition "+record.partition()+" of topic "+record.topic());
		Map<TopicPartition, OffsetAndMetadata> offset = new HashMap<TopicPartition, OffsetAndMetadata>();
		offset.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()+1));
		consumer.commitSync(offset);
	}
	public void ignore(ConsumerRecord<String, byte[]> record){
		System.out.println("Ignoring record in offset "+record.offset()+" in partition "+record.partition()+" of topic "+record.topic());
		consumer.seek(new TopicPartition(record.topic(), record.partition()), record.offset()+1);
	}
	
	public void send(byte[] message){
		producer.send(new ProducerRecord<String,byte[]>(topic, Double.toString(Math.random()*1000), message));
	}
	
	public void finalize(){
		producer.close();
		consumer.close();
	}
}
