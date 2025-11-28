package com.projetoA3.detector.repository;

import com.projetoA3.detector.entity.HistoricoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoUsuarioRepositorio extends JpaRepository<HistoricoUsuario, Long> {

    List<HistoricoUsuario> findByUsuarioIdOrderByDataModificacaoDesc(Long usuarioId);
}
