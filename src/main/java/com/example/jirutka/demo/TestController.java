package com.example.jirutka.demo;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() throws HttpMediaTypeNotSupportedException {
        throw new HttpMediaTypeNotSupportedException("json");
    }
}
