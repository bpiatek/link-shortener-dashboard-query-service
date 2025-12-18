package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import pl.bpiatek.contracts.analytics.AnalyticsEventProto.LinkClickEnrichedEvent;

class EnrichedClickConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnrichedClickConsumer.class);
    private final DashboardLinkRepository repository;

    EnrichedClickConsumer(DashboardLinkRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = "${topic.analytics.enriched}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "linkEnrichedClickEventContainerFactory"
    )
    void consume(LinkClickEnrichedEvent event) {
        log.info("Received EnrichedClickEvent for link_id '{}'. Incrementing counters.", event.getLinkId());
        repository.incrementClickCounters(
                event.getLinkId(),
                event.getCountryCode(),
                event.getDeviceType(),
                event.getOsName()
        );

        repository.incrementCityClicks(
                event.getLinkId(),
                event.getCountryCode(),
                event.getCityName(),
                event.getCityLatitude(),
                event.getCityLongitude()
        );
    }
}
