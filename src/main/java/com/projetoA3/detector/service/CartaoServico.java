package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.CartaoDTO;
import com.projetoA3.detector.entity.Cartao;
import com.projetoA3.detector.entity.Transacao; 
import java.util.List;

public interface CartaoServico {
    Cartao adicionarCartao(CartaoDTO cartaoDto, String emailUsuarioLogado);
    List<CartaoDTO> buscarCartoesPorUsuarioEmail(String email);
    List<Transacao> getTransacoesPorCartaoId(Long cartaoId);
}
