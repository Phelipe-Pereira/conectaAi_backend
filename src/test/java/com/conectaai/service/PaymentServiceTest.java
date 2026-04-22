package com.conectaai.service;

import com.conectaai.adapter.factory.PaymentGatewayAdapterFactory;
import com.conectaai.adapter.gateway.GatewayPaymentResponse;
import com.conectaai.adapter.gateway.PaymentGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.domain.Payment;
import com.conectaai.domain.User;
import com.conectaai.dto.payment.PaymentRequestDto;
import com.conectaai.dto.payment.PaymentResponseDto;
import com.conectaai.enums.Currency;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.exception.InvalidPaymentStatusTransitionException;
import com.conectaai.exception.PaymentNotFoundException;
import com.conectaai.repository.PaymentRepository;
import com.conectaai.repository.UserRepository;
import com.conectaai.service.customer.CustomerService;
import com.conectaai.service.payment.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private PaymentGatewayAdapterFactory adapterFactory;

    @Mock
    private PaymentGatewayAdapter gatewayAdapter;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Customer customer;
    private User user;
    private PaymentRequestDto validRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("João Silva");
        customer.setEmail("joao@example.com");

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        validRequest = new PaymentRequestDto(
                1L,
                Provider.ASAAS,
                new BigDecimal("100.00"),
                Currency.BRL,
                "PIX",
                "Pagamento teste",
                LocalDate.now().plusDays(7)
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createPayment_whenValidRequest_returnsPaymentResponse() {
        GatewayPaymentResponse gatewayResponse = new GatewayPaymentResponse(
                "prov_123", "PENDING", new BigDecimal("100.00"),
                "BRL", "https://pay.url", null, null, null, null
        );

        Payment savedPayment = buildPayment(PaymentStatus.PENDING);

        when(customerService.findCustomerEntityById(1L)).thenReturn(customer);
        doNothing().when(customerService).ensureCustomerInGateway(any(), any());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.existsByExternalId(any())).thenReturn(false);
        when(adapterFactory.getAdapter(Provider.ASAAS)).thenReturn(gatewayAdapter);
        when(gatewayAdapter.createPayment(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(gatewayResponse);
        when(paymentRepository.save(any())).thenReturn(savedPayment);

        PaymentResponseDto response = paymentService.createPayment(validRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING, response.status());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_whenGatewayFails_throwsGatewayException() {
        when(customerService.findCustomerEntityById(1L)).thenReturn(customer);
        doNothing().when(customerService).ensureCustomerInGateway(any(), any());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.existsByExternalId(any())).thenReturn(false);
        when(adapterFactory.getAdapter(Provider.ASAAS)).thenReturn(gatewayAdapter);
        when(gatewayAdapter.createPayment(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new GatewayException(Provider.ASAAS, "Timeout ao conectar com gateway"));

        assertThrows(GatewayException.class, () -> paymentService.createPayment(validRequest));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void findById_whenPaymentExists_returnsPaymentResponse() {
        Payment payment = buildPayment(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponseDto response = paymentService.findById(1L);

        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING, response.status());
    }

    @Test
    void findById_whenPaymentNotFound_throwsPaymentNotFoundException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.findById(99L));
    }

    @Test
    void cancelPayment_whenCancellableStatus_cancelsSuccessfully() {
        Payment payment = buildPayment(PaymentStatus.PENDING);
        payment.setProviderPaymentId(null);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);

        assertDoesNotThrow(() -> paymentService.cancelPayment(1L));

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void cancelPayment_whenNonCancellableStatus_throwsInvalidTransitionException() {
        Payment payment = buildPayment(PaymentStatus.RECEIVED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStatusTransitionException.class,
                () -> paymentService.cancelPayment(1L));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void cancelPayment_whenHasProviderPaymentId_callsGatewayCancel() {
        Payment payment = buildPayment(PaymentStatus.PENDING);
        payment.setProviderPaymentId("prov_abc");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(adapterFactory.getAdapter(Provider.ASAAS)).thenReturn(gatewayAdapter);
        doNothing().when(gatewayAdapter).cancelPayment("prov_abc");
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.cancelPayment(1L);

        verify(gatewayAdapter).cancelPayment("prov_abc");
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
    }

    private Payment buildPayment(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setExternalId("ext_test_123");
        payment.setProvider(Provider.ASAAS);
        payment.setCustomer(customer);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency(Currency.BRL);
        payment.setPaymentMethod("PIX");
        payment.setDueDate(LocalDate.now().plusDays(7));
        payment.setStatus(status);
        payment.setUser(user);
        return payment;
    }
}