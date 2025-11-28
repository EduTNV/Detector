package com.projetoA3.detector.dto;

import com.projetoA3.detector.entity.Transacao;
 
public class TransacaoResponseDTO {
     
    private String statusResposta; 
     
    private String mensagem;
     
    private Transacao transacao;

    public TransacaoResponseDTO(String statusResposta, String mensagem, Transacao transacao) {
        this.statusResposta = statusResposta;
        this.mensagem = mensagem;
        this.transacao = transacao;
    } 
    public String getStatusResposta() {
        return statusResposta;
    }

    public void setStatusResposta(String statusResposta) {
        this.statusResposta = statusResposta;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Transacao getTransacao() {
        return transacao;
    }

    public void setTransacao(Transacao transacao) {
        this.transacao = transacao;
    }
}