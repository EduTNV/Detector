package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.HorarioHabitualDTO;
import com.projetoA3.detector.dto.UsuarioDTO;
import com.projetoA3.detector.dto.CartaoDTO; 
import com.projetoA3.detector.dto.UsuarioSimulacaoDTO; 
import com.projetoA3.detector.entity.HistoricoUsuario;
import com.projetoA3.detector.entity.UsuarioOmitido;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.entity.Transacao;
import com.projetoA3.detector.repository.HistoricoUsuarioRepositorio;
import com.projetoA3.detector.repository.UsuarioOmitidoRepositorio;
import com.projetoA3.detector.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 

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

    @Override
    @Transactional
    public void atualizarPadroesUsuario(Usuarios usuario, Transacao novaTransacao) {
        recalcularMediaGasto(usuario, novaTransacao.getValor());
        usuarioRepositorio.save(usuario);
    }

    private void recalcularMediaGasto(Usuarios usuario, BigDecimal valorNovaTransacao) {
        BigDecimal mediaAntiga = usuario.getMediaGasto();
        int totalTransacoes = usuario.getTotalTransacoesParaMedia();
        BigDecimal novaMedia;

        if (mediaAntiga == null || totalTransacoes == 0) {
            novaMedia = valorNovaTransacao;
            usuario.setTotalTransacoesParaMedia(1);
        } else {
            BigDecimal totalAnterior = mediaAntiga.multiply(new BigDecimal(totalTransacoes));
            BigDecimal novoTotal = totalAnterior.add(valorNovaTransacao);
            int novoContador = totalTransacoes + 1;
            novaMedia = novoTotal.divide(new BigDecimal(novoContador), 2, RoundingMode.HALF_UP);
            usuario.setTotalTransacoesParaMedia(novoContador);
        }
        usuario.setMediaGasto(novaMedia);
    }

    @Override
    @Transactional
    public Usuarios definirHorarioHabitual(String emailUsuarioLogado, HorarioHabitualDTO horarioDTO) {
        Usuarios usuario = usuarioRepositorio.findByEmailAndAtivoTrue(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + emailUsuarioLogado));
        LocalTime inicio = LocalTime.parse(horarioDTO.getHorarioInicio());
        LocalTime fim = LocalTime.parse(horarioDTO.getHorarioFim());
        usuario.setHorarioHabitualInicio(inicio);
        usuario.setHorarioHabitualFim(fim);
        return usuarioRepositorio.save(usuario);
    }

    @Override
    @Transactional
    public void atualizarLocalizacao(String email, double latitude, double longitude) {
        Usuarios usuario = usuarioRepositorio.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        usuario.setLatitudeAtual(latitude);
        usuario.setLongitudeAtual(longitude);
        usuarioRepositorio.save(usuario);
    }

    // --- MÉTODO ADICIONADO E ANOTADO COM TRANSACTIONAL ---
    @Override
    @Transactional(readOnly = true) // Importante para carregar os cartões (Lazy Load)
    public List<UsuarioSimulacaoDTO> listarParaSimulacao() {
        List<Usuarios> usuarios = usuarioRepositorio.findByAtivoTrue();
        
        return usuarios.stream().map(u -> {
            List<CartaoDTO> cartoesDto = u.getCartoes().stream().map(c -> {
                CartaoDTO dto = new CartaoDTO();
                dto.setId(c.getId());
                dto.setNumero(c.getNumero());
                dto.setValidade(c.getValidade());
                dto.setNomeTitular(c.getNomeTitular());
                dto.setBandeira(c.getBandeira());
                return dto;
            }).collect(Collectors.toList());

            return new UsuarioSimulacaoDTO(u.getId(), u.getNome(), u.getEmail(), cartoesDto);
        }).collect(Collectors.toList());
    }
}