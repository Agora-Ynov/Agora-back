package com.agora.dto.response;

import com.agora.enums.AccountStatus;
import com.agora.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegisterResponseDto {

    private final UUID id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final AccountType accountType;
    private final AccountStatus accountStatus;
}
