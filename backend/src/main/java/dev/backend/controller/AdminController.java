package dev.backend.controller;

import dev.backend.dto.BlockUserRequest;
import dev.backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Заблокировать/разблокировать пользователя")
    @PostMapping("/users/block")
    public ResponseEntity<?> blockUser(@RequestBody BlockUserRequest request) {
        adminService.setUserBlockedStatus(request.getUsername(), request.isBlocked());
        return ResponseEntity.ok(Map.of("message", "User status updated"));
    }
}
