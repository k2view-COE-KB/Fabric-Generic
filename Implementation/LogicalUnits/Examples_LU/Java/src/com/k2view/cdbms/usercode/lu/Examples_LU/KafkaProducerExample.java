package com.k2view.cdbms.usercode.lu.Examples_LU;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.concurrent.ExecutionException;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import org.apache.kafka.clients.producer.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerExample {

    protected static Logger log = LoggerFactory.getLogger(KafkaProducerExample.class.getName());
    private Producer<String, JSONObject> producer;

    public KafkaProducerExample() {
        this.producer = new KafkaProducer<>(getProducerProp());
    }

    private void publishMessage(JSONObject GGJsonMsg, String tableName, StringBuilder msgKey) throws ExecutionException, InterruptedException {
        try {
            this.producer.send(new ProducerRecord("<TOPIC>", null, "<KEY>", "<MSG>")).get();
        } catch (Exception e) {
            log.error("KafkaProducerExample: Failed To Send Records To Kafka");
            if (this.producer != null) this.producer.close();
            throw e;
        }
    }

    private Properties getProducerProp() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "<Kafka bootstarp servers and port>");
        props.put("acks", "all");
        props.put("retries", "5");
        props.put("batch.size", "<batch size>");
        props.put("linger.ms", 1);
        props.put("max.block.ms", "<max block>");
        props.put("buffer.memory", "<buffer memory>");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }
}
