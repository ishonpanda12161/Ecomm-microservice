package com.ecommerce.app.payload;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponseDTO {

    private String id;
    private String keycloakId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private List<AddressDTO> addressDTOs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
