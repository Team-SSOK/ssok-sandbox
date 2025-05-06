package kr.ssok.grpc_client.config;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.examples.helloworld.GreeterGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class GrpcClientConfig {

    private ManagedChannel managedChannel;

    // ManagedChannel == HTTP/2 기반 통신 채널을 열기 위한 TCP 연결 객체
    @Bean
    public ManagedChannel managedChannel() {
        // InsecureChannelCredentials == TLS 없이 연결 하겠다라는 뜻
        // TODO. 주소 부분은 따로 application.yml로 뺄 필요 있음
        this.managedChannel = Grpc.newChannelBuilder("localhost:50051", InsecureChannelCredentials.create())
                .build();
        return this.managedChannel;
    }

    @Bean
    public GreeterGrpc.GreeterBlockingStub greeterBlockingStub(ManagedChannel managedChannel) {
        return GreeterGrpc.newBlockingStub(managedChannel);
    }

    // client 서버 종료 시, 안전하게 채널도 shutdown
    @PreDestroy
    public void shutdown() {
        if (this.managedChannel != null && !this.managedChannel.isShutdown()) {
            this.managedChannel.shutdown();
        }
    }
}
