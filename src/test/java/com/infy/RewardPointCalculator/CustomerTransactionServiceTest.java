package com.infy.RewardPointCalculator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.dto.CustomerTransactionDTO;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.CustomerTransaction;
import com.infy.RewardPointCalculator.model.RewardPoints;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.repository.CustomerTransactionRepository;
import com.infy.RewardPointCalculator.repository.RewardPointsRepository;
import com.infy.RewardPointCalculator.service.CustomerTransactionService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.time.LocalDate;
import java.util.*;

public class CustomerTransactionServiceTest {

    @Mock
    private CustomerTransactionRepository transactionRepository;

    @Mock
    private RewardPointsRepository rewardPointsRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerTransactionService transactionService;

    private Customer customer;
    private CustomerTransaction transaction;
    private CustomerTransactionDTO transactionDTO;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
  
            // Initialize the customer
            customer = new Customer();
            customer.setId(9L);
    

            // Initialize the transaction
            transaction = new CustomerTransaction();
            transaction.setId(19L);
            transaction.setCustomer(customer);
            transaction.setAmount(100.0); // Original amount
            transaction.setSpentDetails("Old purchase details");
            transaction.setDate(LocalDate.of(2025, 1, 21)); // Set an old transaction date

            // Initialize the DTO with updated values
            transactionDTO = new CustomerTransactionDTO();
            transactionDTO.setAmount(150.0);
            transactionDTO.setSpentDetails("Updated spent details for transaction");
            transactionDTO.setTransactionDate(LocalDate.now());
        

    }

    @Test
    public void testGetTransactions_ShouldReturnTransactions_WhenTransactionsExist() {
        // Arrange
        when(transactionRepository.findByCustomerId(8L)).thenReturn(Arrays.asList(transaction));

        // Act
        List<CustomerTransaction> result = transactionService.getTransactions(8L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(150.0, result.get(0).getAmount());
    }

    @Test
    public void testGetTransactions_ShouldThrowNotFound_WhenNoTransactionsExist() {
        // Arrange
        when(transactionRepository.findByCustomerId(5L)).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            transactionService.getTransactions(5L);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No transactions found for this customer", exception.getReason());
    }

    @Test
    public void testAddTransaction_ShouldAddTransaction_WhenCustomerHasNoExistingTransactions() {
        // Arrange
        when(transactionRepository.findByCustomerId(8L)).thenReturn(Collections.emptyList()); // No existing transactions
        when(customerRepository.findById(8L)).thenReturn(Optional.of(customer));
        when(transactionRepository.save(any(CustomerTransaction.class))).thenReturn(transaction);
        when(rewardPointsRepository.save(any(RewardPoints.class))).thenReturn(new RewardPoints());

        // Act
        CustomerTransaction result = transactionService.addTransaction(8L, transactionDTO);

        // Assert
        assertNotNull(result);
        assertEquals(150.0, result.getAmount());
        verify(transactionRepository, times(1)).save(any(CustomerTransaction.class));
        verify(rewardPointsRepository, times(1)).save(any(RewardPoints.class));
    }

    @Test
    public void testAddTransaction_ShouldThrowBadRequest_WhenCustomerAlreadyHasTransactions() {
        // Arrange
        when(transactionRepository.findByCustomerId(8L)).thenReturn(Arrays.asList(transaction)); // Customer already has a transaction

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            transactionService.addTransaction(8L, transactionDTO);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Customer already has a transaction. New transaction cannot be added.", exception.getReason());
    }

//    @Test
//    public void testEditTransaction_ShouldUpdateTransaction_WhenTransactionExists() {
//        // Arrange
//        when(transactionRepository.findByCustomerIdAndId(8L, 18L)).thenReturn(Optional.of(transaction));
//        when(transactionRepository.save(any(CustomerTransaction.class))).thenReturn(transaction);
//
//        // Act
//        CustomerTransaction result = transactionService.editTransaction(8L, 18L, transactionDTO);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(150.0, result.getAmount());
//        verify(transactionRepository, times(1)).save(any(CustomerTransaction.class));
//    }

    @Test
    public void testEditTransaction_ShouldThrowNotFound_WhenTransactionNotFound() {
        // Arrange
        when(transactionRepository.findByCustomerIdAndId(8L, 18L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            transactionService.editTransaction(8L, 18L, transactionDTO);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Transaction not found", exception.getReason());
    }

    @Test
    public void testDeleteTransaction_ShouldDeleteTransaction_WhenTransactionExists() {
        // Arrange
        when(transactionRepository.findByCustomerIdAndId(8L, 18L)).thenReturn(Optional.of(transaction));
        when(customerRepository.findById(8L)).thenReturn(Optional.of(customer));
        doNothing().when(transactionRepository).delete(any(CustomerTransaction.class));

        // Act
        transactionService.deleteTransaction(8L, 18L);

        // Assert
        verify(transactionRepository, times(1)).delete(any(CustomerTransaction.class));
    }

    @Test
    public void testDeleteTransaction_ShouldThrowNotFound_WhenTransactionNotFound() {
        // Arrange
        when(transactionRepository.findByCustomerIdAndId(8L, 18L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            transactionService.deleteTransaction(8L, 18L);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Transaction not found", exception.getReason());
    }

    @Test
    public void testGetCustomerById_ShouldThrowRuntimeException_WhenCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(8L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getCustomerById(8L);
        });
        assertEquals("Customer not found", exception.getMessage());
    }

    @AfterEach
    public void teardown() {
        // Reset mocks to clear any interactions after each test
        reset(transactionRepository, rewardPointsRepository, customerRepository);
    }
}
