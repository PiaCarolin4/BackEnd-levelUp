package com.levelup.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidResponse {
    private String message;
}
