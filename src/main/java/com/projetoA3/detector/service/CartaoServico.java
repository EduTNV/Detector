package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.CartaoDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO;  
import java.util.List;

public interface CartaoServico {
 
    CartaoDTO adicionarCartao(CartaoDTO cartaoDto, String emailUsuarioLogado);  
    
    List<CartaoDTO> buscarCartoesPorUsuarioEmail(String email);
 
    List<TransacaoViewDTO> getTransacoesPorCartaoId(Long cartaoId, String emailUsuarioLogado);
}