package dev.backend;

import dev.backend.dto.LoginRequest;
import dev.backend.dto.RegisterRequest;
import dev.backend.entity.User;
import dev.backend.enums.Role;
import dev.backend.repository.BidRepository;
import dev.backend.repository.PaymentRepository;
import dev.backend.repository.TaskRepository;
import dev.backend.repository.UserRepository;
import dev.backend.service.JwtService;
import dev.backend.service.TaskService;
import dev.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_ShouldFail_WhenRoleIsAdmin() {
        //arrange
        RegisterRequest request = new RegisterRequest();
        request.setRole(Role.ADMIN);
        request.setUsername("tester");

        //act && assert
        assertThrows(IllegalArgumentException.class, () ->
                userService.register(request)
        );
    }

    @Test
    void login_ShouldFail_WhenUserIsBlocked() {
        //arrange
        User blockedUser = new User();
        blockedUser.setUsername("tester");
        blockedUser.setBlocked(true);
        LoginRequest req = new LoginRequest();
        req.setUsername("bad_guy");

        when(userRepository.findByUsername(blockedUser.getUsername())).thenReturn(Optional.of(blockedUser));

        //act && assert
        assertThrows(RuntimeException.class, () -> {
            userService.login(req);
        });
    }
}
