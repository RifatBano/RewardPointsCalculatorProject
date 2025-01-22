package com.infy.RewardPointCalculator.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.infy.RewardPointCalculator.Util.UserUtil;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.RewardPoints;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.service.RewardPointsService;

/**
 * RewardPointController handles get apis to get rewardpoint details month and yearwise 
 * or the aggregated points for the authenticated user
 * @author rifat.bano
 */


@RestController
@RequestMapping("/api/customers/reward-points")
public class RewardPointController {

    private static final Logger log = LoggerFactory.getLogger(RewardPointController.class);

    @Autowired
    private RewardPointsService rewardPointsService;

    @Autowired
    private CustomerRepository customerRepository;  // To fetch customer details

    /**
     * Get reward points for the logged-in customer for a specific month and year.
     * 
     * This endpoint retrieves the reward points for a customer based on their logged-in username (email), 
     * and the specified month and year. If the user is not logged in or an error occurs, an appropriate response is returned.
     * 
     * @param month The month for which the reward points are to be fetched (1-12).
     * @param year The year for which the reward points are to be fetched.
     * @return ResponseEntity with the reward points data or an error response if any issues occur
     */
    @GetMapping("/{month}/{year}")
    public ResponseEntity<RewardPoints> getRewardPoints(@PathVariable Integer month, @PathVariable Integer year) {
        String loggedInUsername = UserUtil.getLoggedInUsername();  // Get logged-in username

        if (loggedInUsername == null) {
            log.warn("Attempted to fetch reward points without being logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // If no user is logged in
        }

        try {
            // Find the customer by email (username)
            Customer customer = customerRepository.findByEmail(loggedInUsername)
                    .orElseThrow(() -> {
                        log.error("Customer not found for email: {}", loggedInUsername);
                        return new RuntimeException("Customer not found");
                    });

            // Fetch reward points for the authenticated user
            RewardPoints rewardPoints = rewardPointsService.getRewardPoints(customer.getId(), month, year);
            log.info("Fetched reward points for customer with email: {} for {}/{}", loggedInUsername, month, year);
            return ResponseEntity.ok(rewardPoints);

        } catch (Exception e) {
            log.error("Error occurred while fetching reward points for user with email: {} for {}/{}", loggedInUsername, month, year, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get all reward points for the logged-in customer.
     * 
     * This endpoint retrieves all reward points accrued by the logged-in customer. 
     * If the user is not logged in, it returns an Unauthorized response.
     * 
     * @return ResponseEntity with a list of all reward points or an error response if any issues occur
     */
    @GetMapping("/all")
    public ResponseEntity<List<RewardPoints>> getAllRewardPoints() {
        String loggedInUsername = UserUtil.getLoggedInUsername();  // Get logged-in username

        if (loggedInUsername == null) {
            log.warn("Attempted to fetch all reward points without being logged in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);  // If no user is logged in
        }

        try {
            // Find the customer by email (username)
            Customer customer = customerRepository.findByEmail(loggedInUsername)
                    .orElseThrow(() -> {
                        log.error("Customer not found for email: {}", loggedInUsername);
                        return new RuntimeException("Customer not found");
                    });

            // Fetch all reward points for the authenticated user
            List<RewardPoints> allPoints = rewardPointsService.getAllRewardPoints(customer.getId());
            log.info("Fetched {} reward points for customer with email: {}", allPoints.size(), loggedInUsername);
            return ResponseEntity.ok(allPoints);

        } catch (Exception e) {
            log.error("Error occurred while fetching all reward points for user with email: {}", loggedInUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
