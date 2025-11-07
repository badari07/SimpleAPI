package com.example.productService.Execptions;

import lombok.Data;

@Data
public class ProductNotFundExpection extends Exception{
    private String message;
    private  Long id;
    public ProductNotFundExpection(Long id,String message) {
        super(message);
        this.id = id;
    }

}
