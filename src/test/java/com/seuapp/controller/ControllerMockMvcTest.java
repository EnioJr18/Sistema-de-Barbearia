package com.seuapp.controller;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        classes = BarbeariaApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:barbearia_mockmvc;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "api.security.token.secret=segredo-ficticio-apenas-para-testes"
        })
class ControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private Usuario admin;
    private Usuario cliente;
    private Usuario outroCliente;
    private Usuario barbeiro;
    private Servico servico;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        servicoRepository.deleteAll();
        usuarioRepository.deleteAll();

        admin = usuario("Admin", "admin@example.com", "ADMIN");
        cliente = usuario("Cliente", "cliente@example.com", "CLIENTE");
        outroCliente = usuario("Outro Cliente", "outro@example.com", "CLIENTE");
        barbeiro = usuario("Barbeiro", "barbeiro@example.com", "BARBEIRO");
        servico = servico("Corte", 40);
    }

    @Test
    void postUsuariosComDadosValidosRetornaCliente() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "Novo Cliente",
                                "email", "novo@example.com",
                                "senha", "senha123",
                                "perfil", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void postUsuariosComEmailInvalidoRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "Novo Cliente",
                                "email", "email-invalido",
                                "senha", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fields[*].field", hasItem("email")));
    }

    @Test
    void postUsuariosComNomeVazioRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "",
                                "email", "novo@example.com",
                                "senha", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("nome")));
    }

    @Test
    void postUsuariosComSenhaCurtaRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "Novo Cliente",
                                "email", "novo@example.com",
                                "senha", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("senha")));
    }

    @Test
    void getUsuariosSemTokenRetornaErroDeAutenticacao() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsuariosComClienteRetorna403() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsuariosComAdminRetorna200() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
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
        mockMvc.perform(delete("/usuarios/{id}", outroCliente.getId()).header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void postServicosComAdminRetornaSucesso() throws Exception {
        mockMvc.perform(post("/servicos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicoRequest("Barba", BigDecimal.valueOf(25), 30))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Barba"));
    }

    @Test
    void postServicosComBarbeiroRetornaSucesso() throws Exception {
        mockMvc.perform(post("/servicos")
                        .header("Authorization", bearer(barbeiro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicoRequest("Sobrancelha", BigDecimal.valueOf(15), 20))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Sobrancelha"));
    }

    @Test
    void postServicosComClienteRetorna403() throws Exception {
        mockMvc.perform(post("/servicos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicoRequest("Barba", BigDecimal.valueOf(25), 30))))
                .andExpect(status().isForbidden());
    }

    @Test
    void postServicosComPrecoNegativoRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/servicos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicoRequest("Barba", BigDecimal.valueOf(-1), 30))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("preco")));
    }

    @Test
    void postServicosComDuracaoZeroRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/servicos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicoRequest("Barba", BigDecimal.valueOf(25), 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("duracaoEmMinutos")));
    }

    @Test
    void postServicosComNomeVazioRetorna400ComFields() throws Exception {
        mockMvc.perform(post("/servicos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(servicoRequest("", BigDecimal.valueOf(25), 30))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("nome")));
    }

    @Test
    void getServicosComTokenValidoRetorna200() throws Exception {
        mockMvc.perform(get("/servicos").header("Authorization", bearer(cliente)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteServicoComAdminRetornaSucesso() throws Exception {
        mockMvc.perform(delete("/servicos/{id}", servico.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteServicoComBarbeiroRetorna403() throws Exception {
        mockMvc.perform(delete("/servicos/{id}", servico.getId()).header("Authorization", bearer(barbeiro)))
                .andExpect(status().isForbidden());
    }

    @Test
    void postAgendamentosValidoRetornaStatusPendente() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(cliente, barbeiro, servico, dataUtil().atTime(9, 0)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void postAgendamentosSemClienteRetorna400() throws Exception {
        Map<String, Object> request = agendamentoRequest(cliente, barbeiro, servico, dataUtil().atTime(9, 0));
        request.remove("clienteId");

        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("clienteInformado")));
    }

    @Test
    void postAgendamentosSemBarbeiroRetorna400() throws Exception {
        Map<String, Object> request = agendamentoRequest(cliente, barbeiro, servico, dataUtil().atTime(9, 0));
        request.remove("barbeiroId");

        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("barbeiroInformado")));
    }

    @Test
    void postAgendamentosSemServicoRetorna400() throws Exception {
        Map<String, Object> request = agendamentoRequest(cliente, barbeiro, servico, dataUtil().atTime(9, 0));
        request.remove("servicoId");

        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("servicoInformado")));
    }

    @Test
    void postAgendamentosSemFormaDePagamentoRetorna400() throws Exception {
        Map<String, Object> request = agendamentoRequest(cliente, barbeiro, servico, dataUtil().atTime(9, 0));
        request.remove("formaDePagamento");

        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("formaDePagamento")));
    }

    @Test
    void postAgendamentosNoPassadoRetorna400() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(cliente, barbeiro, servico, LocalDateTime.now().minusDays(1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nao e possivel realizar agendamentos no passado."));
    }

    @Test
    void postAgendamentosNoDomingoRetorna400() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(cliente, barbeiro, servico, proximoDomingo().atTime(9, 0)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A barbearia nao funciona aos domingos."));
    }

    @Test
    void postAgendamentosComConflitoRetorna409() throws Exception {
        salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 0), StatusAgendamento.PENDENTE);

        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(cliente, barbeiro, servico, dataUtil().atTime(9, 20)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este barbeiro ja possui um agendamento neste intervalo de horario."));
    }

    @Test
    void postAgendamentosClienteParaOutroClienteRetorna403() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(outroCliente, barbeiro, servico, dataUtil().atTime(9, 0)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAgendamentosComAdminRetorna200() throws Exception {
        mockMvc.perform(get("/agendamentos").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void getAgendamentosComClienteRetorna403() throws Exception {
        mockMvc.perform(get("/agendamentos").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAgendamentoPorIdComClienteDonoRetorna200() throws Exception {
        Agendamento agendamento = salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 0), StatusAgendamento.PENDENTE);

        mockMvc.perform(get("/agendamentos/{id}", agendamento.getId()).header("Authorization", bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamento.getId()));
    }

    @Test
    void getAgendamentoPorIdComBarbeiroResponsavelRetorna200() throws Exception {
        Agendamento agendamento = salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 0), StatusAgendamento.PENDENTE);

        mockMvc.perform(get("/agendamentos/{id}", agendamento.getId()).header("Authorization", bearer(barbeiro)))
                .andExpect(status().isOk());
    }

    @Test
    void getAgendamentoPorIdComOutroClienteRetorna403() throws Exception {
        Agendamento agendamento = salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 0), StatusAgendamento.PENDENTE);

        mockMvc.perform(get("/agendamentos/{id}", agendamento.getId()).header("Authorization", bearer(outroCliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAgendamentoPorIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/agendamentos/{id}", 999999L).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agendamento nao encontrado"));
    }

    @Test
    void putAgendamentoInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/agendamentos/{id}", 999999L)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoUpdateRequest(cliente, barbeiro, servico, dataUtil().atTime(10, 0)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agendamento nao encontrado"));
    }

    @Test
    void patchCancelarComClienteDonoRetornaSucesso() throws Exception {
        Agendamento agendamento = salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 0), StatusAgendamento.PENDENTE);

        mockMvc.perform(patch("/agendamentos/{id}/cancelar", agendamento.getId()).header("Authorization", bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void patchCancelarComOutroClienteRetorna403() throws Exception {
        Agendamento agendamento = salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 0), StatusAgendamento.PENDENTE);

        mockMvc.perform(patch("/agendamentos/{id}/cancelar", agendamento.getId()).header("Authorization", bearer(outroCliente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchCancelarAgendamentoInexistenteRetorna404() throws Exception {
        mockMvc.perform(patch("/agendamentos/{id}/cancelar", 999999L).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agendamento nao encontrado"));
    }

    @Test
    void patchCancelarAgendamentoJaCanceladoRetorna400() throws Exception {
        Agendamento agendamento = salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 0), StatusAgendamento.CANCELADO);

        mockMvc.perform(patch("/agendamentos/{id}/cancelar", agendamento.getId()).header("Authorization", bearer(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Agendamento ja esta cancelado."));
    }

    @Test
    void deleteAgendamentoInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/agendamentos/{id}", 999999L).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agendamento nao encontrado"));
    }

    @Test
    void getHorariosDisponiveisRetornaLista() throws Exception {
        mockMvc.perform(get("/agendamentos/horarios-disponiveis")
                        .header("Authorization", bearer(cliente))
                        .param("barbeiroId", barbeiro.getId().toString())
                        .param("servicoId", servico.getId().toString())
                        .param("data", dataUtil().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("08:00"));
    }

    @Test
    void getHorariosDisponiveisEmDomingoRetorna400() throws Exception {
        mockMvc.perform(get("/agendamentos/horarios-disponiveis")
                        .header("Authorization", bearer(cliente))
                        .param("barbeiroId", barbeiro.getId().toString())
                        .param("servicoId", servico.getId().toString())
                        .param("data", proximoDomingo().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A barbearia nao funciona aos domingos."));
    }

    @Test
    void getHorariosDisponiveisRemoveHorariosConflitantes() throws Exception {
        salvarAgendamento(cliente, barbeiro, servico, dataUtil().atTime(9, 20), StatusAgendamento.PENDENTE);

        mockMvc.perform(get("/agendamentos/horarios-disponiveis")
                        .header("Authorization", bearer(cliente))
                        .param("barbeiroId", barbeiro.getId().toString())
                        .param("servicoId", servico.getId().toString())
                        .param("data", dataUtil().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", not(hasItem("09:20"))))
                .andExpect(jsonPath("$[*]", hasItem("08:40")));
    }

    @Test
    void loginComCredenciaisValidasRetornaToken() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", cliente.getEmail(), "senha", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("ey")));
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
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDescricao(nome + " descricao");
        servico.setPreco(BigDecimal.valueOf(35));
        servico.setDuracaoEmMinutos(duracaoEmMinutos);
        return servicoRepository.save(servico);
    }

    private Agendamento salvarAgendamento(
            Usuario cliente,
            Usuario barbeiro,
            Servico servico,
            LocalDateTime dataEHora,
            StatusAgendamento status) {

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataEHora(dataEHora);
        agendamento.setStatus(status);
        agendamento.setFormaDePagamento(FormaPagamento.PIX);
        return agendamentoRepository.save(agendamento);
    }

    private Map<String, Object> servicoRequest(String nome, BigDecimal preco, int duracaoEmMinutos) {
        return Map.of(
                "nome", nome,
                "descricao", "Descricao",
                "preco", preco,
                "duracaoEmMinutos", duracaoEmMinutos);
    }

    private Map<String, Object> agendamentoRequest(
            Usuario cliente,
            Usuario barbeiro,
            Servico servico,
            LocalDateTime dataEHora) {

        return new java.util.HashMap<>(Map.of(
                "clienteId", cliente.getId(),
                "barbeiroId", barbeiro.getId(),
                "servicoId", servico.getId(),
                "dataEHora", dataEHora.toString(),
                "formaDePagamento", "PIX"));
    }

    private Map<String, Object> agendamentoUpdateRequest(
            Usuario cliente,
            Usuario barbeiro,
            Servico servico,
            LocalDateTime dataEHora) {

        Map<String, Object> request = agendamentoRequest(cliente, barbeiro, servico, dataEHora);
        request.put("status", "CONFIRMADO");
        return request;
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

    private LocalDate proximoDomingo() {
        LocalDate data = LocalDate.now().plusDays(1);
        while (data.getDayOfWeek() != DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data;
    }
}
