package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "Application is running successfully!";
    }
    
    @GetMapping("/api/status")
    @ResponseBody
    public String status() {
        return "Student Management System - H2 Database Active";
    }
}
