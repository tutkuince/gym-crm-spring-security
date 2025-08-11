package com.epam.gymcrm.db.repository;

import com.epam.gymcrm.db.entity.TrainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

    Optional<TrainerEntity> findByUserUsername(String username);

    @Query("SELECT t FROM TrainerEntity t LEFT JOIN FETCH t.trainees WHERE t.user.username = :username")
    Optional<TrainerEntity> findByUserUsernameWithTrainees(@Param("username") String username);

    @Query("SELECT tr FROM TrainerEntity tr WHERE tr.id NOT IN " +
            "(SELECT ttr.id FROM TraineeEntity trn JOIN trn.trainers ttr WHERE trn.id = :traineeId) " +
            "AND tr.user.isActive = true")
    List<TrainerEntity> findUnassignedTrainersForTrainee(@Param("traineeId") Long traineeId);

    List<TrainerEntity> findAllByUserUsernameIn(List<String> trainerUsernames);

    Boolean existsByUserUsername(String username);
}
