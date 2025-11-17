package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.CartaoDTO;
import com.projetoA3.detector.entity.Cartao;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.entity.Transacao;
import com.projetoA3.detector.repository.CartaoRepositorio;
import com.projetoA3.detector.repository.UsuarioRepositorio;
import com.projetoA3.detector.repository.TransacaoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartaoServicoImpl implements CartaoServico {

    private final CartaoRepositorio cartaoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final TransacaoRepositorio transacaoRepositorio;

    @Autowired
    public CartaoServicoImpl(CartaoRepositorio cartaoRepositorio, UsuarioRepositorio usuarioRepositorio, TransacaoRepositorio transacaoRepositorio) {
        this.cartaoRepositorio = cartaoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.transacaoRepositorio = transacaoRepositorio;
    }

    @Override
    public List<CartaoDTO> buscarCartoesPorUsuarioEmail(String email) {
        // 1. Busca as entidades
        List<Cartao> cartoes = cartaoRepositorio.findByUsuarioEmail(email);
        
        // 2. Mapeia a lista de Entidades para a lista de DTOs
        return cartoes.stream()
                .map(this::convertToDto) // Chama o método auxiliar abaixo
                .collect(Collectors.toList());
    }

    @Override
public Cartao adicionarCartao(CartaoDTO cartaoDTO, String emailUsuarioLogado) {

    // --- INÍCIO DA NOVA LÓGICA DE VALIDAÇÃO ---
        String numeroCartao = cartaoDTO.getNumero().replaceAll("\\s+", ""); // Remove espaços

        // 1. Validação do Algoritmo de Luhn
        if (!isLuhnValid(numeroCartao)) {
            throw new IllegalArgumentException("Número de cartão inválido.");
        }

        // 2. Identificação da Bandeira
        String bandeira = identificarBandeira(numeroCartao);
        if ("DESCONHECIDA".equals(bandeira)) {
            throw new IllegalArgumentException("Bandeira do cartão não suportada ou desconhecida.");
        }
        // --- FIM DA NOVA LÓGICA ---

    Usuarios usuario = usuarioRepositorio.findByEmailAndAtivoTrue(emailUsuarioLogado) // Use o método corrigido
            .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado: " + emailUsuarioLogado));

        
        Cartao novoCartao = new Cartao();
        novoCartao.setNumero(numeroCartao); // Salva o número limpo
        novoCartao.setValidade(cartaoDTO.getValidade());
        novoCartao.setNomeTitular(cartaoDTO.getNomeTitular());
        novoCartao.setUsuario(usuario);
        novoCartao.setBandeira(bandeira); // <-- SALVA A BANDEIRA
        return cartaoRepositorio.save(novoCartao);
    }

    @Override
    public List<Transacao> getTransacoesPorCartaoId(Long cartaoId) {
        // Este método já existe no seu TransacaoRepositorio
        return transacaoRepositorio.findByCartaoIdOrderByDataHoraDesc(cartaoId);
    }

    private CartaoDTO convertToDto(Cartao cartao) {
        CartaoDTO dto = new CartaoDTO();
        
        dto.setId(cartao.getId()); 
        dto.setNumero(cartao.getNumero());
        dto.setValidade(cartao.getValidade());
        dto.setNomeTitular(cartao.getNomeTitular());
        dto.setBandeira(cartao.getBandeira()); // <-- ADICIONA A BANDEIRA NO RETORNO
        // A linha do 'limite' foi removida pois o campo não existe

        return dto;
    }

    //Validação usando o Algoritmo de Luhn (mod 10).
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

    
    // Identificaçaõ de bandeira com base nos primeiros dígitos (IIN).
     
    private String identificarBandeira(String numero) {
        if (numero.startsWith("4")) {
            return "VISA";
        }
        if (numero.matches("^5[1-5].*")) { // 51 a 55
            return "MASTERCARD";
        }
        if (numero.startsWith("34") || numero.startsWith("37")) {
            return "AMEX";
        }
        if (numero.startsWith("6011") || numero.startsWith("65")) {
             return "DISCOVER";
        }
        if (numero.startsWith("3")) {
            return "JCB";
        }
         if (numero.startsWith("35")) {
            return "ELO";
        }
        
        return "DESCONHECIDA";
    }

} 

