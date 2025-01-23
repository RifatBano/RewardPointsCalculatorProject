package com.infy.RewardPointCalculator.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.infy.RewardPointCalculator.Util.UserUtil;
import com.infy.RewardPointCalculator.dto.CustomerTransactionDTO;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.CustomerTransaction;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.service.CustomerTransactionService;
import jakarta.validation.Valid;

/**
 * CustomerTransactionController handles all customer transactions :get, add,
 * edit and delete for the authenticated user.
 * 
 * @author rifat.bano
 */
@RestController
@RequestMapping("/api/customers/transactions")
public class CustomerTransactionController {

	private static final Logger log = LoggerFactory.getLogger(CustomerTransactionController.class);

	@Autowired
	private CustomerTransactionService transactionService;

	@Autowired
	private CustomerRepository customerRepository; // To fetch customer details by email

	/**
	 * Get all transactions for the logged-in user.
	 * 
	 * This endpoint fetches all transactions for the currently authenticated user
	 * by retrieving their transactions from the transaction service and returns
	 * them in the response body.
	 * 
	 * @return ResponseEntity with a list of customer transactions or an error
	 *         message if an issue occurs
	 */
	@GetMapping
	public ResponseEntity<List<CustomerTransaction>> getTransactions() {
		String loggedInUsername = UserUtil.getLoggedInUsername(); // Get logged-in username

		if (loggedInUsername == null) {
			log.warn("Attempted to access transactions with no user logged in.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // If no user is logged in
		}

		try {
			// Find the customer by email (username)
			Customer customer = customerRepository.findByEmail(loggedInUsername).orElseThrow(() -> {
				log.error("Customer not found with email: {}", loggedInUsername);
				return new RuntimeException("Customer not found");
			});

			List<CustomerTransaction> transactions = transactionService.getTransactions(customer.getId());
			log.info("Fetched {} transactions for customer with email: {}", transactions.size(), loggedInUsername);
			return ResponseEntity.ok(transactions);

		} catch (Exception e) {
			log.error("Error occurred while fetching transactions for user with email: {}", loggedInUsername, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Add a new transaction for the logged-in user.
	 * 
	 * This endpoint allows the logged-in user to add a new transaction by
	 * submitting transaction details in the request body. The transaction is added
	 * via the transaction service.
	 * 
	 * @param transactionDTO Data transfer object containing the transaction details
	 * @return ResponseEntity with the newly created transaction or an error message
	 *         if the operation fails
	 */
	@PostMapping
	public ResponseEntity<CustomerTransaction> addTransaction(
			@RequestBody @Valid CustomerTransactionDTO transactionDTO) {
		String loggedInUsername = UserUtil.getLoggedInUsername(); // Get logged-in username

		if (loggedInUsername == null) {
			log.warn("Attempted to add transaction with no user logged in.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // If no user is logged in
		}

		try {
			// Find the customer by email (username)
			Customer customer = customerRepository.findByEmail(loggedInUsername).orElseThrow(() -> {
				log.error("Customer not found with email: {}", loggedInUsername);
				return new RuntimeException("Customer not found");
			});

			CustomerTransaction transaction = transactionService.addTransaction(customer.getId(), transactionDTO);
			log.info("Transaction added for customer with email: {}", loggedInUsername);
			return ResponseEntity.status(HttpStatus.CREATED).body(transaction);

		} catch (Exception e) {
			log.error("Error occurred while adding transaction for user with email: {}", loggedInUsername, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Edit an existing transaction for the logged-in user.
	 * 
	 * This endpoint allows the logged-in user to edit an existing transaction by
	 * providing the transaction ID and updated details in the request body.
	 * 
	 * @param transactionId  The ID of the transaction to be updated
	 * @param transactionDTO Data transfer object containing the updated transaction
	 *                       details
	 * @return ResponseEntity with the updated transaction or an error message if
	 *         the operation fails
	 */
	@PutMapping("/{transactionId}")
	public ResponseEntity<CustomerTransaction> editTransaction(@PathVariable Long transactionId,
			@RequestBody @Valid CustomerTransactionDTO transactionDTO) {
		String loggedInUsername = UserUtil.getLoggedInUsername(); // Get logged-in username

		if (loggedInUsername == null) {
			log.warn("Attempted to edit transaction with no user logged in.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // If no user is logged in
		}

		try {
			// Find the customer by email (username)
			Customer customer = customerRepository.findByEmail(loggedInUsername).orElseThrow(() -> {
				log.error("Customer not found with email: {}", loggedInUsername);
				return new RuntimeException("Customer not found");
			});

			CustomerTransaction updatedTransaction = transactionService.editTransaction(customer.getId(), transactionId,
					transactionDTO);
			log.info("Transaction with ID: {} updated for customer with email: {}", transactionId, loggedInUsername);
			return ResponseEntity.ok(updatedTransaction);

		} catch (Exception e) {
			log.error("Error occurred while editing transaction with ID: {} for user with email: {}", transactionId,
					loggedInUsername, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Delete an existing transaction for the logged-in user.
	 * 
	 * This endpoint allows the logged-in user to delete a transaction by providing
	 * the transaction ID. The transaction is removed via the transaction service.
	 * 
	 * @param transactionId The ID of the transaction to be deleted
	 * @return ResponseEntity with a no-content response or an error message if the
	 *         operation fails
	 */

	@DeleteMapping("/{transactionId}")
	public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
		String loggedInUsername = UserUtil.getLoggedInUsername(); // Get logged-in username

		if (loggedInUsername == null) {
			log.warn("Attempted to delete transaction with no user logged in.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // If no user is logged in
		}

		try {
			// Find the customer by email (username)
			Customer customer = customerRepository.findByEmail(loggedInUsername).orElseThrow(() -> {
				log.error("Customer not found with email: {}", loggedInUsername);
				return new RuntimeException("Customer not found");
			});

			transactionService.deleteTransaction(customer.getId(), transactionId);
			log.info("Transaction with ID: {} deleted for customer with email: {}", transactionId, loggedInUsername);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

		} catch (Exception e) {
			log.error("Error occurred while deleting transaction with ID: {} for user with email: {}", transactionId,
					loggedInUsername, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
}
