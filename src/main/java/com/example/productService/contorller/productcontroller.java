package com.example.productService.contorller;

import com.example.productService.DTO.GetAllProductsResponseDTO;
import com.example.productService.DTO.ProductRequestDTO;
import com.example.productService.DTO.ProductResponeDTO;
import com.example.productService.Execptions.ProductNotFundExpection;
import com.example.productService.model.Product;
import com.example.productService.services.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
            Page<ProductResponeDTO> productPage = new PageImpl<>(getProductResponse);

            getAllProductsResponseDTO.setProducts(productPage);

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
