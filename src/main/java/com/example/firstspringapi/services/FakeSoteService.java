package com.example.firstspringapi.services;

import com.example.firstspringapi.DTO.FakeStoreDTO;
import com.example.firstspringapi.model.Catogory;
import com.example.firstspringapi.model.Product;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
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
        //convert FakeStoreDTO to Product
        assert fakeStoreDTO != null;
        return convertToProduct(fakeStoreDTO);

    }

    @Override
    public List<Product> getAllProducts() {

        FakeStoreDTO[] fakeStoreDTOList = restTemplate.getForObject("https://fakestoreapi.com/products", FakeStoreDTO[].class);
        //convert  list of FakeStoreDTO to  list Product
        System.out.println("debug");
List<Product> products = new ArrayList<>();
        assert fakeStoreDTOList != null;
        for (FakeStoreDTO fakeStoreDTO : fakeStoreDTOList) {
            products.add(convertToProduct(fakeStoreDTO));
        }

        return products;
    }

    @Override
    public Product replaceProduct(Product newProduct, Long id) {
//        restTemplate.put("https://fakestoreapi.com/products/" + id, newProduct);
        FakeStoreDTO fakeStoreDTO = new FakeStoreDTO();
        fakeStoreDTO.setTitle(newProduct.getTitle());
        fakeStoreDTO.setPrice(newProduct.getPrice());
        fakeStoreDTO.setDescription(newProduct.getDescription());
        fakeStoreDTO.setImage(newProduct.getImage());
        //fakeStoreDTO.setCategory(newProduct.getCatogory().getDescription());

        RequestCallback requestCallback = restTemplate.httpEntityCallback(newProduct, FakeStoreDTO.class);
        HttpMessageConverterExtractor<FakeStoreDTO> responseExtractor = new HttpMessageConverterExtractor<>(FakeStoreDTO.class, restTemplate.getMessageConverters());
       FakeStoreDTO res  = restTemplate.execute("https://fakestoreapi.com/products/" + id, HttpMethod.PUT, requestCallback, responseExtractor);
        assert res != null;
        return convertToProduct(res);
    }
}
