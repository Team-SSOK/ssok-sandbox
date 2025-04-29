package kr.ssok.kafka.messaging.client.controller;

import kr.ssok.kafka.messaging.client.service.OpenBankingService;
import kr.ssok.kafka.messaging.client.service.RequestService;
import kr.ssok.model.ResponseMessage;
import kr.ssok.model.TransferRequest;
import kr.ssok.model.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openbanking")
@RequiredArgsConstructor
public class OpenBankingController {

    private final OpenBankingService openBankingService;
    private final RequestService requestService;

//    @PostMapping("/transfer")
//    public ResponseEntity<TransferResponse> transferMoney(@RequestBody TransferRequest request) {
//        TransferResponse response = openBankingService.processTransfer(request);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/send-request")
    public ResponseEntity<ResponseMessage> sendRequest(@RequestParam String message) {
        try {
            // RequestService를 통해 Kafka로 메시지 전송 및 응답 수신
            ResponseMessage response = requestService.sendAndReceive(message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
