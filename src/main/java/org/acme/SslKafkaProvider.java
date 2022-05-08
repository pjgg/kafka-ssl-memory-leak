package org.acme;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.config.SslConfigs;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class SslKafkaProvider extends KafkaProviders {

    @ConfigProperty(name = "kafka-client-ssl.bootstrap.servers", defaultValue = "localhost:9092")
    String sslKafkaBootStrap;

    @ConfigProperty(name = "kafka.ssl.truststore.password", defaultValue = "password")
    String trustStorePassword;

    @ConfigProperty(name = "kafka.ssl.keystore.password", defaultValue = "password")
    String keystorePassword;

    @ConfigProperty(name = "kafka.ssl.truststore.type", defaultValue = "jks")
    String trustStoreType;

    @ConfigProperty(name = "kafka.ssl.keystore.type", defaultValue = "jks")
    String keystoreType;

    @ConfigProperty(name = "kafka.producer.ssl.keystore.location", defaultValue = "secrets/producer.keystore.jks")
    String producerKeystore;

    @ConfigProperty(name = "kafka.producer.ssl.truststore.location", defaultValue = "secrets/producer.truststore.jks")
    String producerTruststore;

    @ConfigProperty(name = "kafka.consumer.ssl.keystore.location", defaultValue = "secrets/consumer.keystore.jks")
    String consumerKeystore;

    @ConfigProperty(name = "kafka.consumer.ssl.truststore.location", defaultValue = "secrets/consumer.truststore.jks")
    String consumerTruststore;

    @Produces
    @Singleton
    @Named("kafka-consumer-ssl")
    KafkaConsumer<String, String> getSslConsumer() {
        Properties props = setupConsumerProperties(sslKafkaBootStrap, "test-consumer");
        sslSetup(consumerTruststore, consumerKeystore, props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-ssl-consumer"));
        return consumer;
    }

    @Produces
    @Singleton
    @Named("kafka-producer-ssl")
    KafkaProducer<String, String> getSslProducer() {
        Properties props = setupProducerProperties(sslKafkaBootStrap);
        sslSetup(producerTruststore, producerKeystore, props);
        return new KafkaProducer<>(props);
    }

    @Produces
    @Singleton
    @Named("kafka-admin-ssl")
    AdminClient getSslAdmin() {
        Properties props = setupConsumerProperties(sslKafkaBootStrap, "test-consumer-admin");
        sslSetup(consumerTruststore, consumerKeystore, props);
        return KafkaAdminClient.create(props);
    }

    protected void sslSetup(String truststorePath, String keystorePath, Properties props) {
        File tsFile = new File(truststorePath);
        File ksFile = new File(keystorePath);
        props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, tsFile.getPath());
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword);
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, trustStoreType);

        props.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, ksFile.getPath());
        props.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, keystoreType);

        props.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
    }

}
