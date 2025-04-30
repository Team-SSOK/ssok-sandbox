package kr.ssok.kafka.messaging.client.comm.promise;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.requestreply.RequestReplyFuture;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class CommQueryPromise {
    private final RequestReplyFuture<String, Object, Object> future;

    public PromiseMessage get() throws InterruptedException, ExecutionException {
        ConsumerRecord<String, Object> response = future.get();
        return new PromiseMessage(response);
    }

}
