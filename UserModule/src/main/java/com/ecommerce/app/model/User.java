package com.ecommerce.app.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;

    private String keycloakId;

    @NotBlank(message = "FirstName cannot be blank.")
    @Size(min = 2,message = "FirstName contain at least 2 characters.")
    private String firstName;

    @NotBlank
    @Size(min = 1,message = "LastName contain at least 1 characters.")
    private String lastName;

    @NotBlank(message = "Username cannot be blank.")
    @Size(min = 3,message = "Username Must contain at least 3 characters.")
    private String username;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 3,message = "Password Must contain at least 3 characters.")
    private String password;

    @Email
    @NotBlank(message = "Email cannot be blank.")
    @Size(min = 3,message = "Email Must contain at least 3 characters.")
    @Indexed(unique = true)
    private String email;

    @Size(min = 10,message = "Phone Number Must contain at least 10 characters.")
    private String phone;

    private UserRole userRole;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
