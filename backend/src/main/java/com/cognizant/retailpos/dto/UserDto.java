package com.cognizant.retailpos.dto;

import java.time.LocalDateTime;
import com.cognizant.retailpos.entity.User;
import com.cognizant.retailpos.enums.UserRole;

public record UserDto(Long id, String username, String fullName, String email,
                      UserRole role, boolean active, LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                user.getRole(), user.isActive(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
