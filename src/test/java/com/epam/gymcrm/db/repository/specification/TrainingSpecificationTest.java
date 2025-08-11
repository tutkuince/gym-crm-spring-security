package com.epam.gymcrm.db.repository.specification;

import com.epam.gymcrm.db.entity.TrainingEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class TrainingSpecificationTest {

    @Test
    void traineeUsername_shouldReturnNull_whenUsernameIsNull() {
        Specification<TrainingEntity> spec = TrainingSpecification.traineeUsername(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void traineeUsername_shouldReturnPredicate_whenUsernameIsNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path trainee = mock(Path.class);
        Path user = mock(Path.class);
        Path usernamePath = mock(Path.class);

        when(root.get("trainee")).thenReturn(trainee);
        when(trainee.get("user")).thenReturn(user);
        when(user.get("username")).thenReturn(usernamePath);

        Predicate predicate = mock(Predicate.class);
        when(cb.equal(usernamePath, "ali.veli")).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainingSpecification.traineeUsername("ali.veli");
        Predicate result = spec.toPredicate(root, query, cb);

        assertEquals(predicate, result);
        verify(cb).equal(usernamePath, "ali.veli");
    }

    @Test
    void fromDate_shouldReturnNull_whenFromIsNull() {
        Specification<TrainingEntity> spec = TrainingSpecification.fromDate(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void fromDate_shouldReturnPredicate_whenFromIsNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path datePath = mock(Path.class);
        LocalDate from = LocalDate.of(2024, 8, 6);

        when(root.get("trainingDate")).thenReturn(datePath);

        Predicate predicate = mock(Predicate.class);
        when(cb.greaterThanOrEqualTo(datePath, from)).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainingSpecification.fromDate(from);
        Predicate result = spec.toPredicate(root, query, cb);

        assertEquals(predicate, result);
        verify(cb).greaterThanOrEqualTo(datePath, from);
    }

    @Test
    void toDate_shouldReturnNull_whenToIsNull() {
        Specification<TrainingEntity> spec = TrainingSpecification.toDate(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void toDate_shouldReturnPredicate_whenToIsNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path datePath = mock(Path.class);
        LocalDate to = LocalDate.of(2024, 12, 31);

        when(root.get("trainingDate")).thenReturn(datePath);

        Predicate predicate = mock(Predicate.class);
        when(cb.lessThanOrEqualTo(datePath, to)).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainingSpecification.toDate(to);
        Predicate result = spec.toPredicate(root, query, cb);

        assertEquals(predicate, result);
        verify(cb).lessThanOrEqualTo(datePath, to);
    }

    @Test
    void trainerName_shouldReturnNull_whenTrainerNameIsNull() {
        Specification<TrainingEntity> spec = TrainingSpecification.trainerName(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void trainerName_shouldReturnPredicate_whenTrainerNameIsNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path trainer = mock(Path.class);
        Path user = mock(Path.class);
        Path firstName = mock(Path.class);
        Path lastName = mock(Path.class);

        when(root.get("trainer")).thenReturn(trainer);
        when(trainer.get("user")).thenReturn(user);
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
        when(cb.like(any(), eq("%ahmet%"))).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainingSpecification.trainerName("Ahmet");
        Predicate result = spec.toPredicate(root, query, cb);

        assertEquals(predicate, result);

        verify(cb).concat(firstName, spaceLiteral);
        verify(cb).concat(concat1, lastName);
        verify(cb).lower(concat2);
        verify(cb).like(any(), eq("%ahmet%"));
    }


    @Test
    void trainingType_shouldReturnNull_whenTypeIsNull() {
        Specification<TrainingEntity> spec = TrainingSpecification.trainingType(null);
        assertNull(spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class)));
    }

    @Test
    void trainingType_shouldReturnPredicate_whenTypeIsNotNull() {
        Root<TrainingEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Join<Object, Object> join = mock(Join.class);
        Path<Object> typeNamePath = mock(Path.class);

        when(root.join("trainingType")).thenReturn(join);
        when(join.get("trainingTypeName")).thenReturn(typeNamePath);

        Predicate predicate = mock(Predicate.class);
        when(cb.equal(typeNamePath, "Yoga")).thenReturn(predicate);

        Specification<TrainingEntity> spec = TrainingSpecification.trainingType("Yoga");
        Predicate result = spec.toPredicate(root, query, cb);

        assertEquals(predicate, result);
        verify(root).join("trainingType");
        verify(join).get("trainingTypeName");
        verify(cb).equal(typeNamePath, "Yoga");
    }
}