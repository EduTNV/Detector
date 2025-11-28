package com.projetoA3.detector.dto;

import java.io.Serializable;

public class AuthRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    private String email;
    private String senha;
 
    public AuthRequest() {
    }

    public AuthRequest(String email, String senha) {
        this.setEmail(email);
        this.setSenha(senha);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}