package com.aot.payments.topic;

import com.aot.payments.model.Payment;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class PaymentInfoSender {
    private final static String TOPIC = "PaymentTopic";
    private final static String BOOTSTRAP_SERVER = "kafka:9092";

    private static Producer<String, String> getProducer() {
        Properties settings = new Properties();
        settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        settings.put(ProducerConfig.CLIENT_ID_CONFIG, "com.aot.orders.topic.OrderStatusProducer");
        settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        settings.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        settings.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/app/apps/AOTCustomerPayment/src/main/java/com/aot/invoice/jks/kafka.truststore.jks");
        settings.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test123");
        settings.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/app/apps/AOTCustomerPayment/src/main/java/com/aot/invoice/jks/kafka.keystore.jks");
        settings.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test123");
        settings.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test123");
        settings.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        settings.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + "user" + "\" password=\"" + "password" + "\";");
        return new KafkaProducer<>(settings);
    }

    public static void sendToInvoice(final String status, final Payment payment) throws ExecutionException, InterruptedException {
        System.out.println("Payment - " + payment.toString());
        Producer<String, String> producer= getProducer();
        long time = System.currentTimeMillis();
        try {
            final ProducerRecord<String, String> record =
                    new ProducerRecord<>(TOPIC, payment.getInvoiceNumber()+"-"+status,
                            payment.toString());
            RecordMetadata metadata = producer.send(record).get();
            long elapsedTime = System.currentTimeMillis() - time;
            System.out.printf("sent record(key=%s value=%s) meta(partition=%d, offset=%d) time=%d\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);


        } finally {
            producer.flush();
            producer.close();
        }
    }

    /*static void runProducer1(final int sendMessageCount) throws Exception {
        final Producer<Long, String> producer = createProducer();
        long time = System.currentTimeMillis();

        try {
            for (long index = time; index < time + sendMessageCount; index++) {
                final ProducerRecord<Long, String> record =
                        new ProducerRecord<>(TOPIC, index,"Message - Response -" + index);

                RecordMetadata metadata = producer.send(record).get();

                long elapsedTime = System.currentTimeMillis() - time;
                System.out.printf("sent record(key=%s value=%s) " +
                                "meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(),
                        metadata.offset(), elapsedTime);

            }
        } finally {
            producer.flush();
            producer.close();
        }
    }*/
}







