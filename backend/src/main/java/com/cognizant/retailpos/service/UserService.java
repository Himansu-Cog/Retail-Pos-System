package com.cognizant.retailpos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.retailpos.entity.User;
import com.cognizant.retailpos.exception.DuplicateResourceException;
import com.cognizant.retailpos.exception.ResourceNotFoundException;
import com.cognizant.retailpos.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> findAll(String search, int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by("fullName").ascending());
        if (search == null || search.isBlank()) return userRepository.findAll(request);
        return userRepository.search(search.trim(), request);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User create(User user) {
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, User input) {
        User existing = findById(id);
        if (!existing.getUsername().equalsIgnoreCase(input.getUsername())
                && userRepository.existsByUsernameIgnoreCase(input.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (!existing.getEmail().equalsIgnoreCase(input.getEmail())
                && userRepository.existsByEmailIgnoreCase(input.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        existing.setUsername(input.getUsername());
        existing.setFullName(input.getFullName());
        existing.setEmail(input.getEmail());
        existing.setRole(input.getRole());
        existing.setActive(input.isActive());
        if (input.getPassword() != null && !input.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(input.getPassword()));
        }
        return userRepository.save(existing);
    }

    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
