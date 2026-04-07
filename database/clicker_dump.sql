CREATE DATABASE IF NOT EXISTS clicker_system;
USE clicker_system;

DROP TABLE IF EXISTS responses;

CREATE TABLE responses (
    questionNo INT NOT NULL,
    choice VARCHAR(1) NOT NULL
);

CREATE INDEX idx_question_choice ON responses (questionNo, choice);
