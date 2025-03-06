package com.example.firstspringapi.services;

import com.example.firstspringapi.Execptions.ProductNotFundExpection;
import com.example.firstspringapi.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("ProductService2")
public class PrductService2 implements ProductService {
    @Override
    public Product getProductById(Long id) throws ProductNotFundExpection {
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        return List.of();
    }

    @Override
    public Product replaceProduct(Product newProduct, Long id) {
        return null;
    }

    @Override
    public Product createProduct(Product newProduct) {
        return null;
    }
}
