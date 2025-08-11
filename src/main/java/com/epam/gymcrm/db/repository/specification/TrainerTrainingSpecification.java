package com.epam.gymcrm.db.repository.specification;

import com.epam.gymcrm.db.entity.TrainingEntity;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Objects;

public class TrainerTrainingSpecification {

    public static Specification<TrainingEntity> trainerUsername(String username) {
        return (root, query, cb) ->
                Objects.isNull(username) ? null : cb.equal(root.get("trainer").get("user").get("username"), username);
    }

    public static Specification<TrainingEntity> fromDate(LocalDate from) {
        return (root, query, cb) ->
                Objects.isNull(from) ? null : cb.greaterThanOrEqualTo(root.get("trainingDate"), from.atStartOfDay());
    }

    public static Specification<TrainingEntity> toDate(LocalDate to) {
        return (root, query, cb) ->
                Objects.isNull(to) ? null : cb.lessThanOrEqualTo(root.get("trainingDate"), to.atTime(23,59,59));
    }

    public static Specification<TrainingEntity> traineeName(String traineeName) {
        return (root, query, cb) -> {
            if (Objects.isNull(traineeName)) return null;
            Expression<String> fullName = cb.concat(
                    root.get("trainee").get("user").get("firstName"), cb.literal(" ")
            );
            fullName = cb.concat(fullName, root.get("trainee").get("user").get("lastName"));
            return cb.like(cb.lower(fullName), "%" + traineeName.toLowerCase() + "%");
        };
    }
}
