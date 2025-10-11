package pl.bpiatek.linkshortenerdashboardqueryservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.bpiatek.linkshortenerdashboardqueryservice.api.dto.DashboardLinkDto;
import pl.bpiatek.linkshortenerdashboardqueryservice.config.TestClockConfiguration;
import pl.bpiatek.linkshortenerdashboardqueryservice.config.TestSecurityConfiguration;
import pl.bpiatek.linkshortenerdashboardqueryservice.domain.DashboardFacade;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.data.domain.Sort.Direction.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfiguration.class, TestClockConfiguration.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardFacade facade;

    @Test
    void shouldGetLinksForUserAndReturnPagedResponse() throws Exception {
        // given
        var userId = "user-1";
        var linkDto = aDashboardLinkDto();
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(linkDto), pageable, 1);

        given(facade.getUserLinks(eq(userId), any(Pageable.class))).willReturn(page);

        // then
        mockMvc.perform(get("/dashboard/links")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].linkId").value(linkDto.linkId()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    private DashboardLinkDto aDashboardLinkDto() {
        return new DashboardLinkDto(
                1L,
                "link-id",
                "user-id",
                "short-url",
                "long-url",
                "title",
                true,
                Instant.parse("2025-08-22T10:00:00Z"),
                Instant.parse("2025-08-22T10:00:00Z"),
                1);
    }

    @Test
    void shouldPassCorrectPageableParametersToFacade() throws Exception {
        // given
        var userId = "user-1";
        var page = 2;
        var size = 5;
        var sort = "total_clicks,desc";

        given(facade.getUserLinks(eq(userId), any(Pageable.class)))
                .willReturn(Page.empty());

        // when
        mockMvc.perform(get("/dashboard/links?page={page}&size={size}&sort={sort}", page, size, sort)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then
        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(facade).getUserLinks(eq(userId), pageableCaptor.capture());

        var capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
        assertThat(capturedPageable.getPageSize()).isEqualTo(size);
        assertThat(capturedPageable.getSort()).isEqualTo(Sort.by(DESC, "total_clicks"));
    }

    @Test
    void shouldReturnEmptyPageWhenFacadeReturnsNoLinks() throws Exception {
        // given
        var userId = "user-with-no-links";
        var pageable = PageRequest.of(0, 10);

        // The facade returns an empty Page for this user
        given(facade.getUserLinks(userId, pageable)).willReturn(Page.empty(pageable));

        // then
        mockMvc.perform(get("/dashboard/links?page=0&size=10")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void shouldReturn403BadRequestWhenUserIdHeaderIsMissing() throws Exception {
        // given: No X-User-Id header is provided

        // then
        mockMvc.perform(get("/dashboard/links")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}