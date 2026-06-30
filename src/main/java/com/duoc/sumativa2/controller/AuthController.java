package com.duoc.sumativa2.controller;

import com.duoc.sumativa2.security.model.LoginRequest;
import com.duoc.sumativa2.security.model.LoginResponse;
import com.duoc.sumativa2.security.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Constructor 
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil               = jwtUtil;
    }

    
    // POST /auth/login
    // Body: { "username": "admin", "password": "1234" }
    // Respuesta exitosa (200):
    // {
    //   "token": "eyJhbGci...",
    //   "tipo": "Bearer",
    //   "username": "admin",
    //   "roles": ["ROLE_ADMIN"],
    //   "expiresIn": 86400
    // }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Username y password son obligatorios"));
        }

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername().trim(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtUtil.generateToken(userDetails);

            LoginResponse response = new LoginResponse(
                    token,
                    userDetails.getUsername(),
                    roles,
                    jwtUtil.getExpirationSeconds()
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            // Credenciales incorrectas → 401
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));

        } catch (DisabledException ex) {
            // Usuario deshabilitado → 403
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Usuario deshabilitado"));

        } catch (Exception ex) {
            // Error inesperado → 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al autenticar"));
        }
    }

    // GET /auth/me   (requiere token valido en el header)
    // util para que se verifique quien esta logueado
    // Header: Authorization: Bearer <token>
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "roles",    roles
        ));
    }
}