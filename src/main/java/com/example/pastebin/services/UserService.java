package com.example.pastebin.services;


import com.example.pastebin.dto.UserDTO;
import com.example.pastebin.entity.User;
import com.example.pastebin.entity.enums.Role;
import com.example.pastebin.mappers.UserMapper;
import com.example.pastebin.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public void createUser(UserDTO userDTO) {
        String username = userDTO.getUsername();

        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("User is not found");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .role(Role.USER)
                .build();

        log.info("Saving new User with username : {}", username);

        userRepository.save(user);
    }

    public UserDTO findUserById(Long id) {
        UserDTO userDTO;

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            userDTO = userMapper.fromUserToDTO(optionalUser.get());
        } else {
            throw new RuntimeException("User not found");
        }

        return userDTO;
    }

    public void editUser(UserDTO userDTO) {
        String username = userDTO.getUsername();

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .role(userDTO.getRole())
                .build();

        userRepository.save(user);

        log.info("Edit new User with username : {}", username);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        } else {
            userRepository.deleteById(id);
        }
    }
}
