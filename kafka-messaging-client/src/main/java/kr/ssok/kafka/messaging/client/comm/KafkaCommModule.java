package kr.ssok.kafka.messaging.client.comm;

import kr.ssok.kafka.messaging.client.comm.promise.CommQueryPromise;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.function.BiConsumer;

public interface KafkaCommModule {
    /**
     * @param key
     * @param request
     * @param timeout
     * @return
     */
    public CommQueryPromise sendPromiseQuery(String key, Object request, int timeout);

    /**
     * @param key
     * @param request
     * @return
     */
    public Message sendMessage(String key, Object request);

    /**
     * @param key
     * @param request
     * @param callback
     * @return
     */
    public Message sendMessage(String key, Object request, BiConsumer<? super SendResult<String, Object>, ? super Throwable> callback);

    /**
     * @return
     */
    public ReplyingKafkaTemplate<String, Object, Object> getReplyingKafkaTemplate();
}
