package com.example.firstspringapi.DTO;

import com.example.firstspringapi.model.Product;
import lombok.Data;

@Data
public class ProductRequestDTO {
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
}
