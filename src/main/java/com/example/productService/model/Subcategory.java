package com.example.productService.model;


import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Subcategory extends BaseModel{

    private String surname;
}
