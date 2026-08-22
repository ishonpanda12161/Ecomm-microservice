package com.ecommerce.app.service;

import com.ecommerce.app.model.Address;
import com.ecommerce.app.payload.AddressDTO;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AddressService {
    Address addAddress(String keycloakId,@Valid AddressDTO addressDTO);

    @Transactional(readOnly = true)
    List<AddressDTO> getAllAddress(String keycloakId);

    @Transactional
    Address updateAddress(String addressId, @Valid AddressDTO addressDTO);

    @Transactional
    Address deleteAddress(String addressId);
}
