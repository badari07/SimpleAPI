package com.example.productService.services.fliteringService;

import com.example.productService.model.Product;

import java.util.List;

public class BrandFilter implements Filter {
    @Override
    public List<Product> applyFilters(List<Product> products, List<String> vlaues) {
        return List.of();
    }
}
