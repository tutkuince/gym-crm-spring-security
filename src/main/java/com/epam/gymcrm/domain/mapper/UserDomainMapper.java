package com.epam.gymcrm.domain.mapper;

import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.domain.model.User;

import java.util.Objects;

public class UserDomainMapper {

    public static UserEntity toUserEntity(User user) {
        if (Objects.isNull(user)) {
            throw new IllegalStateException("UserDomainMapper: User model is null. Data integrity violation!");
        }
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setActive(user.getActive());
        return entity;
    }

    public static User toUser(UserEntity userEntity) {
        if (Objects.isNull(userEntity)) {
            throw new IllegalStateException("UserDomainMapper: UserEntity is null. Data integrity violation!");
        }
        User user = new User();
        user.setId(userEntity.getId());
        user.setFirstName(userEntity.getFirstName());
        user.setLastName(userEntity.getLastName());
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());
        user.setActive(userEntity.getActive());
        return user;
    }
}
