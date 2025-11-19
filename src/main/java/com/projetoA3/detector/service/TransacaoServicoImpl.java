package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.TransacaoDTO;
import com.projetoA3.detector.dto.TransacaoResponseDTO;
import com.projetoA3.detector.dto.TransacaoViewDTO; // Importe o DTO de visualização
import com.projetoA3.detector.entity.Cartao;
import com.projetoA3.detector.entity.Transacao;
import com.projetoA3.detector.entity.TransacaoStatus;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.repository.CartaoRepositorio;
import com.projetoA3.detector.repository.TransacaoRepositorio;
import com.projetoA3.detector.exception.FraudDetectedException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // Importe a exceção de segurança
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransacaoServicoImpl implements TransacaoServico {

    private final TransacaoRepositorio transacaoRepositorio;
    private final CartaoRepositorio cartaoRepositorio;
    private final UsuarioServico usuarioServico;
    
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    
    // Limites de Fraude
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

        // --- LÓGICA DE LOCALIZAÇÃO (SIMULAÇÃO VS REAL) ---
        double latUsuario, lonUsuario;

        // Se as coordenadas vierem zeradas (0.0), é uma simulação do DEV.
        // Nesse caso, usamos a última localização conhecida do usuário (salva no banco).
        if (transacaoDto.getLatitudeUsuario() == 0.0 && transacaoDto.getLongitudeUsuario() == 0.0) {
            if (usuario.getLatitudeAtual() == null || usuario.getLongitudeAtual() == null) {
                throw new FraudDetectedException("ERRO: Localização do usuário desconhecida. O usuário precisa logar no Dashboard.");
            }
            latUsuario = usuario.getLatitudeAtual();
            lonUsuario = usuario.getLongitudeAtual();
            
            // Atualiza o DTO para o cálculo correto
            transacaoDto.setLatitudeUsuario(latUsuario);
            transacaoDto.setLongitudeUsuario(lonUsuario);
        } else {
            // Fluxo normal: usa o que veio do frontend
            latUsuario = transacaoDto.getLatitudeUsuario();
            lonUsuario = transacaoDto.getLongitudeUsuario();
        }

        // --- DETECÇÃO DE FRAUDE ---
        String mensagemFraude = null;

        // 1. Gasto Atípico
        if (usuario.getMediaGasto() != null && usuario.getMediaGasto().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mediaHabitual = usuario.getMediaGasto();
            BigDecimal limiteGasto = mediaHabitual.multiply(new BigDecimal(FATOR_DESVIO_GASTO));
            
            if (transacaoDto.getValor().compareTo(limiteGasto) > 0) {
                mensagemFraude = String.format("ALERTA: Valor (R$ %.2f) muito acima da média (R$ %.2f).",
                                  transacaoDto.getValor(), mediaHabitual);
            }
        }

        // 2. Horário Atípico (apenas se não falhou na regra 1)
        if (mensagemFraude == null && usuario.getHorarioHabitualInicio() != null && usuario.getHorarioHabitualFim() != null) {
            LocalTime horaTransacao = LocalTime.now();
            if (horaTransacao.isBefore(usuario.getHorarioHabitualInicio()) || 
                horaTransacao.isAfter(usuario.getHorarioHabitualFim())) {
                
                mensagemFraude = String.format("ALERTA: Compra às %s, fora do horário habitual (%s - %s).",
                                  horaTransacao, usuario.getHorarioHabitualInicio(), usuario.getHorarioHabitualFim());
            }
        }

        // 3. Localização (apenas se não falhou nas anteriores)
        if (mensagemFraude == null) {
            Double distanciaKm = transacaoRepositorio.calcularDistanciaEntrePontosKm(
                transacaoDto.getLongitude(), transacaoDto.getLatitude(),
                lonUsuario, latUsuario
            );

            if (distanciaKm != null && distanciaKm > DISTANCIA_MAXIMA_KM_PERMITIDA) {
                mensagemFraude = String.format("ALERTA: Compra a %.2f km da sua localização atual.", distanciaKm);
            }
        }
        
        // --- CRIAÇÃO DA TRANSAÇÃO ---
        Point localizacaoPonto = geometryFactory.createPoint(
            new Coordinate(transacaoDto.getLongitude(), transacaoDto.getLatitude())
        );

        Transacao novaTransacao = new Transacao();
        novaTransacao.setValor(transacaoDto.getValor());
        novaTransacao.setEstabelecimento(transacaoDto.getEstabelecimento());
        novaTransacao.setCartao(cartao);
        novaTransacao.setDataHora(LocalDateTime.now());
        novaTransacao.setLocalizacao(localizacaoPonto);
        novaTransacao.setIpAddress(transacaoDto.getIpAddress());
        
        // Define o status com base na fraude
        if (mensagemFraude != null) {
            novaTransacao.setStatus(TransacaoStatus.PENDING);
            Transacao transacaoSalva = transacaoRepositorio.save(novaTransacao);
            return new TransacaoResponseDTO("PENDING_CONFIRMATION", mensagemFraude, transacaoSalva);
        } else {
            novaTransacao.setStatus(TransacaoStatus.COMPLETED);
            Transacao transacaoSalva = transacaoRepositorio.save(novaTransacao);
            // Atualiza a média de gastos se aprovada
            usuarioServico.atualizarPadroesUsuario(usuario, transacaoSalva);
            return new TransacaoResponseDTO("COMPLETED", "Transação registrada com sucesso.", transacaoSalva);
        }
    }

    // --- MÉTODO CORRIGIDO (Recebe email e retorna ViewDTO) ---
    @Override
    @Transactional
    public TransacaoViewDTO confirmarTransacao(Long transacaoId, String emailUsuarioLogado) {
        Transacao transacao = transacaoRepositorio.findById(transacaoId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada."));

        // SEGURANÇA: Verifica se o usuário é o dono
        if (!transacao.getCartao().getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new AccessDeniedException("Acesso negado: Você não é o proprietário desta transação.");
        }

        if (transacao.getStatus() != TransacaoStatus.PENDING) {
            throw new IllegalStateException("Esta transação não está pendente.");
        }

        transacao.setStatus(TransacaoStatus.COMPLETED);
        Usuarios usuario = transacao.getCartao().getUsuario();
        
        // Atualiza o padrão (treina a IA) pois o usuário confirmou que é legítima
        usuarioServico.atualizarPadroesUsuario(usuario, transacao);
        
        Transacao salva = transacaoRepositorio.save(transacao);
        return new TransacaoViewDTO(salva);
    }

    // --- MÉTODO CORRIGIDO (Recebe email e retorna ViewDTO) ---
    @Override
    @Transactional
    public TransacaoViewDTO negarTransacao(Long transacaoId, String emailUsuarioLogado) {
        Transacao transacao = transacaoRepositorio.findById(transacaoId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada."));

        // SEGURANÇA: Verifica se o usuário é o dono
        if (!transacao.getCartao().getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new AccessDeniedException("Acesso negado: Você não é o proprietário desta transação.");
        }

        if (transacao.getStatus() != TransacaoStatus.PENDING) {
            throw new IllegalStateException("Esta transação não está pendente.");
        }

        transacao.setStatus(TransacaoStatus.DENIED);
        Transacao salva = transacaoRepositorio.save(transacao);
        
        return new TransacaoViewDTO(salva);
    }

    // --- NOVO MÉTODO PARA POLLING DO FRONTEND ---
    @Override
    public List<TransacaoViewDTO> buscarPendentesDoUsuario(String emailUsuario) {
        List<Transacao> pendentes = transacaoRepositorio.findByCartaoUsuarioEmailAndStatus(
            emailUsuario, 
            TransacaoStatus.PENDING
        );

        return pendentes.stream()
                .map(TransacaoViewDTO::new)
                .collect(Collectors.toList());
    }
}