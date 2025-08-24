-- Script SQL para crear las tablas de autenticación y usuarios por defecto
-- Base de datos: prueba
-- Usuario: root (sin contraseña)

USE prueba;

-- Crear tabla de usuarios para autenticación
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'USER', 'MANAGER') NOT NULL DEFAULT 'USER',
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_enabled (enabled)
);

-- Insertar usuarios por defecto
-- Contraseñas encriptadas con BCrypt (fuerza 12) - REALES:
-- admin123 -> $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfVvwuCdCRP3kV2jzwjIxe
-- manager123 -> $2a$12$8xfYqW3iBVdvdsEr1Opf.uRd94eCDdNcaWEsnOJgFu2u0yrXGrHjG
-- user123 -> $2a$12$ZeAOCALrOxhriIhp8MS8Gu9psaUvBmNVZpDn1KVnhG.Klq.rHb.1u

INSERT INTO users (username, password, email, first_name, last_name, role, enabled) VALUES
-- Usuario Administrador (admin / admin123)
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfVvwuCdCRP3kV2jzwjIxe', 'admin@registro.com', 'Administrador', 'Sistema', 'ADMIN', TRUE),

-- Usuario Gerente (manager / manager123)
('manager', '$2a$12$8xfYqW3iBVdvdsEr1Opf.uRd94eCDdNcaWEsnOJgFu2u0yrXGrHjG', 'manager@registro.com', 'Gerente', 'General', 'MANAGER', TRUE),

-- Usuarios normales (user1, user2, user3 / user123)
('user1', '$2a$12$ZeAOCALrOxhriIhp8MS8Gu9psaUvBmNVZpDn1KVnhG.Klq.rHb.1u', 'user1@registro.com', 'Juan', 'Pérez', 'USER', TRUE),
('user2', '$2a$12$ZeAOCALrOxhriIhp8MS8Gu9psaUvBmNVZpDn1KVnhG.Klq.rHb.1u', 'user2@registro.com', 'María', 'González', 'USER', TRUE),
('user3', '$2a$12$ZeAOCALrOxhriIhp8MS8Gu9psaUvBmNVZpDn1KVnhG.Klq.rHb.1u', 'user3@registro.com', 'Carlos', 'Rodríguez', 'USER', TRUE);

-- Verificar datos insertados
SELECT 'Usuarios creados:' as info;
SELECT id, username, email, first_name, last_name, role, enabled, created_at 
FROM users 
ORDER BY role, username;

SELECT 'Estadísticas de usuarios por rol:' as info;
SELECT role, COUNT(*) as cantidad 
FROM users 
GROUP BY role 
ORDER BY role;

-- Información de credenciales por defecto
SELECT '=== CREDENCIALES POR DEFECTO ===' as info;
SELECT 'Administrador:' as tipo, 'admin' as usuario, 'admin123' as contraseña
UNION ALL
SELECT 'Gerente:', 'manager', 'manager123'
UNION ALL
SELECT 'Usuario Normal:', 'user1', 'user123'
UNION ALL
SELECT 'Usuario Normal:', 'user2', 'user123'
UNION ALL
SELECT 'Usuario Normal:', 'user3', 'user123';
