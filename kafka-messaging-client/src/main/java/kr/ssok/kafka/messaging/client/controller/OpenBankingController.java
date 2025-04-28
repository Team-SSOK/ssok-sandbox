package kr.ssok.kafka.messaging.client.controller;

import kr.ssok.kafka.messaging.client.service.OpenBankingService;
import kr.ssok.model.TransferRequest;
import kr.ssok.model.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openbanking")
@RequiredArgsConstructor
public class OpenBankingController {

    private final OpenBankingService openBankingService;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferMoney(@RequestBody TransferRequest request) {
        TransferResponse response = openBankingService.processTransfer(request);
        return ResponseEntity.ok(response);
    }
}
