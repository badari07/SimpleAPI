package com.example.productService.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Catogory extends BaseModel{
    @Column(name = "Category_name", unique = true)
    private String name;

    @Basic(fetch = FetchType.LAZY)
    private String description;

    @OneToMany(fetch = jakarta.persistence.FetchType.EAGER)
    private List<Product> futureProducts;

    @OneToMany(mappedBy = "category")
    private List<Product> allProduct;

    @ManyToOne
    private Subcategory subcategory;




}
