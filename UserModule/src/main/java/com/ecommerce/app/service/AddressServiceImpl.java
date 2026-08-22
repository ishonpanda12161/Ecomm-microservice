package com.ecommerce.app.service;

import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.AddressMapper;
import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.User;
import com.ecommerce.app.payload.AddressDTO;
import com.ecommerce.app.repository.AddressRepository;
import com.ecommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService{

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserRepository userRepository;

    @Override
    public Address addAddress(String keycloakId,AddressDTO addressDTO) {

        User user = getUser(keycloakId);
        Address address = addressMapper.toEntity(addressDTO);
        address.setUserId(user.getId());

        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AddressDTO> getAllAddress(String keycloakId) {
        User user = getUser(keycloakId);
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return addressMapper.toDTOList(addresses);
    }

    @Override
    public Address updateAddress(String addressId, AddressDTO addressDTO) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address","AddressId",addressId,LocalDateTime.now()));

        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setZipcode(addressDTO.getZipcode());

        return addressRepository.save(address);

    }

    @Override
    public Address deleteAddress(String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address","AddressId",addressId,LocalDateTime.now()));
        addressRepository.delete(address);
        return address;
    }

    private User getUser(String keycloakId)
    {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(()-> new ResourceNotFoundException("User","Keycloak ID",keycloakId,LocalDateTime.now()));
        return user;
    }
}
