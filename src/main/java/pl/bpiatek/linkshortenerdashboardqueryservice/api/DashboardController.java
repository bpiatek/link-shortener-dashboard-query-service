package pl.bpiatek.linkshortenerdashboardqueryservice.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.DashboardLinkDto;
import pl.bpiatek.linkshortenerdashboardqueryservice.domain.DashboardFacade;

@RestController
class DashboardController {

    private final DashboardFacade facade;

    DashboardController(DashboardFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/links")
    public ResponseEntity<Page<DashboardLinkDto>> getUserLinks(
            @RequestHeader("X-User-Id") String userId,
            Pageable pageable) {

        var links = facade.getUserLinks(userId, pageable);
        return ResponseEntity.ok(links);
    }

}
