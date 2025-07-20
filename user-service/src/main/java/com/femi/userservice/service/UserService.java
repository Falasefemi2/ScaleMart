package com.femi.userservice.service;

import com.femi.userservice.auth.JwtTokenProvider;
import com.femi.userservice.dto.UserDetailsDTO;
import com.femi.userservice.dto.UserLoginDTO;
import com.femi.userservice.dto.UserLoginResponseDTO;
import com.femi.userservice.dto.UserRegisterDTO;
import com.femi.userservice.model.Role;
import com.femi.userservice.model.User;
import com.femi.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserDetailsDTO registerUser(UserRegisterDTO userRegisterDTO) {
        if(userRepository.existsByEmail(userRegisterDTO.getEmail())) {
            throw new IllegalStateException("Email address already in use");
        }

        if(userRepository.existsByUsername(userRegisterDTO.getUsername())) {
            throw new IllegalStateException("Username already in use");
        }

        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .email(userRegisterDTO.getEmail())
                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                .role(Role.BUYER)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with email: {}", user.getEmail());

        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserLoginResponseDTO loginUser(UserLoginDTO userLoginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
            );

            User user = userRepository.findByEmail(userLoginDTO.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = jwtTokenProvider.generateToken(user);
            log.info("User logged in successfully: {}", user.getEmail());

            return UserLoginResponseDTO.builder()
                    .token(token)
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for email: {}", userLoginDTO.getEmail());
            throw new IllegalArgumentException("Invalid email or password", e);
        }
    }

    @Transactional(readOnly = true)
    public UserDetailsDTO getUserDetails(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public void requestSellerRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if(user.getRole() != Role.BUYER) {
            throw new IllegalStateException("Only buyers can request seller role");
        }

        user.setRole(Role.SELLER_PENDING);
        userRepository.save(user);
        log.info("User {} requested seller role", user.getEmail());
    }

    public UserDetailsDTO approveSellerRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if(user.getRole() != Role.SELLER_PENDING) {
            throw new IllegalStateException("User is not in pending seller state");
        }

        user.setRole(Role.SELLER);
        user = userRepository.save(user);
        log.info("User {} approved as seller", user.getEmail());

        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserDetailsDTO> getAllPendingSellers() {
        List<User> pendingSellers = userRepository.findAllByRole(Role.SELLER_PENDING);
        return pendingSellers.stream()
                .map(user -> UserDetailsDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    public void rejectSellerRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if(user.getRole() != Role.SELLER_PENDING) {
            throw new IllegalStateException("User is not in pending seller state");
        }

        user.setRole(Role.BUYER);
        userRepository.save(user);
        log.info("User {} seller request rejected", user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserDetailsDTO getCurrentUserDetails(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserDetailsDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build());
    }
}