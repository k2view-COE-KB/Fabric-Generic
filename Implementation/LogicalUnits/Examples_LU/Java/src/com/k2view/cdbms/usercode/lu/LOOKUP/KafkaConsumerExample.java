package com.k2view.cdbms.usercode.lu.LOOKUP;

import java.util.*;
import java.sql.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.k2view.cdbms.shared.user.UserCode.getLuType;
import static com.k2view.cdbms.shared.user.UserCode.isAborted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerExample {

    protected static Logger log = LoggerFactory.getLogger(KafkaConsumer.class.getName());
    public KafkaConsumer<String, JSONObject> consumer;

    public KafkaConsumerExample(String groupId, String topicName, String IDFINDER_PREFIX) throws ExecutionException, InterruptedException {
        this.consumer = new KafkaConsumer<>(getConsumerProp(groupId));
        poll(topicName);
    }

    protected void poll(String topicName) throws JSONException, ExecutionException, InterruptedException {
        this.consumer.subscribe(Pattern.compile(topicName), new NoOpConsumerRebalanceListener());
        try {
            while (!isAborted()) {
                ConsumerRecords<String, JSONObject> records = consumer.poll(1000);
                for (ConsumerRecord<String, JSONObject> record : records) {
                    ///Do something
                }
                this.consumer.commitAsync();
            }
        } finally {
            if (consumer != null) {
                consumer.unsubscribe();
                consumer.close();
            }
        }
    }

    private Properties getConsumerProp(String groupId) {
        Properties props = new Properties();
        props.put("group.id", groupId);
        props.put("bootstrap.servers", "<Kafka bootstarp servers and port>");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest");
        props.put("max.poll.records", 50);
        props.put("session.timeout.ms", 120000);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "com.k2view.cdbms.kafka.JSONObjectDeserializer");

        return props;
    }

}