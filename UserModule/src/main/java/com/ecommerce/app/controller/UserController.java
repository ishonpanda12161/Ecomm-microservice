package com.ecommerce.app.controller;

import com.ecommerce.app.payload.UserRequestDTO;
import com.ecommerce.app.payload.UserResponseDTO;
import com.ecommerce.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok().body(userService.fetchAllUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String id) {
        return ResponseEntity.ok().body(userService.fetchUser(id));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String id,
                                             @RequestBody @Valid UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.updateUser(id, userRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}