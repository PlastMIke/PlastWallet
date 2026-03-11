package com.wallet.infrastructure.adapter;

import com.wallet.application.port.out.TransactionPort;
import com.wallet.domain.entity.Transaction;
import com.wallet.domain.entity.TransactionStatus;
import com.wallet.infrastructure.converter.TransactionConverter;
import com.wallet.infrastructure.persistence.entity.TransactionEntity;
import com.wallet.infrastructure.persistence.repository.JpaTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionAdapter implements TransactionPort {
    private final JpaTransactionRepository jpaTransactionRepository;
    private final TransactionConverter transactionConverter;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = transactionConverter.toEntity(transaction);
        TransactionEntity saved = jpaTransactionRepository.save(entity);
        return transactionConverter.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaTransactionRepository.findById(id).map(transactionConverter::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaTransactionRepository.deleteById(id);
    }

    @Override
    public List<Transaction> findByFromWalletId(UUID fromWalletId) {
        return jpaTransactionRepository.findByFromWalletId(fromWalletId).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByToWalletId(UUID toWalletId) {
        return jpaTransactionRepository.findByToWalletId(toWalletId).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByStatus(TransactionStatus status) {
        return jpaTransactionRepository.findByStatus(status).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByWalletId(UUID walletId) {
        return jpaTransactionRepository.findAllByWalletId(walletId).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByWalletIdAndStatus(UUID walletId, TransactionStatus status) {
        return jpaTransactionRepository.findAllByWalletIdAndStatus(walletId, status).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByDateRange(Instant startDate, Instant endDate) {
        return jpaTransactionRepository.findAllByDateRange(startDate, endDate).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByWalletIdAndDateRange(UUID walletId, Instant startDate, Instant endDate) {
        return jpaTransactionRepository.findAllByWalletIdAndDateRange(walletId, startDate, endDate).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return jpaTransactionRepository.findAllByAmountRange(minAmount, maxAmount).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByWalletIdAndAmountRange(UUID walletId, BigDecimal minAmount, BigDecimal maxAmount) {
        return jpaTransactionRepository.findAllByWalletIdAndAmountRange(walletId, minAmount, maxAmount).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByStatusAndDateRange(TransactionStatus status, Instant startDate, Instant endDate) {
        return jpaTransactionRepository.findAllByStatusAndDateRange(status, startDate, endDate).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByWalletId(UUID walletId) {
        return jpaTransactionRepository.countByWalletId(walletId);
    }

    @Override
    public long countByStatus(TransactionStatus status) {
        return jpaTransactionRepository.countByStatus(status);
    }

    @Override
    public long countByWalletIdAndStatus(UUID walletId, TransactionStatus status) {
        return jpaTransactionRepository.countByWalletIdAndStatus(walletId, status);
    }

    @Override
    public BigDecimal getTotalAmountByWalletId(UUID walletId) {
        return jpaTransactionRepository.getTotalAmountByWalletId(walletId);
    }

    @Override
    public BigDecimal getTotalAmountByStatus(TransactionStatus status) {
        return jpaTransactionRepository.getTotalAmountByStatus(status);
    }

    @Override
    public BigDecimal getTotalSentAmount(UUID walletId) {
        return jpaTransactionRepository.getTotalSentAmount(walletId);
    }

    @Override
    public BigDecimal getTotalReceivedAmount(UUID walletId) {
        return jpaTransactionRepository.getTotalReceivedAmount(walletId);
    }

    @Override
    public List<Transaction> findTopTransactionsByWalletId(UUID walletId, int limit) {
        return jpaTransactionRepository.findTopTransactionsByWalletId(walletId, PageRequest.of(0, limit)).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findRecentTransactions(int limit) {
        return jpaTransactionRepository.findRecentTransactions(PageRequest.of(0, limit)).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByWalletIdWithPagination(UUID walletId, int page, int size) {
        return jpaTransactionRepository.findByWalletIdWithPagination(walletId, PageRequest.of(page, size)).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByStatusAndCreatedAtBefore(TransactionStatus status, Instant date) {
        jpaTransactionRepository.deleteByStatusAndCreatedAtBefore(status, date);
    }

    @Override
    public List<Transaction> findOldTransactionsByStatus(TransactionStatus status, Instant date) {
        return jpaTransactionRepository.findOldTransactionsByStatus(status, date).stream()
                .map(transactionConverter::toDomain)
                .collect(Collectors.toList());
    }
}
