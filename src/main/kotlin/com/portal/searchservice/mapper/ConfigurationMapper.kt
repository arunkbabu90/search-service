package com.portal.searchservice.mapper

import com.portal.searchservice.domain.Configuration
import com.portal.searchservice.dto.ConfigurationDto
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper(componentModel = "spring")
interface ConfigurationMapper {
    companion object {
        val INSTANCE: ConfigurationMapper = Mappers.getMapper(ConfigurationMapper::class.java)
    }

    fun toConfiguration(configurationDto: ConfigurationDto): Configuration
    fun toConfigurationDto(configuration: Configuration): ConfigurationDto
}
