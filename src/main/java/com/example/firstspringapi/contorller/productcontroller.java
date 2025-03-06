package com.example.firstspringapi.contorller;


import com.example.firstspringapi.Execptions.ProductNotFundExpection;
import com.example.firstspringapi.model.Product;
import com.example.firstspringapi.services.FakeSoteService;
import com.example.firstspringapi.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        public ResponseEntity<Product> getProductById(@PathVariable("id") Long id) throws ProductNotFundExpection {

            Product product = productService.getProductById(id);

            return ResponseEntity.status(HttpStatus.OK).body(product);
        }

        @GetMapping()
        public List<Product> getAllProducts() {
            return productService.getAllProducts();
        }

        @PutMapping("/{id}")
        public Product replaceProduct(@RequestBody Product newProduct , @PathVariable("id") Long id) {
            return productService.replaceProduct(newProduct, id);
        }



}
