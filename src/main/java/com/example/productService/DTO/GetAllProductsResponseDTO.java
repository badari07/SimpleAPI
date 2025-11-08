package com.example.productService.DTO;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class GetAllProductsResponseDTO {
    private Page<ProductResponeDTO> products;
}
