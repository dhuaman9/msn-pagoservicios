package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeadersMapper {

    private final ObjectMapper objectMapper;

    public String headerToJson(HttpHeaders headers) {
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            log.warn("Could not serialize headers to JSON: {}", e.getMessage(), e);
            return "{}";
        }
    }
}
