package com.example.productService.DTO;

import com.example.productService.model.Catogory;
import com.example.productService.model.Product;
import lombok.Data;

@Data
public class ProductResponeDTO {
    private Long id;
    private String title;
    private double price;
    private String description;
    private String image;
    private String category;

    public static ProductResponeDTO fromProduct(Product product) {
        ProductResponeDTO productResponeDTO = new ProductResponeDTO();
        productResponeDTO.setId(product.getId());
        productResponeDTO.setTitle(product.getTitle());
        productResponeDTO.setPrice(product.getPrice());
        productResponeDTO.setDescription(product.getDescription());
        productResponeDTO.setImage(product.getImage());
        Catogory catogory = new Catogory();
        catogory.setName(product.getCategory().getName());
        productResponeDTO.setCategory(catogory.getName());
        return productResponeDTO;
    }
}
