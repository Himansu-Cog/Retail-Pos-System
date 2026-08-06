package com.cognizant.retailpos.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cognizant.retailpos.dto.UserDto;
import com.cognizant.retailpos.enums.UserRole;
import com.cognizant.retailpos.repository.UserRepository;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private static final Logger log = LoggerFactory.getLogger(StaffController.class);
    private final UserRepository userRepository;
    public StaffController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping("/cashiers")
    public List<UserDto> cashiers() {
        log.info("Listing active cashiers");
        return userRepository.findActiveByRole(UserRole.CASHIER)
                .stream().map(UserDto::from).toList();
    }
}
