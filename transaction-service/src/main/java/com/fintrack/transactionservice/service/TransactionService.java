package com.fintrack.transactionservice.service;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.transactionservice.dto.*;
import com.fintrack.transactionservice.entity.Transaction;
import com.fintrack.transactionservice.entity.TransactionCategory;
import com.fintrack.transactionservice.entity.TransactionType;
import com.fintrack.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {
    // Private methods
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;  // Database operations

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .merchant(transaction.getMerchant())
                .accountNumber(transaction.getAccountNumber())
                .notes(transaction.getNotes())
                .status(transaction.getStatus())
                .referenceNumber(transaction.getReferenceNumber())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    private String generateReferenceNumber() {
        return "TXN-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
    }

    
    // Public methods
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public ApiResponse<TransactionResponse> createTransaction(CreateTransactionRequest request, Long userId) {
        log.info("Creating transaction for user: {}", userId);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .merchant(request.getMerchant())
                .accountNumber(request.getAccountNumber())
                .notes(request.getNotes())
                .referenceNumber(generateReferenceNumber())
                .build();
        
        transaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", transaction.getId());

        TransactionResponse response = mapToResponse(transaction);
        return ApiResponse.success("Transaction created successfully", response);
    }

    public ApiResponse<TransactionResponse> getTransactionById(Long transactionId, Long userId) {
        log.info("Fetching transaction ID: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Verify transaction belongs to user
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        TransactionResponse response = mapToResponse(transaction);
        return ApiResponse.success(response);
    }

    public ApiResponse<Page<TransactionResponse>> getAllTransactions(Long userId, Pageable pageable) {
        log.info("Fetching all transactions for user: {}", userId);

        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        Page<TransactionResponse> response = transactions.map(this::mapToResponse);

        return ApiResponse.success(response);
    }

    public ApiResponse<Page<TransactionResponse>> getTransactionsByType(Long userId, TransactionType type, Pageable pageable) {
        log.info("Fetching {} transactions for user: {}", type, userId);

        Page<Transaction> transactions = transactionRepository.findByUserIdAndType(userId, type, pageable);
        Page<TransactionResponse> response = transactions.map(this::mapToResponse);

        return ApiResponse.success(response);
    }

    public ApiResponse<Page<TransactionResponse>> getTransactionsByCategory(Long userId, TransactionCategory category, Pageable pageable) {
        log.info("Fetching transactions in category {} for users: {}", category, userId);

        Page<Transaction> transactions = transactionRepository.findByUserIdAndCategory(userId, category, pageable);

        Page<TransactionResponse> response = transactions.map(this::mapToResponse);

        return ApiResponse.success(response);
    }

    public ApiResponse<Page<TransactionResponse>> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching transactions for user: {} between {} and {}", userId, startDate, endDate);

        Page<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate, pageable);
        
        Page<TransactionResponse> response = transactions.map(this::mapToResponse);

        return ApiResponse.success(response);
    }

    @Transactional 
    public ApiResponse<TransactionResponse> updateTransaction(Long transactionId, UpdateTransactionRequest request, Long userId) {
        log.info("Updating transaction ID: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Verify transaction belongs to user
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        // Update only non-null fields
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getCategory() != null) {
            transaction.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        if (request.getMerchant() != null) {
            transaction.setMerchant(request.getMerchant());
        }
        if (request.getAccountNumber() != null) {
            transaction.setAccountNumber(request.getAccountNumber());
        }
        if (request.getNotes() != null) {
            transaction.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            transaction.setStatus(request.getStatus());
        }

        // Save to database
        transaction = transactionRepository.save(transaction);
        log.info("Transaction updated successfully: {}", transactionId);

        TransactionResponse response = mapToResponse(transaction);

        return ApiResponse.success("Transaction updated successfully", response);
    }

    @Transactional
    public ApiResponse<Void> deleteTransaction(Long transactionId, Long userId) {
        log.info("Deleting transaction ID: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Verify transaction belongs to user
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully: {}", transactionId);

        return ApiResponse.success("Transaction deleted successfully", null);
    }

    public ApiResponse<TransactionSummaryResponse> getTransactionSummary(Long userId) {
        log.info("Calculating transaction summary for user: {}", userId);

        BigDecimal totalIncome = transactionRepository.calculateTotalByUserIdAndType(userId, TransactionType.INCOME);

        BigDecimal totalExpense = transactionRepository.calculateTotalByUserIdAndType(userId, TransactionType.EXPENSE);

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        Long totalTransactions = transactionRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();

        // Get spending by category
        List<Object[]> spendingData = transactionRepository.getSpendingByCategory(userId);
        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        for (Object[] row: spendingData) {
            TransactionCategory category = (TransactionCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            spendingByCategory.put(category.name(), amount);
        }

        TransactionSummaryResponse summary = TransactionSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .totalTransactions(totalTransactions)
                .spendingByCategory(spendingByCategory)
                .build();

        return ApiResponse.success(summary);
    }

    

}
