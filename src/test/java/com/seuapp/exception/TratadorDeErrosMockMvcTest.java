package com.seuapp.exception;

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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        classes = BarbeariaApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:barbearia_erros;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "api.security.token.secret=segredo-ficticio-apenas-para-testes"
        })
class TratadorDeErrosMockMvcTest {

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
    private Usuario barbeiro;
    private Servico servico;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        servicoRepository.deleteAll();
        usuarioRepository.deleteAll();

        admin = usuario("Admin", "admin-erros@example.com", "ADMIN");
        cliente = usuario("Cliente", "cliente-erros@example.com", "CLIENTE");
        barbeiro = usuario("Barbeiro", "barbeiro-erros@example.com", "BARBEIRO");
        servico = servico(40);
    }

    @Test
    void erroDeValidacaoRetornaStatus400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Teste", "email", "email-invalido", "senha", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void erroDeValidacaoRetornaListaFields() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "", "email", "email-invalido", "senha", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("nome")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("email")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("senha")));
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", 999999L).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Usuario nao encontrado"));
    }

    @Test
    void conflitoDeAgendaRetorna409() throws Exception {
        salvarAgendamento(dataUtil().atTime(9, 0));

        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(dataUtil().atTime(9, 20)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void regraDeNegocioInvalidaRetorna400ComMensagemEspecifica() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(dataUtil().atTime(7, 30)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O horario de funcionamento inicia as 08:00."));
    }

    @Test
    void bodyJsonMalformadoRetorna400ComMensagemGenerica() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisicao invalida."))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void acessoNegadoRetorna403() throws Exception {
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(cliente)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    void respostasDeErroNaoExpoemStackTrace() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.message", not(containsString("Exception"))));
    }

    @Test
    void erroDeHorarioInvalidoPreservaMensagemEspecifica() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(dataUtil().atTime(17, 40)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O agendamento deve terminar ate as 18:00."));
    }

    @Test
    void erroDeConflitoDeAgendaPreservaMensagemEspecifica() throws Exception {
        salvarAgendamento(dataUtil().atTime(9, 0));

        mockMvc.perform(post("/agendamentos")
                        .header("Authorization", bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agendamentoRequest(dataUtil().atTime(9, 20)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este barbeiro ja possui um agendamento neste intervalo de horario."));
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

    private Servico servico(int duracaoEmMinutos) {
        Servico novoServico = new Servico();
        novoServico.setNome("Corte");
        novoServico.setDescricao("Corte descricao");
        novoServico.setPreco(BigDecimal.valueOf(35));
        novoServico.setDuracaoEmMinutos(duracaoEmMinutos);
        return servicoRepository.save(novoServico);
    }

    private Agendamento salvarAgendamento(LocalDateTime dataEHora) {
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataEHora(dataEHora);
        agendamento.setFormaDePagamento(FormaPagamento.PIX);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        return agendamentoRepository.save(agendamento);
    }

    private Map<String, Object> agendamentoRequest(LocalDateTime dataEHora) {
        Map<String, Object> request = new HashMap<>();
        request.put("clienteId", cliente.getId());
        request.put("barbeiroId", barbeiro.getId());
        request.put("servicoId", servico.getId());
        request.put("dataEHora", dataEHora.toString());
        request.put("formaDePagamento", "PIX");
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
}
