package com.easyshop.order.web;

import com.easyshop.order.service.OrderService;
import com.easyshop.order.service.OrderService.ProductNotFoundException;
import com.easyshop.order.service.OrderService.ServiceUnavailableException;
import com.easyshop.order.service.OrderService.StockNotAvailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "product.base-url=http://localhost")
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderService service;

    @Test
    void healthEndpointWorks() throws Exception {
        mvc.perform(get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void readyEndpointWorks() throws Exception {
        mvc.perform(get("/readyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

    @Test
    void checkoutSuccessfully() throws Exception {
        when(service.checkout(any(), anyString())).thenReturn(
                Map.of("id", 1, "total", 20, "status", "CREATED", "items", List.of()));

        mvc.perform(post("/api/orders/checkout")
                        .principal(() -> "user@test.com")
                        .contentType("application/json")
                        .content("{\"items\":[{\"productId\":1,\"quantity\":2}]}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void checkoutEmptyCartReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/orders/checkout")
                        .principal(() -> "user@test.com")
                        .contentType("application/json")
                        .content("{\"items\":[]}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Empty cart"));
    }

    @Test
    void checkoutProductNotFound() throws Exception {
        when(service.checkout(any(), anyString())).thenThrow(new ProductNotFoundException());

        mvc.perform(post("/api/orders/checkout")
                        .principal(() -> "user@test.com")
                        .contentType("application/json")
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1}]}")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void checkoutStockNotAvailable() throws Exception {
        when(service.checkout(any(), anyString())).thenThrow(new StockNotAvailableException());

        mvc.perform(post("/api/orders/checkout")
                        .principal(() -> "user@test.com")
                        .contentType("application/json")
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1}]}")
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Stock not available"));
    }

    @Test
    void checkoutServiceUnavailable() throws Exception {
        when(service.checkout(any(), anyString())).thenThrow(new ServiceUnavailableException());

        mvc.perform(post("/api/orders/checkout")
                        .principal(() -> "user@test.com")
                        .contentType("application/json")
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1}]}")
                )
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Product service unavailable"));
    }
}

