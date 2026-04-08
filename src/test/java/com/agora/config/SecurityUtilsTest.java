package com.agora.config;

import com.agora.exception.auth.AuthRequiredException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthenticatedEmail_fromSecurityContext_returnsTrimmedEmail() {
        var auth = new UsernamePasswordAuthenticationToken(
                "  user@example.com  ",
                "n/a",
                AuthorityUtils.NO_AUTHORITIES
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.getAuthenticatedEmail()).isEqualTo("user@example.com");
    }

    @Test
    void getAuthenticatedEmail_whenAnonymous_throws() {
        var anonymous = new AnonymousAuthenticationToken(
                "key",
                "anon",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThatThrownBy(() -> securityUtils.getAuthenticatedEmail())
                .isInstanceOf(AuthRequiredException.class);
    }

    @Test
    void getAuthenticatedEmail_whenNullContext_throws() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> securityUtils.getAuthenticatedEmail())
                .isInstanceOf(AuthRequiredException.class);
    }

    @Test
    void getAuthenticatedEmail_withAuthentication_usesSameRules() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "x@y.fr",
                "pw",
                List.of()
        );

        assertThat(securityUtils.getAuthenticatedEmail(auth)).isEqualTo("x@y.fr");
    }

    @Test
    void getAuthenticatedEmail_blankName_throws() {
        var auth = new UsernamePasswordAuthenticationToken(
                "   ",
                "n/a",
                List.of()
        );

        assertThatThrownBy(() -> securityUtils.getAuthenticatedEmail(auth))
                .isInstanceOf(AuthRequiredException.class);
    }

    @Test
    void tryGetAuthenticatedEmail_whenAnonymous_returnsEmpty() {
        var anonymous = new AnonymousAuthenticationToken(
                "key",
                "anon",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        assertThat(securityUtils.tryGetAuthenticatedEmail(anonymous)).isEmpty();
    }

    @Test
    void hasAuthority_detectsRoleOnAuthentication() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_SECRETARY_ADMIN"))
        );

        assertThat(securityUtils.hasAuthority(auth, "ROLE_SECRETARY_ADMIN")).isTrue();
        assertThat(securityUtils.hasAuthority(auth, "ROLE_USER")).isFalse();
    }

    @Test
    void hasAuthority_nullAuthentication_isFalse() {
        assertThat(securityUtils.hasAuthority((Authentication) null, "ROLE_SECRETARY_ADMIN")).isFalse();
    }

    @Test
    void hasAuthority_blankAuthorityString_isFalse() {
        var auth = new UsernamePasswordAuthenticationToken(
                "u@example.com",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        assertThat(securityUtils.hasAuthority(auth, "")).isFalse();
        assertThat(securityUtils.hasAuthority(auth, "   ")).isFalse();
    }

    @Test
    void hasAuthority_authoritiesNull_doesNotThrow_returnsFalse() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getAuthorities()).thenReturn(null);

        assertThat(securityUtils.hasAuthority(auth, "ROLE_USER")).isFalse();
    }

    @Test
    void hasAuthority_nullGrantedAuthorityEntry_isIgnored() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(null);
        authorities.add(new SimpleGrantedAuthority("ROLE_SECRETARY_ADMIN"));
        doReturn(authorities).when(auth).getAuthorities();

        assertThat(securityUtils.hasAuthority(auth, "ROLE_SECRETARY_ADMIN")).isTrue();
    }

    @Test
    void getAuthenticatedEmail_whenIsAuthenticatedFalse_throws() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(auth.getName()).thenReturn("still@there.com");

        assertThatThrownBy(() -> securityUtils.getAuthenticatedEmail(auth))
                .isInstanceOf(AuthRequiredException.class);
    }

    @Test
    void tryGetAuthenticatedEmail_blankPrincipal_returnsEmpty() {
        var auth = new UsernamePasswordAuthenticationToken(
                "\t\n",
                "n/a",
                List.of()
        );
        assertThat(securityUtils.tryGetAuthenticatedEmail(auth)).isEmpty();
    }

    @Test
    void tryGetAuthenticatedEmail_whenContextUnset_returnsEmpty() {
        SecurityContextHolder.clearContext();
        assertThat(securityUtils.tryGetAuthenticatedEmail()).isEmpty();
    }

    @Test
    void hasAuthority_fromSecurityContext_matchesRole() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_SECRETARY_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.hasAuthority("ROLE_SECRETARY_ADMIN")).isTrue();
        assertThat(securityUtils.hasAuthority("ROLE_SUPERADMIN")).isFalse();
    }

    @Test
    void hasAuthority_roleMustMatchExactly_noPrefixTrick() {
        var auth = new UsernamePasswordAuthenticationToken(
                "u@example.com",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        assertThat(securityUtils.hasAuthority(auth, "USER")).isFalse();
        assertThat(securityUtils.hasAuthority(auth, "ROLE_USER")).isTrue();
    }

    @Test
    void hasAuthority_emptyAuthoritiesList_isFalse() {
        var auth = new UsernamePasswordAuthenticationToken(
                "u@example.com",
                "n/a",
                Collections.emptyList()
        );
        assertThat(securityUtils.hasAuthority(auth, "ROLE_SECRETARY_ADMIN")).isFalse();
    }
}
