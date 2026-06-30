package com.duoc.sumativa2.security.model;

//* DTO que recibe el body del POST /auth/login

public class LoginRequest {

    private String username;
    private String password;

    // Constructores 
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}