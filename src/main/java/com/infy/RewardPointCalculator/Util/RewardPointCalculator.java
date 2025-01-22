package com.infy.RewardPointCalculator.Util;

/**
 * Utility class for calculating reward points for customers.
 * 
 * This class contains methods to calculate reward points based on customer transactions, 
 * spending amounts, or other relevant criteria. It is intended to be used wherever reward 
 * points need to be computed, ensuring consistency across the application.
 * @author rifat.bano
 */
public class RewardPointCalculator {

	/**
	 * This utility method helps to calculate reward points based on customer transactions,  
	 * @param amount spent by the user for particular transaction
	 * @return point based on the calculation given by the formula
	 */
    public static int calculatePoints(double amount) {
        int points = 0;

        if (amount > 100) {
            points += (int)((amount - 100) * 2);  // 2 points per dollar for amount > 100
            amount = 100;
        }
        if (amount > 50) {
            points += (int)((amount - 50) * 1);  // 1 point per dollar for amount between 50 and 100
        }

        return points;
    }
}
