package org.javacode.wallet.repositories;

import org.javacode.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository <Wallet, UUID> {

    @Query(value = "SELECT * " +
                   "FROM wallet w " +
                   "WHERE w.id =?1 FOR UPDATE ", nativeQuery = true)
    Optional<Wallet> findByUUID(UUID uuid);

    @Modifying
    @Query("UPDATE Wallet w " +
            "SET w.balance = :balance " +
            "WHERE w.id = :id")
    int updateBalance(@Param("balance") BigDecimal balance, @Param("id") UUID id);
}
