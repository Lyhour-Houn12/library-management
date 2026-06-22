package com.library.application.mapper;

import com.library.application.entity.User;
import com.library.application.payload.dto.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserDTO userDTO);

    UserDTO toDTO(User user);

    List<UserDTO> toDTOList(List<User> users);

    Set<UserDTO> toDTOSet(Set<User> users);

}
