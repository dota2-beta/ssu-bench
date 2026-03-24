package dev.backend;

import dev.backend.dto.BidResponse;
import dev.backend.dto.CreateTaskRequest;
import dev.backend.dto.TaskResponse;
import dev.backend.entity.Bid;
import dev.backend.entity.Task;
import dev.backend.entity.User;
import dev.backend.enums.Role;
import dev.backend.enums.TaskStatus;
import dev.backend.repository.BidRepository;
import dev.backend.repository.PaymentRepository;
import dev.backend.repository.TaskRepository;
import dev.backend.repository.UserRepository;
import dev.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private TaskService taskService;

    private User customer;
    private User executor;

    @BeforeEach
    public void setUp() {
        customer = new User();
        customer.setUsername("customer_user");
        customer.setRole(Role.CUSTOMER);
        customer.setPoints(100L);

        executor = new User();
        executor.setUsername("executor_user");
        executor.setRole(Role.EXECUTOR);
        executor.setPoints(0L);
    }

    @Test
    void createTask_ShouldFail_WhenUserIsExecutor() {
        //arrange
        CreateTaskRequest request = new CreateTaskRequest();
        request.setDescription("Task 1");
        request.setTitle("Task 1");
        request.setCost(10L);

        when(userRepository.findByUsername(executor.getUsername())).thenReturn(Optional.of(executor));
        //act && assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(request, executor.getUsername());
        }, "Should throw exception because EXECUTOR cannot create tasks");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void completeTask_ShouldFail_WhenNotAssignedExecutor() {
        //arrange
        Task task = new Task();
        task.setTitle("test");
        task.setDescription("test");
        task.setStatus(TaskStatus.CREATED);
        task.setCustomer(customer);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        //act & assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.completeTask(task.getId(), executor.getUsername());
        }, "Should throw exception because EXECUTOR is null");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void completeTask_ShouldFail_WhenWrongExecutorCallMethod() {
        //arrange
        User wrongExecutor = new User();
        wrongExecutor.setUsername("wrong_user");

        Task task = new Task();
        task.setExecutor(executor);
        task.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        //act assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.completeTask(task.getId(), "wrong_user");
        });
    }

    @Test
    void confirmTask_ShouldFail_WhenInsufficientPoints() {
        // arrange
        customer.setPoints(10L);
        Task task = new Task();
        task.setCustomer(customer);
        task.setCost(20L);
        task.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        //act && assert
        assertThrows(IllegalStateException.class, () -> {
            taskService.confirmTask(task.getId(), customer.getUsername());
        });
    }

    @Test
    void confirmTask_ShouldTransferPoints_WhenOk() {
        customer.setPoints(100L);
        executor.setPoints(0L);

        Task task = new Task();
        task.setExecutor(executor);
        task.setCustomer(customer);
        task.setCost(30L);
        task.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        //act
        taskService.confirmTask(task.getId(), customer.getUsername());
        //assert
        assertEquals(30L, executor.getPoints());
        assertEquals(70L, customer.getPoints());
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    void cancelTask_ShouldFail_WhenTaskConfirmed() {
        //arrange
        Task task = new Task();
        task.setCustomer(customer);
        task.setExecutor(executor);
        task.setStatus(TaskStatus.CONFIRMED);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        //act && assert
        assertThrows(IllegalStateException.class, () -> taskService.cancelTask(task.getId(), customer.getUsername()));
    }

    @Test
    void selectExecutor_ShouldFail_WhenNotOwner() {
        //arrange
        Task task = new Task();
        task.setCustomer(customer);

        Bid bid = new Bid();
        bid.setTask(task);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(bidRepository.findById(bid.getId())).thenReturn(Optional.of(bid));
        //act && assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.selectExecutor(task.getId(), bid.getId(), "other_customer");
        });
    }

    @Test
    void cancelTask_ShouldSuccess_WhenOk () {
        //arrange
        Task task = new Task();
        task.setCustomer(customer);
        task.setStatus(TaskStatus.CREATED);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        //act
        TaskResponse response = taskService.cancelTask(task.getId(), customer.getUsername());
        //assert
        assertEquals(TaskStatus.CANCELLED, response.getStatus());
        verify(taskRepository, times(1)).save(task);
    }
}

