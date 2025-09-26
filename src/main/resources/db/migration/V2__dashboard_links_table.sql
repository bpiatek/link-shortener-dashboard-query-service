-- This table acts as a materialized view. It is built by consuming from both the
-- 'link-lifecycle-events' and 'analytics-enriched-clicks' Kafka topics.
-- Its structure is optimized for fast reads to power the user dashboard UI.
CREATE TABLE dashboard_links (
    -- Internal primary key for the database.
     id                  BIGSERIAL PRIMARY KEY,

    -- The public, unique identifier for the link entity.
     link_id             VARCHAR(255) NOT NULL UNIQUE,

    -- The ID of the user who owns the link. This is the primary query column.
     user_id             VARCHAR(255) NOT NULL,

    -- Core link information, copied from the LinkCreated event.
     short_url           VARCHAR(255) NOT NULL,
     long_url            TEXT NOT NULL,
     title               VARCHAR(255),
     is_active           BOOLEAN NOT NULL,
     created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
     updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,

    -- =============================================================
    -- Pre-aggregated Analytics Data
    -- These columns are updated by consuming from the 'analytics-enriched-clicks' topic.
    -- =============================================================

    -- A simple, fast counter for the total number of clicks.
     total_clicks        BIGINT NOT NULL DEFAULT 0,

    -- We use JSONB to store aggregated counts for different dimensions.
    -- This is extremely efficient to query and flexible to extend.
    -- Example: {"US": 150, "DE": 75, "PL": 50}
     clicks_by_country   JSONB NOT NULL DEFAULT '{}'::jsonb,

    -- Example: {"Desktop": 200, "Phone": 75}
     clicks_by_device    JSONB NOT NULL DEFAULT '{}'::jsonb,

    -- Example: {"Windows": 150, "iOS": 50, "Android": 25}
     clicks_by_os        JSONB NOT NULL DEFAULT '{}'::jsonb
);

-- The most important index: allows for fast lookups of all links for a given user.
CREATE INDEX idx_dashboard_links_on_user_id ON dashboard_links (user_id);