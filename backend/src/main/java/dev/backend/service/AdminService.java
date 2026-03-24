package dev.backend.service;

import dev.backend.entity.User;
import dev.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    @Transactional
    public void setUserBlockedStatus(String username, boolean blocked) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setBlocked(blocked);
        userRepository.save(user);
    }
}
