package com.fintrack.transactionservice.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.transactionservice.dto.*;
import com.fintrack.transactionservice.entity.TransactionCategory;
import com.fintrack.transactionservice.entity.TransactionType;
import com.fintrack.transactionservice.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@Valid @RequestBody CreateTransactionRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        ApiResponse<TransactionResponse> response = transactionService.createTransaction(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        ApiResponse<TransactionResponse> response = transactionService.getTransactionById(id, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "transactionDate") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir,
        HttpServletRequest httpRequest) {

            Long userId = getUserIdFromRequest(httpRequest);

            Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);

            ApiResponse<Page<TransactionResponse>> response = transactionService.getAllTransactions(userId, pageable);

            return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionsByType(
        @PathVariable TransactionType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "transactionDate") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir,
        HttpServletRequest httpRequest) {

            Long userId = getUserIdFromRequest(httpRequest);

            Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);

            ApiResponse<Page<TransactionResponse>> response = transactionService.getTransactionsByType(userId, type, pageable);

            return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionsByCategory(
        @PathVariable TransactionCategory category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "transactionDate") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir,
        HttpServletRequest httpRequest) {

            Long userId = getUserIdFromRequest(httpRequest);

            Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);

            ApiResponse<Page<TransactionResponse>> response = transactionService.getTransactionsByCategory(userId, category, pageable);

            return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        ApiResponse<Page<TransactionResponse>> response = 
                transactionService.getTransactionsByDateRange(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        ApiResponse<TransactionResponse> response = 
                transactionService.updateTransaction(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        ApiResponse<Void> response = transactionService.deleteTransaction(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getTransactionSummary(
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        ApiResponse<TransactionSummaryResponse> response = 
                transactionService.getTransactionSummary(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transaction Service is running!");
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        UserContext userContext = (UserContext) request.getAttribute("userContext");
        if (userContext == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userContext.getUserId();
    }
}
