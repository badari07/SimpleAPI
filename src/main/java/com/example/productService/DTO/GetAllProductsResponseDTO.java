package com.example.productService.DTO;

import lombok.Data;

import java.util.List;

@Data
public class GetAllProductsResponseDTO {
    List<ProductResponeDTO> products;
}
