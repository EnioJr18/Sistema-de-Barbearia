package com.seuapp.controller;

import com.seuapp.dto.AutenticacaoDTO;
import com.seuapp.model.Usuario;
import com.seuapp.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;

    @PostMapping
    public String efetuarLogin(@RequestBody AutenticacaoDTO dados) {
        var tokenAutenticacao = new UsernamePasswordAuthenticationToken(dados.getEmail(), dados.getSenha());

        var authentication = manager.authenticate(tokenAutenticacao);

        String tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return tokenJWT;
    }
}