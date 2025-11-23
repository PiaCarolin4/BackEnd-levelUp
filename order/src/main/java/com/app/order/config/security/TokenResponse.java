package com.app.order.config.security;

import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String token;

}
