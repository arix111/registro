-- Script para limpiar y recrear usuarios correctamente
USE prueba;

-- Eliminar todos los usuarios existentes con datos incorrectos
DELETE FROM users;

-- Resetear el auto_increment
ALTER TABLE users AUTO_INCREMENT = 1;

-- Recrear la tabla con las columnas correctas
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'USER', 'MANAGER') NOT NULL DEFAULT 'USER',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_enabled (enabled)
);

-- Verificar que la tabla esté vacía
SELECT COUNT(*) as total_usuarios FROM users;
