CREATE DATABASE IF NOT EXISTS clicker_system;
USE clicker_system;

DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS responses;

CREATE TABLE questions (
    questionNo INT NOT NULL,
    questionText VARCHAR(255) NOT NULL,
    startTime DATETIME NOT NULL,
    endTime DATETIME NOT NULL,
    PRIMARY KEY (questionNo)
);

CREATE TABLE responses (
    questionNo INT NOT NULL,
    choice VARCHAR(1) NOT NULL
);

CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    questionNo INT NOT NULL,
    commentText VARCHAR(240) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

INSERT INTO questions (questionNo, questionText, startTime, endTime)
VALUES (1, 'Who is the coolest Marvel Hero?', '2026-01-01 00:00:00', '2026-12-31 23:59:59');

CREATE INDEX idx_question_choice ON responses (questionNo, choice);
CREATE INDEX idx_comments_question_created ON comments (questionNo, createdAt);
