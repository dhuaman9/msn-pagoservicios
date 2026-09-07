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

import java.util.HashMap;
import java.util.Map;
import pe.financiera.framework.event.base.message.third.party.Error;
import pe.financiera.framework.event.base.message.third.party.Header;
import pe.financiera.framework.pubsub.messaging.Message;

@Data
@ToString
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"header", "body", "error", "customProperties"})
public class TransactionEvent implements Message<Body> {

    @JsonProperty("header")
    private Header header;

    @JsonProperty("body")
    private Body body;

    @JsonProperty("error")
    private Error error;

    @JsonProperty("customProperties")
    private Map<String, Object> customProperties;

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
