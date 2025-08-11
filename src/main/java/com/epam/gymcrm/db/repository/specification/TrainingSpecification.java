package com.epam.gymcrm.db.repository.specification;

import com.epam.gymcrm.db.entity.TrainingEntity;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Objects;

public class TrainingSpecification {

    public static Specification<TrainingEntity> traineeUsername(String username) {
        return (root, query, cb) ->
                username == null ? null : cb.equal(root.get("trainee").get("user").get("username"), username);
    }

    public static Specification<TrainingEntity> fromDate(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("trainingDate"), from);
    }

    public static Specification<TrainingEntity> toDate(LocalDate to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("trainingDate"), to);
    }

    public static Specification<TrainingEntity> trainerName(String trainerName) {
        return (root, query, cb) -> {
            if (Objects.isNull(trainerName)) return null;
            Expression<String> fullName = cb.concat(
                    root.get("trainer").get("user").get("firstName"), cb.literal(" ")
            );
            fullName = cb.concat(fullName, root.get("trainer").get("user").get("lastName"));
            return cb.like(cb.lower(fullName), "%" + trainerName.toLowerCase() + "%");
        };
    }

    public static Specification<TrainingEntity> trainingType(String trainingTypeName) {
        return (root, query, cb) -> {
            if (trainingTypeName == null) return null;
            Join<Object, Object> trainingTypeJoin = root.join("trainingType");
            return cb.equal(trainingTypeJoin.get("trainingTypeName"), trainingTypeName);
        };
    }
}
