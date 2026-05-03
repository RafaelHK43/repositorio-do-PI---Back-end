package br.edu.senac.sistema_ac.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String root() {
        return "SGAC Back-end está online e rodando no Render!";
    }
}
