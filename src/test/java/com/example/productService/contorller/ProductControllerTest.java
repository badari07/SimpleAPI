package com.example.productService.contorller;

import com.example.productService.DTO.ProductRequestDTO;
import com.example.productService.model.Catogory;
import com.example.productService.model.Product;
import com.example.productService.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(productcontroller.class)
@Import(ProductControllerTest.MockConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ProductService productService;
    private final RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    ProductControllerTest(ProductService productService, RestTemplate restTemplate) {
        this.productService = productService;
        this.restTemplate = restTemplate;
    }

    @Test
    void createProduct_WhenTokenValid_ReturnsCreatedProduct() throws Exception {
        ProductRequestDTO requestDTO = new ProductRequestDTO("Laptop", 999.0, "High-end device", "image.png", "Electronics");

        Product savedProduct = new Product();
        savedProduct.setId(100L);
        savedProduct.setTitle(requestDTO.getTitle());
        savedProduct.setPrice(requestDTO.getPrice());
        savedProduct.setDescription(requestDTO.getDescription());
        savedProduct.setImage(requestDTO.getImage());
        Catogory category = new Catogory();
        category.setName(requestDTO.getCategory());
        savedProduct.setCategory(category);

        when(restTemplate.postForObject(eq("http://userService/auth/validate"), any(), eq(Boolean.class))).thenReturn(Boolean.TRUE);
        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Laptop"))
                .andExpect(jsonPath("$.category").value("Electronics"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProduct_WhenTokenInvalid_ReturnsUnauthorized() throws Exception {
        ProductRequestDTO requestDTO = new ProductRequestDTO("Book", 20.0, "Novel", "image.png", "Books");

        when(restTemplate.postForObject(eq("http://userService/auth/validate"), any(), eq(Boolean.class))).thenReturn(Boolean.FALSE);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean(name = "ProductService2")
        ProductService productService() {
            return mock(ProductService.class);
        }

        @Bean
        RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }
    }
}

