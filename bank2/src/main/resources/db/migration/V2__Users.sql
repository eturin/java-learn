-- Таблица для хранения ролей пользователей
CREATE TABLE if not exists users
(
    id          BIGSERIAL PRIMARY KEY,
    login       VARCHAR(50) NOT NULL UNIQUE,
    fio         VARCHAR(255),
    role_id     BIGINT not null REFERENCES roles(id) ON DELETE RESTRICT,
    pwd_hash    VARCHAR(255) NOT NULL,         -- Хеш пароля (BCrypt)
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP
);

COMMENT ON TABLE users IS 'Пользователи';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор';
COMMENT ON COLUMN users.login IS 'Логин';
COMMENT ON COLUMN users.fio IS 'ФИО';
COMMENT ON COLUMN users.role_id IS 'Идентификато роли';
COMMENT ON COLUMN users.pwd_hash IS 'Хеш пароля';
COMMENT ON COLUMN users.created_at IS 'Дата добавления пользователя';
COMMENT ON COLUMN users.updated_at IS 'Дата изменения свойств пользователя';
COMMENT ON COLUMN users.deleted_at IS 'Дата удаления пользователя';

-- Добавляем функцию и триггер
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


-- Добавляем администатора
-- Пароль: admin123
INSERT INTO users (login, role_id, pwd_hash)
VALUES ('admin', (select id from roles where name = 'ADMIN' limit 1), '$2a$10$N9qo8uLOickgx2ZMRZoMyeaj6vWj8.9.3Z1Q9P5Qw2Yz5h6G8vQO')
    ON CONFLICT (login) DO nothing;
