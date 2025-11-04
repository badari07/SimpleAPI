package com.example.firstspringapi.model;


import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Subcategory extends BaseModel{

    private String surname;
}
