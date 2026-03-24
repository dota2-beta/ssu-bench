package dev.backend.controller;

import dev.backend.dto.BidResponse;
import dev.backend.dto.CreateTaskRequest;
import dev.backend.dto.TaskResponse;
import dev.backend.entity.Task;
import dev.backend.service.BidService;
import dev.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final BidService bidService;

    @Operation(summary = "Получить все задачи")
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getAllTasks(page, size));
    }

    @Operation(summary = "Получить задачу по id")
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @Operation(summary = "Получить все отклики на задачу")
    @GetMapping("/{taskId}/bids")
    public ResponseEntity<List<BidResponse>> getTaskBids(@PathVariable Long taskId, Principal principal) {
        return ResponseEntity.ok(bidService.getBidsByTaskId(taskId, principal.getName()));
    }

    @Operation(summary = "Получить мои задачи (для исполнителя)")
    @GetMapping("/my-assignments")
    public ResponseEntity<Page<TaskResponse>> getMyAssignedTasks(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(taskService.getMyAssignedTasks(principal.getName(), page, size));
    }

    @Operation(summary = "Создать задачу")
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request, Principal principal) {
        TaskResponse response = taskService.createTask(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Откликнуться на задачу")
    @PostMapping("/{taskId}/bids")
    public ResponseEntity<BidResponse> createBid(@PathVariable Long taskId, Principal principal) {
        BidResponse response = bidService.createBid(taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Выбрать исполнителя")
    @PostMapping("/{taskId}/select-executor/{bidId}")
    public ResponseEntity<TaskResponse> selectExecutor(@PathVariable Long taskId, @PathVariable Long bidId, Principal principal) {
        TaskResponse response = taskService.selectExecutor(taskId, bidId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Выполнить задачу")
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long taskId, Principal principal) {
        TaskResponse response = taskService.completeTask(taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Подтвердить выполнение задачи")
    @PostMapping("/{taskId}/confirm")
    public ResponseEntity<TaskResponse> confirmTask(@PathVariable Long taskId, Principal principal) {
        TaskResponse response = taskService.confirmTask(taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Отменить задачу")
    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<TaskResponse> cancelTask(@PathVariable Long taskId, Principal principal) {
        TaskResponse response = taskService.cancelTask(taskId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Отправить задачу на доработку")
    @PostMapping("/{taskId}/reject")
    public ResponseEntity<TaskResponse> rejectTask(@PathVariable Long taskId, Principal principal) {
        TaskResponse response = taskService.rejectTask(taskId, principal.getName());
        return ResponseEntity.ok(response);
    }
}
