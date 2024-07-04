package org.javacode.wallet.services;

import org.javacode.wallet.dto.in.WalletTransferRequestDTO;
import org.javacode.wallet.dto.out.WalletResponseDTO;

import java.util.UUID;

public interface WalletService {
    WalletResponseDTO getWalletByUUID(UUID walletUUID);

    void postTransferMoney(WalletTransferRequestDTO walletTransferRequestDTO);
}
