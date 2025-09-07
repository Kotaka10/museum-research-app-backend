package com.example.museumsearch.mapper;

import org.mapstruct.Mapper;

import com.example.museumsearch.dto.UserDTO;
import com.example.museumsearch.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEnitity(UserDTO userDTO);
}
