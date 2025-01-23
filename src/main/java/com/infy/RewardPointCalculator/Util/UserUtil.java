package com.infy.RewardPointCalculator.Util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility class for retrieving user-related information from the security
 * context. This class contains helper methods to access details of the
 * currently logged-in user, such as the username, in a Spring Security-based
 * application.
 * 
 * @author rifat.bano
 */
public class UserUtil {

	/**
	 * Retrieves the username of the currently logged-in user.
	 * 
	 * This method extracts the username of the authenticated user from the Spring
	 * Security context. If the user is authenticated and the authentication details
	 * are available, the method will return the username. If the authentication
	 * details are not available or the user is not authenticated, it returns null.
	 * 
	 * @return The username of the currently authenticated user, or null if the user
	 *         is not authenticated.
	 */
	public static String getLoggedInUsername() {
		// Retrieve the current authentication object from the Spring Security context
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// Check if authentication is present and the user is authenticated
		if (authentication != null && authentication.isAuthenticated()) {
			// Get the principal (user details) associated with the authentication
			Object principal = authentication.getPrincipal();

			// If the principal is an instance of UserDetails (Spring Security user object)
			if (principal instanceof UserDetails) {
				// Return the username of the logged-in user
				return ((UserDetails) principal).getUsername();
			} else {
				// If the principal is not of type UserDetails, return its string representation
				return principal.toString();
			}
		}
		// Return null if authentication is not available or user is not authenticated
		return null;
	}
}
