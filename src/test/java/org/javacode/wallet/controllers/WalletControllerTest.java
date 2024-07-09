package org.javacode.wallet.controllers;

import org.javacode.wallet.dto.in.WalletTransferRequestDTO;
import org.javacode.wallet.dto.out.WalletResponseDTO;
import org.javacode.wallet.enums.OperationType;
import org.javacode.wallet.model.Wallet;
import org.javacode.wallet.repositories.WalletRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class WalletControllerTest {

    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private MockMvc mockMvc;

    private UUID uuid;
    private final String wrongUUID = "7087c37f-0502-461d-b680-2d63f7985f79";
    private final BigDecimal balance = BigDecimal.valueOf(10000);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Wallet wallet = new Wallet();
    private final String walletHolderName = "Ivan";

    @BeforeEach
    void setUp(){
        wallet.setId(uuid);
        wallet.setBalance(balance);
        wallet.setWalletHolderName(walletHolderName);
        uuid = walletRepository.save(wallet).getId();
    }

    @BeforeAll
    public static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    public static void afterAll() {
        postgreSQLContainer.stop();
    }

    @Test
    @DisplayName("Тест успешного получения кошелька по id")
    void testOkGetWalletByUUID() throws Exception {
        //given
        WalletResponseDTO walletResponseDTO = WalletResponseDTO.builder()
                .uuid(uuid)
                .walletHolder(walletHolderName)
                .balance(balance)
                .build();

        //when
        mockMvc.perform(get("/api/v1/wallets/"+uuid)
                .contentType(MediaType.APPLICATION_JSON)
        //then
                ).andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(walletResponseDTO))
                );
    }

    @Test
    @DisplayName("Тест получения кошелька при неверном id")
    void testNotFoundGetWalletByUUID() throws Exception {
        //when
        mockMvc.perform(get("/api/v1/wallets/" + wrongUUID)
                       .contentType(MediaType.APPLICATION_JSON)
        //then
        ).andExpectAll(
                status().isNotFound(),
                jsonPath("$.errorCode").value("404"),
                jsonPath("$.errorMessage").value("Кошелек с id " + wrongUUID + " не найден")
        );
    }

    @Test
    @DisplayName("Тест успешного пополнения кошелька")
    void testOkPostTransferMoney_deposit() throws Exception {
        //given
        WalletTransferRequestDTO walletTransferRequestDTO = WalletTransferRequestDTO.builder()
                .id(uuid)
                .operationType(OperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(10000))
                .build();

        BigDecimal expectedBalance = balance.add(BigDecimal.valueOf(10000));
        //when
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletTransferRequestDTO))
        //then
        ).andExpect(
                status().isOk()
        );

        BigDecimal actualBalance = walletRepository.findById(uuid).get().getBalance();

        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    @DisplayName("Тест проведения операции при неверном id кошелька")
    void testNotFoundPostTransferMoney() throws Exception {
        //given
        WalletTransferRequestDTO walletTransferRequestDTO = WalletTransferRequestDTO.builder()
                .id(UUID.fromString(wrongUUID))
                .operationType(OperationType.DEPOSIT)
                .amount(BigDecimal.valueOf(10000))
                .build();

        //when
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(walletTransferRequestDTO))
        //then
        ).andExpectAll(
                status().isNotFound(),
                jsonPath("$.errorCode").value("404"),
                jsonPath("$.errorMessage").value("Кошелек с id " + wrongUUID + " не найден")
        );
    }

    @Test
    @DisplayName("Тест успешного списания средств с кошелька")
    void testOkPostTransferMoney_withdraw() throws Exception {
        //given
        WalletTransferRequestDTO walletTransferRequestDTO = WalletTransferRequestDTO.builder()
                .id(uuid)
                .operationType(OperationType.WITHDRAW)
                .amount(BigDecimal.valueOf(10000))
                .build();

        BigDecimal expectedBalance = BigDecimal.ZERO;

        //when
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletTransferRequestDTO))
        //then
        ).andExpect(
                status().isOk()
        );

        BigDecimal actualBalance = walletRepository.findById(uuid).get().getBalance();

        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    @DisplayName("Тест списания суммы больше баланса кошелька")
    void testBadRequestPostTransferMoney_withdraw_InvalidAmount() throws Exception {
        //given
        WalletTransferRequestDTO walletTransferRequestDTO = WalletTransferRequestDTO.builder()
                .id(uuid)
                .operationType(OperationType.WITHDRAW)
                .amount(BigDecimal.valueOf(40000))
                .build();

        BigDecimal expectedBalance = walletRepository.findById(uuid).get().getBalance();
        //when
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletTransferRequestDTO))
        //then
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.errorCode").value("400"),
                jsonPath("$.errorMessage").value(
                                "Сумма списания больше баланса кошелька с id "+ wallet.getId())
        );

        BigDecimal actualBalance = walletRepository.findById(uuid).get().getBalance();

        assertEquals(expectedBalance, actualBalance);
    }
}