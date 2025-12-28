-- Таблица для хранения ролей пользователей
CREATE TABLE if not exists roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

COMMENT ON TABLE roles IS 'Роли пользователей';
COMMENT ON COLUMN roles.id IS 'Уникальный идентификатор';
COMMENT ON COLUMN roles.name IS 'Код роли (например: USER, ADMIN)';
COMMENT ON COLUMN roles.description IS 'Описание';

-- Добавляем базовые роли
INSERT INTO roles (name, description)
VALUES ('USER', 'Пользователь'),
       ('ADMIN', 'Администратор')
ON CONFLICT (name) DO UPDATE
    SET description = EXCLUDED.description;

