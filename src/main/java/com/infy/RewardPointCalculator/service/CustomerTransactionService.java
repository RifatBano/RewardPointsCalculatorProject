package com.infy.RewardPointCalculator.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.Util.RewardPointCalculator;
import com.infy.RewardPointCalculator.dto.CustomerTransactionDTO;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.CustomerTransaction;
import com.infy.RewardPointCalculator.model.RewardPoints;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.repository.CustomerTransactionRepository;
import com.infy.RewardPointCalculator.repository.RewardPointsRepository;

import jakarta.validation.Valid;


/**
 * This service handles all the customer transaction : get, add, edit, delete.
 * It has one async method updateRewardPointsAsync, which should trigger for the background 
 * calculation for every transaction changes happen.
 * @author rifat.bano
 */

@Service 
public class CustomerTransactionService {

    Logger log = LoggerFactory.getLogger(CustomerTransactionService.class);

    @Autowired
    private CustomerTransactionRepository transactionRepository;

    @Autowired
    private RewardPointsRepository rewardPointsRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Get all transactions for a specific customer
    public List<CustomerTransaction> getTransactions(Long customerId) {
        try {
            List<CustomerTransaction> transactions = transactionRepository.findByCustomerId(customerId);
            if (transactions.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transactions found for this customer");
            }
            return transactions;
        } catch (Exception e) {
            log.error("Error occurred while fetching transactions for customerId: {}", customerId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while fetching transactions", e);
        }
    }

    // Add a new transaction and calculate reward points
    public CustomerTransaction addTransaction(Long customerId, CustomerTransactionDTO transactionDTO) {
        try {
        	
            // Check if the customer already has a transaction
        	List<CustomerTransaction> existingTransactions = transactionRepository.findByCustomerId(customerId);
            if (!existingTransactions.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer already has a transaction. New transaction cannot be added.");
            }
            Customer customer = getCustomerById(customerId);
            CustomerTransaction transaction = createTransaction(customer, transactionDTO);
            updateRewardPointsForTransaction(customer, transaction);

            log.info("Transaction added for customerId: {}", customerId);
            return transaction;
        } catch (Exception e) {
            log.error("Error occurred while adding transaction for customerId: {}", customerId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while adding the transaction", e);
        }
    }

    // Edit an existing transaction
    public CustomerTransaction editTransaction(Long customerId, Long transactionId, @Valid CustomerTransactionDTO transactionDTO) {
        try {
            CustomerTransaction existingTransaction = getTransactionById(customerId, transactionId);
            updateTransactionDetails(existingTransaction, transactionDTO);
            updateRewardPointsForTransaction(existingTransaction.getCustomer(), existingTransaction);

            log.info("Transaction updated for customerId: {}", customerId);
            return existingTransaction;
        } catch (Exception e) {
            log.error("Error occurred while editing transaction for customerId: {}", customerId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while editing the transaction", e);
        }
    }

    // Delete a transaction and adjust reward points
    public void deleteTransaction(Long customerId, Long transactionId) {
        try {
            Customer customer = getCustomerById(customerId);
            CustomerTransaction transaction = getTransactionById(customerId, transactionId);
            adjustRewardPointsForDeletion(customer, transaction);
            transactionRepository.delete(transaction);

            log.info("Transaction deleted for customerId: {}", customerId);
        } catch (Exception e) {
            log.error("Error occurred while deleting transaction for customerId: {}", customerId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the transaction", e);
        }
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    private CustomerTransaction createTransaction(Customer customer, CustomerTransactionDTO transactionDTO) {
        CustomerTransaction transaction = new CustomerTransaction();
        transaction.setCustomer(customer);
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setSpentDetails(transactionDTO.getSpentDetails());
        transaction.setDate(transactionDTO.getTransactionDate());
        return transactionRepository.save(transaction);
    }

    private void updateTransactionDetails(CustomerTransaction transaction, CustomerTransactionDTO transactionDTO) {
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setSpentDetails(transactionDTO.getSpentDetails());
        transaction.setDate(transactionDTO.getTransactionDate());
        transactionRepository.save(transaction);
    }

    private CustomerTransaction getTransactionById(Long customerId, Long transactionId) {
        return transactionRepository.findByCustomerIdAndId(customerId, transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    private void updateRewardPointsForTransaction(Customer customer, CustomerTransaction transaction) {
        int month = transaction.getDate().getMonthValue();
        int year = transaction.getDate().getYear();
        int points = RewardPointCalculator.calculatePoints(transaction.getAmount());

        RewardPoints rewardPoints = getOrCreateRewardPoints(customer, month, year);
        rewardPoints.setPoints(rewardPoints.getPoints() + points);
        rewardPointsRepository.save(rewardPoints);

        updateRewardPointsAsync(customer.getId(), month, year);
    }

    private void adjustRewardPointsForDeletion(Customer customer, CustomerTransaction transaction) {
        int points = RewardPointCalculator.calculatePoints(transaction.getAmount());
        List<RewardPoints> rewardPointsList = rewardPointsRepository.findByCustomerAndMonthAndYear(customer,
                transaction.getDate().getMonthValue(), transaction.getDate().getYear());

        if (!rewardPointsList.isEmpty()) {
            RewardPoints rewardPoints = rewardPointsList.get(0);
            rewardPoints.setPoints(rewardPoints.getPoints() - points);
            rewardPointsRepository.save(rewardPoints);
        }
    }

    private RewardPoints getOrCreateRewardPoints(Customer customer, int month, int year) {
        List<RewardPoints> rewardPointsList = rewardPointsRepository.findByCustomerAndMonthAndYear(customer, month, year);
        if (rewardPointsList.isEmpty()) {
            RewardPoints rewardPoints = new RewardPoints();
            rewardPoints.setCustomer(customer);
            rewardPoints.setMonth(month);
            rewardPoints.setYear(year);
            rewardPoints.setPoints(0);  // Initialize with zero points if new entry
            return rewardPoints;
        }
        return rewardPointsList.get(0);
    }

    @Async
    public void updateRewardPointsAsync(Long customerId, Integer month, Integer year) {
        // Perform the long-running task of updating reward points asynchronously
        updateRewardPoints(customerId, month, year);
    }

    public void updateRewardPoints(Long customerId, Integer month, Integer year) {
        log.info("updateRewardPoints started running for customerId: {} in {}-{}", customerId, month, year);

        Customer customer = getCustomerById(customerId);
        List<CustomerTransaction> transactions = transactionRepository.findByCustomerIdAndDateBetween(
                customerId,
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth())
        );

        int totalPoints = transactions.stream()
                .mapToInt(transaction -> RewardPointCalculator.calculatePoints(transaction.getAmount()))
                .sum();

        RewardPoints rewardPoints = getOrCreateRewardPoints(customer, month, year);
        rewardPoints.setPoints(totalPoints);
        rewardPointsRepository.save(rewardPoints);

        log.info("updateRewardPoints finished running for customerId: {} in {}-{}", customerId, month, year);
    }
}
