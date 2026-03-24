package dev.backend.service;

import dev.backend.dto.CreateTaskRequest;
import dev.backend.dto.TaskResponse;
import dev.backend.entity.Bid;
import dev.backend.entity.Payment;
import dev.backend.entity.Task;
import dev.backend.entity.User;
import dev.backend.enums.Role;
import dev.backend.enums.TaskStatus;
import dev.backend.repository.BidRepository;
import dev.backend.repository.PaymentRepository;
import dev.backend.repository.TaskRepository;
import dev.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertTaskToDTO(task);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest createTaskRequest, String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Only customers can create tasks");
        }
        if(customer.getPoints() < createTaskRequest.getCost()) {
            throw new IllegalArgumentException("Cost less than customer points");
        }

        Task task = new Task();
        task.setTitle(createTaskRequest.getTitle());
        task.setDescription(createTaskRequest.getDescription());
        task.setCustomer(customer);
        task.setCost(createTaskRequest.getCost());
        task.setStatus(TaskStatus.CREATED);

        taskRepository.save(task);

        return convertTaskToDTO(task);
    }

    @Transactional
    public TaskResponse selectExecutor(Long taskId, Long bidId, String customerUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        if(!task.getCustomer().getUsername().equals(customerUsername)) {
            throw new IllegalArgumentException("Only task owner can choose the executor");
        }
        if(task.getStatus() != TaskStatus.CREATED) {
            throw new RuntimeException("Task is already running or completed");
        }
        if(!Objects.equals(bid.getTask().getId(), taskId)) {
            throw new RuntimeException("This bid is for a different task");
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setExecutor(bid.getExecutor());
        taskRepository.save(task);

        return convertTaskToDTO(task);
    }

    @Transactional
    public TaskResponse completeTask(Long taskId, String executorUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if(task.getExecutor() == null) {
            throw new IllegalArgumentException("Task executor not found");
        }
        if(!task.getExecutor().getUsername().equals(executorUsername)) {
            throw new IllegalArgumentException("Only the assigned executor can mark this task as completed");
        }
        if(task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new RuntimeException("Task is already completed");
        }
        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        return convertTaskToDTO(task);
    }

    @Transactional
    public TaskResponse confirmTask(Long taskId, String customerUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if(!task.getCustomer().getUsername().equals(customerUsername)) {
            throw new IllegalArgumentException("Only task owner can confirm task");
        }
        if(task.getStatus() != TaskStatus.COMPLETED) {
            throw new RuntimeException("Task must be completed by executor first");
        }
        User customer = task.getCustomer();
        User executor = task.getExecutor();

        if(customer.getPoints() < task.getCost()) {
            throw new IllegalStateException("Insufficient points. You need " + task.getCost() + " points.");
        }

        customer.setPoints(customer.getPoints() - task.getCost());
        executor.setPoints(executor.getPoints() + task.getCost());

        Payment payment = new Payment();
        payment.setTask(task);
        payment.setPoints(task.getCost());
        payment.setPayer(customer);
        payment.setReceiver(executor);
        paymentRepository.save(payment);

        task.setStatus(TaskStatus.CONFIRMED);
        taskRepository.save(task);

        return convertTaskToDTO(task);
    }

    @Transactional
    public TaskResponse cancelTask(Long taskId, String customerUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getCustomer().getUsername().equals(customerUsername)) {
            throw new IllegalArgumentException("Only task owner can cancel it");
        }

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel a completed or confirmed task");
        }

        task.setStatus(TaskStatus.CANCELLED);
        return convertTaskToDTO(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse rejectTask(Long taskId, String customerUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getCustomer().getUsername().equals(customerUsername)) {
            throw new IllegalArgumentException("Only task owner can reject the result");
        }

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("You can only reject tasks that are in COMPLETED status");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        return convertTaskToDTO(taskRepository.save(task));
    }

    public Page<TaskResponse> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Task> taskPage = taskRepository.findAll(pageable);
        return taskPage.map(this::convertTaskToDTO);
    }

    public Page<TaskResponse> getMyAssignedTasks(String username, int page, int size) {
        User executor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return taskRepository.findByExecutor(executor, pageable)
                .map(this::convertTaskToDTO);
    }

    private TaskResponse convertTaskToDTO(Task task) {
        TaskResponse taskResponseDTO = new TaskResponse();
        taskResponseDTO.setId(task.getId());
        taskResponseDTO.setStatus(task.getStatus());
        taskResponseDTO.setCost(task.getCost());
        taskResponseDTO.setTitle(task.getTitle());
        taskResponseDTO.setDescription(task.getDescription());
        taskResponseDTO.setCustomerUsername(task.getCustomer().getUsername());
        if (task.getExecutor() != null) {
            taskResponseDTO.setExecutorUsername(task.getExecutor().getUsername());
        }
        return taskResponseDTO;
    }
}
