package pl.bpiatek.linkshortenerdashboardqueryservice.config;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import pl.bpiatek.contracts.analytics.AnalyticsEventProto.LinkClickEnrichedEvent;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto.LinkLifecycleEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // =================================================================================
    // CONSUMER 1: LinkClickEnrichedEvent
    // =================================================================================

    @Bean
    ConsumerFactory<String, LinkClickEnrichedEvent> linkEnrichedClickEventConsumerFactory() {
        Map<String, Object> props = baseConsumerProperties();
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, LinkClickEnrichedEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, LinkClickEnrichedEvent> linkEnrichedClickEventContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<String, LinkClickEnrichedEvent> linkEnrichedClickEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, LinkClickEnrichedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        configurer.configure(
                (ConcurrentKafkaListenerContainerFactory) factory,
                (ConsumerFactory) linkEnrichedClickEventConsumerFactory
        );

        return factory;
    }

    // =================================================================================
    // CONSUMER 2: LinkLifecycleEvent
    // =================================================================================

    @Bean
    ConsumerFactory<String, LinkLifecycleEvent> linkLifecycleEventConsumerFactory() {
        Map<String, Object> props = baseConsumerProperties();
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, LinkLifecycleEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @Primary
    ConcurrentKafkaListenerContainerFactory<String, LinkLifecycleEvent> linkLifecycleEventsContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<String, LinkLifecycleEvent> linkLifecycleEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, LinkLifecycleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        configurer.configure(
                (ConcurrentKafkaListenerContainerFactory) factory,
                (ConsumerFactory) linkLifecycleEventConsumerFactory
        );

        return factory;
    }

    // =================================================================================
    // HELPERS
    // =================================================================================

    private Map<String, Object> baseConsumerProperties() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);

        var schemaRegistryUrl = kafkaProperties.getProperties().get("schema.registry.url");
        if (schemaRegistryUrl != null) {
            props.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        }

        return props;
    }
}