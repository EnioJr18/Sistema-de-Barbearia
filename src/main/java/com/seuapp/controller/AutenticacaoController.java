package com.seuapp.controller;

import com.seuapp.dto.AutenticacaoDTO;
import com.seuapp.dto.ErroApiDTO;
import com.seuapp.model.Usuario;
import com.seuapp.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Autenticacao", description = "Login e emissao de token JWT.")
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;

    @Operation(summary = "Realiza login", description = "Autentica o usuario com email e senha e retorna um token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token JWT gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class))),
            @ApiResponse(responseCode = "403", description = "Credenciais invalidas",
                    content = @Content(schema = @Schema(implementation = ErroApiDTO.class)))
    })
    @PostMapping
    public String efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados) {
        var tokenAutenticacao = new UsernamePasswordAuthenticationToken(dados.getEmail(), dados.getSenha());

        var authentication = manager.authenticate(tokenAutenticacao);

        String tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return tokenJWT;
    }
}
