package com.example.study_project.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping()
    public String test() {
        return "Hello World";
    }

    @GetMapping("/{id}")
    public String testPathVariable(@PathVariable(required = false) int id) {
        return "Hello World! Id " + id;
    }

    
}
