package com.example.planAndRemind.mapper;

import com.example.planAndRemind.dto.UserDTO;
import com.example.planAndRemind.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO userToDto(UserEntity userEntity);

    UserEntity DtoToUser(UserDTO dto);

    List<UserDTO> usersListTODtos(List<UserEntity> userEntities);
    List<UserEntity>  dtosToUsers(List<UserDTO> dtos);

}
