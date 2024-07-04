package org.javacode.wallet.dto.in;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;
import org.javacode.wallet.enums.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class WalletTransferRequestDTO {
    @Nonnull
    private UUID id;
    @Nonnull
    private OperationType operationType;
    @Nonnull
    private BigDecimal amount;
}
