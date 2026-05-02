package br.com.notafacil.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class TestController {

    @PostMapping("/test-login")
    public String testLogin(@RequestBody String body) {
        return "TEST_OK: " + body;
    }

    @GetMapping("/test-health")
    public String testHealth() {
        return "TEST_HEALTH_OK";
    }
}
