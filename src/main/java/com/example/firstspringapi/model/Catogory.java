package com.example.firstspringapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Catogory extends BaseModel{
    private String name;

    private String description;

    @OneToMany
    private List<Product> futureProducts;

    @OneToMany(mappedBy = "category")
    private List<Product> allProduct;



}
