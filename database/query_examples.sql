USE clicker_system;

INSERT INTO responses (questionNo, choice) VALUES (1, 'a');
INSERT INTO responses (questionNo, choice) VALUES (1, 'b');
INSERT INTO responses (questionNo, choice) VALUES (1, 'b');

SELECT choice, COUNT(*) AS count
FROM responses
WHERE questionNo = 1
GROUP BY choice
ORDER BY choice;
