package com.ztp.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import java.util.List;

@Configuration
@EnableJdbcHttpSession
public class SessionConfig {

    @Bean
    public DataSourceScriptDatabaseInitializer sessionDataSourceScriptDatabaseInitializer(DataSource dataSource) {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:org/springframework/session/jdbc/schema-mysql.sql"));
        settings.setMode(DatabaseInitializationMode.ALWAYS); // <-- the missing piece
        settings.setContinueOnError(true);
        return new DataSourceScriptDatabaseInitializer(dataSource, settings);
    }
}