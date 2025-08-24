package com.registro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("=== INICIANDO CREACIÓN DE TABLA AUDIT_LOGS ===");
            
            // Primero intentar eliminar la tabla si existe (para recrearla limpia)
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS audit_logs");
                System.out.println("Tabla audit_logs eliminada (si existía)");
            } catch (Exception e) {
                System.out.println("No se pudo eliminar tabla audit_logs: " + e.getMessage());
            }
            
            // Crear tabla audit_logs desde cero
            System.out.println("Creando tabla audit_logs...");
            
            String createTableQuery = 
                "CREATE TABLE audit_logs (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL, " +
                "action VARCHAR(50) NOT NULL, " +
                "entity_type VARCHAR(100) NOT NULL, " +
                "entity_id VARCHAR(255), " +
                "details TEXT, " +
                "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "ip_address VARCHAR(45)" +
                ")";
            
            jdbcTemplate.execute(createTableQuery);
            System.out.println("✅ Tabla audit_logs creada exitosamente!");
            
            // Crear índices uno por uno
            try {
                jdbcTemplate.execute("CREATE INDEX idx_audit_logs_username ON audit_logs(username)");
                System.out.println("✅ Índice username creado");
            } catch (Exception e) {
                System.out.println("⚠️ Error creando índice username: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_audit_logs_action ON audit_logs(action)");
                System.out.println("✅ Índice action creado");
            } catch (Exception e) {
                System.out.println("⚠️ Error creando índice action: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp)");
                System.out.println("✅ Índice timestamp creado");
            } catch (Exception e) {
                System.out.println("⚠️ Error creando índice timestamp: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type)");
                System.out.println("✅ Índice entity_type creado");
            } catch (Exception e) {
                System.out.println("⚠️ Error creando índice entity_type: " + e.getMessage());
            }
            
            // Insertar datos de ejemplo uno por uno
            System.out.println("Insertando datos de ejemplo...");
            
            String[] insertQueries = {
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'LOGIN', 'Sistema', NULL, 'Usuario inició sesión', NOW() - INTERVAL 1 HOUR)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'VER', 'Página', NULL, 'Acceso a: Dashboard Principal', NOW() - INTERVAL 50 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'CREAR', 'Usuario', 'USR001', 'Usuario creado: Juan Pérez (Legajo: 12345)', NOW() - INTERVAL 45 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'EDITAR', 'Usuario', 'USR001', 'Usuario actualizado: cambio de teléfono', NOW() - INTERVAL 30 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'CREAR', 'Equipo', 'EQ001', 'Equipo creado: Notebook Dell Latitude (Serie: DL123456)', NOW() - INTERVAL 25 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'EDITAR', 'Equipo', 'EQ001', 'Equipo asignado a usuario: Juan Pérez', NOW() - INTERVAL 20 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'VER', 'Página', NULL, 'Acceso a: Gestión de Equipos', NOW() - INTERVAL 15 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'ELIMINAR', 'Equipo', 'EQ002', 'Equipo eliminado: Mouse Logitech (Serie: LG789)', NOW() - INTERVAL 10 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'VER', 'Página', NULL, 'Acceso a: Estadísticas', NOW() - INTERVAL 5 MINUTE)",
                "INSERT INTO audit_logs (username, action, entity_type, entity_id, details, timestamp) VALUES ('admin', 'VER', 'Página', NULL, 'Acceso a: Auditoría del Sistema', NOW())"
            };
            
            for (int i = 0; i < insertQueries.length; i++) {
                try {
                    jdbcTemplate.execute(insertQueries[i]);
                    System.out.println("✅ Registro " + (i + 1) + "/10 insertado");
                } catch (Exception e) {
                    System.out.println("⚠️ Error insertando registro " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            // Verificar que la tabla se creó correctamente
            try {
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM audit_logs", Integer.class);
                System.out.println("🎉 TABLA AUDIT_LOGS CREADA EXITOSAMENTE CON " + count + " REGISTROS!");
            } catch (Exception e) {
                System.err.println("❌ Error verificando tabla: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO al inicializar la tabla audit_logs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
