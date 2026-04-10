USE clicker_system;

INSERT INTO responses (questionNo, choice) VALUES (1, 'a');
INSERT INTO responses (questionNo, choice) VALUES (1, 'b');
INSERT INTO responses (questionNo, choice) VALUES (1, 'b');
INSERT INTO comments (questionNo, commentText) VALUES (1, 'I still think Iron Man should win this one.');
INSERT INTO comments (questionNo, commentText) VALUES (1, 'Spider-Man is the easiest character to relate to.');

SELECT choice, COUNT(*) AS count
FROM responses
WHERE questionNo = 1
GROUP BY choice
ORDER BY choice;

SELECT commentText, createdAt
FROM comments
WHERE questionNo = 1
ORDER BY createdAt DESC
LIMIT 10;
