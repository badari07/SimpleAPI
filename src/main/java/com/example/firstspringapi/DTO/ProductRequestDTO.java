package com.example.firstspringapi.DTO;

import com.example.firstspringapi.model.Catogory;
import com.example.firstspringapi.model.Product;
import lombok.Data;
import lombok.NonNull;

@Data
public class ProductRequestDTO {
    private Long id;
    @NonNull
    private String title;
    private Double price;
    private String description;
    private String image;
    @NonNull
    private String category;

    public ProductRequestDTO( @NonNull String title, double price, String description, String image, @NonNull String category) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.image = image;
        this.category = category;

    }

    public ProductRequestDTO() {

    }

    public Product toProduct() {
        Product product = new Product();
        product.setId(this.id);
        product.setTitle(this.title);
        product.setPrice(this.price);
        product.setDescription(this.description);
        product.setImage(this.image);
        Catogory catogory = new Catogory();
        catogory.setName(category);
        product.setCategory(catogory);
        return product;
    }
}
