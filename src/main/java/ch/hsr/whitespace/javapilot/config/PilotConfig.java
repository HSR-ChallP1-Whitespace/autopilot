package ch.hsr.whitespace.javapilot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "ch.hsr.whitespace", "com.zuehlke.carrera.javapilot.services" })
@EnableConfigurationProperties({ PilotProperties.class }) // loaded from
															// /resources/application.yml
public class PilotConfig {

}
