package com.example.firstspringapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private Long id;
    private String title;
    private double price;
    private String description;
    private String image;
    private Catogory catogory;
}
