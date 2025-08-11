package com.epam.gymcrm.db.repository.specification;

import com.epam.gymcrm.db.entity.TrainingEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class TrainerTrainingSpecificationTest {

    @Test
    void trainerUsername_shouldReturnNull_whenUsernameIsNull() {
        Specification<TrainingEntity> spec = TrainerTrainingSpecification.trainerUsername(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void trainerUsername_shouldReturnPredicate_whenUsernameNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path trainer = mock(Path.class);
        Path user = mock(Path.class);
        Path usernamePath = mock(Path.class);

        when(root.get("trainer")).thenReturn(trainer);
        when(trainer.get("user")).thenReturn(user);
        when(user.get("username")).thenReturn(usernamePath);

        Predicate predicate = mock(Predicate.class);
        when(cb.equal(usernamePath, "mehmet")).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainerTrainingSpecification.trainerUsername("mehmet");
        Predicate result = spec.toPredicate(root, mock(CriteriaQuery.class), cb);

        assertEquals(predicate, result);
        verify(cb).equal(usernamePath, "mehmet");
    }

    @Test
    void fromDate_shouldReturnNull_whenFromIsNull() {
        Specification<TrainingEntity> spec = TrainerTrainingSpecification.fromDate(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void fromDate_shouldReturnPredicate_whenFromNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path datePath = mock(Path.class);
        when(root.get("trainingDate")).thenReturn(datePath);

        Predicate predicate = mock(Predicate.class);
        LocalDate from = LocalDate.of(2024, 8, 6);
        LocalDateTime fromStart = from.atStartOfDay();
        when(cb.greaterThanOrEqualTo(datePath, fromStart)).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainerTrainingSpecification.fromDate(from);
        Predicate result = spec.toPredicate(root, mock(CriteriaQuery.class), cb);

        assertEquals(predicate, result);
        verify(cb).greaterThanOrEqualTo(datePath, fromStart);
    }

    @Test
    void toDate_shouldReturnNull_whenToIsNull() {
        Specification<TrainingEntity> spec = TrainerTrainingSpecification.toDate(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void toDate_shouldReturnPredicate_whenToNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path datePath = mock(Path.class);
        when(root.get("trainingDate")).thenReturn(datePath);

        Predicate predicate = mock(Predicate.class);
        LocalDate to = LocalDate.of(2024, 8, 7);
        LocalDateTime toEnd = to.atTime(23, 59, 59);
        when(cb.lessThanOrEqualTo(datePath, toEnd)).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainerTrainingSpecification.toDate(to);
        Predicate result = spec.toPredicate(root, mock(CriteriaQuery.class), cb);

        assertEquals(predicate, result);
        verify(cb).lessThanOrEqualTo(datePath, toEnd);
    }

    @Test
    void traineeName_shouldReturnNull_whenTraineeNameIsNull() {
        Specification<TrainingEntity> spec = TrainerTrainingSpecification.traineeName(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void traineeName_shouldReturnPredicate_whenTraineeNameNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path trainee = mock(Path.class);
        Path user = mock(Path.class);
        Path firstName = mock(Path.class);
        Path lastName = mock(Path.class);

        when(root.get("trainee")).thenReturn(trainee);
        when(trainee.get("user")).thenReturn(user);
        when(user.get("firstName")).thenReturn(firstName);
        when(user.get("lastName")).thenReturn(lastName);

        Expression<String> spaceLiteral = mock(Expression.class);
        when(cb.literal(" ")).thenReturn(spaceLiteral);

        Expression<String> concat1 = mock(Expression.class);
        Expression<String> concat2 = mock(Expression.class);
        when(cb.concat(firstName, spaceLiteral)).thenReturn(concat1);
        when(cb.concat(concat1, lastName)).thenReturn(concat2);

        Expression<String> lower = mock(Expression.class);
        when(cb.lower(concat2)).thenReturn(lower);

        Predicate predicate = mock(Predicate.class);
        when(cb.like(any(), eq("%mehmet%"))).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainerTrainingSpecification.traineeName("Mehmet");
        Predicate result = spec.toPredicate(root, mock(CriteriaQuery.class), cb);

        assertEquals(predicate, result);

        verify(cb).concat(firstName, spaceLiteral);
        verify(cb).concat(concat1, lastName);
        verify(cb).lower(concat2);
        verify(cb).like(any(), eq("%mehmet%"));
    }
}