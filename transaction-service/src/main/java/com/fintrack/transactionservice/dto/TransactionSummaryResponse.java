package com.fintrack.transactionservice.dto;

import java.math.BigDecimal;
import java.util.Map;

public class TransactionSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private Long totalTransactions;
    private Map<String, BigDecimal> spendingByCategory;

    // Constructors
    public TransactionSummaryResponse() {
    }

    public TransactionSummaryResponse(BigDecimal totalIncome, BigDecimal totalExpense, 
                                     BigDecimal netBalance, Long totalTransactions,
                                     Map<String, BigDecimal> spendingByCategory) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.totalTransactions = totalTransactions;
        this.spendingByCategory = spendingByCategory;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netBalance;
        private Long totalTransactions;
        private Map<String, BigDecimal> spendingByCategory;

        public Builder totalIncome(BigDecimal totalIncome) {
            this.totalIncome = totalIncome;
            return this;
        }

        public Builder totalExpense(BigDecimal totalExpense) {
            this.totalExpense = totalExpense;
            return this;
        }

        public Builder netBalance(BigDecimal netBalance) {
            this.netBalance = netBalance;
            return this;
        }

        public Builder totalTransactions(Long totalTransactions) {
            this.totalTransactions = totalTransactions;
            return this;
        }

        public Builder spendingByCategory(Map<String, BigDecimal> spendingByCategory) {
            this.spendingByCategory = spendingByCategory;
            return this;
        }

        public TransactionSummaryResponse build() {
            return new TransactionSummaryResponse(totalIncome, totalExpense, netBalance,
                                                 totalTransactions, spendingByCategory);
        }
    }

    // Getters and Setters
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Map<String, BigDecimal> getSpendingByCategory() {
        return spendingByCategory;
    }

    public void setSpendingByCategory(Map<String, BigDecimal> spendingByCategory) {
        this.spendingByCategory = spendingByCategory;
    }
}