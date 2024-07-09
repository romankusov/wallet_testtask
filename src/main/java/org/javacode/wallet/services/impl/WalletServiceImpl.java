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
        return walletRepository.findByUUID(walletUUID).map(this::mapToDto).orElseThrow(() ->
                new NotFoundException("Кошелек с id " + walletUUID + " не найден"));
    }

    @Override
    @Transactional
    public void postTransferMoney(WalletTransferRequestDTO walletTransferRequestDTO) {
        log.info("Получение из БД кошелька с id {}", walletTransferRequestDTO.getId());
        Wallet wallet = walletRepository.findById(walletTransferRequestDTO.getId()).orElseThrow(() ->
                new NotFoundException("Кошелек с id " + walletTransferRequestDTO.getId() + " не найден"));
        BigDecimal amount = walletTransferRequestDTO.getAmount();
        BigDecimal newBalance = null;

        if (walletTransferRequestDTO.getOperationType() == OperationType.DEPOSIT) {
            log.info("Расчет баланса после зачисления {} на кошелек с id {}", amount, wallet.getId());
            newBalance = wallet.getBalance().add(amount);
        }
        if (walletTransferRequestDTO.getOperationType() == OperationType.WITHDRAW) {
            if (walletTransferRequestDTO.getAmount().compareTo(wallet.getBalance()) > 0) {
                throw new BadRequestException("Сумма списания больше баланса кошелька с id " + wallet.getId());
            }
            log.info("Расчет баланса после списания {} с кошелька с id {}", amount, wallet.getId());
            newBalance =  wallet.getBalance().subtract(amount);
        }

        log.info("Сохранение нового баланса в размере {} кошелька с id {}", newBalance, wallet.getId());
        walletRepository.updateBalance(newBalance, wallet.getId());
    }

    private WalletResponseDTO mapToDto(Wallet wallet) {
        return WalletResponseDTO.builder()
                .uuid(wallet.getId())
                .walletHolder(wallet.getWalletHolderName())
                .balance(wallet.getBalance())
                .build();
    }
}
