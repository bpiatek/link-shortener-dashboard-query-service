package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.DashboardLinkDetailsResponse;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.DashboardLinksResponse;

import java.util.Optional;

public class DashboardFacade {

    private final Logger log = LoggerFactory.getLogger(DashboardFacade.class);

    private final DashboardLinkRepository dashboardLinkRepository;
    private final DashboardLinkDetailsDtoMapper dashboardLinkDetailsDtoMapper;

    DashboardFacade(DashboardLinkRepository dashboardLinkRepository,
                    DashboardLinkDetailsDtoMapper dashboardLinkDetailsDtoMapper) {
        this.dashboardLinkRepository = dashboardLinkRepository;
        this.dashboardLinkDetailsDtoMapper = dashboardLinkDetailsDtoMapper;
    }

    public Page<DashboardLinksResponse> getUserLinks(String userId, Pageable pageable) {
        return dashboardLinkRepository.findByUserId(userId, pageable)
                .map(this::toDto);
    }

    private DashboardLinksResponse toDto(DashboardLink entity) {
        return new DashboardLinksResponse(
                entity.id(),
                entity.linkId(),
                entity.userId(),
                entity.shortUrl(),
                entity.longUrl(),
                entity.title(),
                entity.isActive(),
                entity.createdAt(),
                entity.updatedAt(),
                entity.totalClicks()
        );
    }

    public Optional<DashboardLinkDetailsResponse> getLink(String userId, String linkId) {
        return dashboardLinkRepository.getByLinkIdAndUser(userId, linkId)
                .map(dashboardLinkDetailsDtoMapper::toResponse);
    }
}
