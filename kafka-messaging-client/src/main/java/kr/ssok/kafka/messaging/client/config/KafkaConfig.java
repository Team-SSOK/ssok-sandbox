package kr.ssok.kafka.messaging.client.config;

import kr.ssok.model.TransferRequest;
import kr.ssok.model.TransferResponse;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

//클라이언트 config
@Configuration
public class KafkaConfig {

    @Value("${openbanking.kafka.request-topic}")
    private String requestTopic;

    @Value("${openbanking.kafka.reply-topic}")
    private String replyTopic;

    @Value("${openbanking.kafka.timeout}")
    private long replyTimeout;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic requestTopic() {
        return TopicBuilder.name(requestTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic replyTopic() {
        return TopicBuilder.name(replyTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // 요청 메시지용 Consumer Factory
    @Bean
    public ConsumerFactory<String, TransferRequest> requestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bank-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "kr.ssok.model");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 응답을 위한 리스너 컨테이너 팩토리 추가
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransferResponse> replyListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransferResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(replyConsumerFactory());
        return factory;
    }

    // 응답 메시지용 Consumer Factory
    @Bean
    public ConsumerFactory<String, TransferResponse> replyConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "openbanking-replies");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "kr.ssok.model");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // ReplyingKafkaTemplate 설정 - 요청-응답 패턴을 위한 템플릿
    @Bean
    public ReplyingKafkaTemplate<String, TransferRequest, TransferResponse> replyingKafkaTemplate(
            ProducerFactory<String, TransferRequest> pf,
            ConcurrentMessageListenerContainer<String, TransferResponse> repliesContainer) {
        ReplyingKafkaTemplate<String, TransferRequest, TransferResponse> template =
                new ReplyingKafkaTemplate<>(pf, repliesContainer);
        template.setDefaultReplyTimeout(Duration.ofMillis(replyTimeout));
        return template;
    }

    // 응답을 받기 위한 리스너 컨테이너 설정
    @Bean
    public ConcurrentMessageListenerContainer<String, TransferResponse> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, TransferResponse> containerFactory) {

        ConcurrentMessageListenerContainer<String, TransferResponse> repliesContainer =
                containerFactory.createContainer(replyTopic);
        repliesContainer.getContainerProperties().setGroupId("openbanking-replies");
        repliesContainer.setAutoStartup(true);
        return repliesContainer;
    }


}
