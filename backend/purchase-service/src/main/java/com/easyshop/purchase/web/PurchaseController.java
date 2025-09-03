package com.easyshop.purchase.web;

import com.easyshop.common.web.ApiResponseDto;
import com.easyshop.purchase.service.PurchaseService;
import com.easyshop.purchase.service.PurchaseService.ProductNotFoundException;
import com.easyshop.purchase.service.PurchaseService.ServiceUnavailableException;
import com.easyshop.purchase.service.PurchaseService.StockNotAvailableException;
import com.easyshop.purchase.web.dto.CheckoutDto;
import com.easyshop.purchase.web.dto.PurchaseResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class PurchaseController {
    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @GetMapping("/healthz")
    public ApiResponseDto health() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/readyz")
    public ApiResponseDto ready() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/api/purchases")
    public List<PurchaseResponseDto> myPurchases(Principal p) {
        return service.list(p.getName());
    }

    @PostMapping("/api/purchases/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutDto dto, Principal pr) {
        if (dto.items() == null || dto.items().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Empty cart"));
        }
        try {
            return ResponseEntity.ok(service.checkout(dto, pr.getName()));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto(false, "Product not found"));
        } catch (StockNotAvailableException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponseDto(false, "Stock not available"));
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponseDto(false, "Product service unavailable"));
        }
    }
}
