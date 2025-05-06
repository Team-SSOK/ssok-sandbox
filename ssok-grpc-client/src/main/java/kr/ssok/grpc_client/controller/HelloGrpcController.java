package kr.ssok.grpc_client.controller;

import kr.ssok.grpc_client.service.HelloGrpcClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
public class HelloGrpcController {
    private final HelloGrpcClientService helloGrpcClientService;

    @GetMapping
    public String sayHello(@RequestParam(defaultValue = "world") String name) {
        return helloGrpcClientService.sayHello(name);
    }
}
