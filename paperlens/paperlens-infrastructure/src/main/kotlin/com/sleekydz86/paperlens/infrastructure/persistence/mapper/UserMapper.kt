package com.sleekydz86.paperlens.infrastructure.persistence.mapper

import com.sleekydz86.paperlens.domain.user.User
import com.sleekydz86.paperlens.infrastructure.persistence.entity.UserEntity

object UserMapper {

    fun toDomain(e: UserEntity): User = User(
        id = e.id,
        email = e.email,
        name = e.name,
        role = e.role,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
        deletedAt = e.deletedAt,
    )
}
