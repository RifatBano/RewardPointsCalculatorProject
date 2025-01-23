package com.infy.RewardPointCalculator.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.RewardPoints;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.repository.RewardPointsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is RewardPointsService. It has two different methods. First method
 * getRewardPoints gives the total point of that month and year which customer
 * has selected Second method getAllRewardPoints gives total points aggregated
 * 
 * @author rifat.bano
 */

@Service
public class RewardPointsService {

	private static final Logger log = LoggerFactory.getLogger(RewardPointsService.class);

	@Autowired
	private RewardPointsRepository rewardPointsRepository;
	@Autowired
	private CustomerRepository customerRepository;

	/**
	 * Retrieves the aggregated reward points for a customer for a given month and
	 * year.
	 * 
	 * This method fetches the customer's reward points for the specified month and
	 * year. If no points are found, it returns a `RewardPoints` object with zero
	 * points. If multiple entries for the same month and year exist, it aggregates
	 * their points and returns the total.
	 * 
	 * @param customerId The ID of the customer for whom the reward points are being
	 *                   fetched.
	 * @param month      The month for which the reward points need to be fetched.
	 * @param year       The year for which the reward points need to be fetched.
	 * @return A `RewardPoints` object representing the total points for the
	 *         customer for the specified month and year.
	 * @throws ResponseStatusException If an error occurs during the retrieval
	 *                                 process, such as data integrity issues or
	 *                                 general errors.
	 */
	public RewardPoints getRewardPoints(Long customerId, Integer month, Integer year) {
		try {
			log.info("Fetching reward points for customerId: {}, month: {}, year: {}", customerId, month, year);

			// Fetch customer
			Customer customer = customerRepository.findById(customerId).orElseThrow(() -> {
				log.error("Customer not found with customerId: {}", customerId);
				return new RuntimeException("Customer not found");
			});

			// Fetch reward points for the given customer, month, and year
			List<RewardPoints> rewardPointsList = rewardPointsRepository.findByCustomerAndMonthAndYear(customer, month,
					year);

			if (rewardPointsList.isEmpty()) {
				log.warn("No reward points found for customerId: {}, month: {}, year: {}", customerId, month, year);
				return new RewardPoints(null, customer, 0, month, year); // Return zero points if not found
			}

			// Aggregate the points
			int totalPoints = rewardPointsList.stream().mapToInt(RewardPoints::getPoints).sum();

			// Prepare aggregated reward points
			RewardPoints aggregatedRewardPoints = new RewardPoints();
			aggregatedRewardPoints.setCustomer(customer);
			aggregatedRewardPoints.setPoints(totalPoints);
			aggregatedRewardPoints.setMonth(month);
			aggregatedRewardPoints.setYear(year);

			log.info("Successfully fetched aggregated reward points for customerId: {}, month: {}, year: {}: {}",
					customerId, month, year, totalPoints);

			return aggregatedRewardPoints;

		} catch (DataIntegrityViolationException e) {
			log.error("Data integrity violation while retrieving reward points for customerId: {}, month: {}, year: {}",
					customerId, month, year, e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Data integrity violation while retrieving reward points", e);
		} catch (Exception e) {
			log.error("An error occurred while retrieving reward points for customerId: {}, month: {}, year: {}",
					customerId, month, year, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"An error occurred while retrieving reward points", e);
		}
	}

	/**
	 * Retrieves all reward points for a customer.
	 * 
	 * This method fetches all reward points associated with the given customer. If
	 * no reward points are found, it logs a warning. It also handles data retrieval
	 * errors and ensures appropriate logging and exception handling.
	 * 
	 * @param customerId The ID of the customer for whom the reward points are being
	 *                   fetched.
	 * @return A list of `RewardPoints` objects associated with the given customer.
	 * @throws ResponseStatusException If an error occurs during data retrieval,
	 *                                 such as data integrity issues or general
	 *                                 errors.
	 */
	public List<RewardPoints> getAllRewardPoints(Long customerId) {
		try {
			log.info("Fetching all reward points for customerId: {}", customerId);

			// Fetch customer
			Customer customer = customerRepository.findById(customerId).orElseThrow(() -> {
				log.error("Customer not found with customerId: {}", customerId);
				return new RuntimeException("Customer not found");
			});

			// Fetch all reward points for the customer
			List<RewardPoints> rewardPoints = rewardPointsRepository.findByCustomer(customer);

			if (rewardPoints.isEmpty()) {
				log.warn("No reward points found for customerId: {}", customerId);
			}

			log.info("Successfully fetched {} reward points for customerId: {}", rewardPoints.size(), customerId);
			return rewardPoints;

		} catch (DataIntegrityViolationException e) {
			log.error("Data integrity violation while retrieving all reward points for customerId: {}", customerId, e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Data integrity violation while retrieving all reward points", e);
		} catch (Exception e) {
			log.error("An error occurred while retrieving all reward points for customerId: {}", customerId, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"An error occurred while retrieving all reward points", e);
		}
	}
}
