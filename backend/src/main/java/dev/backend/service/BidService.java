package dev.backend.service;

import dev.backend.dto.BidResponse;
import dev.backend.entity.Bid;
import dev.backend.entity.Task;
import dev.backend.entity.User;
import dev.backend.enums.Role;
import dev.backend.enums.TaskStatus;
import dev.backend.repository.BidRepository;
import dev.backend.repository.TaskRepository;
import dev.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<BidResponse> getBidsByTaskId(Long taskId, String taskOwnerUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if(!task.getCustomer().getUsername().equals(taskOwnerUsername)) {
            throw new RuntimeException("Only task owner can get bids");
        }

        return bidRepository.findByTask(task).stream()
                .map(this::convertBidToDTO)
                .toList();
    }

    public BidResponse createBid(Long taskId, String executorUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User executor = userRepository.findByUsername(executorUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(executor.getRole() != Role.EXECUTOR) {
            throw new RuntimeException("Only executors can create bids");
        }
        if(task.getStatus() != TaskStatus.CREATED) {
            throw new RuntimeException("Task is already running or completed");
        }
        if(bidRepository.existsByTaskAndExecutor(task, executor)) {
            throw new RuntimeException("Task already exists");
        }
        if (task.getCustomer().getId().equals(executor.getId())) {
            throw new IllegalArgumentException("You cannot bid on your own task");
        }

        Bid bid = new Bid();
        bid.setExecutor(executor);
        bid.setTask(task);
        bidRepository.save(bid);

        return convertBidToDTO(bid);
    }

    private BidResponse convertBidToDTO(Bid bid) {
        BidResponse response = new BidResponse();
        response.setBidId(bid.getId());
        response.setTaskId(bid.getTask().getId());
        response.setExecutorUsername(bid.getExecutor().getUsername());
        return response;
    }
}
