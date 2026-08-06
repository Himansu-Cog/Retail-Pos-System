package com.cognizant.retailpos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.retailpos.dto.UserDto;
import com.cognizant.retailpos.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> currentUser(Authentication authentication) {
        log.info("Loading authenticated user username={}", authentication.getName());
        return ResponseEntity.ok(UserDto.from(userService.findByUsername(authentication.getName())));
    }
}
