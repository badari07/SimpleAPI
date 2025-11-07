package com.example.productService.services;

import com.example.productService.Execptions.ProductNotFundExpection;
import com.example.productService.model.Product;

import java.util.List;

public interface ProductService {
    Product getProductById(Long id) throws ProductNotFundExpection;
    List<Product> getAllProducts();
    Product replaceProduct(Product newProduct, Long id);
    Product createProduct(Product newProduct);
    Product partialUpdateProduct(Product newProduct, Long id) throws ProductNotFundExpection;
}
