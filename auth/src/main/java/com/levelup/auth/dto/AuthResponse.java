package com.levelup.auth.dto;

import com.levelup.auth.model.Role;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Date issuedAt;
    private Date expiresAt;
    private String username;
    private Set<Role> roles;
    private String messageResponse;

}
