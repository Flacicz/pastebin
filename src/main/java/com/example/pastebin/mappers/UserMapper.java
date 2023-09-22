package com.example.pastebin.mappers;

import com.example.pastebin.dto.UserDTO;
import com.example.pastebin.entity.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", config = IgnoreUnmappedMapperConfig.class)
public interface UserMapper {
    User fromDTOToUser(UserDTO userDTO);

    @InheritInverseConfiguration
    UserDTO fromUserToDTO(User user);

    List<User> fromUserDTOToUserList(List<UserDTO> userDTOs);

    List<UserDTO> fromUserToUserDTOList(List<User> users);
}