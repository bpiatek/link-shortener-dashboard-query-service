package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

interface DashboardLinkRepository {
    void create(DashboardLink link);
    void update(DashboardLink link);
    void delete(String linkId);
    void incrementClickCounters(String linkId, String countryCode, String deviceType, String osName);
    Page<DashboardLink> findByUserId(String userId, Pageable pageable);
    Optional<DashboardLinkDetails> getByLinkIdAndUser(String userId, String linkId);
    void incrementCityClicks(String linkId, String countryCode, String cityName, String latitude, String longitude);
}
