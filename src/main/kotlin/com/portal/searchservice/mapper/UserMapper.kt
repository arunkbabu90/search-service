package com.portal.searchservice.mapper

import com.portal.searchservice.domain.User
import com.portal.searchservice.dto.UserDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers

@Mapper(componentModel = "spring")
interface UserMapper {
    companion object {
        val INSTANCE: UserMapper = Mappers.getMapper(UserMapper::class.java)
    }

    fun toUserDto(user: User): UserDto

    @Mapping(target = "timesheet", ignore = true)
    fun toUser(userDto: UserDto): User
}