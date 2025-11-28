package com.projetoA3.detector.controller;

import com.projetoA3.detector.dto.AuthRequest;
import com.projetoA3.detector.dto.AuthResponse;
import com.projetoA3.detector.entity.Usuarios;
import com.projetoA3.detector.repository.UsuarioRepositorio;
import com.projetoA3.detector.service.CustomUserDetailsService;
import com.projetoA3.detector.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;  

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(), authRequest.getSenha()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos");
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário desabilitado");
        } 
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);
 
        Usuarios usuario = usuarioRepositorio.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Erro interno: Usuário autenticado não encontrado no banco."));
 
        return ResponseEntity.ok(new AuthResponse(token, usuario.getNome(), usuario.getEmail()));
    }

    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok("v2.1-admin-support");
    }
}