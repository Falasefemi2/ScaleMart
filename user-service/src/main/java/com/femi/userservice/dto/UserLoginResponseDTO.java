package com.femi.userservice.dto;

import com.femi.userservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponseDTO {
    private String token;
    private Long id;
    private String username;
    private String email;
    private Role role;
}