package com.example.firstspringapi.DTO;

import lombok.Data;

import java.util.List;

@Data
public class GetAllProductsResponseDTO {
    List<ProductResponeDTO> products;
}
