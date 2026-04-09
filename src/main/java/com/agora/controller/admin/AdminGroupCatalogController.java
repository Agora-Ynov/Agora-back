package com.agora.controller.admin;

import com.agora.dto.request.admin.AddGroupMemberRequestDto;
import com.agora.dto.request.admin.CreateAdminGroupRequestDto;
import com.agora.dto.request.admin.UpdateAdminGroupRequestDto;
import com.agora.dto.response.admin.AdminGroupMemberResponseDto;
import com.agora.dto.response.admin.AdminGroupResponseDto;
import com.agora.service.admin.AdminGroupCatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
@Tag(name = "Admin Groups", description = "Catalogue et membres des groupes")
public class AdminGroupCatalogController {

    private final AdminGroupCatalogService adminGroupCatalogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public List<AdminGroupResponseDto> list() {
        return adminGroupCatalogService.listGroups();
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminGroupResponseDto getOne(@PathVariable UUID groupId) {
        return adminGroupCatalogService.getGroup(groupId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminGroupResponseDto create(@Valid @RequestBody CreateAdminGroupRequestDto request) {
        return adminGroupCatalogService.createGroup(request);
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public AdminGroupResponseDto update(
            @PathVariable UUID groupId,
            @Valid @RequestBody UpdateAdminGroupRequestDto request
    ) {
        return adminGroupCatalogService.updateGroup(groupId, request);
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable UUID groupId) {
        adminGroupCatalogService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public List<AdminGroupMemberResponseDto> listMembers(@PathVariable UUID groupId) {
        return adminGroupCatalogService.listMembers(groupId);
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public void addMember(@PathVariable UUID groupId, @Valid @RequestBody AddGroupMemberRequestDto body) {
        adminGroupCatalogService.addMember(groupId, body);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SECRETARY_ADMIN', 'ADMIN_SUPPORT')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> removeMember(@PathVariable UUID groupId, @PathVariable UUID userId) {
        adminGroupCatalogService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }
}
