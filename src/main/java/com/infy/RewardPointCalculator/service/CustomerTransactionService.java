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

    private static final Logger log = LoggerFactory.getLogger(CustomerTransactionService.class);

    @Autowired
    private CustomerTransactionRepository transactionRepository;

    @Autowired
    private RewardPointsRepository rewardPointsRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * This method fetches all transactions for a given customer by their customer ID.
     * If no transactions are found, an exception is thrown. If an error occurs during
     * the transaction retrieval, an exception is thrown with an appropriate error message.
     *
     * @param customerId The ID of the customer whose transactions are to be fetched.
     * @return A list of CustomerTransaction objects associated with the specified customer ID.
     */    
    public List<CustomerTransaction> getTransactions(Long customerId) {
        try {
            List<CustomerTransaction> transactions = transactionRepository.findByCustomerId(customerId);
            if (transactions.isEmpty()) {
                log.info("No transactions found for customerId: {}", customerId);  // Add this line for debugging
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transactions found for this customer");
            }
            return transactions;
        } catch (Exception e) {
            log.error("Error occurred while fetching transactions for customerId: {}", customerId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while fetching transactions", e);
        }
    }

    /**
     * This method adds a new transaction for a customer and updates their reward points.
     * It first checks if the customer exists, then creates the transaction and updates 
     * the associated reward points based on the transaction amount.
     * If an error occurs during the process, an exception is thrown.
     *
     * @param customerId The ID of the customer to add the transaction for.
     * @param transactionDTO Data Transfer Object containing the transaction details.
     * @return The created CustomerTransaction object.
     */
    public CustomerTransaction addTransaction(Long customerId, CustomerTransactionDTO transactionDTO) {
        try {
        	
            // Check if the customer already has a transaction
//        	List<CustomerTransaction> existingTransactions = transactionRepository.findByCustomerId(customerId);
//            if (!existingTransactions.isEmpty()) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer already has a transaction. New transaction cannot be added.");
//            }
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

    /**
     * This method is responsible for editing an existing transaction for a customer.
     * It retrieves the existing transaction using the customerId and transactionId, 
     * updates its details with the provided data, and adjusts the reward points 
     * based on the updated transaction.
     *
     * @param customerId The ID of the customer whose transaction needs to be edited.
     * @param transactionId The ID of the transaction to be edited.
     * @param transactionDTO The DTO containing the updated transaction details.
     * @return The updated CustomerTransaction object.
     */
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

    /**
     * This method deletes a specific transaction for a customer and adjusts their reward points 
     * based on the transaction deletion. It first retrieves the customer and transaction, 
     * then modifies the customer's reward points before deleting the transaction from the repository.
     *
     * @param customerId The ID of the customer whose transaction needs to be deleted.
     * @param transactionId The ID of the transaction to be deleted.
     */
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

    /**
     * This method retrieves a customer by their unique customerId from the repository.
     * If no customer is found with the provided customerId, a RuntimeException is thrown 
     * with an appropriate error message.
     *
     * @param customerId The ID of the customer to be retrieved.
     * @return The Customer object corresponding to the provided customerId.
     * @throws RuntimeException If no customer is found with the given customerId.
     */
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    /**
     * Creates a new transaction for the specified customer using the provided transaction details (DTO).
     * The transaction is then saved to the database.
     * 
     * @param customer The customer for whom the transaction is being created.
     * @param transactionDTO The data transfer object (DTO) containing the transaction details.
     * @return The saved CustomerTransaction object after being persisted in the database.
     */
    private CustomerTransaction createTransaction(Customer customer, CustomerTransactionDTO transactionDTO) {
        CustomerTransaction transaction = new CustomerTransaction();
        transaction.setCustomer(customer);
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setSpentDetails(transactionDTO.getSpentDetails());
        transaction.setDate(transactionDTO.getTransactionDate());
        CustomerTransaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction created for customerId: {} with amount: {}", customer.getId(), savedTransaction.getAmount());
        return savedTransaction;

    }

    /**
     * Updates the details of an existing transaction with the information provided in the transactionDTO.
     * The updated transaction is then saved to the database.
     * 
     * @param transaction The existing transaction to be updated.
     * @param transactionDTO The data transfer object (DTO) containing the updated transaction details.
     */
    private void updateTransactionDetails(CustomerTransaction transaction, CustomerTransactionDTO transactionDTO) {
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setSpentDetails(transactionDTO.getSpentDetails());
        transaction.setDate(transactionDTO.getTransactionDate());
        transactionRepository.save(transaction);
        log.info("Transaction updated for transactionId: {}", transaction.getId());

    }

    /**
     * Retrieves a transaction by its ID for a specific customer.
     * If the transaction is not found, a RuntimeException is thrown.
     * 
     * @param customerId The ID of the customer whose transaction is being retrieved.
     * @param transactionId The ID of the transaction to be retrieved.
     * @return The transaction associated with the specified customer and transaction ID.
     * @throws RuntimeException If no transaction is found for the given customer and transaction ID.
     */
    private CustomerTransaction getTransactionById(Long customerId, Long transactionId) {
        return transactionRepository.findByCustomerIdAndId(customerId, transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    /**
     * Updates the reward points for a customer based on a transaction.
     * It calculates the points for the transaction and updates the reward points 
     * for the corresponding month and year. The reward points are saved in the repository.
     * Additionally, an asynchronous process is triggered to update the reward points.
     * 
     * @param customer The customer for whom the reward points are being updated.
     * @param transaction The transaction details used to calculate reward points.
     */
    private void updateRewardPointsForTransaction(Customer customer, CustomerTransaction transaction) {
        int month = transaction.getDate().getMonthValue();
        int year = transaction.getDate().getYear();
        int points = RewardPointCalculator.calculatePoints(transaction.getAmount());

        RewardPoints rewardPoints = getOrCreateRewardPoints(customer, month, year);
        rewardPoints.setPoints(rewardPoints.getPoints() + points);
        rewardPointsRepository.save(rewardPoints);

        updateRewardPointsAsync(customer.getId(), month, year);
        log.info("Reward points updated for customerId: {} for month: {} and year: {}", customer.getId(), month, year);

    }

    /**
     * Adjusts the reward points for a customer after deleting a transaction.
     * This method recalculates the reward points by subtracting the points associated 
     * with the deleted transaction from the customer's total reward points for the 
     * specific month and year.
     * 
     * @param customer The customer whose reward points need to be adjusted.
     * @param transaction The transaction that was deleted, used to determine the points to be subtracted.
     */
    private void adjustRewardPointsForDeletion(Customer customer, CustomerTransaction transaction) {
        int points = RewardPointCalculator.calculatePoints(transaction.getAmount());
        List<RewardPoints> rewardPointsList = rewardPointsRepository.findByCustomerAndMonthAndYear(customer,
                transaction.getDate().getMonthValue(), transaction.getDate().getYear());

        if (!rewardPointsList.isEmpty()) {
            RewardPoints rewardPoints = rewardPointsList.get(0);
            rewardPoints.setPoints(rewardPoints.getPoints() - points);
            rewardPointsRepository.save(rewardPoints);
        }
        log.info("Reward points adjusted for customerId: {} after transaction deletion", customer.getId());

    }

    /**
     * Retrieves the existing reward points for a customer for the given month and year,
     * or creates a new reward points entry with zero points if no such entry exists.
     * 
     * This method checks if a reward points record exists for the customer in the specified
     * month and year. If no such record exists, a new entry is created with zero points.
     * 
     * @param customer The customer whose reward points are being retrieved or created.
     * @param month The month for which the reward points are being checked or created.
     * @param year The year for which the reward points are being checked or created.
     * @return The existing or newly created RewardPoints object for the specified customer, month, and year.
     */
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

    /**
     * Asynchronously updates the reward points for a customer for a given month and year.
     * 
     * This method is annotated with @Async to ensure that the reward points update operation is executed
     * in a separate thread, allowing the main thread to continue processing other tasks without waiting
     * for the operation to complete. The method calls the updateRewardPoints method to perform the actual update.
     * 
     * @param customerId The ID of the customer whose reward points are being updated.
     * @param month The month for which the reward points need to be updated.
     * @param year The year for which the reward points need to be updated.
     */
    @Async
    public void updateRewardPointsAsync(Long customerId, Integer month, Integer year) {
        // Perform the long-running task of updating reward points asynchronously
        updateRewardPoints(customerId, month, year);
    }

    /**
     * Updates the reward points for a customer for a given month and year based on their transactions.
     * 
     * This method fetches the customer's transactions for the specified month and year, calculates 
     * the total points from these transactions, and updates or creates a `RewardPoints` entry with 
     * the calculated points. The points are computed using the `RewardPointCalculator` utility.
     * 
     * @param customerId The ID of the customer whose reward points need to be updated.
     * @param month The month for which the reward points need to be updated.
     * @param year The year for which the reward points need to be updated.
     */
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
