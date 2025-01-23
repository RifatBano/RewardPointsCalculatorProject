package com.infy.RewardPointCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.infy.RewardPointCalculator.Util.RewardPointCalculator;

/**
 * Unit tests for the {@link RewardPointCalculator} class.
 * <p>
 * These tests verify the logic for calculating reward points based on different
 * transaction amounts. The reward point calculation logic is based on specific
 * conditions: - No points for amounts less than or equal to 50 - Points
 * calculated as (100 - 50) * 1 for amounts between 51 and 100 - Points
 * calculated as (amount - 100) * 2 for amounts greater than 100
 * </p>
 */
class RewardPointCalculatorApplicationTests {

	@Test
	void contextLoads() {
	}

	/**
	 * This test verifies that the correct reward points are calculated for amounts
	 * less than or equal to 50.
	 * <p>
	 * According to the reward points logic, no points should be awarded for
	 * transactions with amounts less than or equal to 50.
	 * </p>
	 */
	@Test
	public void testCalculatePoints_AmountLessThan50() {
		double amount = 30;
		int expectedPoints = 0; // No points for amounts <= 50
		int actualPoints = RewardPointCalculator.calculatePoints(amount);
		assertEquals(expectedPoints, actualPoints, "Points should be 0 for amounts less than or equal to 50");
	}

	/**
	 * This test verifies that the correct reward points are calculated for amounts
	 * between 51 and 100.
	 * <p>
	 * For amounts in this range, the reward points are calculated as (amount - 50).
	 * For example, a transaction of 75 would earn 25 points (75 - 50).
	 * </p>
	 */
	@Test
	public void testCalculatePoints_AmountBetween51And100() {
		double amount = 75;
		int expectedPoints = 25;
		int actualPoints = RewardPointCalculator.calculatePoints(amount);
		assertEquals(expectedPoints, actualPoints,
				"Points should be calculated as (amount - 50) for amounts between 51 and 100");
	}

	/**
	 * This test verifies that the correct reward points are calculated when the
	 * amount is exactly 50.
	 * <p>
	 * According to the reward points logic, no points should be awarded for a
	 * transaction of exactly 50.
	 * </p>
	 */
	@Test
	public void testCalculatePoints_AmountEqualTo50() {
		double amount = 50;
		int expectedPoints = 0; // No points for exactly 50
		int actualPoints = RewardPointCalculator.calculatePoints(amount);
		assertEquals(expectedPoints, actualPoints, "Points should be 0 for amounts exactly equal to 50");
	}

	/**
	 * This test verifies that the correct reward points are calculated for amounts
	 * between 101 and 200.
	 * <p>
	 * For amounts greater than 100 but less than or equal to 200, the reward points
	 * are calculated as (100-50) * 1 + (amount - 100) * 2. For example, a
	 * transaction of 150 would earn 150 points ((100-50) * 1 + (150 - 100) * 2).
	 * </p>
	 */
	@Test
	public void testCalculatePoints_AmountBetween101And200() {
		double amount = 150;
		int expectedPoints = 150;
		int actualPoints = RewardPointCalculator.calculatePoints(amount);
		assertEquals(expectedPoints, actualPoints,
				"Points should be calculated as (100-50) * 1 +(amount - 100) * 2 for amounts greater than 100");
	}

	/**
	 * This test verifies that the correct reward points are calculated for amounts
	 * greater than 200.
	 * <p>
	 * For amounts greater than 200, the reward points are also calculated as
	 * (amount - 100) * 2. For example, a transaction of 250 would earn 350 points
	 * ((100-50) * 1+(250 - 100) * 2).
	 * </p>
	 */
	@Test
	public void testCalculatePoints_AmountGreaterThan200() {
		double amount = 250;
		int expectedPoints = 350;
		int actualPoints = RewardPointCalculator.calculatePoints(amount);
		assertEquals(expectedPoints, actualPoints,
				"Points should be calculated as (100-50) * 1 + (amount - 100) * 2 for amounts greater than 100");
	}

	/**
	 * This test verifies that the correct reward points are calculated when the
	 * amount is exactly 100.
	 * <p>
	 * According to the reward points logic, a transaction of exactly 100 should
	 * award 50 points (100 - 50).
	 * </p>
	 */
	@Test
	public void testCalculatePoints_AmountEqualTo100() {
		double amount = 100;
		int expectedPoints = 50;
		int actualPoints = RewardPointCalculator.calculatePoints(amount);
		assertEquals(expectedPoints, actualPoints, "Points should be (amount-50)*1 ");
	}
}
