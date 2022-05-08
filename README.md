# kafka-ssl-memory-leak Project (reproducer)

Motivation: https://github.com/quarkusio/quarkus/issues/25098#issuecomment-1119401765

### Conclusion

When the following properties are added into the `application.properties` file, and kafka producer/consumer are invoked
, we have seen an increase in application memory usage. Note that these properties are not needed at all, were left over 
from another scenario. 

```shell
mp.messaging.outgoing.generated-price.connector=smallrye-kafka
mp.messaging.outgoing.generated-price.topic=prices
mp.messaging.outgoing.generated-price.value.serializer=org.apache.kafka.common.serialization.IntegerSerializer

mp.messaging.incoming.prices.connector=smallrye-kafka
mp.messaging.incoming.prices.value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
```


### Steps to reproduce the "issue"

1. Generate required secrets:
```shell
cd src/main/resources 
./create-certs.sh
```

2. Launch Kafka by the given docker-compose
```shell
docker-compose up
```
Note: be sure that Kafka start without errors.

3. Generate the native app pointing to the generated certificates
```shell
mvn clean package -Pnative -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel:21.3.1.1-Final-java17 -Dquarkus.platform.version=2.7.5.Final -Dkafka.producer.ssl.keystore.location=${PWD}/src/main/resources/secrets/producer.keystore.jks -Dkafka.producer.ssl.truststore.location=${PWD}/src/main/resources/secrets/producer.truststore.jks -Dkafka.consumer.ssl.keystore.location=${PWD}/src/main/resources/secrets/consumer.keystore.jks -Dkafka.consumer.ssl.truststore.location=${PWD}/src/main/resources/secrets/consumer.truststore.jks
```
4. Launch the app
```shell
 ./target/kafka-ssl-memory-leak-1.0.0-SNAPSHOT-runner
```
5. Launch a client emulator
```shell
./src/main/resources/client.sh
```
7. Take a look at the memory usage of the process `kafka-ssl-memory-leak-1.0.0-SNAPSHOT-runner`