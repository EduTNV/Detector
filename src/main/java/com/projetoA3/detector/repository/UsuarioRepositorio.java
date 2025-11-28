package com.projetoA3.detector.repository;

import com.projetoA3.detector.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;  
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuarios, Long> {

    
    Optional<Usuarios> findByEmail(String email);

    Optional<Usuarios> findByEmailAndAtivoTrue(String email);
 
    List<Usuarios> findByAtivoTrue();
}

