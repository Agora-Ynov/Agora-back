package com.agora.service.superadmin;

import com.agora.dto.response.admin.AdminSupportUserDto;

import java.util.List;
import java.util.UUID;

public interface SuperadminAdminSupportService {

    List<AdminSupportUserDto> listActiveAdminSupport();

    AdminSupportUserDto promote(UUID userId);

    void revoke(UUID userId);
}
