package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.HorarioHabitualDTO; // <-- IMPORTADO
import com.projetoA3.detector.dto.UsuarioDTO;
import com.projetoA3.detector.entity.HistoricoUsuario;
import com.projetoA3.detector.entity.UsuarioOmitido;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.entity.Transacao; // <-- IMPORTADO
import com.projetoA3.detector.repository.HistoricoUsuarioRepositorio;
import com.projetoA3.detector.repository.UsuarioOmitidoRepositorio;
import com.projetoA3.detector.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // <-- IMPORTADO
import java.math.RoundingMode; // <-- IMPORTADO
import java.time.LocalTime; // <-- IMPORTADO
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServicoImpl implements UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final HistoricoUsuarioRepositorio historicoUsuarioRepositorio;
    private final UsuarioOmitidoRepositorio usuarioOmitidoRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServicoImpl(UsuarioRepositorio usuarioRepositorio,
                              HistoricoUsuarioRepositorio historicoUsuarioRepositorio,
                              UsuarioOmitidoRepositorio usuarioOmitidoRepositorio,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.historicoUsuarioRepositorio = historicoUsuarioRepositorio;
        this.usuarioOmitidoRepositorio = usuarioOmitidoRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuarios criarUsuario(Usuarios usuario) {
        Optional<Usuarios> usuarioExistente = usuarioRepositorio.findByEmail(usuario.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("Email já registado.");
        }
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        return usuarioRepositorio.save(usuario);
    }

    @Override
    public List<Usuarios> listarTodos() {
        return usuarioRepositorio.findByAtivoTrue();
    }

    @Override
    public List<HistoricoUsuario> listarHistorico(Long id) {
        return historicoUsuarioRepositorio.findByUsuarioIdOrderByDataModificacaoDesc(id);
    }

    @Override
    @Transactional
    public Optional<Usuarios> atualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        return usuarioRepositorio.findById(id).map(usuarioExistente -> {
            HistoricoUsuario historico = new HistoricoUsuario(usuarioExistente);
            historicoUsuarioRepositorio.save(historico);

            if (usuarioDTO.getNome() != null) {
                usuarioExistente.setNome(usuarioDTO.getNome());
            }
            if (usuarioDTO.getEmail() != null) {
                usuarioExistente.setEmail(usuarioDTO.getEmail());
            }
            if (usuarioDTO.getSenha() != null && !usuarioDTO.getSenha().isEmpty()) {
                usuarioExistente.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
            }

            return usuarioRepositorio.save(usuarioExistente);
        });
    }

    @Override
    @Transactional
    public boolean omitirUsuario(Long id) {
        Optional<Usuarios> usuarioOpt = usuarioRepositorio.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuarios usuario = usuarioOpt.get();

            UsuarioOmitido omitido = new UsuarioOmitido(usuario);
            usuarioOmitidoRepositorio.save(omitido);

            usuario.setAtivo(false);
            usuarioRepositorio.save(usuario);
            return true;
        }
        return false;
    }

    @Override
    public List<UsuarioOmitido> listarOmitidos() {
        return usuarioOmitidoRepositorio.findAll();
    }

    // --- (MÉTODO ATUALIZADO) ---
    @Override
    @Transactional
    public void atualizarPadroesUsuario(Usuarios usuario, Transacao novaTransacao) {
        
        // 1. Recalcular Média de Gasto (Mantido)
        recalcularMediaGasto(usuario, novaTransacao.getValor());

        // 2. Ajustar Horário (REMOVIDO, conforme sua solicitação)
        // A lógica de aprendizado automático de horário foi retirada.

        // 3. Salvar as alterações (apenas a média)
        usuarioRepositorio.save(usuario);
    }

    private void recalcularMediaGasto(Usuarios usuario, BigDecimal valorNovaTransacao) {
        BigDecimal mediaAntiga = usuario.getMediaGasto();
        int totalTransacoes = usuario.getTotalTransacoesParaMedia();
        BigDecimal novaMedia;

        if (mediaAntiga == null || totalTransacoes == 0) {
            // Caso da primeira transação
            novaMedia = valorNovaTransacao;
            usuario.setTotalTransacoesParaMedia(1);
        } else {
            // Cálculo da média ponderada com BigDecimal
            BigDecimal totalAnterior = mediaAntiga.multiply(new BigDecimal(totalTransacoes));
            BigDecimal novoTotal = totalAnterior.add(valorNovaTransacao);
            int novoContador = totalTransacoes + 1;

            // Divide com 2 casas decimais e arredondamento padrão
            novaMedia = novoTotal.divide(new BigDecimal(novoContador), 2, RoundingMode.HALF_UP);
            
            usuario.setTotalTransacoesParaMedia(novoContador);
        }

        usuario.setMediaGasto(novaMedia);
    }

    // O método private ajustarHorarioHabitual(Usuarios usuario, LocalTime horaTransacao)
    // foi removido pois não é mais chamado.

    // --- (NOVO MÉTODO) ---
    @Override
    @Transactional
    public Usuarios definirHorarioHabitual(String emailUsuarioLogado, HorarioHabitualDTO horarioDTO) {
        
        // Busca o usuário pelo email
        Usuarios usuario = usuarioRepositorio.findByEmailAndAtivoTrue(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + emailUsuarioLogado));

        // Converte as strings (ex: "08:00") para objetos LocalTime
        LocalTime inicio = LocalTime.parse(horarioDTO.getHorarioInicio());
        LocalTime fim = LocalTime.parse(horarioDTO.getHorarioFim());

        // Define os novos horários
        usuario.setHorarioHabitualInicio(inicio);
        usuario.setHorarioHabitualFim(fim);

        // Salva e retorna o usuário atualizado
        return usuarioRepositorio.save(usuario);
    }
}