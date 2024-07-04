package org.javacode.wallet.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacode.wallet.dto.in.WalletTransferRequestDTO;
import org.javacode.wallet.dto.out.WalletResponseDTO;
import org.javacode.wallet.enums.OperationType;
import org.javacode.wallet.exceptions.exceptiontype.BadRequestException;
import org.javacode.wallet.exceptions.exceptiontype.NotFoundException;
import org.javacode.wallet.model.Wallet;
import org.javacode.wallet.repositories.WalletRepository;
import org.javacode.wallet.services.WalletService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    @Override
    public WalletResponseDTO getWalletByUUID(UUID walletUUID) {
        log.info("Получение из БД кошелька с id {}", walletUUID);
        return walletRepository.findById(walletUUID).map(this::mapToDto).orElseThrow(() ->
                new NotFoundException("Кошелек с id " + walletUUID + " не найден"));
    }

    @Override
    @Transactional
    public void postTransferMoney(WalletTransferRequestDTO walletTransferRequestDTO) {
        log.info("Получение из БД кошелька с id {}", walletTransferRequestDTO.getId());
        Wallet wallet = walletRepository.findById(walletTransferRequestDTO.getId()).orElseThrow(() ->
                new NotFoundException("Кошелек с id " + walletTransferRequestDTO.getId() + " не найден"));
        BigDecimal amount = walletTransferRequestDTO.getAmount();

        if (walletTransferRequestDTO.getOperationType() == OperationType.DEPOSIT) {
            log.info("Зачисление средств в размере {} на кошелек с id {}", amount, wallet.getId());
            depositMoney(wallet, amount);
        }
        if (walletTransferRequestDTO.getOperationType() == OperationType.WITHDRAW) {
            if (walletTransferRequestDTO.getAmount().compareTo(wallet.getBalance()) > 0) {
                throw new BadRequestException("Сумма списания больше баланса кошелька с id " + wallet.getId());
            }
            log.info("Списание средств в размере {} с кошелька с id {}", amount, wallet.getId());
            withdrawMoney(wallet, amount);
        }
    }

    private void depositMoney(Wallet wallet, BigDecimal amount) {
        BigDecimal newAmount = wallet.getBalance().add(amount);
        wallet.setBalance(newAmount);
        walletRepository.save(wallet);
    }

    private void withdrawMoney(Wallet wallet, BigDecimal amount) {
        BigDecimal newAmount = wallet.getBalance().subtract(amount);
        wallet.setBalance(newAmount);
        walletRepository.save(wallet);
    }

    private WalletResponseDTO mapToDto(Wallet wallet) {
        return WalletResponseDTO.builder()
                .uuid(wallet.getId())
                .walletHolder(wallet.getWalletHolderName())
                .balance(wallet.getBalance())
                .build();
    }
}
