package dev.backend.service;

import dev.backend.dto.LoginRequest;
import dev.backend.dto.RegisterRequest;
import dev.backend.dto.UserResponse;
import dev.backend.entity.User;
import dev.backend.enums.Role;
import dev.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Wrong password");

        return jwtService.generateToken(user);
    }

    public void register(@Valid RegisterRequest request) {
        if(request.getRole().equals(Role.ADMIN))
            throw new IllegalArgumentException("Registration as ADMIN is not allowed");
        if(userRepository.findByUsername(request.getUsername()).isPresent())
            throw new IllegalArgumentException("Username already exists");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPoints(100L);
        user.setBlocked(false);
        userRepository.save(user);
    }

    public UserResponse getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setPoints(user.getPoints());
        response.setRole(user.getRole());
        return response;
    }
}
