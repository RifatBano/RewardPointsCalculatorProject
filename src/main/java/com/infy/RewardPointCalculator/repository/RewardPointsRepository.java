package com.infy.RewardPointCalculator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.RewardPoints;

/**
 * Repository interface for managing `RewardPoints` entities in the database.
 * 
 * This interface extends `JpaRepository` to provide CRUD operations for the
 * `RewardPoints` entity. It includes custom query methods for retrieving reward
 * points based on the associated customer and filtering by customer, month, and
 * year.
 * 
 * The repository is used for interacting with the reward points data in the
 * database, such as fetching the total reward points for a specific customer or
 * retrieving reward points for a specific customer for a particular month and
 * year.
 * 
 * @author rifat.bano
 */
@Repository
public interface RewardPointsRepository extends JpaRepository<RewardPoints, Long> {
	/**
	 * Finds all reward points associated with a specific customer.
	 * 
	 * @param customer The customer whose reward points are to be retrieved.
	 * @return A list of `RewardPoints` objects associated with the specified
	 *         customer.
	 */
	List<RewardPoints> findByCustomer(Customer customer);

	/**
	 * Finds reward points associated with a specific customer for a given month and
	 * year.
	 * 
	 * This method retrieves all reward points for a customer in a specific month
	 * and year.
	 * 
	 * @param customer The customer whose reward points are to be retrieved.
	 * @param month    The month for which the reward points are to be fetched.
	 * @param year     The year for which the reward points are to be fetched.
	 * @return A list of `RewardPoints` objects that match the provided customer,
	 *         month, and year.
	 */
	List<RewardPoints> findByCustomerAndMonthAndYear(Customer customer, Integer month, Integer year);

}
