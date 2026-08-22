package com.ecommerce.app.repository;

import com.ecommerce.app.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

    User findByEmail(@Email @NotBlank(message = "Email cannot be blank.") @Size(min = 3,message = "Email Must contain at least 3 characters.") String email);

    User findByUsername(@NotBlank(message = "Username cannot be blank.") @Size(min = 3,message = "Username Must contain at least 3 characters.") String username);

    Optional<User> findByKeycloakId(String keycloakId);
}
