package com.example.firstspringapi.services;


import com.example.firstspringapi.DTO.ProductRequestDTO;
import com.example.firstspringapi.Execptions.NoProductsFoundException;
import com.example.firstspringapi.Execptions.ProductNotFundExpection;
import com.example.firstspringapi.model.Product;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Service("fakeSoteService")
public class FakeSoteService implements ProductService {
    private RestTemplate restTemplate;

    FakeSoteService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Product getProductById(Long id) throws ProductNotFundExpection {

       // int i = 1 /0 ;

        ProductRequestDTO response = restTemplate.getForObject("https://fakestoreapi.com/products/" + id, ProductRequestDTO.class);
        //convert FakeStoreDTO to Product
     if(response == null) {
         throw new ProductNotFundExpection(id, "not found");
     }

     return response.toProduct();
    }

    @Override
    public List<Product> getAllProducts() {

        ProductRequestDTO[] fakeStoreDTOList = restTemplate.getForObject("https://fakestoreapi.com/products", ProductRequestDTO[].class);
        //convert  list of FakeStoreDTO to  list Product
       List<Product> products = new ArrayList<>();
        if(fakeStoreDTOList == null) {
            throw new NoProductsFoundException("No products found");
        }
        for (ProductRequestDTO fakeStoreDTO : fakeStoreDTOList) {
            products.add(fakeStoreDTO.toProduct());
        }

        return products;
    }

    @Override
    public Product replaceProduct(Product newProduct, Long id) {

        ProductRequestDTO reuest = new ProductRequestDTO();
        reuest.setTitle(newProduct.getTitle());
        reuest.setPrice(newProduct.getPrice());
        reuest.setDescription(newProduct.getDescription());
        reuest.setImage(newProduct.getImage());
        reuest.setCategory(newProduct.getCategory());



        RequestCallback requestCallback = restTemplate.httpEntityCallback(newProduct, ProductRequestDTO.class);
        HttpMessageConverterExtractor<ProductRequestDTO> responseExtractor = new HttpMessageConverterExtractor<>(ProductRequestDTO.class, restTemplate.getMessageConverters());
        ProductRequestDTO res  = restTemplate.execute("https://fakestoreapi.com/products/" + id, HttpMethod.PUT, requestCallback, responseExtractor);
        return res.toProduct();
    }

    @Override
    public Product createProduct(Product newProduct) {
        ProductRequestDTO reuest = new ProductRequestDTO();
        reuest.setTitle(newProduct.getTitle());
        reuest.setPrice(newProduct.getPrice());
        reuest.setDescription(newProduct.getDescription());
        reuest.setImage(newProduct.getImage());
        reuest.setCategory(newProduct.getCategory());
        ProductRequestDTO response = restTemplate.postForObject("https://fakestoreapi.com/products", reuest, ProductRequestDTO.class);

        return response.toProduct();


    }
}
