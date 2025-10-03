package com.example.firstspringapi.contorller;

import com.example.firstspringapi.DTO.GetAllProductsResponseDTO;
import com.example.firstspringapi.DTO.ProductRequestDTO;
import com.example.firstspringapi.DTO.ProductResponeDTO;
import com.example.firstspringapi.Execptions.ProductNotFundExpection;
import com.example.firstspringapi.model.Product;
import com.example.firstspringapi.services.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class productcontroller {

    ProductService productService ;
    productcontroller(@Qualifier("ProductService2") ProductService productService){
        this.productService = productService;
    }

    @GetMapping(   "/{id}")
        public ResponseEntity<ProductResponeDTO> getProductById(@PathVariable("id") Long id) throws ProductNotFundExpection {
            Product product = productService.getProductById(id);
            return ResponseEntity.status(HttpStatus.OK).body(ProductResponeDTO.fromProduct(product));
        }

        @GetMapping()
        public ResponseEntity<GetAllProductsResponseDTO> getAllProducts() {
             List<Product>products= productService.getAllProducts();
             GetAllProductsResponseDTO getAllProductsResponseDTO = new GetAllProductsResponseDTO();
            List<ProductResponeDTO> getProductResponse = new ArrayList<>();
            for (Product product : products) {
                getProductResponse.add(ProductResponeDTO.fromProduct(product));
            }

          getAllProductsResponseDTO.setProducts(getProductResponse);

            return ResponseEntity.status(HttpStatus.OK).body(getAllProductsResponseDTO);


        }

        @PutMapping("/{id}")
        public ResponseEntity<ProductResponeDTO> replaceProduct(@RequestBody ProductRequestDTO newProduct , @PathVariable("id") Long id) {

            Product product=  productService.replaceProduct(newProduct.toProduct(), id);

            return ResponseEntity.status(HttpStatus.OK).body(ProductResponeDTO.fromProduct(product));
        }

        @PostMapping()
        public ResponseEntity<ProductResponeDTO> createProduct(@RequestBody ProductRequestDTO requestProductDTO) {

            Product product = productService.createProduct(requestProductDTO.toProduct());
            return new ResponseEntity<>(ProductResponeDTO.fromProduct(product) , HttpStatus.CREATED);
            //return null;
        }

        @PatchMapping("/{id}")
        public ResponseEntity<ProductResponeDTO> partialUpdateProduct(@RequestBody ProductRequestDTO newProduct , @PathVariable("id") Long id) throws ProductNotFundExpection {

            Product product=  productService.partialUpdateProduct(newProduct.toProduct(), id);

            return ResponseEntity.status(HttpStatus.OK).body(ProductResponeDTO.fromProduct(product));
        }





}
