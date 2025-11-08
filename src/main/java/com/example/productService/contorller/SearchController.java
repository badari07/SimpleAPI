package com.example.productService.contorller;

import com.example.productService.DTO.GetAllProductsResponseDTO;
import com.example.productService.DTO.ProductResponeDTO;
import com.example.productService.DTO.ProductSearchFilterDTO;
import com.example.productService.DTO.SortingCriteria;
import com.example.productService.model.Product;
import com.example.productService.services.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final ProductService productService;

    public SearchController(@Qualifier("ProductService2") ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public ResponseEntity<GetAllProductsResponseDTO> searchProducts(
            @RequestParam(name = "query", required = false) String query,
            @RequestBody(required = false) List<ProductSearchFilterDTO> filters,
            @RequestParam(name = "sort", required = false) SortingCriteria sortingCriteria,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProducts(query, filters, sortingCriteria, pageable);
        return buildResponse(products);
    }

    @GetMapping("/category")
    public ResponseEntity<GetAllProductsResponseDTO> searchByCategory(
            @RequestParam(name = "category") String category,
            @RequestParam(name = "sort", required = false) SortingCriteria sortingCriteria,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        ProductSearchFilterDTO filter = new ProductSearchFilterDTO();
        filter.setCategory(category);
        List<ProductSearchFilterDTO> filters = new ArrayList<>();
        filters.add(filter);

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProducts(null, filters, sortingCriteria, pageable);
        return buildResponse(products);
    }

    private ResponseEntity<GetAllProductsResponseDTO> buildResponse(Page<Product> products) {
        List<ProductResponeDTO> productResponses = new ArrayList<>();
        for (Product product : products.getContent()) {
            productResponses.add(ProductResponeDTO.fromProduct(product));
        }
        Page<ProductResponeDTO> productPage = new PageImpl<>(
                productResponses,
                products.getPageable(),
                products.getTotalElements()
        );

        GetAllProductsResponseDTO responseDTO = new GetAllProductsResponseDTO();
        responseDTO.setProducts(productPage);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }
}

