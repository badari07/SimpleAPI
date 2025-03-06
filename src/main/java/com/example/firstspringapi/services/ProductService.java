package com.example.firstspringapi.services;

import com.example.firstspringapi.Execptions.ProductNotFundExpection;
import com.example.firstspringapi.model.Product;

import java.util.List;

public interface ProductService {
    Product getProductById(Long id) throws ProductNotFundExpection;
    List<Product> getAllProducts();
    Product replaceProduct(Product newProduct, Long id);
}
