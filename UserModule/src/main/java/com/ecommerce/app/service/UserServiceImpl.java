package com.ecommerce.app.service;

import com.ecommerce.app.config.HttpService.KeycloakAdminService;
import com.ecommerce.app.exception.ResourceAlreadyExistsException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.AddressMapper;
import com.ecommerce.app.mapper.UserMapper;
import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.User;
import com.ecommerce.app.payload.AddressDTO;
import com.ecommerce.app.payload.UserRequestDTO;
import com.ecommerce.app.payload.UserResponseDTO;
import com.ecommerce.app.repository.AddressRepository;
import com.ecommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final KeycloakAdminService keycloakAdminService;

    @Override
    public List<UserResponseDTO> fetchAllUser() {
        List<User> users = userRepository.findAll();

        //N+1 issue removed
        List<String> userIds = users.stream().map(
                User::getId
        ).toList();

        List<Address> addresses = addressRepository.findByUserIdIn(userIds);

        //O(n*m) filtering -> O(m+n)

        Map<String,List<Address>> userAddress =  addresses.stream()
                .collect(Collectors.groupingBy(Address::getUserId));

        List<UserResponseDTO> userResponseDTOS = users.stream()
                .map(user ->
                {
                    UserResponseDTO userResponseDTO = userMapper.toDto(user);
                    userResponseDTO.setAddressDTOs(addressMapper.toDTOList(userAddress.get(user.getId())));
                    return userResponseDTO;

                }).toList();

        return userResponseDTOS;

    }

    @Override
    public UserResponseDTO fetchUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User","UserId",id,LocalDateTime.now()));
        List<Address> addresses = addressRepository.findByUserId(id);
        UserResponseDTO userResponseDTO = userMapper.toDto(user);
        userResponseDTO.setAddressDTOs(addressMapper.toDTOList(addresses));
        return userResponseDTO;
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User check = userRepository.findByEmail(userRequestDTO.getEmail());
        if(check!=null)
        {
            throw new ResourceAlreadyExistsException("User","Email",userRequestDTO.getEmail(),LocalDateTime.now());
        }
        check = userRepository.findByUsername(userRequestDTO.getUsername());
        if(check!=null)
        {
            throw new ResourceAlreadyExistsException("User","Username",userRequestDTO.getUsername(),LocalDateTime.now());
        }
        User user = userMapper.toEntity(userRequestDTO);

        String token = keycloakAdminService.getAccessToken();
        String keycloakUserId = keycloakAdminService.createUser(token,userRequestDTO);
        user.setKeycloakId(keycloakUserId);

        keycloakAdminService.assignClientRoleToUser(userRequestDTO.getUsername(),"USER",keycloakUserId);

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDTO updateUser(String id, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User","UserId",id,LocalDateTime.now()));

        user.setFirstName(userRequestDTO.getFirstName());
        user.setLastName(userRequestDTO.getLastName());
        user.setUsername(userRequestDTO.getUsername());
        user.setPassword(userRequestDTO.getPassword());
        user.setEmail(userRequestDTO.getEmail());
        user.setPhone(userRequestDTO.getPhone());
        user.setUserRole(userRequestDTO.getUserRole());

        return userMapper.toDto(userRepository.save(user));

    }

    @Override
    public UserResponseDTO deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User","UserId",id,LocalDateTime.now()));
        List<Address> addresses = addressRepository.findByUserId(id);
        addressRepository.deleteAll(addresses);
        userRepository.delete(user);

        return userMapper.toDto(user);
    }

}
