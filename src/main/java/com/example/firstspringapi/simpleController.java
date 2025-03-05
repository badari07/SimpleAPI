package com.example.firstspringapi;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class simpleController {

    @GetMapping("/hello/{name}")
    public String sayHello(@PathVariable String name){
        return "Hello" + " " +   name;
    }
}
