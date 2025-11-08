package com.example.productService.services.fliteringService;

import com.example.productService.model.Product;

import java.util.List;

public interface Filter {

    List<Product> applyFilters(List<Product> products, List<String> vlaues);
}
