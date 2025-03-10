package com.example.firstspringapi.DTO;

import com.example.firstspringapi.model.Catogory;
import com.example.firstspringapi.model.Product;
import lombok.Data;

@Data
public class FakeStoreDTO {
    private Long id;
    private String title;
    private double price;
    private String description;
    private String image;
    private String category;

    public Product toProduct() {
        Product product = new Product();
        product.setId(this.id);
        product.setTitle(this.title);
        product.setPrice(this.price);
        product.setDescription(this.description);
        product.setImage(this.image);
        product.setCategory(this.category);
        return product;
    }

    public static FakeStoreDTO fromProduct(Product product) {
        FakeStoreDTO fakeStoreDTO = new FakeStoreDTO();
        fakeStoreDTO.setId(product.getId());
        fakeStoreDTO.setTitle(product.getTitle());
        fakeStoreDTO.setPrice(product.getPrice());
        fakeStoreDTO.setDescription(product.getDescription());
        fakeStoreDTO.setImage(product.getImage());
        fakeStoreDTO.setCategory(product.getCategory());
        return fakeStoreDTO;
    }
}
