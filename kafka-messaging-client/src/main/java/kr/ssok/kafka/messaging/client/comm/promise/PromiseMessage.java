package kr.ssok.kafka.messaging.client.comm.promise;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@RequiredArgsConstructor
public class PromiseMessage {

    private final ConsumerRecord<String, Object> response;

    public <R> R getDataObject(Class<R> responseType) {
        Object value = response.value();

        if (value == null) return null;

        if (responseType.isInstance(response.value()))
            return responseType.cast(response.value());
        else {
            throw new ClassCastException(String.format(
                    "응답 값을 %s 타입으로 변환할 수 없습니다. 실제 타입: %s",
                    responseType.getName(),
                    value.getClass().getName()
            ));
        }
    }

}
