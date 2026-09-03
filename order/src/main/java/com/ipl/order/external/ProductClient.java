package com.ipl.order.external;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class ProductClient {
    private final WebClient http;
    private final String baseUrl;

    public ProductClient(WebClient http, @Value("${external.product-service}") String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl;
    }

    public ProductDto getProduct(Long id) {
        return http.get()
                .uri(baseUrl + "/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> Mono.error(new RuntimeException("PRODUCT_NOT_FOUND")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new RuntimeException("PRODUCT_BAD_REQUEST")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new RuntimeException("PRODUCT_SERVICE_UNAVAILABLE")))
                .bodyToMono(ProductDto.class)
                .block();
    }

    public void reserve(Long id, int qty) {
        ReserveRequest req = new ReserveRequest(); req.quantity = qty;
        http.post()
                .uri(baseUrl + "/products/{id}/reserve", id)
                .bodyValue(req)
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        resp -> Mono.error(new EntityNotFoundException("INSUFFICIENT_STOCK")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new RuntimeException("PRODUCT_RESERVE_BAD_REQUEST")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new RuntimeException("PRODUCT_SERVICE_UNAVAILABLE")))
                .toBodilessEntity()
                .block();
    }

    public static class ProductDto {
        public Long id;
        public String name;
        public BigDecimal price;
        public Integer stock;
    }

    public static class ReserveRequest {
        public Integer quantity;
    }
}