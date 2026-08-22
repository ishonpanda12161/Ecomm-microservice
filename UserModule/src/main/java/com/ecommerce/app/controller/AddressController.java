package com.ecommerce.app.controller;

import com.ecommerce.app.model.Address;
import com.ecommerce.app.payload.AddressDTO;
import com.ecommerce.app.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/user")
    public ResponseEntity<Address> addAddress(
            @RequestBody @Valid AddressDTO addressDTO,
            @AuthenticationPrincipal Jwt jwt
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(jwt.getSubject(),addressDTO));
    }

    @GetMapping("/user")
    public ResponseEntity<List<AddressDTO>> getAllAddress(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.getAllAddress(jwt.getSubject()));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable String addressId,
            @RequestBody @Valid AddressDTO addressDTO
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.updateAddress(addressId,addressDTO));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable String addressId
    )
    {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

}
