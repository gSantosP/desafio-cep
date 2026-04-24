package com.example.cep.application;

import com.example.cep.domain.exception.CepInvalidoException;
import com.example.cep.domain.exception.CepNaoEncontradoException;
import com.example.cep.domain.exception.ProvedorCepException;
import com.example.cep.domain.model.ConsultaLog;
import com.example.cep.domain.model.Endereco;
import com.example.cep.domain.model.StatusConsulta;
import com.example.cep.domain.port.CepProvider;
import com.example.cep.domain.port.ConsultaLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do caso de uso principal. O objetivo nao e so validar
 * comportamento, mas DEMONSTRAR o ganho da arquitetura hexagonal: o teste
 * nao sobe Spring nem WireMock - substitui as portas por mocks.
 */
@ExtendWith(MockitoExtension.class)
class ConsultarCepUseCaseTest {

    @Mock
    private CepProvider cepProvider;

    @Mock
    private ConsultaLogRepository logRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ConsultarCepUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new ConsultarCepUseCase(cepProvider, logRepository, objectMapper);
    }

    @Test
    void deveConsultarCepComSucessoEPersistirLog() {
        Endereco esperado = new Endereco("01310-100", "Avenida Paulista", "", "Bela Vista",
                "Sao Paulo", "SP", "3550308", "1004", "11", "7107");
        when(cepProvider.buscar("01310100")).thenReturn(esperado);
        when(logRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Endereco resultado = useCase.executar("01310-100");

        assertThat(resultado).isEqualTo(esperado);

        ArgumentCaptor<ConsultaLog> captor = ArgumentCaptor.forClass(ConsultaLog.class);
        verify(logRepository).salvar(captor.capture());
        ConsultaLog log = captor.getValue();
        assertThat(log.cep()).isEqualTo("01310100");
        assertThat(log.status()).isEqualTo(StatusConsulta.SUCESSO);
        assertThat(log.respostaApi()).contains("Avenida Paulista");
    }

    @Test
    void deveRejeitarCepComFormatoInvalidoSemChamarProvedor() {
        assertThatThrownBy(() -> useCase.executar("abc"))
                .isInstanceOf(CepInvalidoException.class)
                .hasMessageContaining("abc");

        verifyNoInteractions(cepProvider, logRepository);
    }

    @Test
    void deveRejeitarCepNuloSemChamarProvedor() {
        assertThatThrownBy(() -> useCase.executar(null))
                .isInstanceOf(CepInvalidoException.class);

        verifyNoInteractions(cepProvider, logRepository);
    }

    @Test
    void devePersistirLogQuandoCepNaoEncontrado() {
        when(cepProvider.buscar(anyString())).thenThrow(new CepNaoEncontradoException("99999999"));
        when(logRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> useCase.executar("99999999"))
                .isInstanceOf(CepNaoEncontradoException.class);

        ArgumentCaptor<ConsultaLog> captor = ArgumentCaptor.forClass(ConsultaLog.class);
        verify(logRepository).salvar(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(StatusConsulta.NAO_ENCONTRADO);
    }

    @Test
    void devePersistirLogQuandoProvedorFalha() {
        when(cepProvider.buscar(anyString()))
                .thenThrow(new ProvedorCepException("timeout", null));
        when(logRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> useCase.executar("01310100"))
                .isInstanceOf(ProvedorCepException.class);

        ArgumentCaptor<ConsultaLog> captor = ArgumentCaptor.forClass(ConsultaLog.class);
        verify(logRepository).salvar(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(StatusConsulta.ERRO);
        assertThat(captor.getValue().respostaApi()).contains("timeout");
    }

    @Test
    void deveSanitizarCepRemovendoMascaraEEspacos() {
        Endereco endereco = new Endereco("04567-000", "Av Nacoes Unidas", "", "Vila Olimpia",
                "Sao Paulo", "SP", null, null, "11", null);
        when(cepProvider.buscar("04567000")).thenReturn(endereco);
        when(logRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.executar("04567-000");

        verify(cepProvider).buscar("04567000");
    }
}
