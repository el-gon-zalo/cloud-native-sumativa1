package com.duoc.sumativa2.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class HolaMundoController {

 
    @GetMapping("/hola")
    public String holaMundo() {
       return "Bienvenido a la Sumativa 2 del curso Cloud Native de Gonzalo Riquelme";
    }


}