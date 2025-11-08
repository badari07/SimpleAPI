package com.example.productService.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ProductSearchFilterDTO {
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String key;
    private List<String> values;
}
