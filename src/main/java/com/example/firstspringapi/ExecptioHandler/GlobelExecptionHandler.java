package com.example.firstspringapi.ExecptioHandler;

import com.example.firstspringapi.DTO.ExecptionDTO;
import com.example.firstspringapi.DTO.ProductNotFundExpectionDTO;
import com.example.firstspringapi.Execptions.ProductNotFundExpection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobelExecptionHandler {

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<ExecptionDTO> handleArimeticExecptions() {

        ExecptionDTO execptionDTO = new ExecptionDTO();
        execptionDTO.setMessage("somthing went wrong");
        execptionDTO.setResolution("arithmetic cant do much");
            return new ResponseEntity<>(execptionDTO,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFundExpection.class)
        public ResponseEntity<ProductNotFundExpectionDTO> handleProductNotFundExpection(ProductNotFundExpection ex) {
        ProductNotFundExpectionDTO productNotFundExpectionDTO = new ProductNotFundExpectionDTO();
        productNotFundExpectionDTO.setMessage("product with id is not there" + " :  " +ex.getId());

        return new ResponseEntity<>(productNotFundExpectionDTO,HttpStatus.NOT_FOUND);
    }
}

