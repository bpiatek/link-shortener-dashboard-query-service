package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;


@Component
@ActiveProfiles("test")
class DashboardLinkFixtures {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert linkInsert;
    private final ObjectMapper objectMapper; // For parsing JSONB

    DashboardLinkFixtures(NamedParameterJdbcTemplate namedParameterJdbcTemplate, ObjectMapper objectMapper) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.linkInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName("dashboard_links")
                .usingGeneratedKeyColumns("id");
        this.objectMapper = objectMapper;
    }

    private final RowMapper<TestDashboardLink> DASHBOARD_LINK_ROW_MAPPER = (rs, rowNum) -> TestDashboardLink.builder()
            .id(rs.getLong("id"))
            .linkId(rs.getString("link_id"))
            .userId(rs.getString("user_id"))
            .shortUrl(rs.getString("short_url"))
            .longUrl(rs.getString("long_url"))
            .title(rs.getString("title"))
            .isActive(rs.getBoolean("is_active"))
            .createdAt(rs.getTimestamp("created_at").toInstant())
            .updatedAt(rs.getTimestamp("updated_at").toInstant())
            .totalClicks(rs.getLong("total_clicks"))
            .clicksByCountry(parseJsonbToMap(rs.getString("clicks_by_country")))
            .clicksByDevice(parseJsonbToMap(rs.getString("clicks_by_device")))
            .clicksByOs(parseJsonbToMap(rs.getString("clicks_by_os")))
            .build();

    TestDashboardLink aDashboardLink(TestDashboardLink link) {

        var params = new MapSqlParameterSource()
                .addValue("linkId", link.getLinkId())
                .addValue("userId", link.getUserId())
                .addValue("shortUrl", link.getShortUrl())
                .addValue("longUrl", link.getLongUrl())
                .addValue("title", link.getTitle())
                .addValue("isActive", link.isActive())
                .addValue("createdAt", Timestamp.from(link.getCreatedAt()))
                .addValue("updatedAt", Timestamp.from(link.getUpdatedAt()))
                .addValue("total_clicks", link.getTotalClicks())
                .addValue("clicks_by_country", toJson(link.getClicksByCountry()))
                .addValue("clicks_by_device", toJson(link.getClicksByDevice()))
                .addValue("clicks_by_os", toJson(link.getClicksByOs()));


        linkInsert.execute(params);

        return getByShortUrl(link.getShortUrl());
    }

    TestDashboardLink aDashboardLink() {
        return aDashboardLink(TestDashboardLink.builder().build());
    }


    TestDashboardLink getByShortUrl(String shortUrl) {
        var sql = """
            SELECT
            dl.id,
            dl.link_id,
            dl.user_id,
            dl.short_url,
            dl.long_url,
            dl.title,
            dl.is_active,
            dl.created_at,
            dl.updated_at,
            dl.total_clicks,
            dl.clicks_by_country,
            dl.clicks_by_device,
            dl.clicks_by_os
            FROM dashboard_links dl WHERE dl.short_url = :shortUrl""";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    sql,
                    Map.of("shortUrl", shortUrl),
                    DASHBOARD_LINK_ROW_MAPPER
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Map<String, Long> parseJsonbToMap(String json) {
        if (json == null || json.equals("{}")) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private String toJson(Map<String, Long> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
