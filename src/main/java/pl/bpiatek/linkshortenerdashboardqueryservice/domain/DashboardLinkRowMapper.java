package pl.bpiatek.linkshortenerdashboardqueryservice.domain;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class DashboardLinkRowMapper implements RowMapper<DashboardLink> {

    @Override
    public DashboardLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new DashboardLink(
                rs.getLong("id"),
                rs.getString("link_id"),
                rs.getString("user_id"),
                rs.getString("short_url"),
                rs.getString("long_url"),
                rs.getString("title"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getLong("total_clicks")
        );
    }
}
