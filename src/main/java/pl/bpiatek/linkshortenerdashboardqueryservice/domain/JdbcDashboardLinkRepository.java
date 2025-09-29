package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Map;

@Repository
class JdbcDashboardLinkRepository implements DashboardLinkRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    JdbcDashboardLinkRepository(JdbcTemplate jdbcTemplate) {
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }


    @Override
    public void create(DashboardLink link) {
        var sql = """
            INSERT INTO dashboard_links (link_id, user_id, short_url, long_url, title, is_active, created_at, updated_at)
            VALUES (:linkId, :userId, :shortUrl, :longUrl, :title, :isActive, :createdAt, :updatedAt)
            ON CONFLICT (link_id) DO NOTHING
            """;
        namedJdbcTemplate.update(sql, toSqlParams(link));
    }

    @Override
    public void update(DashboardLink link) {
        var sql = """
            UPDATE dashboard_links SET
                long_url = :longUrl,
                title = :title,
                is_active = :isActive,
                updated_at = :updatedAt
            WHERE link_id = :linkId
            """;
        namedJdbcTemplate.update(sql, toSqlParams(link));
    }

    @Override
    public void delete(String linkId) {
        final String sql = "DELETE FROM dashboard_links WHERE link_id = :linkId";
        namedJdbcTemplate.update(sql, Map.of("linkId", linkId));
    }

    @Override
    public void incrementClickCounters(String linkId, String countryCode, String deviceType, String osName) {
        var sql = """
            UPDATE dashboard_links
            SET
              total_clicks = total_clicks + 1,
              clicks_by_country = jsonb_set(
                clicks_by_country,
                ARRAY[:countryCode],
                (COALESCE(clicks_by_country ->> :countryCode, '0')::int + 1)::text::jsonb,
                true
              ),
              clicks_by_device = jsonb_set(
                clicks_by_device,
                ARRAY[:deviceType],
                (COALESCE(clicks_by_device ->> :deviceType, '0')::int + 1)::text::jsonb,
                true
              ),
              clicks_by_os = jsonb_set(
                clicks_by_os,
                ARRAY[:osName],
                (COALESCE(clicks_by_os ->> :osName, '0')::int + 1)::text::jsonb,
                true
              )
            WHERE link_id = :linkId
            """;

        var params = new MapSqlParameterSource()
                .addValue("linkId", linkId)
                .addValue("countryCode", countryCode)
                .addValue("deviceType", deviceType)
                .addValue("osName", osName);

        namedJdbcTemplate.update(sql, params);
    }

    private MapSqlParameterSource toSqlParams(DashboardLink link) {
        return new MapSqlParameterSource()
                .addValue("linkId", link.linkId())
                .addValue("userId", link.userId())
                .addValue("shortUrl", link.shortUrl())
                .addValue("longUrl", link.longUrl())
                .addValue("title", link.title())
                .addValue("isActive", link.isActive())
                .addValue("createdAt", Timestamp.from(link.createdAt()))
                .addValue("updatedAt", Timestamp.from(link.updatedAt()));
    }
}
