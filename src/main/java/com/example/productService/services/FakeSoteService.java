package com.example.productService.services;


import com.example.productService.DTO.ProductRequestDTO;
import com.example.productService.DTO.ProductSearchFilterDTO;
import com.example.productService.DTO.SortingCriteria;
import com.example.productService.Execptions.NoProductsFoundException;
import com.example.productService.Execptions.ProductNotFundExpection;
import com.example.productService.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service("fakeSoteService")
public class FakeSoteService implements ProductService {
    private RestTemplate restTemplate;
    private RedisTemplate<String,Object> redisTemplate;



    FakeSoteService(RestTemplate restTemplate, RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
    }

    @Override
    public Product getProductById(Long id) throws ProductNotFundExpection {

       // int i = 1 /0 ;

        Product product = (Product) redisTemplate.opsForHash().get("PRODUCTS", "PRODUCTS_" + id);
        if (product != null) {
            return product;
        }

        //call fake store api
        ProductRequestDTO response = restTemplate.getForObject("https://fakestoreapi.com/products/" + id, ProductRequestDTO.class);
        //convert FakeStoreDTO to Product
     if(response == null) {
         throw new ProductNotFundExpection(id, "not found");
     }


     Product prod = response.toProduct();
        //store in redis
        redisTemplate.opsForHash().put("PRODUCTS", "PRODUCTS_" + prod.getId(), prod);

     return prod;
    }

    @Override
    public List<Product> getAllProducts() {

        ProductRequestDTO[] fakeStoreDTOList = restTemplate.getForObject("https://fakestoreapi.com/products", ProductRequestDTO[].class);
        //convert  list of FakeStoreDTO to  list Product
       List<Product> products = new ArrayList<>();
        if(fakeStoreDTOList == null) {
            throw new NoProductsFoundException("No products found");
        }
        for (ProductRequestDTO fakeStoreDTO : fakeStoreDTOList) {
            products.add(fakeStoreDTO.toProduct());
        }

        return products;
    }

    @Override
    public Product replaceProduct(Product newProduct, Long id) {

        ProductRequestDTO reuest = new ProductRequestDTO(newProduct.getTitle(),newProduct.getPrice(),newProduct.getDescription(),newProduct.getImage(),newProduct.getCategory().getName());
        reuest.setTitle(newProduct.getTitle());
        reuest.setPrice(newProduct.getPrice());
        reuest.setDescription(newProduct.getDescription());
        reuest.setImage(newProduct.getImage());
        reuest.setCategory(newProduct.getCategory().getName());



        RequestCallback requestCallback = restTemplate.httpEntityCallback(newProduct, ProductRequestDTO.class);
        HttpMessageConverterExtractor<ProductRequestDTO> responseExtractor = new HttpMessageConverterExtractor<>(ProductRequestDTO.class, restTemplate.getMessageConverters());
        ProductRequestDTO res  = restTemplate.execute("https://fakestoreapi.com/products/" + id, HttpMethod.PUT, requestCallback, responseExtractor);
        return res.toProduct();
    }

    @Override
    public Product createProduct(Product newProduct) {
        ProductRequestDTO reuest = new ProductRequestDTO();
        reuest.setTitle(newProduct.getTitle());
        reuest.setPrice(newProduct.getPrice());
        reuest.setDescription(newProduct.getDescription());
        reuest.setImage(newProduct.getImage());
        reuest.setCategory(newProduct.getCategory().getName());
        ProductRequestDTO response = restTemplate.postForObject("https://fakestoreapi.com/products", reuest, ProductRequestDTO.class);

        return response.toProduct();


    }

    @Override
    public Product partialUpdateProduct(Product newProduct, Long id) {
        return null;
    }

    @Override
    public Page<Product> searchProducts(String query, List<ProductSearchFilterDTO> filters, SortingCriteria sortingCriteria, Pageable pageable) {
        String lowerCaseQuery = query == null ? "" : query.toLowerCase();
        List<Product> products = getAllProducts().stream()
                .filter(product -> product.getTitle() != null && product.getTitle().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());

        products = applyFilters(products, filters);
        products = applySorting(products, sortingCriteria);

        return applyPagination(products, pageable);
    }

    private List<Product> applyFilters(List<Product> products, List<ProductSearchFilterDTO> filters) {
        if (filters == null || filters.isEmpty()) {
            return products;
        }

        Set<String> categoryFilter = filters.stream()
                .map(ProductSearchFilterDTO::getCategory)
                .filter(Objects::nonNull)
                .map(category -> category.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        Double minPrice = filters.stream()
                .map(ProductSearchFilterDTO::getMinPrice)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);

        Double maxPrice = filters.stream()
                .map(ProductSearchFilterDTO::getMaxPrice)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return List.of();
        }

        return products.stream()
                .filter(product -> categoryFilter.isEmpty() ||
                        (product.getCategory() != null &&
                                categoryFilter.contains(product.getCategory().getName().toLowerCase(Locale.ROOT))))
                .filter(product -> minPrice == null ||
                        (product.getPrice() != null && product.getPrice() >= minPrice))
                .filter(product -> maxPrice == null ||
                        (product.getPrice() != null && product.getPrice() <= maxPrice))
                .collect(Collectors.toList());
    }

    private List<Product> applySorting(List<Product> products, SortingCriteria sortingCriteria) {
        if (sortingCriteria == null) {
            return products;
        }

        Comparator<Product> comparator = sortingCriteria.getComparator();
        return products.stream()
                .filter(Objects::nonNull)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Page<Product> applyPagination(List<Product> products, Pageable pageable) {
        if (pageable == null) {
            return new PageImpl<>(products);
        }

        int total = products.size();
        int start = (int) pageable.getOffset();
        if (start >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        int end = Math.min(start + pageable.getPageSize(), total);
        List<Product> subList = products.subList(start, end);
        return new PageImpl<>(subList, pageable, total);
    }
}
