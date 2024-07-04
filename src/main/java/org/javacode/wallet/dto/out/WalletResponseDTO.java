package org.javacode.wallet.dto.out;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class WalletResponseDTO {
    @Nonnull
    private UUID uuid;
    @Nonnull
    private String walletHolder;
    @Nonnull
    private BigDecimal balance;
}
