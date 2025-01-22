package com.infy.RewardPointCalculator.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infy.RewardPointCalculator.model.Customer;

/**
 * Repository interface for managing `Customer` entities in the database.
 * 
 * This interface extends `JpaRepository` to provide CRUD operations for the `Customer` entity,
 * and it includes a custom query method for finding a customer by their email address.
 * 
 * The repository is used for interacting with the customer data in the database, such as retrieving customer 
 * details, saving new customers, updating existing customers, and deleting customers.
 * @author rifat.bano
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	/**
     * Finds a customer by their email address.
     * 
     * This method searches the database for a customer that matches the provided email.
     * If no customer is found with that email, it returns an empty `Optional`.
     * 
     * @param email The email address of the customer to search for.
     * @return An `Optional<Customer>` containing the customer if found, otherwise empty.
     */
	Optional<Customer> findByEmail(String email);
}
