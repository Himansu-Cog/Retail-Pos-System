package com.cognizant.retailpos.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cognizant.retailpos.dto.UserDto;
import com.cognizant.retailpos.entity.User;
import com.cognizant.retailpos.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public Page<UserDto> findAll(@RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        log.info("Listing users: page={}, size={}", page, size);
        return userService.findAll(search, page, size).map(UserDto::from);
    }
    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        log.info("Loading user id={}", id);
        return UserDto.from(userService.findById(id));
    }
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody User user) {
        log.info("Creating user username={}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(userService.create(user)));
    }
    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody User user) {
        log.info("Updating user id={}", id);
        return UserDto.from(userService.update(id, user));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting user id={}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
