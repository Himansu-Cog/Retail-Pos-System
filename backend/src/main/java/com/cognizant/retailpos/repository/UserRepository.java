package com.cognizant.retailpos.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cognizant.retailpos.entity.User;
import com.cognizant.retailpos.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    @Query("select u from User u where lower(u.username) like lower(concat('%', :text, '%')) " +
            "or lower(u.fullName) like lower(concat('%', :text, '%'))")
    Page<User> search(@Param("text") String text, Pageable pageable);

    @Query("select u from User u where u.role = :role and u.active = true order by u.fullName")
    java.util.List<User> findActiveByRole(@Param("role") UserRole role);
}
