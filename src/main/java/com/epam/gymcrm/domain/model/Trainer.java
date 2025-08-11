package com.epam.gymcrm.domain.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Trainer {

    private Long id;
    private User user;
    private TrainingType specialization;

    private Set<Training> trainings = new HashSet<>();
    private Set<Trainee> trainees = new HashSet<>();

    public Trainer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public Set<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(Set<Training> trainings) {
        this.trainings = trainings;
    }

    public Set<Trainee> getTrainees() {
        return trainees;
    }

    public void setTrainees(Set<Trainee> trainees) {
        this.trainees = trainees;
    }

    public void updateProfile(String firstName, String lastName, boolean isActive) {
        if (Objects.isNull(this.user)) {
            throw new IllegalStateException("Trainer: User is null. Cannot update profile.");
        }
        this.user.updateProfile(firstName, lastName, isActive);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trainer trainer = (Trainer) o;
        return Objects.equals(id, trainer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
