// Local: src/main/java/com/projetoA3/detector/config/JwtRequestFilter.java
package com.projetoA3.detector.config;

import com.projetoA3.detector.service.CustomUserDetailsService;
import com.projetoA3.detector.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException; // <-- IMPORTE ESTE
import io.jsonwebtoken.SignatureException; // <-- IMPORTE ESTE
import io.jsonwebtoken.UnsupportedJwtException; // <-- IMPORTE ESTE
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            
            // --- (INÍCIO) BLOCO DE CATCH ATUALIZADO ---
            } catch (IllegalArgumentException e) {
                System.out.println("Token JWT inválido: Não foi possível obter o token (argumento ilegal).");
            } catch (ExpiredJwtException e) {
                System.out.println("Token JWT expirou.");
            } catch (MalformedJwtException e) {
                System.out.println("Token JWT mal formado.");
            } catch (SignatureException e) {
                System.out.println("Token JWT possui assinatura inválida.");
            } catch (UnsupportedJwtException e) {
                System.out.println("Token JWT não é suportado.");
            }
            // --- (FIM) BLOCO DE CATCH ATUALIZADO ---

        } else if (requestTokenHeader != null) {
            // Isso é da nossa correção anterior, está correto.
            logger.warn("Token JWT não começa com 'Bearer '.");
        }

        // Depois de obter o token, validamos ele.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}