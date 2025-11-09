package com.example.productService.services;

import com.example.productService.DTO.ProductSearchFilterDTO;
import com.example.productService.DTO.SortingCriteria;
import com.example.productService.Execptions.ProductNotFundExpection;
import com.example.productService.model.Catogory;
import com.example.productService.model.Product;
import com.example.productService.repositories.CategoryRepository;
import com.example.productService.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service("ProductService2")
public class PrductService2 implements ProductService {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public PrductService2(ProductRepository productRepository, CategoryRepository categoryRepository ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    @Override
    public Product getProductById(Long id) throws ProductNotFundExpection {

        // Try to read from Redis cache first (optional)
        try {
            Object cached = redisTemplate.opsForHash().get("PRODUCTS", "PRODUCTS_" + id);
            if (cached instanceof Product) {
                return (Product) cached;
            }
        } catch (Exception ignored) {
            // Redis may not be available in all environments; ignore caching errors
        }


        // Fetch from DB and unwrap Optional safely
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFundExpection(id, "not found"));

        // Cache in Redis (if available) using the product id as part of the key
        try {
            redisTemplate.opsForHash().put("PRODUCTS", "PRODUCTS_" + product.getId(), product);
        } catch (Exception ignored) {
        }

        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product replaceProduct(Product newProduct, Long id) {
        return null;
    }

    @Override
    public Product createProduct(Product newProduct) {

        Optional<Catogory> catogory = categoryRepository.findByName(newProduct.getCategory().getName());
        Catogory toBeNull = null;
        if(catogory.isEmpty()) {
            Catogory newCategory = new Catogory();
            newCategory.setName(newProduct.getCategory().getName());
            toBeNull = categoryRepository.save(newCategory);
        } else {
            toBeNull= catogory.get();

        }
        newProduct.setCategory(toBeNull);
        return productRepository.save(newProduct);
    }

    @Override
    public Product partialUpdateProduct(Product newProduct, Long id) throws ProductNotFundExpection {
        Optional<Product> productToUpdateOptional = productRepository.findById(id);

        if(productToUpdateOptional.isEmpty()) {
            throw new ProductNotFundExpection(id, "not found");
        }

        Product productToUpdate = productToUpdateOptional.get();

        if(newProduct.getTitle() != null) {
            productToUpdate.setTitle(newProduct.getTitle());
        }
        if(newProduct.getPrice() != null) {
            productToUpdate.setPrice(newProduct.getPrice());
        }
        if(newProduct.getDescription() != null) {
            productToUpdate.setDescription(newProduct.getDescription());
        }
        if(newProduct.getImage() != null) {
            productToUpdate.setImage(newProduct.getImage());
        }
        if(newProduct.getCategory() != null) {
            Optional<Catogory> catogory = categoryRepository.findByName(newProduct.getCategory().getName());
            Catogory toBeNull = null;
            if(catogory.isEmpty()) {
                Catogory newCategory = new Catogory();
                newCategory.setName(newProduct.getCategory().getName());
                toBeNull= categoryRepository.save(newCategory);
            } else {
                toBeNull= catogory.get();

            }
            productToUpdate.setCategory(toBeNull);
        }

        return productRepository.save(productToUpdate);
    }

    @Override
    public Page<Product> searchProducts(String query, List<ProductSearchFilterDTO> filters, SortingCriteria sortingCriteria, Pageable pageable) {
        String effectiveQuery = query == null ? "" : query;
        List<Product> products = productRepository.findByTitleContainingIgnoreCase(effectiveQuery);

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
