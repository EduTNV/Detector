package com.projetoA3.detector.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PathVariable; 

import com.projetoA3.detector.dto.CartaoDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO;  
import com.projetoA3.detector.service.CartaoServico;
import java.util.List; 

@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    private final CartaoServico cartaoServico;

    @Autowired
    public CartaoController(CartaoServico cartaoServico) {
        this.cartaoServico = cartaoServico;
    }
 
    @PostMapping
    public ResponseEntity<CartaoDTO> adicionarCartao(@RequestBody CartaoDTO cartaoDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        CartaoDTO cartaoSalvo = cartaoServico.adicionarCartao(cartaoDto, emailUsuario);
        return new ResponseEntity<>(cartaoSalvo, HttpStatus.CREATED);  
    }

    @GetMapping("/meus")
    public ResponseEntity<List<CartaoDTO>> getCartoesDoUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();
        List<CartaoDTO> cartoes = cartaoServico.buscarCartoesPorUsuarioEmail(emailUsuario);
        return ResponseEntity.ok(cartoes);
    }
 
    @GetMapping("/{cartaoId}/transacoes")
    public ResponseEntity<List<TransacaoViewDTO>> getTransacoesDoCartao(@PathVariable Long cartaoId) { 
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();
         
        List<TransacaoViewDTO> transacoes = cartaoServico.getTransacoesPorCartaoId(cartaoId, emailUsuario);
        return ResponseEntity.ok(transacoes);  
    }

}