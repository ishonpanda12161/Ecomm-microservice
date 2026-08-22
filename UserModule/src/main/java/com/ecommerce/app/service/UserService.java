package com.ecommerce.app.service;


import com.ecommerce.app.payload.UserRequestDTO;
import com.ecommerce.app.payload.UserResponseDTO;
import com.ecommerce.app.payload.UserSearchResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    UserSearchResponseDTO fetchAllUser(Integer pageNum, Integer pageSize, String sortBy, String sortDir);

    UserResponseDTO fetchUser(String id);

    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    UserResponseDTO updateUser(String keycloakId, UserRequestDTO userRequestDTO);

    UserResponseDTO deleteUser(String keycloakId);

    UserResponseDTO fetchUserByKeycloakId(String keycloakId);

}
