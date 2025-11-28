package com.projetoA3.detector.dto;

import java.util.List;

public class UsuarioSimulacaoDTO {
    private Long id;
    private String nome;
    private String email;
    private List<CartaoDTO> cartoes;
 
    public UsuarioSimulacaoDTO(Long id, String nome, String email, List<CartaoDTO> cartoes) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cartoes = cartoes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<CartaoDTO> getCartoes() { return cartoes; }
    public void setCartoes(List<CartaoDTO> cartoes) { this.cartoes = cartoes; }
}