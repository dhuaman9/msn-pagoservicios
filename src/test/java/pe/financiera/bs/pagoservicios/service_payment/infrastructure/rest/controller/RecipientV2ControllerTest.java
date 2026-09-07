package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Alert;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientDashboard;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.FindLastRecipientsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.RecipientRestMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant;


import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipientV2Controller.class)
class RecipientV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FindLastRecipientsUseCase findLastRecipientsUseCase;

    @MockitoBean
    private RecipientRestMapper restMapper;

    private String codigoInterno;
    private RecipientDashboard recipientDashboardMock;


    @BeforeEach
    void setUp() {
        codigoInterno = "INT-001";

        Recipient recipientMock = Recipient.builder()
                .id("REC-001")
                .name("Claro")
                .build();

        Alert alertMock = Alert.builder()
                .title("Alert Title")
                .message("Alert Message")
                .type("INFO")
                .build();

        recipientDashboardMock = RecipientDashboard.builder()
                .recipients(List.of(recipientMock))
                .synchronizationDate("2024-06-01T12:00:00")
                .alert(alertMock)
                .build();

    }

    // ─────────────────────────────────────────────────────────────────────────
    // getRecipients endpoint: Éxito
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getRecipients_whenValidHeader_shouldReturnOkWithRecipientsResponse() throws Exception {
        // Arrange
        when(findLastRecipientsUseCase.execute(codigoInterno))
                .thenReturn(recipientDashboardMock);
        when(restMapper.toResponseList(any()))
                .thenReturn(Collections.emptyList());
        when(restMapper.toAlertResponse(any()))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/recipient/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderConstant.HEADER_COD_INTERNO, codigoInterno))
                .andExpect(status().isOk());
    }

    @Test
    void getRecipients_whenValidHeader_shouldExecuteFindLastRecipientsUseCase() throws Exception {
        // Arrange
        when(findLastRecipientsUseCase.execute(codigoInterno))
                .thenReturn(recipientDashboardMock);
        when(restMapper.toResponseList(any()))
                .thenReturn(Collections.emptyList());
        when(restMapper.toAlertResponse(any()))
                .thenReturn(null);

        // Act
        mockMvc.perform(get("/recipient/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderConstant.HEADER_COD_INTERNO, codigoInterno))
                .andExpect(status().isOk());

        // Assert
        verify(findLastRecipientsUseCase).execute(codigoInterno);
    }

    @Test
    void getRecipients_whenValidHeader_shouldMapRecipientsCorrectly() throws Exception {
        // Arrange
        when(findLastRecipientsUseCase.execute(codigoInterno))
                .thenReturn(recipientDashboardMock);
        when(restMapper.toResponseList(recipientDashboardMock.getRecipients()))
                .thenReturn(Collections.emptyList());
        when(restMapper.toAlertResponse(any()))
                .thenReturn(null);

        // Act
        mockMvc.perform(get("/recipient/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderConstant.HEADER_COD_INTERNO, codigoInterno))
                .andExpect(status().isOk());

        // Assert
        verify(restMapper).toResponseList(recipientDashboardMock.getRecipients());
    }

    @Test
    void getRecipients_whenValidHeader_shouldMapAlertCorrectly() throws Exception {
        // Arrange
        when(findLastRecipientsUseCase.execute(codigoInterno))
                .thenReturn(recipientDashboardMock);
        when(restMapper.toResponseList(any()))
                .thenReturn(Collections.emptyList());
        when(restMapper.toAlertResponse(recipientDashboardMock.getAlert()))
                .thenReturn(null);

        // Act
        mockMvc.perform(get("/recipient/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderConstant.HEADER_COD_INTERNO, codigoInterno))
                .andExpect(status().isOk());

        // Assert
        verify(restMapper).toAlertResponse(recipientDashboardMock.getAlert());
    }



    @Test
    void getRecipients_whenValidHeader_shouldPassCorrectCodigoInternoToUseCase() throws Exception {
        // Arrange
        String differentCodigoInterno = "INT-002";
        when(findLastRecipientsUseCase.execute(differentCodigoInterno))
                .thenReturn(recipientDashboardMock);
        when(restMapper.toResponseList(any()))
                .thenReturn(Collections.emptyList());
        when(restMapper.toAlertResponse(any()))
                .thenReturn(null);

        // Act
        mockMvc.perform(get("/recipient/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderConstant.HEADER_COD_INTERNO, differentCodigoInterno))
                .andExpect(status().isOk());

        // Assert
        verify(findLastRecipientsUseCase).execute(differentCodigoInterno);
    }
}

