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
            // 이 부분이 protobuf에서 optional로 선언했기 때문에, 이런 식으로 구현 가능
            // repeated는 isEmpty로 판별
            if(response.hasMessage()) {
                log.info(response.getMessage());
            } // 이 부분에 protobuf에서 optional 했으니까 제대로 왔는지 확인하고 싶음
            return response.getMessage();
        } catch (StatusRuntimeException e) {
            log.warn("gRPC request failed: {}", e.getStatus(), e);
            return "gRPC request failed: " + e.getStatus().getDescription();
        }
    }
}
