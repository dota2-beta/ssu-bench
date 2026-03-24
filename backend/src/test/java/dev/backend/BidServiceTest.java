package dev.backend;

import dev.backend.entity.Task;
import dev.backend.entity.User;
import dev.backend.enums.Role;
import dev.backend.enums.TaskStatus;
import dev.backend.repository.BidRepository;
import dev.backend.repository.TaskRepository;
import dev.backend.repository.UserRepository;
import dev.backend.service.BidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {
    @Mock
    private BidRepository bidRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BidService bidService;

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
    void createBid_ShouldFail_WhenCustomerBiddingOnTask() {
        //arrange
        Task task = new Task();
        task.setCustomer(customer);
        task.setStatus(TaskStatus.CREATED);

        User customer2 = new User();
        customer2.setUsername("tester");

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByUsername(customer.getUsername())).thenReturn(Optional.of(customer));

        //act && assert
        assertThrows(RuntimeException.class, () -> {
            bidService.createBid(task.getId(), customer.getUsername());
        });
    }

    @Test
    void createBid_ShouldFail_WhenTaskNotCreated() {
        //arrange
        Task task = new Task();
        task.setCustomer(customer);
        task.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername(executor.getUsername())).thenReturn(Optional.of(executor));

        //act assert
        assertThrows(RuntimeException.class, () ->
                bidService.createBid(1L, executor.getUsername())
        );
    }

    @Test
    void createBid_ShouldFail_WhenDuplicateBid() {
        //arrange
        Task task = new Task();
        task.setStatus(TaskStatus.CREATED);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByUsername(executor.getUsername())).thenReturn(Optional.of(executor));
        when(bidRepository.existsByTaskAndExecutor(any(), any())).thenReturn(true);

        //act && assert
        assertThrows(RuntimeException.class, () ->
                bidService.createBid(task.getId(), executor.getUsername())
        );
    }
}
