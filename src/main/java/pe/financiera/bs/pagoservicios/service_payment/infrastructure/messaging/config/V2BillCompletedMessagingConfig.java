package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.ProjectSubscriptionName;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.threeten.bp.Duration;
import pe.financiera.framework.pubsub.messaging.ProcessingResult;
import pe.financiera.framework.pubsub.queue.consumer.SubscriberHandler;
import pe.financiera.framework.pubsub.queue.consumer.message.DeduplicationMessageReceiver;
import pe.financiera.framework.pubsub.queue.consumer.message.MessageContextParser;
import pe.financiera.framework.pubsub.queue.consumer.message.MessageConsumer;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.TransactionEvent;

/**
 * Configuración V2 para el suscriptor de PubSub de Facturas Completadas.
 * Independiente de la configuración del paquete interbank.
 */
@Configuration
@Slf4j
public class V2BillCompletedMessagingConfig {

    @Value("${queue.sp.bill.completed.v2.subscription.name}")
    private String queueSubscriptionName;

    @Value("${queue.dead.letter.publish.name}")
    private String deadLetterQueue;

    @Value("${queue.sp.bill.completed.v2.subscription.max-retries}")
    private Integer maxRetries;

    @Value("${queue.sp.bill.completed.v2.subscription.deduplication-lock-ttl}")
    private Integer deduplicationLockTtl;

    @Value("${queue.sp.bill.completed.v2.subscription.key-ttl}")
    private Integer keyTtl;

    @Value("${queue.sp.bill.completed.v2.subscription.max-ack-extension-period}")
    private Integer maxAckExtensionPeriod;

    @Bean(name = "v2ServicePaymentBillCompletedMessageReceiver")
    public DeduplicationMessageReceiver<TransactionEvent> v2PaymentMessageReceiver(
        @NonNull final String projectId,
        @NonNull final CredentialsProvider credentialsProvider,
        final ObjectMapper objectMapper,
        final RMapCache<String, ProcessingResult> processingResultRMap,
        @Qualifier("v2BillCompletedConsumer") final MessageConsumer<TransactionEvent> v2BillCompletedConsumer) {

        log.info("Registering V2 Bill Completed Message Receiver for subscription: {}", queueSubscriptionName);

        return DeduplicationMessageReceiver.<TransactionEvent>builder()
            .maxRetries(maxRetries)
            .objectMapper(objectMapper)
            .lockMap(processingResultRMap)
            .eventConsumer(v2BillCompletedConsumer)
            .clazz(TransactionEvent.class)
            .leaseTimeSeconds(deduplicationLockTtl)
            .keyTtlSeconds(keyTtl)
            .subscriptionName(queueSubscriptionName)
            .projectId(projectId)
            .credentialsProvider(credentialsProvider)
            .deadLetterQueue(deadLetterQueue)
            .messageContextParser(new MessageContextParser())
            .build();
    }

    @Bean(name = "v2BillCompletedQueueConsumerHandler", destroyMethod = "terminate")
    public SubscriberHandler v2BillCompletedQueueConsumer(
        @NonNull final String projectId,
        @NonNull @Qualifier("v2ServicePaymentBillCompletedMessageReceiver") final DeduplicationMessageReceiver<TransactionEvent> messageReceiver,
        @NonNull final CredentialsProvider credentialsProvider,
        final ObjectProvider<TransportChannelProvider> channelProviderProvider) {

        final ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, queueSubscriptionName);

        final Subscriber.Builder builder =
            Subscriber.newBuilder(subscriptionName, messageReceiver)
                .setMaxAckExtensionPeriod(Duration.ofSeconds(maxAckExtensionPeriod))
                .setCredentialsProvider(credentialsProvider);

        // En perfil local existe un channel provider al emulador Pub/Sub; en otros perfiles no.
        final TransportChannelProvider channelProvider = channelProviderProvider.getIfAvailable();
        if (channelProvider != null) {
            log.info("Using custom Pub/Sub channel provider (emulator) for subscription: {}", queueSubscriptionName);
            builder.setChannelProvider(channelProvider);
        }

        final Subscriber subscriber = builder.build();

        subscriber.addListener(
            new Subscriber.Listener() {
                public void failed(Subscriber.State from, Throwable failure) {
                    log.error("V2 Subscriber Failure with state {} from queue {}", from, queueSubscriptionName, failure);
                }
            },
            MoreExecutors.directExecutor());

        log.info("Starting V2 PubSub Subscriber for: {}", queueSubscriptionName);
        subscriber.startAsync();

        return new SubscriberHandler(subscriber);
    }
}
