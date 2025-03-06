package emtech.loanManagement.controller;

import emtech.loanManagement.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/generate-token")
    public String generateToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        return jwtUtil.generateToken(userDetails);
    }
}