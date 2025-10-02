package pl.bpiatek.linkshortenerdashboardqueryservice.domain;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.bpiatek.contracts.analytics.AnalyticsEventProto.LinkClickEnrichedEvent;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class EnrichedClickConsumerTest implements WithFullInfrastructure {

    @Autowired
    private KafkaTemplate<String, LinkClickEnrichedEvent> kafkaTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${topic.analytics.enriched}")
    private String topicName;

    @Autowired
    DashboardLinkFixtures fixtures;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", redpanda::getBootstrapServers);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM dashboard_links");
    }

    @Test
    void shouldIncrementCounters() {
        // given
        var dashboardLink = fixtures.aDashboardLink();
        var event = LinkClickEnrichedEvent.newBuilder()
                .setLinkId(dashboardLink.getLinkId())
                .setCountryCode("US")
                .setDeviceType("Desktop")
                .setOsName("Windows")
                .build();

        // when
        kafkaTemplate.send(topicName, event);

        // then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var linkByShortUrl = fixtures.getByShortUrl(dashboardLink.getShortUrl());
            assertThat(linkByShortUrl).isNotNull();
            assertSoftly(s -> {
                s.assertThat(linkByShortUrl.getClicksByCountry().size()).isOne();
                s.assertThat(linkByShortUrl.getClicksByCountry().get(event.getCountryCode())).isEqualTo(1L);
                s.assertThat(linkByShortUrl.getClicksByDevice().size()).isOne();
                s.assertThat(linkByShortUrl.getClicksByDevice().get(event.getDeviceType())).isEqualTo(1L);
                s.assertThat(linkByShortUrl.getClicksByOs().size()).isOne();
                s.assertThat(linkByShortUrl.getClicksByOs().get(event.getOsName())).isEqualTo(1L);
            });
        });
    }
}