package com.almazbekov.SkillUp.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/api/main")
    public String mainListener() {
        return "Hello world";
    }
}
