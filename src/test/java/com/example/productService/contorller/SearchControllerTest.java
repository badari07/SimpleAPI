package com.example.productService.contorller;

import com.example.productService.DTO.SortingCriteria;
import com.example.productService.model.Catogory;
import com.example.productService.model.Product;
import com.example.productService.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@Import(SearchControllerTest.MockConfig.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ProductService productService;

    SearchControllerTest(ProductService productService) {
        this.productService = productService;
    }

    @Test
    void searchByCategory_ReturnsPagedResults() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setTitle("Running Shoes");
        product.setPrice(120.0);
        product.setDescription("Comfortable shoes");
        product.setImage("shoes.png");
        Catogory category = new Catogory();
        category.setName("Sports");
        product.setCategory(category);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

        when(productService.searchProducts(eq(null), anyList(), eq(SortingCriteria.PRICE_ASC), any())).thenReturn(page);

        mockMvc.perform(get("/search/category")
                        .param("category", "Sports")
                        .param("sort", "PRICE_ASC")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.content[0].id").value(1))
                .andExpect(jsonPath("$.products.content[0].title").value("Running Shoes"))
                .andExpect(jsonPath("$.products.content[0].category").value("Sports"))
                .andExpect(jsonPath("$.products.totalElements").value(1));
    }

    @Test
    void searchProducts_WithQueryAndPagination_ReturnsResults() throws Exception {
        Product product = new Product();
        product.setId(5L);
        product.setTitle("Laptop");
        product.setPrice(1500.0);
        product.setDescription("Gaming laptop");
        product.setImage("laptop.png");
        Catogory category = new Catogory();
        category.setName("Electronics");
        product.setCategory(category);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(1, 10), 12);

        when(productService.searchProducts(eq("lap"), anyList(), eq(null), any()))
                .thenReturn(page);

        mockMvc.perform(get("/search/")
                        .param("query", "lap")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.content[0].id").value(5))
                .andExpect(jsonPath("$.products.content[0].title").value("Laptop"))
                .andExpect(jsonPath("$.products.totalElements").value(12))
                .andExpect(jsonPath("$.products.pageable.pageNumber").value(1));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean(name = "ProductService2")
        ProductService productService() {
            return mock(ProductService.class);
        }
    }
}

