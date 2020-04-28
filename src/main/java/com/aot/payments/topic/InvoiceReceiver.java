package com.aot.payments.topic;

import com.aot.payments.model.Invoice;
import com.aot.payments.service.PaymentFileService;
import com.google.gson.Gson;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

public class InvoiceReceiver {
    private final static String TOPIC = "InvoiceTopic";
    private final static String BOOTSTRAP_SERVER = "kafka:9092";

    private static Consumer<String, String> createConsumer() {
        final Properties settings = new Properties();
        settings.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        settings.put(ConsumerConfig.GROUP_ID_CONFIG, "OrderConsumerGroup");
        settings.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        settings.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        settings.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        settings.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/app/apps/AOTCustomerPayment/src/main/java/com/aot/invoice/jks/kafka.truststore.jks");
        settings.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test123");
        settings.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/app/apps/AOTCustomerPayment/src/main/java/com/aot/invoice/jks/kafka.keystore.jks");
        settings.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test123");
        settings.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test123");
        settings.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        settings.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + "user" + "\" password=\"" + "password" + "\";");
        final Consumer<String, String> consumer = new KafkaConsumer<>(settings);
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }

    public static void getInvoiceFromTopic()  {
        final Consumer<String, String> consumer = createConsumer();
        final ConsumerRecords<String, String> consumerRecords =
                consumer.poll(1000);
        consumerRecords.forEach(record -> {
            System.out.println(record.key() + ":" + record.value());
            Gson g = new Gson();
            Invoice invoice = g.fromJson(record.value(), Invoice.class);
            System.out.println("From gson:" + invoice.getInvoiceNumber() + invoice.getAddress().getAddressLine1());
        });
        new PaymentFileService().triggerInvoiceEvent(consumerRecords);
        consumer.commitAsync();
        consumer.close();
        //System.out.println("Fetched Invoice");
    }
}







