package com.epam.gymcrm.infrastructure.security;

import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.db.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_returnsUserDetails_whenUserExistsAndActive() {
        // arrange
        UserEntity ue = new UserEntity();
        ue.setUsername("ali.veli");
        ue.setPassword("{bcrypt}hash");
        ue.setActive(true);
        when(userRepository.findByUsername("ali.veli")).thenReturn(Optional.of(ue));

        // act
        UserDetails details = service.loadUserByUsername("ali.veli");

        // assert
        assertEquals("ali.veli", details.getUsername());
        assertEquals("{bcrypt}hash", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());

        // authorities -> ROLE_USER var mı?
        Collection<? extends GrantedAuthority> auths = details.getAuthorities();
        assertNotNull(auths);
        assertTrue(auths.stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));

        verify(userRepository).findByUsername("ali.veli");
    }

    @Test
    void loadUserByUsername_setsEnabledFalse_whenUserInactive() {
        // arrange
        UserEntity ue = new UserEntity();
        ue.setUsername("in.active");
        ue.setPassword("hash");
        ue.setActive(false);
        when(userRepository.findByUsername("in.active")).thenReturn(Optional.of(ue));

        // act
        UserDetails details = service.loadUserByUsername("in.active");

        // assert
        assertEquals("in.active", details.getUsername());
        assertFalse(details.isEnabled()); // inactive -> disabled
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_throws_whenUserMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing")
        );
        assertTrue(ex.getMessage().contains("missing"));
        verify(userRepository).findByUsername("missing");
    }
}