package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.TransacaoDTO;
import com.projetoA3.detector.dto.TransacaoResponseDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO;
import java.util.List; 

public interface TransacaoServico {


    TransacaoResponseDTO registrarTransacao(TransacaoDTO transacaoDto);

    List<TransacaoViewDTO> buscarPendentesDoUsuario(String emailUsuario);

    TransacaoViewDTO confirmarTransacao(Long transacaoId, String emailUsuarioLogado);
    
    TransacaoViewDTO negarTransacao(Long transacaoId, String emailUsuarioLogado);
}