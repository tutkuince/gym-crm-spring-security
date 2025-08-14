package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.db.repository.UserRepository;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserAccountService service;

    @BeforeEach
    void setUp() {
        service = new UserAccountService(userRepository, passwordEncoder);
    }

    @Test
    void generateUniqueUsername_returnsBase_whenAvailable() {
        when(userRepository.existsByUsername("ali.veli")).thenReturn(false);

        String result = service.generateUniqueUsername("Ali", "Veli");

        assertEquals("ali.veli", result);
        verify(userRepository).existsByUsername("ali.veli");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void generateUniqueUsername_appendsCounter_untilAvailable() {
        when(userRepository.existsByUsername("ali.veli")).thenReturn(true);
        when(userRepository.existsByUsername("ali.veli1")).thenReturn(true);
        when(userRepository.existsByUsername("ali.veli2")).thenReturn(false);

        String result = service.generateUniqueUsername("Ali", "Veli");

        assertEquals("ali.veli2", result);
        verify(userRepository).existsByUsername("ali.veli");
        verify(userRepository).existsByUsername("ali.veli1");
        verify(userRepository).existsByUsername("ali.veli2");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void generateRandomPassword_hasLength10_andPrintableAscii() {
        String pwd = service.generateRandomPassword();

        assertNotNull(pwd);
        assertEquals(10, pwd.length(), "Password length must be 10");

        for (char c : pwd.toCharArray()) {
            assertTrue((int) c >= 33 && (int) c <= 126,
                    "All chars must be printable ASCII (33..126). Offender: " + (int) c);
        }
    }

    @Test
    void hash_delegatesToPasswordEncoder() {
        when(passwordEncoder.encode("raw")).thenReturn("ENC");

        String hashed = service.hash("raw");

        assertEquals("ENC", hashed);
        verify(passwordEncoder).encode("raw");
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void createUser_buildsUser_andEncodesPassword_andSetsActiveTrue() {
        UserAccountService spyService = Mockito.spy(service);

        doReturn("ali.veli").when(spyService).generateUniqueUsername("Ali", "Veli");
        doReturn("RAW_PASS").when(spyService).generateRandomPassword();
        when(passwordEncoder.encode("RAW_PASS")).thenReturn("ENC_PASS");

        User u = spyService.createUser("Ali", "Veli");

        assertNotNull(u);
        assertEquals("Ali", u.getFirstName());
        assertEquals("Veli", u.getLastName());
        assertEquals("ali.veli", u.getUsername());
        assertEquals("ENC_PASS", u.getPassword());
        assertEquals("RAW_PASS", u.getRawPassword());
        assertEquals(Boolean.TRUE, u.getActive());

        verify(spyService).generateUniqueUsername("Ali", "Veli");
        verify(spyService).generateRandomPassword();
        verify(passwordEncoder).encode("RAW_PASS");
        verifyNoMoreInteractions(passwordEncoder);
    }
}