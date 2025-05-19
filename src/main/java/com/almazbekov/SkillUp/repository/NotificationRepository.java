package com.almazbekov.SkillUp.repository;

import com.almazbekov.SkillUp.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
    List<Notification> findByIsReadFalse();
    long countByIsReadFalse();
} 