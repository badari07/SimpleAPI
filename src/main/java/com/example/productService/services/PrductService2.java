package com.example.productService.services;

import com.example.productService.Execptions.ProductNotFundExpection;
import com.example.productService.model.Catogory;
import com.example.productService.model.Product;
import com.example.productService.repositories.CategoryRepository;
import com.example.productService.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service("ProductService2")
public class PrductService2 implements ProductService {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    public PrductService2(ProductRepository productRepository, CategoryRepository categoryRepository ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    @Override
    public Product getProductById(Long id) throws ProductNotFundExpection {

            Optional<Product> product = productRepository.findById(id);
            if(product.isEmpty()) {
                throw new ProductNotFundExpection(id, "not found");
            }
            return product.get();
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
}
