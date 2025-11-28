package com.projetoA3.detector.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;  
import com.fasterxml.jackson.annotation.JsonIgnore;
 
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Point;  

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private String estabelecimento;

    @Column(nullable = false)
    private LocalDateTime dataHora;
 
    @JsonBackReference  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id", nullable = false)
    private Cartao cartao;

    @JsonIgnore
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point localizacao;

    @Column
    private String ipAddress;

    @Enumerated(EnumType.STRING)  
    @Column(nullable = false)
    private TransacaoStatus status;

     
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(String estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Cartao getCartao() {
        return cartao;
    }

    public void setCartao(Cartao cartao) {
        this.cartao = cartao;
    }

    public Point getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(Point localizacao) {
        this.localizacao = localizacao;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public TransacaoStatus getStatus() {
        return status;
    }

    public void setStatus(TransacaoStatus status) {
        this.status = status;
    }
}