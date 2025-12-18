package pl.bpiatek.linkshortenerdashboardqueryservice.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.DashboardLinksResponse;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.PagedResponse;
import pl.bpiatek.linkshortenerdashboardqueryservice.domain.DashboardFacade;

@RestController
@RequestMapping("/dashboard")
class DashboardController {

    private final DashboardFacade facade;

    DashboardController(DashboardFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/links")
    ResponseEntity<PagedResponse<DashboardLinksResponse>> getUserLinks(
            @RequestHeader("X-User-Id") String userId,
            Pageable pageable) {

        var links = facade.getUserLinks(userId, pageable);
        var response = PagedResponse.from(links);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/links/{linkId}")
    ResponseEntity<Object> getLink(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String linkId) {
        var optionalLink = facade.getLink(userId, linkId);
        return optionalLink.<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
