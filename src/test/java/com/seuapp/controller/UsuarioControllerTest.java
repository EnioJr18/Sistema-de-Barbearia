package com.seuapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seuapp.BarbeariaApplication;
import com.seuapp.model.Usuario;
import com.seuapp.repository.AgendamentoRepository;
import com.seuapp.repository.ServicoRepository;
import com.seuapp.repository.UsuarioRepository;
import com.seuapp.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        classes = BarbeariaApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:barbearia_usuario_controller;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.jpa.show-sql=false",
                "spring.flyway.enabled=true",
                "spring.flyway.locations=classpath:db/migration",
                "api.security.token.secret=segredo-ficticio-apenas-para-testes"
        })
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario admin;
    private Usuario cliente;
    private Usuario usuarioParaExcluir;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        servicoRepository.deleteAll();
        usuarioRepository.deleteAll();

        admin = usuario("Admin", "admin-usuario-controller@example.com", "ADMIN");
        cliente = usuario("Cliente", "cliente-usuario-controller@example.com", "CLIENTE");
        usuarioParaExcluir = usuario("Excluir", "excluir-usuario-controller@example.com", "CLIENTE");
    }

    @Test
    void postUsuariosComDadosValidosRetornaSucessoEPerfilCliente() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioRequest("Novo Cliente", "novo-usuario-controller@example.com", "CLIENTE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void postUsuariosTentandoEnviarAdminRetornaCliente() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioRequest("Novo Admin", "novo-admin-publico@example.com", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    void postUsuariosComEmailInvalidoRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioRequest("Usuario", "email-invalido", "CLIENTE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("email")));
    }

    @Test
    void postUsuariosComNomeVazioRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioRequest("", "nome-vazio@example.com", "CLIENTE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("nome")));
    }

    @Test
    void postUsuariosComSenhaCurtaRetorna400ComFields() throws Exception {
        Map<String, Object> request = usuarioRequest("Usuario", "senha-curta@example.com", "CLIENTE");
        request.put("senha", "123");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("senha")));
    }

    @Test
    void getUsuariosComAdminRetorna200() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void getUsuariosComClienteRetorna403() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", 999999L).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario nao encontrado"));
    }

    @Test
    void putUsuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/usuarios/{id}", 999999L)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioUpdateRequest("Inexistente", "inexistente@example.com", "CLIENTE"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario nao encontrado"));
    }

    @Test
    void putSenhaUsuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/usuarios/{id}/senha", 999999L)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senha", "senhaNova123"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario nao encontrado"));
    }

    @Test
    void putSenhaComSenhaCurtaRetorna400() throws Exception {
        mockMvc.perform(put("/usuarios/{id}/senha", cliente.getId())
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senha", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("senha")));
    }

    @Test
    void deleteUsuarioComClienteRetorna403() throws Exception {
        mockMvc.perform(delete("/usuarios/{id}", usuarioParaExcluir.getId()).header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUsuarioComAdminRetornaSucesso() throws Exception {
        mockMvc.perform(delete("/usuarios/{id}", usuarioParaExcluir.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + tokenService.gerarToken(usuario);
    }

    private Usuario usuario(String nome, String email, String perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuario.setPerfil(perfil);
        return usuarioRepository.save(usuario);
    }

    private Map<String, Object> usuarioRequest(String nome, String email, String perfil) {
        return new java.util.HashMap<>(Map.of(
                "nome", nome,
                "email", email,
                "senha", "senha123",
                "perfil", perfil));
    }

    private Map<String, Object> usuarioUpdateRequest(String nome, String email, String perfil) {
        return Map.of(
                "nome", nome,
                "email", email,
                "perfil", perfil);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
