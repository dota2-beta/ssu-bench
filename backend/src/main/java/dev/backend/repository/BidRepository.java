package dev.backend.repository;

import dev.backend.entity.Bid;
import dev.backend.entity.Task;
import dev.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByTask(Task task);
    boolean existsByTaskAndExecutor(Task task, User executor);
}
