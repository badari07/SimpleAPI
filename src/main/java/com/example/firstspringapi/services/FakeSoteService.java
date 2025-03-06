package com.example.firstspringapi.services;

import com.example.firstspringapi.DTO.FakeStoreDTO;
import com.example.firstspringapi.DTO.ProductRequestDTO;
import com.example.firstspringapi.Execptions.NoProductsFoundException;
import com.example.firstspringapi.Execptions.ProductNotFundExpection;
import com.example.firstspringapi.model.Catogory;
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

    private Product convertToProduct(FakeStoreDTO fakeStoreDTO) {
        Product product = new Product();
        product.setId(fakeStoreDTO.getId());
        product.setTitle(fakeStoreDTO.getTitle());
        product.setPrice(fakeStoreDTO.getPrice());
        product.setDescription(fakeStoreDTO.getDescription());
        product.setImage(fakeStoreDTO.getImage());
//        Catogory catogory = new Catogory();
//        catogory.setDescription(fakeStoreDTO.getDescription());
//        //product.setCatogory(catogory);
        return product;
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

        FakeStoreDTO[] fakeStoreDTOList = restTemplate.getForObject("https://fakestoreapi.com/products", FakeStoreDTO[].class);
        //convert  list of FakeStoreDTO to  list Product
       List<Product> products = new ArrayList<>();
        if(fakeStoreDTOList == null) {
            throw new NoProductsFoundException("No products found");
        }
        for (FakeStoreDTO fakeStoreDTO : fakeStoreDTOList) {
            products.add(fakeStoreDTO.toProduct());
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
        FakeStoreDTO response = restTemplate.postForObject("https://fakestoreapi.com/products", reuest, FakeStoreDTO.class);

        return response.toProduct();

        //return null;
    }
}
