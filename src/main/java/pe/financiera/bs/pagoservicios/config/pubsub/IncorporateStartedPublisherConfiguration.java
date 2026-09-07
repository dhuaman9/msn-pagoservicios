package pe.financiera.bs.pagoservicios.config.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;
import lombok.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.financiera.framework.pubsub.queue.publisher.MessagePublisher;
import pe.financiera.framework.pubsub.queue.publisher.PublisherHandler;

import java.io.IOException;

@Configuration
public class IncorporateStartedPublisherConfiguration {

    @Value("${queue.publish.incorporate-started}")
    private String topicIncorporateStarted;

    @Bean(destroyMethod = "terminate", name = "incorporateStartedHandler")
    public PublisherHandler incorporateStartedHandler(@NonNull String projectId,
                                                      @NonNull CredentialsProvider credentialsProvider,
                                                      ObjectProvider<TransportChannelProvider> channelProviderProvider) throws IOException {

        return createPublishHandler(projectId, topicIncorporateStarted, credentialsProvider, channelProviderProvider);
    }

    @Bean(name = "incorporateStartedTopic")
    public MessagePublisher incorporateStartedMessagePublisher(@NonNull ObjectMapper objectMapper,
                                                               @NonNull @Qualifier("incorporateStartedHandler") PublisherHandler publisherHandler) {
        return new MessagePublisher(publisherHandler, objectMapper);
    }

    private PublisherHandler createPublishHandler(String projectId, String topic, CredentialsProvider credentialsProvider,
                                                  ObjectProvider<TransportChannelProvider> channelProviderProvider) throws IOException {
        final ProjectTopicName projectTopicName = ProjectTopicName.of(projectId, topic);
        final Publisher.Builder builder = Publisher
            .newBuilder(projectTopicName)
            .setCredentialsProvider(credentialsProvider);

        // En perfil local existe un channel provider al emulador Pub/Sub; en otros perfiles no.
        final TransportChannelProvider channelProvider = channelProviderProvider.getIfAvailable();
        if (channelProvider != null) {
            builder.setChannelProvider(channelProvider);
        }

        final Publisher publisher = builder.build();
        return new PublisherHandler(publisher);
    }
}
