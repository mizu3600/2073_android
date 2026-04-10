CREATE DATABASE IF NOT EXISTS clicker_system;
USE clicker_system;

DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS responses;

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

CREATE INDEX idx_question_choice ON responses (questionNo, choice);
CREATE INDEX idx_comments_question_created ON comments (questionNo, createdAt);
