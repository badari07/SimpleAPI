package com.example.firstspringapi.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.Generated;
import org.springframework.stereotype.Service;

import java.util.Date;


@MappedSuperclass
@Data
public class BaseModel {
    @Id
    private Long id;

    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;

}
