package com.fintrack.transactionservice.repository;

import com.fintrack.transactionservice.entity.Transaction;
import com.fintrack.transactionservice.entity.TransactionCategory;
import com.fintrack.transactionservice.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Transaction database operations

    // Find all transactions for a user
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    // Find transactions by user and type
    Page<Transaction> findByUserIdAndType(Long userId, TransactionType type, Pageable pageable);

    // Find transactions by user and category
    Page<Transaction> findByUserIdAndCategory(Long userId, TransactionCategory category, Pageable pageable);

    // Find transactions within date range
    Page<Transaction> findByUserIdAndTransactionDateBetween(
        Long userId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    // Calculate total by type for a user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " + 
           "WHERE t.userId = :userId AND t.type = :type")
    BigDecimal calculateTotalByUserIdAndType(
        @Param("userId") Long userId,
        @Param("type") TransactionType type
    );

    // Get spending by category for a user
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.type = com.fintrack.transactionservice.entity.TransactionType.EXPENSE " +
           "GROUP BY t.category " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getSpendingByCategory(@Param("userId") Long userId);
}
