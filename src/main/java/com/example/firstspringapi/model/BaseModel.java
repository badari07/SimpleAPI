package com.example.firstspringapi.model;

import jakarta.annotation.Generated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Date;


@MappedSuperclass
@Data
public class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

}
