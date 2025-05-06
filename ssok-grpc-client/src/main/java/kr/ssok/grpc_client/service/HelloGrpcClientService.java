package kr.ssok.grpc_client.service;

import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelloGrpcClientService {
    private final GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    public String sayHello(String name) {
        // 요청 DTO를 만드는 것과 유사
        // name 자리에 RequestDto를 대입하는 방식으로 구현 가능
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        try {
            // 요청에 대한 결과 받기
            // 내가(클라이언트가) 가진 메소드가 아닌, 서버가 가진 메소드를 사용
            HelloReply response = greeterBlockingStub.sayHello(request);
            return response.getMessage();
        } catch (StatusRuntimeException e) {
            log.warn("gRPC request failed: {}", e.getStatus(), e);
            return "gRPC request failed: " + e.getStatus().getDescription();
        }
    }
}
