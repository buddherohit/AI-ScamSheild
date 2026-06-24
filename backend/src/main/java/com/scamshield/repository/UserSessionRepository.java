package com.scamshield.repository;

import com.scamshield.entity.User;
import com.scamshield.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserOrderByCreatedAtDesc(User user);
    Optional<UserSession> findByIdAndUser(Long id, User user);
    void deleteByUser(User user);
}
