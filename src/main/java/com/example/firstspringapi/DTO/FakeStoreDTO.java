package com.example.firstspringapi.DTO;

import com.example.firstspringapi.model.Catogory;
import lombok.Data;

@Data
public class FakeStoreDTO {
    private Long id;
    private String title;
    private double price;
    private String description;
    private String image;
    private String catogory;
}
