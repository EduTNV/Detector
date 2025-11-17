package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.CartaoDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO; // <-- IMPORTAR
import com.projetoA3.detector.entity.Cartao;
import com.projetoA3.detector.entity.Transacao;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.repository.CartaoRepositorio;
import com.projetoA3.detector.repository.TransacaoRepositorio; 
import com.projetoA3.detector.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // <-- IMPORTAR
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; // <-- IMPORTAR
import java.util.stream.Collectors;

@Service
public class CartaoServicoImpl implements CartaoServico {

    private final CartaoRepositorio cartaoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final TransacaoRepositorio transacaoRepositorio; 

    @Autowired
    public CartaoServicoImpl(CartaoRepositorio cartaoRepositorio, 
                             UsuarioRepositorio usuarioRepositorio,
                             TransacaoRepositorio transacaoRepositorio) {
        this.cartaoRepositorio = cartaoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.transacaoRepositorio = transacaoRepositorio; 
    }

    @Override
    public List<CartaoDTO> buscarCartoesPorUsuarioEmail(String email) {
        List<Cartao> cartoes = cartaoRepositorio.findByUsuarioEmail(email);
        return cartoes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- (MÉTODO ATUALIZADO - RETORNA DTO) ---
    @Override
    public CartaoDTO adicionarCartao(CartaoDTO cartaoDTO, String emailUsuarioLogado) {
        String numeroCartao = cartaoDTO.getNumero().replaceAll("\\s+", ""); 
        if (!isLuhnValid(numeroCartao)) {
            throw new IllegalArgumentException("Número de cartão inválido.");
        }
        String bandeira = identificarBandeira(numeroCartao);
        if ("DESCONHECIDA".equals(bandeira)) {
            throw new IllegalArgumentException("Bandeira do cartão não suportada ou desconhecida.");
        }

        Usuarios usuario = usuarioRepositorio.findByEmailAndAtivoTrue(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado: " + emailUsuarioLogado));
        
        Cartao novoCartao = new Cartao();
        novoCartao.setNumero(numeroCartao); 
        novoCartao.setValidade(cartaoDTO.getValidade());
        novoCartao.setNomeTitular(cartaoDTO.getNomeTitular());
        novoCartao.setUsuario(usuario);
        novoCartao.setBandeira(bandeira); 
        
        Cartao cartaoSalvo = cartaoRepositorio.save(novoCartao);
        
        // **CORREÇÃO DE VAZAMENTO DE DADOS**
        return convertToDto(cartaoSalvo); // Retorna o DTO seguro
    }

    // --- (MÉTODO ATUALIZADO - SEGURANÇA IDOR E RETORNO DTO) ---
    @Override
    public List<TransacaoViewDTO> getTransacoesPorCartaoId(Long cartaoId, String emailUsuarioLogado) {
        
        // **CORREÇÃO DE SEGURANÇA (IDOR)**
        Optional<Cartao> cartaoOpt = cartaoRepositorio.findById(cartaoId);
        if (cartaoOpt.isEmpty()) {
            throw new RuntimeException("Cartão não encontrado.");
        }
        
        Cartao cartao = cartaoOpt.get();
        if (!cartao.getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new AccessDeniedException("Acesso negado: Este cartão não pertence a você.");
        }

        // Se o usuário é o dono, busca as transações
        List<Transacao> transacoes = transacaoRepositorio.findByCartaoIdOrderByDataHoraDesc(cartaoId);

        // **CORREÇÃO DE VAZAMENTO DE DADOS**
        // Converte a lista de Entidades para uma lista de DTOs seguros
        return transacoes.stream()
                .map(TransacaoViewDTO::new) // Usa o construtor (Transacao t)
                .collect(Collectors.toList());
    }

    // --- (MÉTODO HELPER) ---
    private CartaoDTO convertToDto(Cartao cartao) {
        CartaoDTO dto = new CartaoDTO();
        dto.setId(cartao.getId()); 
        // Oculta o número completo do cartão por segurança (Boas práticas)
        dto.setNumero("**** **** **** " + cartao.getNumero().substring(cartao.getNumero().length() - 4));
        dto.setValidade(cartao.getValidade()); 
        dto.setNomeTitular(cartao.getNomeTitular());
        dto.setBandeira(cartao.getBandeira());
        return dto;
    }

    // --- (Métodos privados de validação de cartão) ---
    private boolean isLuhnValid(String numero) {
        int nSoma = 0;
        boolean isSegundo = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int d = numero.charAt(i) - '0';
            if (isSegundo) {
                d = d * 2;
            }
            nSoma += d / 10;
            nSoma += d % 10;
            isSegundo = !isSegundo;
        }
        return (nSoma % 10 == 0);
    }

    private String identificarBandeira(String numero) {
        if (numero.startsWith("4")) { return "VISA"; }
        if (numero.matches("^5[1-5].*")) { return "MASTERCARD"; }
        if (numero.startsWith("34") || numero.startsWith("37")) { return "AMEX"; }
        if (numero.startsWith("6011") || numero.startsWith("65")) { return "DISCOVER"; }
        if (numero.startsWith("3")) { return "JCB"; }
        if (numero.startsWith("35")) { return "ELO"; }
        return "DESCONHECIDA";
    }
}