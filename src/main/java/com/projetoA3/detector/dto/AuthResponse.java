package com.projetoA3.detector.dto;

import java.io.Serializable;

public class AuthResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String token;
    private final String nome;
    private final String email;

    public AuthResponse(String token, String nome, String email) {
        this.token = token;
        this.nome = nome;
        this.email = email;
    }

    public String getToken() {
        return this.token;
    }

    public String getNome() {
        return this.nome;
    }

    public String getEmail() {
        return this.email;
    }
}