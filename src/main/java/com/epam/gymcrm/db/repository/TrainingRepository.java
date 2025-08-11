package com.epam.gymcrm.db.repository;

import com.epam.gymcrm.db.entity.TrainingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long>, JpaSpecificationExecutor<TrainingEntity> {
    Optional<TrainingEntity> findByTrainerIdAndTrainingDate(Long trainerId, LocalDateTime trainingDate);
}
