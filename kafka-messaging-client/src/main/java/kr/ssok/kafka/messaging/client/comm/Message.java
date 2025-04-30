package kr.ssok.kafka.messaging.client.comm;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class Message {
    private final CompletableFuture<SendResult<String, Object>> future;

    public void addCallback(BiConsumer<? super SendResult<String, Object>, ? super Throwable> callback) {
        if (callback != null) this.future.whenComplete(callback);
    }

}
