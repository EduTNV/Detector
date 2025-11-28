package com.projetoA3.detector.repository;

import com.projetoA3.detector.entity.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartaoRepositorio extends JpaRepository<Cartao, Long> {

    List<Cartao> findByUsuarioEmail(String email);
}
