package com.portal.searchservice.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
class ApplicationConfiguration(
    private val env: Environment
) {

    @Bean
    fun dataSource() = DriverManagerDataSource().apply {
        env.getProperty("spring.datasource.driver-class-name")?.let { setDriverClassName(it) }
        url = env.getProperty("spring.datasource.url")
        username = env.getProperty("spring.datasource.username")
        password = env.getProperty("spring.datasource.password")
    }
}