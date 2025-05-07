package kr.ssok.grpc_server.service;

import io.grpc.Status;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class GreeterService extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        // 이 부분이 protobuf에서 optional로 선언했기 때문에, 이런 식으로 구현 가능
        // repeated는 isEmpty로 판별
        if (!req.hasName()) {
            // 클라이언트가 name 필드를 아예 안 보낸 경우
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Name is required.")
                    .asRuntimeException());
            return;
        }

        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello " + req.getName())
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
