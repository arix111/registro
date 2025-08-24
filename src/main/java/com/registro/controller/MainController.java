package com.registro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "redirect:/usuarios";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "redirect:/usuarios";
    }
    
    @GetMapping("/index")
    public String indexPage() {
        return "redirect:/usuarios";
    }
}
