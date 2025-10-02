package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import com.google.protobuf.util.Timestamps;
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
import pl.bpiatek.contracts.link.LinkLifecycleEventProto;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto.LinkDeleted;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto.LinkLifecycleEvent;
import pl.bpiatek.contracts.link.LinkLifecycleEventProto.LinkUpdated;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class LinkLifecycleConsumerTest implements WithFullInfrastructure {

    @Autowired
    private KafkaTemplate<String, LinkLifecycleEvent> kafkaTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${topic.link.lifecycle}")
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
    void shouldHandleLinkCreateEventAndSaveLink() {
        // given
        var now = Instant.parse("2025-08-04T10:11:30Z");
        var shortUrl = "en78Se";

        var linkCreated = LinkLifecycleEventProto.LinkCreated.newBuilder()
                .setLinkId("12")
                .setUserId("user-13")
                .setShortUrl(shortUrl)
                .setLongUrl("https://example.com/some-long-url")
                .setTitle("title")
                .setIsActive(true)
                .setCreatedAt(Timestamps.fromMillis(now.toEpochMilli()))
                .build();

        var event = LinkLifecycleEvent.newBuilder()
                .setLinkCreated(linkCreated)
                .build();

        // when
        kafkaTemplate.send(topicName, event);

        // then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var linkByShortUrl = fixtures.getByShortUrl(shortUrl);
            assertThat(linkByShortUrl).isNotNull();
            assertSoftly(s -> {
                s.assertThat(linkByShortUrl.getLinkId()).isEqualTo(linkCreated.getLinkId());
                s.assertThat(linkByShortUrl.getUserId()).isEqualTo(linkCreated.getUserId());
                s.assertThat(linkByShortUrl.isActive()).isEqualTo(linkCreated.getIsActive());
                s.assertThat(linkByShortUrl.getShortUrl()).isEqualTo(linkCreated.getShortUrl());
                s.assertThat(linkByShortUrl.getCreatedAt()).isEqualTo(now);
                s.assertThat(linkByShortUrl.getUpdatedAt()).isEqualTo(now);
                s.assertThat(linkByShortUrl.getTitle()).isEqualTo(linkCreated.getTitle());
                s.assertThat(linkByShortUrl.getLongUrl()).isEqualTo(linkCreated.getLongUrl());
                s.assertThat(linkByShortUrl.getTotalClicks()).isZero();
            });
        });
    }

    @Test
    void shouldHandleLinkUpdatedEvent() {
        // given
        var creationTime = Instant.parse("2025-08-04T10:11:30Z");
        var updateTime = Instant.parse("2025-09-01T10:11:30Z");
        var shortUrl = "en78Se";

        var alreadyInsertedLink = fixtures.aDashboardLink(TestDashboardLink.builder()
                .shortUrl(shortUrl)
                .createdAt(creationTime)
                .updatedAt(creationTime)
                .longUrl("https://example.com/some-long-url")
                .isActive(true)
                .title("some title")
                .build());

        var linkUpdated = LinkUpdated.newBuilder()
                .setShortUrl(shortUrl)
                .setUserId(alreadyInsertedLink.getUserId())
                .setLinkId(alreadyInsertedLink.getLinkId())
                .setUpdatedAt(Timestamps.fromMillis(updateTime.toEpochMilli()))
                .setLongUrl("https://test.com/long-url")
                .setTitle("updated title")
                .setIsActive(false)
                .build();

        var event = LinkLifecycleEvent.newBuilder()
                .setLinkUpdated(linkUpdated)
                .build();

        // when
        kafkaTemplate.send(topicName, event);

        // then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var linkByShortUrl = fixtures.getByShortUrl(shortUrl);
            assertThat(linkByShortUrl).isNotNull();
            assertSoftly(s -> {
                s.assertThat(linkByShortUrl.getLongUrl()).isEqualTo(linkUpdated.getLongUrl());
                s.assertThat(linkByShortUrl.getTitle()).isEqualTo(linkUpdated.getTitle());
                s.assertThat(linkByShortUrl.isActive()).isFalse();
                s.assertThat(linkByShortUrl.getLinkId()).isEqualTo(alreadyInsertedLink.getLinkId());
                s.assertThat(linkByShortUrl.getUserId()).isEqualTo(alreadyInsertedLink.getUserId());
                s.assertThat(linkByShortUrl.getShortUrl()).isEqualTo(shortUrl);
                s.assertThat(linkByShortUrl.getCreatedAt()).isEqualTo(creationTime);
                s.assertThat(linkByShortUrl.getUpdatedAt()).isEqualTo(updateTime);
            });
        });
    }

    @Test
    void shouldHandleLinkDeletedEvent() {
        // given
        var shortUrl = "en78Se";

        var alreadyInsertedLink = fixtures.aDashboardLink();
        var linkDeleted = LinkDeleted.newBuilder()
                .setLinkId(alreadyInsertedLink.getLinkId())
                .build();

        var event = LinkLifecycleEvent.newBuilder()
                .setLinkDeleted(linkDeleted)
                .build();

        // when
        kafkaTemplate.send(topicName, event);

        // then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var linkByShortUrl = fixtures.getByShortUrl(shortUrl);
            assertThat(linkByShortUrl).isNull();
        });
    }
}