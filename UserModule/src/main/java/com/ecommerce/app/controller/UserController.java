package com.ecommerce.app.controller;

import com.ecommerce.app.config.AppConfig;
import com.ecommerce.app.payload.UserRequestDTO;
import com.ecommerce.app.payload.UserResponseDTO;
import com.ecommerce.app.payload.UserSearchResponseDTO;
import com.ecommerce.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<UserSearchResponseDTO> getAllUsers(
            @RequestParam(name = "pageNum",defaultValue = AppConfig.PAGE_NUMBER,required = false) Integer pageNum,
            @RequestParam(name = "pageSize",defaultValue = AppConfig.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConfig.SORT_USERS_BY,required = false) String sortBy,
            @RequestParam(name = "sortDir",defaultValue = AppConfig.SORT_DIR,required = false) String sortDir
    ) {
        return ResponseEntity.ok().body(userService.fetchAllUser(pageNum,pageSize,sortBy,sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String id) {
        return ResponseEntity.ok().body(userService.fetchUser(id));
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<UserResponseDTO> getUserByKeycloakId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String keycloakId
    ) {
        if(jwt.getSubject().compareTo(keycloakId)<0)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity.ok().body(userService.fetchUserByKeycloakId(keycloakId));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDTO));
    }

    @PutMapping
    public ResponseEntity<UserResponseDTO> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.updateUser(jwt.getSubject(), userRequestDTO));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }


}