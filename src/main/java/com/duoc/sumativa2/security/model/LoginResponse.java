package com.duoc.sumativa2.security.model;

import java.util.List;


 //* DTO que devuelve el POST /auth/login al cliente.

public class LoginResponse {

    private String token;
    private String tipo;         // siempre "Bearer"
    private String username;
    private List<String> roles;
    private long expiresIn;      // segundos hasta expiración

    // Constructor 
    public LoginResponse(String token, String username,
                         List<String> roles, long expiresIn) {
        this.token     = token;
        this.tipo      = "Bearer";
        this.username  = username;
        this.roles     = roles;
        this.expiresIn = expiresIn;
    }

    // Getters 
    public String getToken()        { return token; }
    public String getTipo()         { return tipo; }
    public String getUsername()     { return username; }
    public List<String> getRoles()  { return roles; }
    public long getExpiresIn()      { return expiresIn; }
}