package com.easyshop.order.service;

import com.easyshop.order.domain.OrderItemRepository;
import com.easyshop.order.domain.OrderRepository;
import com.easyshop.order.web.dto.CheckoutDto;
import com.easyshop.order.web.dto.OrderResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "product.base-url=http://localhost"
})
@Transactional
@Disabled
class OrderServiceTest {

    @Autowired
    private OrderService service;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private OrderItemRepository items;

    private MockRestServiceServer server;

    @BeforeEach
    void setupServer() {
        var builder = org.springframework.web.client.RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        var client = builder.baseUrl("http://localhost").build();
        ReflectionTestUtils.setField(service, "http", client);
    }

    @Test
    void checkoutSuccessfully() {
        server.expect(MockRestRequestMatchers.requestTo("http://localhost/api/products/1"))
                .andRespond(MockRestResponseCreators.withSuccess("{\"id\":1,\"name\":\"P1\",\"price\":10}", MediaType.APPLICATION_JSON));
        server.expect(MockRestRequestMatchers.requestTo("http://localhost/api/admin/products/1/reserve?qty=2"))
                .andRespond(MockRestResponseCreators.withSuccess());

        CheckoutDto dto = new CheckoutDto(List.of(new CheckoutDto.Item(1L, 2)));
        OrderResponseDto res = service.checkout(dto, "user@test.com");
        server.verify();

        assertThat(res.total()).isEqualTo(new BigDecimal("20"));
        assertThat(orders.count()).isEqualTo(1);
        assertThat(items.count()).isEqualTo(1);
    }

    @Test
    void checkoutFailsWhenProductNotFound() {
        server.expect(MockRestRequestMatchers.requestTo("http://localhost/api/products/1"))
                .andRespond(MockRestResponseCreators.withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        CheckoutDto dto = new CheckoutDto(List.of(new CheckoutDto.Item(1L, 1)));
        assertThatThrownBy(() -> service.checkout(dto, "user@test.com"))
                .isInstanceOf(OrderService.ProductNotFoundException.class);
        server.verify();
        assertThat(orders.count()).isZero();
    }

    @Test
    void checkoutFailsWhenStockNotAvailable() {
        server.expect(MockRestRequestMatchers.requestTo("http://localhost/api/products/1"))
                .andRespond(MockRestResponseCreators.withSuccess("{\"id\":1,\"name\":\"P1\",\"price\":10}", MediaType.APPLICATION_JSON));
        server.expect(MockRestRequestMatchers.requestTo("http://localhost/api/admin/products/1/reserve?qty=2"))
                .andRespond(MockRestResponseCreators.withStatus(org.springframework.http.HttpStatus.CONFLICT));

        CheckoutDto dto = new CheckoutDto(List.of(new CheckoutDto.Item(1L, 2)));
        assertThatThrownBy(() -> service.checkout(dto, "user@test.com"))
                .isInstanceOf(OrderService.StockNotAvailableException.class);
        server.verify();
        assertThat(orders.count()).isZero();
    }

    @Test
    void checkoutFailsWhenServiceUnavailable() {
        server.expect(MockRestRequestMatchers.requestTo("http://localhost/api/products/1"))
                .andRespond(MockRestResponseCreators.withServerError());

        CheckoutDto dto = new CheckoutDto(List.of(new CheckoutDto.Item(1L, 1)));
        assertThatThrownBy(() -> service.checkout(dto, "user@test.com"))
                .isInstanceOf(OrderService.ServiceUnavailableException.class);
        server.verify();
        assertThat(orders.count()).isZero();
    }
}

