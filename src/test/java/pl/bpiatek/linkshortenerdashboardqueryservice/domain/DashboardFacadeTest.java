package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@ActiveProfiles("test")
class DashboardFacadeTest implements WithFullInfrastructure {

    @Autowired
    DashboardFacade facade;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DashboardLinkFixtures fixtures;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM dashboard_links");
    }

    @Test
    void shouldFindUserLinks() {
        // given
        var userId = "user-1";
        var link = fixtures.aDashboardLink(TestDashboardLink.builder()
                .userId(userId)
                .linkId("link-1")
                .shortUrl("test-short-url")
                .build());

        var pageable = PageRequest.of(0, 10);

        // when
        var page = facade.getUserLinks(userId, pageable);

        // then
        assertSoftly(s -> {
            s.assertThat(page.getTotalElements()).isOne();
            var linkDto = page.getContent().getFirst();
            s.assertThat(linkDto.userId()).isEqualTo(link.getUserId());
            s.assertThat(linkDto.linkId()).isEqualTo(link.getLinkId());
            s.assertThat(linkDto.shortUrl()).isEqualTo(link.getShortUrl());
            s.assertThat(linkDto.longUrl()).isEqualTo(link.getLongUrl());
            s.assertThat(linkDto.title()).isEqualTo(link.getTitle());
            s.assertThat(linkDto.isActive()).isEqualTo(link.isActive());
            s.assertThat(linkDto.createdAt()).isEqualTo(link.getCreatedAt());
            s.assertThat(linkDto.updatedAt()).isEqualTo(link.getUpdatedAt());
            s.assertThat(linkDto.totalClicks()).isEqualTo(link.getTotalClicks());
        });
    }

}