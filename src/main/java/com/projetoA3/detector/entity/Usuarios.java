package com.projetoA3.detector.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; 
import jakarta.persistence.*;
import java.math.BigDecimal; // Import necessário
import java.time.LocalTime;  // Import necessário
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column
    private Double latitudeAtual;

    @Column
    private Double longitudeAtual;

    @JsonIgnore 
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cartao> cartoes;

    @Column(nullable = false)
    private boolean ativo = true;

    // --- CAMPOS DE PADRÃO (CORRIGIDOS) ---
    @Column
    private BigDecimal mediaGasto; 

    @Column
    // MUDANÇA AQUI: De 'int' para 'Integer' e inicializando com 0
    private Integer totalTransacoesParaMedia = 0; 

    @Column
    private LocalTime horarioHabitualInicio; 

    @Column
    private LocalTime horarioHabitualFim; 

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public List<Cartao> getCartoes() { return cartoes; }
    public void setCartoes(List<Cartao> cartoes) { this.cartoes = cartoes; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    // Novos Getters e Setters
    public BigDecimal getMediaGasto() { return mediaGasto; }
    public void setMediaGasto(BigDecimal mediaGasto) { this.mediaGasto = mediaGasto; }

    // MUDANÇA AQUI: Getter e Setter agora usam Integer
    public Integer getTotalTransacoesParaMedia() { 
        return totalTransacoesParaMedia == null ? 0 : totalTransacoesParaMedia; 
    }
    public void setTotalTransacoesParaMedia(Integer totalTransacoesParaMedia) { 
        this.totalTransacoesParaMedia = totalTransacoesParaMedia; 
    }

    public LocalTime getHorarioHabitualInicio() { return horarioHabitualInicio; }
    public void setHorarioHabitualInicio(LocalTime horarioHabitualInicio) { this.horarioHabitualInicio = horarioHabitualInicio; }

    public LocalTime getHorarioHabitualFim() { return horarioHabitualFim; }
    public void setHorarioHabitualFim(LocalTime horarioHabitualFim) { this.horarioHabitualFim = horarioHabitualFim; }

    public Double getLatitudeAtual() { return latitudeAtual; }
    public void setLatitudeAtual(Double latitudeAtual) { this.latitudeAtual = latitudeAtual; }

    public Double getLongitudeAtual() { return longitudeAtual; }
    public void setLongitudeAtual(Double longitudeAtual) { this.longitudeAtual = longitudeAtual; }
}