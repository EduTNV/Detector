package com.projetoA3.detector.controller;

import com.projetoA3.detector.dto.HorarioHabitualDTO;
import com.projetoA3.detector.dto.UsuarioDTO;
import com.projetoA3.detector.dto.UsuarioSimulacaoDTO;
import com.projetoA3.detector.entity.HistoricoUsuario;
import com.projetoA3.detector.entity.UsuarioOmitido;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.service.UsuarioServico;
import com.projetoA3.detector.service.UsuarioServicoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
 
class LocalizacaoRequest {
    public double latitude;
    public double longitude;
}

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioServico usuarioServico;

    @Autowired
    public UsuarioController(UsuarioServico usuarioServico) {
        this.usuarioServico = usuarioServico;
    }

    @PostMapping
    public ResponseEntity<Usuarios> criarUsuario(@RequestBody UsuarioDTO usuarioDto) {
        Usuarios novoUsuario = new Usuarios();
        novoUsuario.setNome(usuarioDto.getNome());
        novoUsuario.setEmail(usuarioDto.getEmail());
        novoUsuario.setSenha(usuarioDto.getSenha());
        
        Usuarios usuarioSalvo = usuarioServico.criarUsuario(novoUsuario);
        
        return new ResponseEntity<>(usuarioSalvo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Usuarios>> listarTodosUsuarios() {
        return ResponseEntity.ok(usuarioServico.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuarios> atualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioServico.atualizarUsuario(id, usuarioDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoUsuario>> getHistoricoUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioServico.listarHistorico(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> omitirUsuario(@PathVariable Long id) {
        if (usuarioServico.omitirUsuario(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/omitidos")
    public ResponseEntity<List<UsuarioOmitido>> getOmitidos() {
        return ResponseEntity.ok(usuarioServico.listarOmitidos());
    } 
    @PutMapping("/meu-horario")
    public ResponseEntity<Usuarios> setHorarioHabitual(@RequestBody HorarioHabitualDTO horarioDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        Usuarios usuarioAtualizado = usuarioServico.definirHorarioHabitual(emailUsuario, horarioDTO);
        return ResponseEntity.ok(usuarioAtualizado);
    } 
    @PutMapping("/minha-localizacao")
    public ResponseEntity<Void> atualizarLocalizacao(@RequestBody LocalizacaoRequest loc) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        usuarioServico.atualizarLocalizacao(auth.getName(), loc.latitude, loc.longitude);
        return ResponseEntity.ok().build();
    }
 
    @GetMapping("/admin/simulacao-dados")
    public ResponseEntity<List<UsuarioSimulacaoDTO>> getDadosSimulacao() {
        return ResponseEntity.ok(((UsuarioServicoImpl) usuarioServico).listarParaSimulacao());
    }
}