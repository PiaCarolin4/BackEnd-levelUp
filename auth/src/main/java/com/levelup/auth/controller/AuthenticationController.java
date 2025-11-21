package com.levelup.auth.controller;

import com.levelup.auth.dto.*;
import com.levelup.auth.service.AuthService;
import com.levelup.auth.service.exception.UserAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/authentication")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para gestión de autenticación y autorización")
public class AuthenticationController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y genera token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<EntityModel<AuthResponse>> signIn(@RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        EntityModel<AuthResponse> model = EntityModel.of(authResponse,
                linkTo(methodOn(AuthenticationController.class).signIn(request)).withSelfRel(),
                linkTo(methodOn(AuthenticationController.class).verifyToken(new TokenValidationRequest(authResponse.getToken()))).withRel("verify-token"),
                linkTo(methodOn(AuthenticationController.class).signUp(null)).withRel("register")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Verificar token", description = "Valida la integridad y vigencia de un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/token/verify")
    public ResponseEntity<EntityModel<TokenValidResponse>> verifyToken(@RequestBody TokenValidationRequest request) {
        authService.validateToken(request.getToken());

        TokenValidResponse response = TokenValidResponse.builder()
                .message("Token válido")
                .build();

        EntityModel<TokenValidResponse> model = EntityModel.of(response,
                linkTo(methodOn(AuthenticationController.class).verifyToken(request)).withSelfRel(),
                linkTo(methodOn(AuthenticationController.class).signIn(null)).withRel("sign-in")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<EntityModel<AuthResponse>> signUp(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.createUser(request);

            EntityModel<AuthResponse> model = EntityModel.of(response,
                    linkTo(methodOn(AuthenticationController.class).signUp(request)).withSelfRel(),
                    linkTo(methodOn(AuthenticationController.class).signIn(null)).withRel("sign-in")
            );

            return ResponseEntity.ok(model);
        } catch (UserAlreadyExistsException e) {
            AuthResponse error = AuthResponse.builder().messageResponse(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(EntityModel.of(error));
        } catch (ServiceException e) {
            AuthResponse error = AuthResponse.builder().messageResponse(e.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(EntityModel.of(error));
        }
    }


    
    @Operation(hidden = true)
    @ExceptionHandler({
            BadCredentialsException.class,
            AccessDeniedException.class,
            AuthenticationException.class
    })
    public ResponseEntity<EntityModel<AuthResponse>> handleAuthenticationExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        if (e instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        }

        AuthResponse errorResponse = AuthResponse.builder()
                .messageResponse(e.getMessage())
                .build();

        EntityModel<AuthResponse> model = EntityModel.of(errorResponse,
                linkTo(methodOn(AuthenticationController.class).signIn(null)).withRel("sign-in"),
                linkTo(methodOn(AuthenticationController.class).signUp(null)).withRel("sign-up")
        );

        return ResponseEntity.status(status).body(model);
    }

    @Operation(hidden = true)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<EntityModel<AuthResponse>> handleGenericException(Exception e) {
        logger.error("Error interno del servidor: ", e);

        AuthResponse errorResponse = AuthResponse.builder()
                .messageResponse("Error interno del servidor")
                .build();

        EntityModel<AuthResponse> model = EntityModel.of(errorResponse,
                linkTo(methodOn(AuthenticationController.class).signIn(null)).withRel("sign-in"),
                linkTo(methodOn(AuthenticationController.class).signUp(null)).withRel("sign-up")
        );

        return ResponseEntity.internalServerError().body(model);
    }
}