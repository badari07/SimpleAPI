package com.example.firstspringapi.contorller;


import com.example.firstspringapi.model.Product;
import com.example.firstspringapi.services.FakeSoteService;
import com.example.firstspringapi.services.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class productcontroller {

    ProductService productService ;
    productcontroller(ProductService productService){
        this.productService = productService;
    }

    @GetMapping(   "/{id}")
        public Product getProductById(@PathVariable("id") Long id){
            return productService.getProductById(id);
        }

        @GetMapping()
        public List<Product> getAllProducts() {
            return productService.getAllProducts();
        }



}
