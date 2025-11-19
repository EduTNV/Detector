package com.projetoA3.detector.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // <-- IMPORTAR
import org.springframework.security.core.context.SecurityContextHolder; // <-- IMPORTAR
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projetoA3.detector.dto.TransacaoDTO;
import com.projetoA3.detector.dto.TransacaoResponseDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO; // <-- IMPORTAR
// import com.projetoA3.detector.entity.Transacao; // <-- NÃO USAR MAIS A ENTIDADE NO RETORNO
import com.projetoA3.detector.service.TransacaoServico;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoServico transacaoServico;

    @Autowired
    public TransacaoController(TransacaoServico transacaoServico) {
        this.transacaoServico = transacaoServico;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> registrarTransacao(@RequestBody TransacaoDTO transacaoDto) {
        TransacaoResponseDTO resposta = transacaoServico.registrarTransacao(transacaoDto);
        return ResponseEntity.ok(resposta);
    }

    // --- (ENDPOINT ATUALIZADO) ---
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<TransacaoViewDTO> confirmarTransacao(@PathVariable Long id) {
        // Extrai o email do usuário logado (do token)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();
        
        TransacaoViewDTO transacao = transacaoServico.confirmarTransacao(id, emailUsuario);
        return ResponseEntity.ok(transacao); // Retorna o DTO seguro
    }

    // --- (ENDPOINT ATUALIZADO) ---
    @PostMapping("/{id}/negar")
    public ResponseEntity<TransacaoViewDTO> negarTransacao(@PathVariable Long id) {
        // Extrai o email do usuário logado (do token)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        TransacaoViewDTO transacao = transacaoServico.negarTransacao(id, emailUsuario);
        return ResponseEntity.ok(transacao); // Retorna o DTO seguro
    }
    @GetMapping("/pendentes")
    public ResponseEntity<List<TransacaoViewDTO>> verificarPendencias() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        List<TransacaoViewDTO> pendentes = transacaoServico.buscarPendentesDoUsuario(emailUsuario);
        return ResponseEntity.ok(pendentes);
    }
}