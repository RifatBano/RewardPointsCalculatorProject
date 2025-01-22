package com.infy.RewardPointCalculator.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infy.RewardPointCalculator.model.CustomerTransaction;

/**
 * Repository interface for managing `CustomerTransaction` entities in the database.
 * 
 * This interface extends `JpaRepository` to provide CRUD operations for the `CustomerTransaction` entity.
 * It includes custom query methods for retrieving transactions based on various filters, such as customer ID, 
 * transaction ID, and transaction dates.
 * 
 * The repository is used for interacting with the customer transaction data in the database, including fetching 
 * transactions for a specific customer, filtering transactions by date, and retrieving individual transactions by 
 * both customer ID and transaction ID.
 * @author rifat.bano
 */
@Repository
public interface CustomerTransactionRepository extends JpaRepository<CustomerTransaction, Long> {
	/**
     * Finds all transactions associated with a specific customer by their customer ID.
     * 
     * @param customerId The ID of the customer whose transactions are to be retrieved.
     * @return A list of `CustomerTransaction` objects related to the specified customer.
     */
	List<CustomerTransaction> findByCustomerId(Long customerId);
	
	/**
     * Finds a specific transaction for a customer based on customer ID and transaction ID.
     * 
     * This method retrieves a single transaction for a customer if both the customer ID and transaction ID match.
     * 
     * @param customerId The ID of the customer whose transaction is to be retrieved.
     * @param transactionId The ID of the specific transaction to be retrieved.
     * @return An `Optional<CustomerTransaction>` containing the transaction if found, or empty if not found.
     */
	Optional<CustomerTransaction> findByCustomerIdAndId(Long customerId, Long transactionId);
	
	/**
     * Finds all transactions for a customer that occurred between two specific dates.
     * 
     * @param customerId The ID of the customer whose transactions are to be retrieved.
     * @param of The start date of the date range.
     * @param withDayOfMonth The end date of the date range.
     * @return A list of `CustomerTransaction` objects that occurred between the provided dates for the given customer.
     */
	List<CustomerTransaction> findByCustomerIdAndDateBetween(Long customerId, LocalDate of, LocalDate withDayOfMonth);

	}
