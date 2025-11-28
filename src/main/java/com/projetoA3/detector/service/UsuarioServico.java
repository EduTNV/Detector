package com.projetoA3.detector.service;

import com.projetoA3.detector.dto.UsuarioDTO;
import com.projetoA3.detector.dto.UsuarioSimulacaoDTO;
import com.projetoA3.detector.dto.HorarioHabitualDTO;
import com.projetoA3.detector.entity.HistoricoUsuario;
import com.projetoA3.detector.entity.UsuarioOmitido;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.entity.Transacao;

import java.util.List;
import java.util.Optional;

public interface UsuarioServico {

    Usuarios criarUsuario(Usuarios usuario);
    
    List<Usuarios> listarTodos();
    
    List<HistoricoUsuario> listarHistorico(Long id);
    
    Optional<Usuarios> atualizarUsuario(Long id, UsuarioDTO usuarioDTO);
    
    boolean omitirUsuario(Long id);
    
    List<UsuarioOmitido> listarOmitidos();

    List<UsuarioSimulacaoDTO> listarParaSimulacao();

    void atualizarPadroesUsuario(Usuarios usuario, Transacao novaTransacao);

    Usuarios definirHorarioHabitual(String emailUsuarioLogado, HorarioHabitualDTO horarioDTO);

    void atualizarLocalizacao(String email, double latitude, double longitude);
}
