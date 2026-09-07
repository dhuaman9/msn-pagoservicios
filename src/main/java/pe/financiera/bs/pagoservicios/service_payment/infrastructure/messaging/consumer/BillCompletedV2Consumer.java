package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import pe.financiera.framework.pubsub.queue.consumer.message.MessageConsumer;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.TransactionEvent;

@Slf4j
@Component("v2BillCompletedConsumer")
@RequiredArgsConstructor
public class BillCompletedV2Consumer implements MessageConsumer<TransactionEvent> {

    private final CompleteBillPaymentUseCase completeBillPaymentUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public void accept(TransactionEvent event, Integer nbRetry) {
        log.info("V2 Messaging: Event received: {}", serialize(event));
        log.info("V2 Messaging: Received transaction event for operation {}", event.getBody().getOperationNumber());
        CompleteBillPaymentUseCase.CompleteBillPaymentCommand command =
                new CompleteBillPaymentUseCase.CompleteBillPaymentCommand(
                        event.getBody().getOperationNumber(),
                        event.getBody().getTransactionId(),
                        Boolean.TRUE.equals(event.getBody().getResult()),
                        event.getBody().getTransferAmount(),
                        event.getBody().getUserId(),
                        event.getBody().getOperationId(),
                        event.getError() != null ? event.getError().getCode() : null,
                        event.getCustomProperties()
                );
        completeBillPaymentUseCase.execute(command);
    }

    private String serialize(TransactionEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize TransactionEvent for logging", e);
            return String.valueOf(event);
        }
    }
}
