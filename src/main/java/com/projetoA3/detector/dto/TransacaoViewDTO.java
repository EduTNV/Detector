package com.projetoA3.detector.dto;

import com.projetoA3.detector.entity.Transacao;
import com.projetoA3.detector.entity.TransacaoStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO seguro para *exibir* dados de transação no frontend.
 * (Diferente do TransacaoDTO, que é para *receber* dados).
 */
public class TransacaoViewDTO {

    private Long id;
    private BigDecimal valor;
    private String estabelecimento;
    private LocalDateTime dataHora;
    private TransacaoStatus status;

    // Construtor vazio
    public TransacaoViewDTO() {
    }

    // Construtor para facilitar a conversão
    public TransacaoViewDTO(Transacao transacao) {
        this.id = transacao.getId();
        this.valor = transacao.getValor();
        this.estabelecimento = transacao.getEstabelecimento();
        this.dataHora = transacao.getDataHora();
        this.status = transacao.getStatus();
    }

    // Getters e Setters
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

    public TransacaoStatus getStatus() {
        return status;
    }

    public void setStatus(TransacaoStatus status) {
        this.status = status;
    }
}