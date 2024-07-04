package org.javacode.wallet.controllers;

import lombok.RequiredArgsConstructor;
import org.javacode.wallet.dto.in.WalletTransferRequestDTO;
import org.javacode.wallet.dto.out.WalletResponseDTO;
import org.javacode.wallet.services.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/wallets/{walletUUID}")
    public ResponseEntity<WalletResponseDTO> getWalletByUUID(@PathVariable UUID walletUUID) {
        return ResponseEntity.ok(walletService.getWalletByUUID(walletUUID));
    }

    @PostMapping("/wallet")
    public void postTransferMoney(@RequestBody WalletTransferRequestDTO walletTransferRequestDTO) {
        walletService.postTransferMoney(walletTransferRequestDTO);
    }
}
