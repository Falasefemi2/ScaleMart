package com.femi.userservice.controller;

import com.femi.userservice.dto.*;
import com.femi.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> registerUser(
            @Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        try {
            UserDetailsDTO userDetails = userService.registerUser(userRegisterDTO);
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(userDetails)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("Error during user registration", e);
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(false)
                    .message("Registration failed")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponseDTO>> loginUser(
            @Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            UserLoginResponseDTO loginResponse = userService.loginUser(userLoginDTO);
            ApiResponse<UserLoginResponseDTO> response = ApiResponse.<UserLoginResponseDTO>builder()
                    .success(true)
                    .message("Login successful")
                    .data(loginResponse)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<UserLoginResponseDTO> response = ApiResponse.<UserLoginResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            log.error("Error during user login", e);
            ApiResponse<UserLoginResponseDTO> response = ApiResponse.<UserLoginResponseDTO>builder()
                    .success(false)
                    .message("Login failed")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUYER') or hasRole('SELLER')) and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> getUserDetails(@PathVariable Long id) {
        try {
            UserDetailsDTO userDetails = userService.getUserDetails(id);
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(true)
                    .message("User details retrieved successfully")
                    .data(userDetails)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> getCurrentUserDetails(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserDetailsDTO userDetails = userService.getCurrentUserDetails(email);
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(true)
                    .message("Current user details retrieved successfully")
                    .data(userDetails)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/request-seller")
    public ResponseEntity<ApiResponse<String>> requestSellerRole(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserDetailsDTO currentUser = userService.getCurrentUserDetails(email);
            userService.requestSellerRole(currentUser.getId());

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Seller role request submitted successfully")
                    .data("Your request is pending admin approval")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/approve-seller/{userId}")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> approveSellerRole(@PathVariable Long userId) {
        try {
            UserDetailsDTO userDetails = userService.approveSellerRole(userId);
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(true)
                    .message("Seller role approved successfully")
                    .data(userDetails)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            ApiResponse<UserDetailsDTO> response = ApiResponse.<UserDetailsDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/reject-seller/{userId}")
    public ResponseEntity<ApiResponse<String>> rejectSellerRole(@PathVariable Long userId) {
        try {
            userService.rejectSellerRole(userId);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Seller role request rejected successfully")
                    .data("User has been reverted to buyer role")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/pending-sellers")
    public ResponseEntity<ApiResponse<List<UserDetailsDTO>>> getAllPendingSellers() {
        try {
            List<UserDetailsDTO> pendingSellers = userService.getAllPendingSellers();
            ApiResponse<List<UserDetailsDTO>> response = ApiResponse.<List<UserDetailsDTO>>builder()
                    .success(true)
                    .message("Pending sellers retrieved successfully")
                    .data(pendingSellers)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving pending sellers", e);
            ApiResponse<List<UserDetailsDTO>> response = ApiResponse.<List<UserDetailsDTO>>builder()
                    .success(false)
                    .message("Failed to retrieve pending sellers")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserDetailsDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.Direction.fromString(sortDir), sortBy);
            Page<UserDetailsDTO> users = userService.getAllUsers(pageable);

            ApiResponse<List<UserDetailsDTO>> response = ApiResponse.<List<UserDetailsDTO>>builder()
                    .success(true)
                    .message("Users retrieved successfully")
                    .data(users.getContent())
                    .totalElements(users.getTotalElements())
                    .totalPages(users.getTotalPages())
                    .currentPage(users.getNumber())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all users", e);
            ApiResponse<List<UserDetailsDTO>> response = ApiResponse.<List<UserDetailsDTO>>builder()
                    .success(false)
                    .message("Failed to retrieve users")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Logout successful")
                    .data("User logged out successfully")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during logout", e);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message("Logout failed")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .message("Access denied: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}