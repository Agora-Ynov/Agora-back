package com.agora.controller.group;

import com.agora.dto.response.auth.UserGroupSummaryDto;
import com.agora.service.group.UserGroupsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Groupes visibles par l'utilisateur connecté")
public class GroupsController {

    private final UserGroupsService userGroupsService;

    @GetMapping
    @Operation(
            summary = "Lister mes groupes",
            description = "Groupes dont l'utilisateur est membre, avec effectif et règles de réservation / tarifs.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public List<UserGroupSummaryDto> listMyGroups(Authentication authentication) {
        return userGroupsService.listForCurrentUser(authentication);
    }

    @GetMapping("/{groupId}")
    @Operation(
            summary = "Détail d'un groupe (membre)",
            description = "Uniquement si l'utilisateur connecté appartient au groupe.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public UserGroupSummaryDto getMyGroup(
            @PathVariable UUID groupId,
            Authentication authentication
    ) {
        return userGroupsService.getForCurrentUser(groupId, authentication);
    }
}
