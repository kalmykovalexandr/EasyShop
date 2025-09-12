package com.easyshop.purchase.client;

import com.easyshop.purchase.Application;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "spring.cloud.config.enabled=false",
        "spring.config.import="
    })
@Disabled
class ProductClientTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private ProductClient productClient;

    @MockBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerUrl(DynamicPropertyRegistry registry) {
        registry.add("product.service.url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void requestContainsBearerToken() throws Exception {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(60));

        ClientRegistration registration = ClientRegistration.withRegistrationId("purchase-service")
                .tokenUri("http://auth-server/token")
                .clientId("client")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                registration,
                "purchase-service",
                accessToken);

        Mockito.when(authorizedClientManager.authorize(Mockito.any())).thenReturn(authorizedClient);

        mockWebServer.enqueue(new MockResponse().setBody("""
            {
              "id":1,
              "name":"Test",
              "description":"Test product",
              "price":1.00,
              "stock":10
            }
        """).addHeader("Content-Type", "application/json"));

        productClient.getProduct(1L);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token");
    }
}
