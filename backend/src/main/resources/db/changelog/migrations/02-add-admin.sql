--liquibase formatted sql

--changeset dota2-beta:add_admin_user
INSERT INTO users (username, password, points, role, is_blocked)
VALUES ('admin', '$2a$10$tzEg2WmnbpHMm3T1NYhzgONabHO02hhMiFdPgBHE0T8bvp37x9Y/6', 0, 'ADMIN', false);