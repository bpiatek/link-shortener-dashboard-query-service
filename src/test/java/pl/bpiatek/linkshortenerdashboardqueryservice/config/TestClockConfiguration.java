package pl.bpiatek.linkshortenerdashboardqueryservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ClockConfig.class)
public class TestClockConfiguration {
}
