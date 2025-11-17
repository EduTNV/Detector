package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.TransacaoDTO;
import com.projetoA3.detector.dto.TransacaoResponseDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO; // <-- IMPORTAR
import com.projetoA3.detector.entity.Cartao;
import com.projetoA3.detector.entity.Transacao;
import com.projetoA3.detector.entity.TransacaoStatus;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.repository.CartaoRepositorio;
import com.projetoA3.detector.repository.TransacaoRepositorio;
import com.projetoA3.detector.exception.FraudDetectedException; // <-- IMPORTAR (SE NÃO ESTIVER)

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // <-- IMPORTAR
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransacaoServicoImpl implements TransacaoServico {

    // ... (injeções e constantes existentes) ...
    private final TransacaoRepositorio transacaoRepositorio;
    private final CartaoRepositorio cartaoRepositorio;
    private final UsuarioServico usuarioServico; 
    
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    
    private static final double FATOR_DESVIO_GASTO = 3.0; 
    private static final double DISTANCIA_MAXIMA_KM_PERMITIDA = 25.0; 

    @Autowired
    public TransacaoServicoImpl(TransacaoRepositorio transacaoRepositorio, 
                                CartaoRepositorio cartaoRepositorio,
                                UsuarioServico usuarioServico) {
        this.transacaoRepositorio = transacaoRepositorio;
        this.cartaoRepositorio = cartaoRepositorio;
        this.usuarioServico = usuarioServico;
    }

    @Override
    @Transactional
    public TransacaoResponseDTO registrarTransacao(TransacaoDTO transacaoDto) {
        
        Cartao cartao = cartaoRepositorio.findById(transacaoDto.getCartaoId())
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado com o ID: " + transacaoDto.getCartaoId()));
        Usuarios usuario = cartao.getUsuario();

        // --- LÓGICA DE DETECÇÃO (NÃO MAIS LANÇA EXCEÇÃO) ---
        String mensagemFraude = null; 

        // REGRA TIPO 1: Gasto Atípico
        if (usuario.getMediaGasto() != null && usuario.getMediaGasto().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mediaHabitual = usuario.getMediaGasto();
            BigDecimal limiteGasto = mediaHabitual.multiply(new BigDecimal(FATOR_DESVIO_GASTO));
            
            if (transacaoDto.getValor().compareTo(limiteGasto) > 0) {
                mensagemFraude = String.format("ALERTA: Valor (R$ %.2f) muito acima da sua média (R$ %.2f).",
                                  transacaoDto.getValor(), mediaHabitual);
            }
        }

        // REGRA TIPO 2: Horário Atípico
        if (mensagemFraude == null && usuario.getHorarioHabitualInicio() != null && usuario.getHorarioHabitualFim() != null) {
            LocalTime horaTransacao = LocalTime.now(); // Usa o fuso do servidor (corrigido no DetentorApplication)
            if (horaTransacao.isBefore(usuario.getHorarioHabitualInicio()) || 
                horaTransacao.isAfter(usuario.getHorarioHabitualFim())) {
                
                mensagemFraude = String.format("ALERTA: Compra às %s, fora do seu horário habitual (%s - %s).",
                                  horaTransacao, usuario.getHorarioHabitualInicio(), usuario.getHorarioHabitualFim());
            }
        }

        // REGRA TIPO 3: Localização
        if (mensagemFraude == null) {
            Double distanciaKm = transacaoRepositorio.calcularDistanciaEntrePontosKm(
                transacaoDto.getLongitude(), transacaoDto.getLatitude(),
                transacaoDto.getLongitudeUsuario(), transacaoDto.getLatitudeUsuario()
            );

            if (distanciaKm != null && distanciaKm > DISTANCIA_MAXIMA_KM_PERMITIDA) {
                mensagemFraude = String.format("ALERTA: Compra a %.2f km da sua localização atual.", distanciaKm);
            }
        }
        
        Point localizacaoPonto = geometryFactory.createPoint(
            new Coordinate(transacaoDto.getLongitude(), transacaoDto.getLatitude())
        );

        Transacao novaTransacao = new Transacao();
        novaTransacao.setValor(transacaoDto.getValor());
        novaTransacao.setEstabelecimento(transacaoDto.getEstabelecimento());
        novaTransacao.setCartao(cartao);
        novaTransacao.setDataHora(LocalDateTime.now()); // Usa o fuso do servidor
        novaTransacao.setLocalizacao(localizacaoPonto);
        novaTransacao.setIpAddress(transacaoDto.getIpAddress());
        
        if (mensagemFraude != null) {
            novaTransacao.setStatus(TransacaoStatus.PENDING);
            Transacao transacaoSalva = transacaoRepositorio.save(novaTransacao);
            return new TransacaoResponseDTO("PENDING_CONFIRMATION", mensagemFraude, transacaoSalva);
        } else {
            novaTransacao.setStatus(TransacaoStatus.COMPLETED);
            Transacao transacaoSalva = transacaoRepositorio.save(novaTransacao);
            usuarioServico.atualizarPadroesUsuario(usuario, transacaoSalva);
            return new TransacaoResponseDTO("COMPLETED", "Transação registrada com sucesso.", transacaoSalva);
        }
    }

    // --- (MÉTODO ATUALIZADO COM SEGURANÇA IDOR E RETORNO DTO) ---
    @Override
    @Transactional
    public TransacaoViewDTO confirmarTransacao(Long transacaoId, String emailUsuarioLogado) {
        Transacao transacao = transacaoRepositorio.findById(transacaoId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada."));

        // **CORREÇÃO DE SEGURANÇA (IDOR)**
        // Verifica se o usuário logado é o dono da transação
        if (!transacao.getCartao().getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new AccessDeniedException("Acesso negado: Você não é o proprietário desta transação.");
        }

        if (transacao.getStatus() != TransacaoStatus.PENDING) {
            throw new IllegalStateException("Esta transação não está pendente de confirmação.");
        }

        transacao.setStatus(TransacaoStatus.COMPLETED);
        Usuarios usuario = transacao.getCartao().getUsuario();
        usuarioServico.atualizarPadroesUsuario(usuario, transacao);
        
        Transacao transacaoSalva = transacaoRepositorio.save(transacao);
        
        // **CORREÇÃO DE VAZAMENTO DE DADOS**
        return new TransacaoViewDTO(transacaoSalva); // Retorna o DTO seguro
    }

    // --- (MÉTODO ATUALIZADO COM SEGURANÇA IDOR E RETORNO DTO) ---
    @Override
    @Transactional
    public TransacaoViewDTO negarTransacao(Long transacaoId, String emailUsuarioLogado) {
        Transacao transacao = transacaoRepositorio.findById(transacaoId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada."));

        // **CORREÇÃO DE SEGURANÇA (IDOR)**
        if (!transacao.getCartao().getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new AccessDeniedException("Acesso negado: Você não é o proprietário desta transação.");
        }

        if (transacao.getStatus() != TransacaoStatus.PENDING) {
            throw new IllegalStateException("Esta transação não está pendente de confirmação.");
        }

        transacao.setStatus(TransacaoStatus.DENIED);
        Transacao transacaoSalva = transacaoRepositorio.save(transacao);

        // **CORREÇÃO DE VAZAMENTO DE DADOS**
        return new TransacaoViewDTO(transacaoSalva); // Retorna o DTO seguro
    }
}