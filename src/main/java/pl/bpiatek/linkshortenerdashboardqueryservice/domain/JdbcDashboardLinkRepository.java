package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class JdbcDashboardLinkRepository implements DashboardLinkRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final DashboardLinkRowMapper rowMapper = new DashboardLinkRowMapper();
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("created_at", "total_clicks", "title", "short_url");

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
        namedJdbcTemplate.update(sql, toSqlInsertParams(link));
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

        var params = new MapSqlParameterSource()
                .addValue("linkId", link.linkId())
                .addValue("longUrl", link.longUrl())
                .addValue("title", link.title())
                .addValue("isActive", link.isActive())
                .addValue("updatedAt", Timestamp.from(link.updatedAt()));

        namedJdbcTemplate.update(sql, params);
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

    @Override
    public Page<DashboardLink> findByUserId(String userId, Pageable pageable) {
        long total = countByUserId(userId);
        if (total == 0) {
            return Page.empty(pageable);
        }

        var selectSql = new StringBuilder("""
        SELECT
        dl.id, dl.link_id, dl.user_id, dl.short_url, dl.long_url,
        dl.title, dl.is_active, dl.created_at, dl.updated_at, dl.total_clicks
        FROM dashboard_links dl WHERE user_id = :userId""")
        .append(createOrderByClause(pageable.getSort()))
        .append(" LIMIT :limit OFFSET :offset");

        var selectParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        var content = namedJdbcTemplate.query(selectSql.toString(), selectParams, rowMapper);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<DashboardLinkDetails> getByLinkIdAndUser(String userId, String linkId) {

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
        FROM dashboard_links dl
        WHERE dl.user_id = :userId
          AND dl.link_id = :linkId
        """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("linkId", linkId);

        var result = namedJdbcTemplate.query(sql, params, (rs, rowNum) ->
                new DashboardLinkDetails(
                        rs.getLong("id"),
                        rs.getString("link_id"),
                        rs.getString("user_id"),
                        rs.getString("short_url"),
                        rs.getString("long_url"),
                        rs.getString("title"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant(),
                        rs.getLong("total_clicks"),
                        rs.getString("clicks_by_country"),
                        rs.getString("clicks_by_device"),
                        rs.getString("clicks_by_os")
                )
        );

        return result.stream().findFirst();
    }

    @Override
    public void incrementCityClicks(String linkId, String countryCode, String cityName, String latitude, String longitude) {
        var sql = """
        INSERT INTO dashboard_link_city_stats (
            link_id, country_code, city_name, latitude, longitude, clicks
        )
        VALUES (
            :linkId, :countryCode, :cityName, :latitude, :longitude, 1
        )
        ON CONFLICT (link_id, country_code, city_name)
        DO UPDATE SET clicks = dashboard_link_city_stats.clicks + 1
        """;

        var params = new MapSqlParameterSource()
                .addValue("linkId", linkId)
                .addValue("countryCode", countryCode)
                .addValue("cityName", cityName)
                .addValue("latitude", latitude)
                .addValue("longitude", longitude);

        namedJdbcTemplate.update(sql, params);
    }


    private String createOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY created_at DESC";
        }

        var orderBy = sort.stream()
                .filter(order -> ALLOWED_SORT_COLUMNS.contains(order.getProperty()))
                .map(order -> order.getProperty() + " " + order.getDirection())
                .collect(Collectors.joining(", "));

        if (orderBy.isBlank()) {
            return " ORDER BY created_at DESC";
        }

        return " ORDER BY " + orderBy;
    }


    private long countByUserId(String userId) {
        var sql = "SELECT COUNT(*) FROM dashboard_links WHERE user_id = :userId";
        var total = namedJdbcTemplate.queryForObject(sql, Map.of("userId", userId), Long.class);

        return total == null ? 0 : total;
    }

    private MapSqlParameterSource toSqlInsertParams(DashboardLink link) {
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
