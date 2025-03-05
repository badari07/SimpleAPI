package com.example.firstspringapi.services;

import com.example.firstspringapi.model.Product;

import java.util.List;

public interface ProductService {
    Product getProductById(Long id);
    List<Product> getAllProducts();
}
