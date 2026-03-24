package dev.backend.repository;

import dev.backend.entity.Task;
import dev.backend.entity.User;
import dev.backend.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findAll(Pageable pageable);
    Page<Task> findByExecutor(User executor, Pageable pageable);
    Optional<Task> findByCustomer(User user);
}
