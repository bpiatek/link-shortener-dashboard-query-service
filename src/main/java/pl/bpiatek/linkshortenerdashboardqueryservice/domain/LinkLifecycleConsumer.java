package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto.LinkCreated;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto.LinkDeleted;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto.LinkUpdated;

import java.time.Instant;

@Component
class LinkLifecycleConsumer {

    private static final Logger log = LoggerFactory.getLogger(LinkLifecycleConsumer.class);
    private final DashboardLinkRepository dashboardLinkRepository;

    LinkLifecycleConsumer(DashboardLinkRepository dashboardLinkRepository) {
        this.dashboardLinkRepository = dashboardLinkRepository;
    }

    @KafkaListener(
            topics = "${topic.link.lifecycle}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "linkLifecycleEventsContainerFactory"
    )
    public void consumeLinkLifecycleEvent(LinkLifecycleEventProto.LinkLifecycleEvent event) {
        var payloadCase = event.getEventPayloadCase();
        switch (payloadCase) {
            case LINK_CREATED -> handleLinkCreated(event.getLinkCreated());
            case LINK_UPDATED -> handleLinkUpdated(event.getLinkUpdated());
            case LINK_DELETED -> handleLinkDeleted(event.getLinkDeleted());
            case EVENTPAYLOAD_NOT_SET ->
                    log.warn("Received LinkLifecycleEvent with no payload set.");
            default ->
                    log.warn("Received unknown event type in LinkLifecycleEvent: {}", payloadCase);
        }
    }

    private void handleLinkCreated(LinkCreated linkCreated) {
        log.info("Received LinkCreated event for link_id '{}'. Creating dashboard view.", linkCreated.getLinkId());
        var link = new DashboardLink(
                null,
                linkCreated.getLinkId(),
                linkCreated.getUserId(),
                linkCreated.getShortUrl(),
                linkCreated.getLongUrl(),
                null,
                linkCreated.getIsActive(),
                convertProtoToInstant(linkCreated.getCreatedAt()),
                convertProtoToInstant(linkCreated.getCreatedAt()),
                0
        );
        dashboardLinkRepository.create(link);
    }

    private void handleLinkUpdated(LinkUpdated linkUpdated) {
        log.info("Received LinkUpdated event for link_id '{}'. Updating dashboard view.", linkUpdated.getLinkId());
        var link = new DashboardLink(
                null,
                linkUpdated.getLinkId(),
                linkUpdated.getUserId(),
                linkUpdated.getShortUrl(),
                linkUpdated.getLongUrl(),
                linkUpdated.getTitle(),
                linkUpdated.getIsActive(),
                null,
                convertProtoToInstant(linkUpdated.getUpdatedAt()),
                0
        );
        dashboardLinkRepository.update(link);
    }

    private void handleLinkDeleted(LinkDeleted linkDeleted) {
        log.info("Received LinkDeleted event for link_id '{}'. Deleting from dashboard view.", linkDeleted.getLinkId());
        dashboardLinkRepository.delete(linkDeleted.getLinkId());
    }

    private Instant convertProtoToInstant(Timestamp protoTimestamp) {
        if (protoTimestamp == null) {
            return null;
        }

        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}
