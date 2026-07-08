package com.example.seuapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seuapp.BarbeariaApplication;
import com.seuapp.model.Agendamento;
import com.seuapp.model.Agendamento.FormaPagamento;
import com.seuapp.model.Agendamento.StatusAgendamento;
import com.seuapp.model.Servico;
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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        classes = BarbeariaApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:barbearia_security;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "api.security.token.secret=segredo-ficticio-apenas-para-testes"
        })
class SecurityMockMvcTest {

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
    private Usuario outroCliente;
    private Usuario barbeiro;
    private Usuario outroBarbeiro;
    private Servico servico;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        servicoRepository.deleteAll();
        usuarioRepository.deleteAll();

        admin = usuario("Admin", "admin-security@example.com", "ADMIN");
        cliente = usuario("Cliente", "cliente-security@example.com", "CLIENTE");
        outroCliente = usuario("Outro Cliente", "outro-cliente-security@example.com", "CLIENTE");
        barbeiro = usuario("Barbeiro", "barbeiro-security@example.com", "BARBEIRO");
        outroBarbeiro = usuario("Outro Barbeiro", "outro-barbeiro-security@example.com", "BARBEIRO");
        servico = servico("Corte", 40);
    }

    @Test
    void postLoginEhPublico() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", cliente.getEmail(), "senha", "senha123"))))
                .andExpect(status().isOk());
    }

    @Test
    void postUsuariosEhPublico() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioRequest("Publico", "publico-security@example.com", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    void rotaProtegidaSemTokenEhBloqueada() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rotaProtegidaComTokenInvalidoEhBloqueada() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAcessaGetUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNaoAcessaGetUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void barbeiroNaoAcessaGetUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(barbeiro)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAcessaGetAgendamentos() throws Exception {
        mockMvc.perform(get("/agendamentos").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNaoAcessaGetAgendamentos() throws Exception {
        mockMvc.perform(get("/agendamentos").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void barbeiroNaoAcessaGetAgendamentos() throws Exception {
        mockMvc.perform(get("/agendamentos").header("Authorization", bearer(barbeiro)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConsegueCriarBarbeiro() throws Exception {
        mockMvc.perform(post("/usuarios/barbeiro")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioRequest("Novo Barbeiro", "novo-barbeiro-security@example.com", "CLIENTE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("BARBEIRO"));
    }

    @Test
    void clienteNaoConsegueCriarBarbeiro() throws Exception {
        mockMvc.perform(post("/usuarios/barbeiro")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(usuarioRequest("Novo Barbeiro", "bloqueado-security@example.com", "BARBEIRO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteAcessaApenasAgendamentoProprio() throws Exception {
        Agendamento proprio = salvarAgendamento(cliente, barbeiro, dataUtil().atTime(9, 0));
        Agendamento deOutroCliente = salvarAgendamento(outroCliente, barbeiro, dataUtil().atTime(10, 0));

        mockMvc.perform(get("/agendamentos/{id}", proprio.getId()).header("Authorization", bearer(cliente)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/agendamentos/{id}", deOutroCliente.getId()).header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void barbeiroAcessaApenasAgendamentoEmQueEhResponsavel() throws Exception {
        Agendamento responsavel = salvarAgendamento(cliente, barbeiro, dataUtil().atTime(9, 0));
        Agendamento deOutroBarbeiro = salvarAgendamento(cliente, outroBarbeiro, dataUtil().atTime(10, 0));

        mockMvc.perform(get("/agendamentos/{id}", responsavel.getId()).header("Authorization", bearer(barbeiro)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/agendamentos/{id}", deOutroBarbeiro.getId()).header("Authorization", bearer(barbeiro)))
                .andExpect(status().isForbidden());
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

    private Servico servico(String nome, int duracaoEmMinutos) {
        Servico novoServico = new Servico();
        novoServico.setNome(nome);
        novoServico.setDescricao(nome + " descricao");
        novoServico.setPreco(BigDecimal.valueOf(35));
        novoServico.setDuracaoEmMinutos(duracaoEmMinutos);
        return servicoRepository.save(novoServico);
    }

    private Agendamento salvarAgendamento(Usuario cliente, Usuario barbeiro, LocalDateTime dataEHora) {
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataEHora(dataEHora);
        agendamento.setFormaDePagamento(FormaPagamento.PIX);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        return agendamentoRepository.save(agendamento);
    }

    private Map<String, Object> usuarioRequest(String nome, String email, String perfil) {
        return Map.of(
                "nome", nome,
                "email", email,
                "senha", "senha123",
                "perfil", perfil);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private LocalDate dataUtil() {
        LocalDate data = LocalDate.now().plusDays(7);
        while (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data;
    }
}
