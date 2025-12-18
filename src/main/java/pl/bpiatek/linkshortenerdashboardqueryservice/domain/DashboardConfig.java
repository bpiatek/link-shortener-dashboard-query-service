package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
class DashboardConfig {

    @Bean
    EnrichedClickConsumer enrichedClickConsumer(DashboardLinkRepository dashboardLinkRepository) {
        return new EnrichedClickConsumer(dashboardLinkRepository);
    }

    @Bean
    DashboardLinkRepository dashboardLinkRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcDashboardLinkRepository(jdbcTemplate);
    }

    @Bean
    LinkLifecycleConsumer linkLifecycleConsumer(DashboardLinkRepository dashboardLinkRepository) {
        return new LinkLifecycleConsumer(dashboardLinkRepository);
    }

    @Bean
    DashboardFacade dashboardFacade(DashboardLinkRepository dashboardLinkRepository,
                                    DashboardLinkDetailsDtoMapper dashboardLinkDetailsDtoMapper) {
        return new DashboardFacade(dashboardLinkRepository, dashboardLinkDetailsDtoMapper);
    }

    @Bean
    DashboardLinkDetailsDtoMapper dashboardLinkDetailsDtoMapper(ObjectMapper objectMapper) {
        return new DashboardLinkDetailsDtoMapper(objectMapper);
    }
}
