package com.infy.RewardPointCalculator;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.RewardPoints;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.repository.RewardPointsRepository;
import com.infy.RewardPointCalculator.service.RewardPointsService;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RewardPointsServiceTest {

    @Mock
    private RewardPointsRepository rewardPointsRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RewardPointsService rewardPointsService;

    private Customer customer;

    @BeforeEach
    public void setup() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
    }

    @Test
    public void testGetRewardPoints_ShouldReturnAggregatedPoints_WhenPointsExist() {
        // Arrange
        List<RewardPoints> rewardPointsList = Arrays.asList(
                new RewardPoints(8L, customer, 50, 1, 2025),
                new RewardPoints(9L, customer, 40, 1, 2025)
        );
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(rewardPointsRepository.findByCustomerAndMonthAndYear(customer, 1, 2025)).thenReturn(rewardPointsList);

        // Act
        RewardPoints result = rewardPointsService.getRewardPoints(customer.getId(), 1, 2025);

        // Assert
        assertNotNull(result);
        assertEquals(90, result.getPoints());  // Sum of 50 + 40
        assertEquals(1, result.getMonth());
        assertEquals(2025, result.getYear());
    }

    @Test
    public void testGetRewardPoints_ShouldReturnZeroPoints_WhenNoPointsExist() {
        // Arrange
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(rewardPointsRepository.findByCustomerAndMonthAndYear(customer, 1, 2025)).thenReturn(Collections.emptyList());

        // Act
        RewardPoints result = rewardPointsService.getRewardPoints(customer.getId(), 1, 2025);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPoints());  // No points found, should return 0 points
    }

    
    @Test
    public void testGetRewardPoints_ShouldThrowBadRequest_WhenDataIntegrityViolationOccurs() {
        // Arrange
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(rewardPointsRepository.findByCustomerAndMonthAndYear(customer, 1, 2025))
                .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            rewardPointsService.getRewardPoints(customer.getId(), 1, 2025);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Data integrity violation while retrieving reward points", exception.getReason());
    }

    @Test
    public void testGetAllRewardPoints_ShouldReturnRewardPoints_WhenPointsExist() {
        // Arrange
        List<RewardPoints> rewardPointsList = Arrays.asList(
                new RewardPoints(8L, customer, 90, 1, 2025),
                new RewardPoints(9L, customer, 40, 1, 2025)
        );
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(rewardPointsRepository.findByCustomer(customer)).thenReturn(rewardPointsList);

        // Act
        List<RewardPoints> result = rewardPointsService.getAllRewardPoints(customer.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(90, result.get(0).getPoints());
        assertEquals(40, result.get(1).getPoints());
    }

    @Test
    public void testGetAllRewardPoints_ShouldReturnEmptyList_WhenNoPointsExist() {
        // Arrange
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(rewardPointsRepository.findByCustomer(customer)).thenReturn(Collections.emptyList());

        // Act
        List<RewardPoints> result = rewardPointsService.getAllRewardPoints(customer.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());  // No reward points should be returned
    }

   
    @Test
    public void testGetAllRewardPoints_ShouldThrowBadRequest_WhenDataIntegrityViolationOccurs() {
        // Arrange
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(rewardPointsRepository.findByCustomer(customer)).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            rewardPointsService.getAllRewardPoints(customer.getId());
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Data integrity violation while retrieving all reward points", exception.getReason());
    }
}
