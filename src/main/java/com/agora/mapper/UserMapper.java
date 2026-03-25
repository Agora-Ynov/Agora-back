package com.agora.mapper;

import com.agora.dto.response.RegisterResponseDto;
import com.agora.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public RegisterResponseDto toRegisterResponse(User user) {
        return new RegisterResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAccountType(),
                user.getAccountStatus()
        );
    }
}
