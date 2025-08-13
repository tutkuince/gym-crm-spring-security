-- USERS
INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES
    ('John', 'Doe', 'john.doe', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true),
    ('Jane', 'Staging', 'jane.staging', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true),
    ('Michael', 'Brown', 'michael.brown', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true),
    ('Emily', 'Davis', 'emily.davis', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true),
    ('Sophia', 'Taylor', 'sophia.taylor', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true),
    ('William', 'Wilson', 'william.wilson', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true),
    ('Olivia', 'Moore', 'olivia.moore', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true),
    ('James', 'Miller', 'james.miller', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', false),
    ('Emma', 'Anderson', 'emma.anderson', '$2a$10$iN0lUG1/ekAPik1AIA8hmu41akz/3riRyGIbvlxGt58NR7NUVjQce', true);

-- TRAINING_TYPE
INSERT INTO training_types (training_type_name)
VALUES
    ('Cardio'),
    ('Strength'),
    ('Yoga'),
    ('Pilates'),
    ('HIIT');

-- TRAINEE
INSERT INTO trainees (user_id, date_of_birth, address)
VALUES
    ((SELECT id FROM users WHERE username = 'john.doe'), '2000-01-01', 'New York'),
    ((SELECT id FROM users WHERE username = 'michael.brown'), '1995-05-10', 'Los Angeles'),
    ((SELECT id FROM users WHERE username = 'emily.davis'), '2001-03-15', 'Chicago'),
    ((SELECT id FROM users WHERE username = 'sophia.taylor'), '1997-09-21', 'Houston'),
    ((SELECT id FROM users WHERE username = 'james.miller'), '1993-11-12', 'Phoenix'),
    ((SELECT id FROM users WHERE username = 'emma.anderson'), '2002-07-18', 'Philadelphia');

-- TRAINER
INSERT INTO trainers (user_id, specialization_id)
VALUES
    ((SELECT id FROM users WHERE username = 'jane.staging'), (SELECT id FROM training_types WHERE training_type_name = 'Cardio')),
    ((SELECT id FROM users WHERE username = 'william.wilson'), (SELECT id FROM training_types WHERE training_type_name = 'Strength')),
    ((SELECT id FROM users WHERE username = 'olivia.moore'), (SELECT id FROM training_types WHERE training_type_name = 'Yoga')),
    ((SELECT id FROM users WHERE username = 'sophia.taylor'), (SELECT id FROM training_types WHERE training_type_name = 'Pilates'));

-- TRAINING
INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES
    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'john.doe')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'jane.staging')),
     'Morning Cardio', (SELECT id FROM training_types WHERE training_type_name = 'Cardio'),
     '2025-07-22 09:00:00', 60),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'michael.brown')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'william.wilson')),
     'Strength Basics', (SELECT id FROM training_types WHERE training_type_name = 'Strength'),
     '2025-07-22 10:00:00', 45),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'emily.davis')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'olivia.moore')),
     'Yoga for Beginners', (SELECT id FROM training_types WHERE training_type_name = 'Yoga'),
     '2025-07-23 08:30:00', 50),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'sophia.taylor')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'sophia.taylor')),
     'Pilates One-on-One', (SELECT id FROM training_types WHERE training_type_name = 'Pilates'),
     '2025-07-23 15:00:00', 40),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'james.miller')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'jane.staging')),
     'HIIT Rush', (SELECT id FROM training_types WHERE training_type_name = 'HIIT'),
     '2025-07-24 17:00:00', 35),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'emma.anderson')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'olivia.moore')),
     'Yoga Stretch', (SELECT id FROM training_types WHERE training_type_name = 'Yoga'),
     '2025-07-25 19:00:00', 55),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'john.doe')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'william.wilson')),
     'Strength Mix', (SELECT id FROM training_types WHERE training_type_name = 'Strength'),
     '2025-07-25 18:00:00', 45);

-- TRAINEE_TRAINER
INSERT INTO trainee_trainer (trainee_id, trainer_id)
VALUES
    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'john.doe')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'jane.staging'))),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'john.doe')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'william.wilson'))),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'michael.brown')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'william.wilson'))),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'emily.davis')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'olivia.moore'))),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'sophia.taylor')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'sophia.taylor'))),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'james.miller')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'jane.staging'))),

    ((SELECT id FROM trainees WHERE user_id = (SELECT id FROM users WHERE username = 'emma.anderson')),
     (SELECT id FROM trainers WHERE user_id = (SELECT id FROM users WHERE username = 'olivia.moore')));
