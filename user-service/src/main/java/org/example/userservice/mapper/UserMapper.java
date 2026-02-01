package org.example.userservice.mapper;

import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.example.userservice.dto.response.UserResponse;
import org.example.userservice.model.User;
import org.mapstruct.*;

import java.time.Instant;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {Instant.class, Collectors.class})
public interface UserMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);

    UserResponse toUserResponse(User user);
    ProfileResponse toProfileResponse(User user);
}
