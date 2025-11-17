package com.projetoA3.detector.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication; // Importe este
import org.springframework.security.core.context.SecurityContextHolder; // Importe este
import org.springframework.web.bind.annotation.GetMapping; // Importe este
import org.springframework.web.bind.annotation.PathVariable;

import com.projetoA3.detector.dto.CartaoDTO;
import com.projetoA3.detector.entity.Cartao;
import com.projetoA3.detector.entity.Transacao;
import com.projetoA3.detector.service.CartaoServico;
import java.util.List; // <--- ADICIONE ESTA LINHA

@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    private final CartaoServico cartaoServico;

    @Autowired
    public CartaoController(CartaoServico cartaoServico) {
        this.cartaoServico = cartaoServico;
    }

    @PostMapping
    public ResponseEntity<Cartao> adicionarCartao(@RequestBody CartaoDTO cartaoDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        Cartao cartaoSalvo = cartaoServico.adicionarCartao(cartaoDto, emailUsuario);
        return new ResponseEntity<>(cartaoSalvo, HttpStatus.CREATED);
    }

    @GetMapping("/meus")
    public ResponseEntity<List<CartaoDTO>> getCartoesDoUsuario() {

        // 1. Obtém o objeto de autenticação (contém o email do usuário logado)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. O 'getName()' na Autenticação é o email, conforme configurado no
        // CustomUserDetailsService
        String emailUsuario = authentication.getName();

        // 3. Chama o serviço para buscar os cartões
        List<CartaoDTO> cartoes = cartaoServico.buscarCartoesPorUsuarioEmail(emailUsuario);

        // 4. Retorna a lista de cartões (DTOs)
        return ResponseEntity.ok(cartoes);
    }
    
    @GetMapping("/{cartaoId}/transacoes")
    public ResponseEntity<List<Transacao>> getTransacoesDoCartao(@PathVariable Long cartaoId) {
        // (Opcional: Adicionar verificação se o cartaoId pertence ao usuário logado)
        List<Transacao> transacoes = cartaoServico.getTransacoesPorCartaoId(cartaoId);
        return ResponseEntity.ok(transacoes);
    }

}
