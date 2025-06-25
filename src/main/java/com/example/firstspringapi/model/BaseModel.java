package com.example.firstspringapi.model;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Date;

@Data
public class BaseModel {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

}
