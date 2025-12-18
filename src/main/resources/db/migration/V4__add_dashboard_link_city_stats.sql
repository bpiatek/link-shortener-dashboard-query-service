CREATE TABLE dashboard_link_city_stats (
   id           BIGSERIAL PRIMARY KEY,
   link_id      VARCHAR(255) NOT NULL,
   country_code VARCHAR(2)   NOT NULL,
   city_name    VARCHAR(255) NOT NULL,
   latitude     VARCHAR(255) NOT NULL,
   longitude    VARCHAR(255) NOT NULL,
   clicks       BIGINT NOT NULL DEFAULT 0,

   CONSTRAINT uk_link_city UNIQUE (link_id, country_code, city_name)
);

CREATE INDEX idx_link_city_stats_link_id
    ON dashboard_link_city_stats (link_id);