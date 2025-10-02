package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@JdbcTest
@Import({JdbcDashboardLinkRepository.class, DashboardLinkFixtures.class})
@ActiveProfiles("test")
class JdbcDashboardLinkRepositoryTest implements WithPostgres {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcDashboardLinkRepository repository;

    @Autowired
    DashboardLinkFixtures fixtures;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM dashboard_links");
    }

    @Test
    void shouldSaveDashboardLinkInDatabase() {
        // given
        var now = Instant.parse("2025-08-22T10:00:00Z");
        var link = new DashboardLink(
                null,
                "linkId",
                "userId",
                "shortUrl",
                "longUrl",
                "title",
                true,
                now,
                now,
                0
        );

        // when
        repository.create(link);

        // then
        var fromDb = fixtures.getByShortUrl("shortUrl");
        assertSoftly(s -> {
            s.assertThat(fromDb.getId()).isPositive();
            s.assertThat(fromDb.getLinkId()).isEqualTo(link.linkId());
            s.assertThat(fromDb.getUserId()).isEqualTo(link.userId());
            s.assertThat(fromDb.getShortUrl()).isEqualTo(link.shortUrl());
            s.assertThat(fromDb.getLongUrl()).isEqualTo(link.longUrl());
            s.assertThat(fromDb.getTitle()).isEqualTo(link.title());
            s.assertThat(fromDb.isActive()).isEqualTo(link.isActive());
            s.assertThat(fromDb.getCreatedAt()).isEqualTo(link.createdAt());
            s.assertThat(fromDb.getUpdatedAt()).isEqualTo(link.updatedAt());
            s.assertThat(fromDb.getTotalClicks()).isEqualTo(link.totalClicks());
            s.assertThat(fromDb.getClicksByCountry()).isEmpty();
            s.assertThat(fromDb.getClicksByDevice()).isEmpty();
            s.assertThat(fromDb.getClicksByOs()).isEmpty();
        });
    }

    @Test
    void shouldUpdateDashboardLink() {
        // given
        var dashboardLink = fixtures.aDashboardLink(TestDashboardLink.builder()
                .title("test title")
                .isActive(true)
                .updatedAt(Instant.parse("2025-09-01T10:00:00Z"))
                .longUrl("https://example.com/long-test-url")
                .build());

        var toUpdate = new DashboardLink(
                null,
                dashboardLink.getLinkId(),
                null,
                null,
                "https://test.com/long-url",
                "updated title",
                false,
                null,
                Instant.parse("2025-09-11T10:00:00Z"),
                0);

        // when
        repository.update(toUpdate);

        // then
        var fromDb = fixtures.getByShortUrl(dashboardLink.getShortUrl());
        assertThat(fromDb).isNotNull();
        assertSoftly(s -> {
            s.assertThat(fromDb.getId()).isEqualTo(dashboardLink.getId());
            s.assertThat(fromDb.getLinkId()).isEqualTo(dashboardLink.getLinkId());
            s.assertThat(fromDb.getUserId()).isEqualTo(dashboardLink.getUserId());
            s.assertThat(fromDb.getShortUrl()).isEqualTo(dashboardLink.getShortUrl());
            s.assertThat(fromDb.getLongUrl()).isEqualTo(toUpdate.longUrl());
            s.assertThat(fromDb.getTitle()).isEqualTo(toUpdate.title());
            s.assertThat(fromDb.isActive()).isEqualTo(toUpdate.isActive());
            s.assertThat(fromDb.getCreatedAt()).isEqualTo(dashboardLink.getCreatedAt());
            s.assertThat(fromDb.getUpdatedAt()).isEqualTo(toUpdate.updatedAt());
        });
    }

    @Test
    void shouldDeleteDashboardLinkByLinkId() {
        // given
        var dashboardLink = fixtures.aDashboardLink();

        // when
        repository.delete(dashboardLink.getLinkId());

        // then
        var fromDb = fixtures.getByShortUrl(dashboardLink.getShortUrl());
        assertThat(fromDb).isNull();
    }

    @Test
    void shouldIncrementCountersFromEmpty() {
        // given
        var dashboardLink = fixtures.aDashboardLink();

        // when
        repository.incrementClickCounters(dashboardLink.getLinkId(), "US", "Desktop", "Windows");

        // then
        var fromDb = fixtures.getByShortUrl(dashboardLink.getShortUrl());
        assertThat(fromDb).isNotNull();
        assertSoftly(s -> {
            s.assertThat(fromDb.getId()).isEqualTo(dashboardLink.getId());
            s.assertThat(fromDb.getClicksByCountry().size()).isOne();
            s.assertThat(fromDb.getClicksByCountry().get("US")).isEqualTo(1L);
            s.assertThat(fromDb.getClicksByDevice().size()).isOne();
            s.assertThat(fromDb.getClicksByDevice().get("Desktop")).isEqualTo(1L);
            s.assertThat(fromDb.getClicksByOs().size()).isOne();
            s.assertThat(fromDb.getClicksByOs().get("Windows")).isEqualTo(1L);
            s.assertThat(fromDb.getTotalClicks()).isEqualTo(1L);
        });
    }

    @Test
    void shouldIncrementCountersTwiceWithSameData() {
        // given
        var dashboardLink = fixtures.aDashboardLink();

        // when
        repository.incrementClickCounters(dashboardLink.getLinkId(), "US", "Desktop", "Windows");
        repository.incrementClickCounters(dashboardLink.getLinkId(), "US", "Desktop", "Windows");

        // then
        var fromDb = fixtures.getByShortUrl(dashboardLink.getShortUrl());
        assertThat(fromDb).isNotNull();
        assertSoftly(s -> {
            s.assertThat(fromDb.getId()).isEqualTo(dashboardLink.getId());
            s.assertThat(fromDb.getClicksByCountry().size()).isOne();
            s.assertThat(fromDb.getClicksByCountry().get("US")).isEqualTo(2L);
            s.assertThat(fromDb.getClicksByDevice().size()).isOne();
            s.assertThat(fromDb.getClicksByDevice().get("Desktop")).isEqualTo(2L);
            s.assertThat(fromDb.getClicksByOs().size()).isOne();
            s.assertThat(fromDb.getClicksByOs().get("Windows")).isEqualTo(2L);
            s.assertThat(fromDb.getTotalClicks()).isEqualTo(2L);
        });
    }

    @Test
    void shouldIncrementCountersForDifferentClicks() {
        // given
        var dashboardLink = fixtures.aDashboardLink();

        // when
        repository.incrementClickCounters(dashboardLink.getLinkId(), "US", "Desktop", "Windows");
        repository.incrementClickCounters(dashboardLink.getLinkId(), "PL", "Phone", "IOS");

        // then
        var fromDb = fixtures.getByShortUrl(dashboardLink.getShortUrl());
        assertThat(fromDb).isNotNull();
        assertSoftly(s -> {
            s.assertThat(fromDb.getId()).isEqualTo(dashboardLink.getId());
            s.assertThat(fromDb.getClicksByCountry().size()).isEqualTo(2);
            s.assertThat(fromDb.getClicksByDevice().size()).isEqualTo(2);
            s.assertThat(fromDb.getClicksByOs().size()).isEqualTo(2);

            s.assertThat(fromDb.getClicksByCountry().get("US")).isEqualTo(1L);
            s.assertThat(fromDb.getClicksByDevice().get("Desktop")).isEqualTo(1L);
            s.assertThat(fromDb.getClicksByOs().get("Windows")).isEqualTo(1L);

            s.assertThat(fromDb.getClicksByCountry().get("PL")).isEqualTo(1L);
            s.assertThat(fromDb.getClicksByDevice().get("Phone")).isEqualTo(1L);
            s.assertThat(fromDb.getClicksByOs().get("IOS")).isEqualTo(1L);

            s.assertThat(fromDb.getTotalClicks()).isEqualTo(2L);
        });
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper;
        }
    }
}