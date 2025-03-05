package com.example.firstspringapi.services;

import com.example.firstspringapi.DTO.FakeStoreDTO;
import com.example.firstspringapi.model.Catogory;
import com.example.firstspringapi.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Service
public class FakeSoteService implements ProductService {
    private RestTemplate restTemplate;

    FakeSoteService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private Product convertToProduct(FakeStoreDTO fakeStoreDTO) {
        Product product = new Product();
        product.setId(fakeStoreDTO.getId());
        product.setTitle(fakeStoreDTO.getTitle());
        product.setPrice(fakeStoreDTO.getPrice());
        product.setDescription(fakeStoreDTO.getDescription());
        product.setImage(fakeStoreDTO.getImage());
        Catogory catogory = new Catogory();
        catogory.setDescription(fakeStoreDTO.getDescription());
        product.setCatogory(catogory);
        return product;
    }

    @Override
    public Product getProductById(Long id) {

        FakeStoreDTO fakeStoreDTO = restTemplate.getForObject("https://fakestoreapi.com/products/" + id, FakeStoreDTO.class);
        assert fakeStoreDTO != null;
        return convertToProduct(fakeStoreDTO);

    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>();
    }
}
