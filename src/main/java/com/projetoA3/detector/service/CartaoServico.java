package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.CartaoDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO; // <-- IMPORTAR
// import com.projetoA3.detector.entity.Cartao; // <-- NÃO USAR MAIS A ENTIDADE NO RETORNO
// import com.projetoA3.detector.entity.Transacao; // <-- NÃO USAR MAIS A ENTIDADE NO RETORNO
import java.util.List;

public interface CartaoServico {

    /**
     * Adiciona um novo cartão a um usuário existente.
     * @param cartaoDto Os dados do cartão a ser adicionado.
     * @return O DTO do Cartao que foi salvo no banco.
     */
    CartaoDTO adicionarCartao(CartaoDTO cartaoDto, String emailUsuarioLogado); // <-- RETORNO ATUALIZADO
    
    List<CartaoDTO> buscarCartoesPorUsuarioEmail(String email);

    // --- (ASSINATURA ATUALIZADA) ---
    /**
     * Busca todas as transações de um cartão específico.
     * @param cartaoId O ID do cartão.
     * @param emailUsuarioLogado O email do usuário autenticado (para segurança).
     * @return Uma lista de DTOs de transações ordenadas pela data.
     */
    List<TransacaoViewDTO> getTransacoesPorCartaoId(Long cartaoId, String emailUsuarioLogado);
}