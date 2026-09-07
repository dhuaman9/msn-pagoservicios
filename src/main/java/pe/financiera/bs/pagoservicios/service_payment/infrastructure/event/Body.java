package pe.financiera.bs.pagoservicios.service_payment.infrastructure.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "operationId", "transactionId", "operationNumber", "transferType",
    "currencyType", "transferAmount", "contract", "tokenTunki",
    "extractNumber", "movementNumber", "referenceNumber", "documentNumber",
    "documentType", "sourceType", "userId", "creationDate", "dynamicTopic", "result"
})
public class Body {

    @JsonProperty("operationId")
    private String operationId;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("operationNumber")
    private String operationNumber;

    @JsonProperty("transferType")
    private String transferType;

    @JsonProperty("currencyType")
    private String currencyType;

    @JsonProperty("transferAmount")
    private BigDecimal transferAmount;

    @JsonProperty("contract")
    private String contract;

    @JsonProperty("tokenTunki")
    private String tokenTunki;

    @JsonProperty("extractNumber")
    private Integer extractNumber;

    @JsonProperty("movementNumber")
    private Integer movementNumber;

    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("documentNumber")
    private String documentNumber;

    @JsonProperty("documentType")
    private String documentType;

    @JsonProperty("sourceType")
    private String sourceType;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("creationDate")
    private String creationDate;

    @JsonProperty("dynamicTopic")
    private String dynamicTopic;

    @JsonProperty("result")
    private Boolean result;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
