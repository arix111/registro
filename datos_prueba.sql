-- ============================================
-- SCRIPT SQL PARA DATOS DE PRUEBA
-- Base de datos: prueba
-- Usuario: root (sin contraseña)
-- ============================================

USE prueba;

-- Limpiar datos existentes (opcional)
-- DELETE FROM equipos_informaticos;
-- DELETE FROM usuarios;

-- ============================================
-- INSERTAR 50 USUARIOS DE DIFERENTES SITES
-- ============================================

INSERT INTO usuarios (legajo, nombre, apellido, telefono, correo_electronico, direccion, site) VALUES
-- Buenos Aires (15 usuarios)
('LEG001', 'Juan', 'Pérez', '11-1234-5678', 'juan.perez@empresa.com', 'Av. Corrientes 1234, CABA', 'BUENOS_AIRES'),
('LEG002', 'María', 'González', '11-2345-6789', 'maria.gonzalez@empresa.com', 'Av. Santa Fe 567, CABA', 'BUENOS_AIRES'),
('LEG003', 'Carlos', 'Rodríguez', '11-3456-7890', 'carlos.rodriguez@empresa.com', 'Av. Rivadavia 890, CABA', 'BUENOS_AIRES'),
('LEG004', 'Ana', 'López', '11-4567-8901', 'ana.lopez@empresa.com', 'Av. Callao 234, CABA', 'BUENOS_AIRES'),
('LEG005', 'Luis', 'Martínez', '11-5678-9012', 'luis.martinez@empresa.com', 'Av. 9 de Julio 345, CABA', 'BUENOS_AIRES'),
('LEG006', 'Laura', 'Fernández', '11-6789-0123', 'laura.fernandez@empresa.com', 'Av. Belgrano 456, CABA', 'BUENOS_AIRES'),
('LEG007', 'Diego', 'Sánchez', '11-7890-1234', 'diego.sanchez@empresa.com', 'Av. San Juan 567, CABA', 'BUENOS_AIRES'),
('LEG008', 'Sofía', 'Ramírez', '11-8901-2345', 'sofia.ramirez@empresa.com', 'Av. Independencia 678, CABA', 'BUENOS_AIRES'),
('LEG009', 'Miguel', 'Torres', '11-9012-3456', 'miguel.torres@empresa.com', 'Av. Entre Ríos 789, CABA', 'BUENOS_AIRES'),
('LEG010', 'Valentina', 'Flores', '11-0123-4567', 'valentina.flores@empresa.com', 'Av. Boedo 890, CABA', 'BUENOS_AIRES'),
('LEG011', 'Sebastián', 'Morales', '11-1357-2468', 'sebastian.morales@empresa.com', 'Av. Pueyrredón 123, CABA', 'BUENOS_AIRES'),
('LEG012', 'Camila', 'Herrera', '11-2468-1357', 'camila.herrera@empresa.com', 'Av. Scalabrini Ortiz 234, CABA', 'BUENOS_AIRES'),
('LEG013', 'Facundo', 'Silva', '11-3579-0246', 'facundo.silva@empresa.com', 'Av. Juan B. Justo 345, CABA', 'BUENOS_AIRES'),
('LEG014', 'Agustina', 'Romero', '11-4680-1357', 'agustina.romero@empresa.com', 'Av. Warnes 456, CABA', 'BUENOS_AIRES'),
('LEG015', 'Nicolás', 'Vargas', '11-5791-2468', 'nicolas.vargas@empresa.com', 'Av. Córdoba 567, CABA', 'BUENOS_AIRES'),

-- Córdoba (12 usuarios)
('LEG016', 'Lucía', 'Castro', '351-123-4567', 'lucia.castro@empresa.com', 'Av. Colón 123, Córdoba', 'CORDOBA'),
('LEG017', 'Mateo', 'Mendoza', '351-234-5678', 'mateo.mendoza@empresa.com', 'Av. Fader 234, Córdoba', 'CORDOBA'),
('LEG018', 'Isabella', 'Jiménez', '351-345-6789', 'isabella.jimenez@empresa.com', 'Av. Rafael Núñez 345, Córdoba', 'CORDOBA'),
('LEG019', 'Tomás', 'Aguilar', '351-456-7890', 'tomas.aguilar@empresa.com', 'Av. Vélez Sarsfield 456, Córdoba', 'CORDOBA'),
('LEG020', 'Martina', 'Ruiz', '351-567-8901', 'martina.ruiz@empresa.com', 'Av. Chacabuco 567, Córdoba', 'CORDOBA'),
('LEG021', 'Joaquín', 'Ortega', '351-678-9012', 'joaquin.ortega@empresa.com', 'Av. General Paz 678, Córdoba', 'CORDOBA'),
('LEG022', 'Emilia', 'Guerrero', '351-789-0123', 'emilia.guerrero@empresa.com', 'Av. Hipólito Yrigoyen 789, Córdoba', 'CORDOBA'),
('LEG023', 'Bautista', 'Medina', '351-890-1234', 'bautista.medina@empresa.com', 'Av. Marcelo T. de Alvear 890, Córdoba', 'CORDOBA'),
('LEG024', 'Renata', 'Ramos', '351-901-2345', 'renata.ramos@empresa.com', 'Av. 24 de Septiembre 123, Córdoba', 'CORDOBA'),
('LEG025', 'Thiago', 'Peña', '351-012-3456', 'thiago.pena@empresa.com', 'Av. Duarte Quirós 234, Córdoba', 'CORDOBA'),
('LEG026', 'Julieta', 'Cabrera', '351-123-7890', 'julieta.cabrera@empresa.com', 'Av. Poeta Lugones 345, Córdoba', 'CORDOBA'),
('LEG027', 'Santino', 'Vega', '351-234-8901', 'santino.vega@empresa.com', 'Av. Recta Martinoli 456, Córdoba', 'CORDOBA'),

-- Tucumán (10 usuarios)
('LEG028', 'Delfina', 'Molina', '381-123-4567', 'delfina.molina@empresa.com', 'Av. Aconquija 123, Tucumán', 'TUCUMAN'),
('LEG029', 'Lautaro', 'Paredes', '381-234-5678', 'lautaro.paredes@empresa.com', 'Av. Mate de Luna 234, Tucumán', 'TUCUMAN'),
('LEG030', 'Amparo', 'Sosa', '381-345-6789', 'amparo.sosa@empresa.com', 'Av. Soldati 345, Tucumán', 'TUCUMAN'),
('LEG031', 'Ignacio', 'Córdoba', '381-456-7890', 'ignacio.cordoba@empresa.com', 'Av. Roca 456, Tucumán', 'TUCUMAN'),
('LEG032', 'Milagros', 'Ibarra', '381-567-8901', 'milagros.ibarra@empresa.com', 'Av. Kirchner 567, Tucumán', 'TUCUMAN'),
('LEG033', 'Benjamín', 'Acosta', '381-678-9012', 'benjamin.acosta@empresa.com', 'Av. Perón 678, Tucumán', 'TUCUMAN'),
('LEG034', 'Catalina', 'Figueroa', '381-789-0123', 'catalina.figueroa@empresa.com', 'Av. Mitre 789, Tucumán', 'TUCUMAN'),
('LEG035', 'Gael', 'Maldonado', '381-890-1234', 'gael.maldonado@empresa.com', 'Av. Sarmiento 890, Tucumán', 'TUCUMAN'),
('LEG036', 'Pilar', 'Navarro', '381-901-2345', 'pilar.navarro@empresa.com', 'Av. Belgrano 123, Tucumán', 'TUCUMAN'),
('LEG037', 'Máximo', 'Ríos', '381-012-3456', 'maximo.rios@empresa.com', 'Av. San Martín 234, Tucumán', 'TUCUMAN'),

-- Mar del Plata (8 usuarios)
('LEG038', 'Zoe', 'Campos', '223-123-4567', 'zoe.campos@empresa.com', 'Av. Constitución 123, Mar del Plata', 'MAR_DEL_PLATA'),
('LEG039', 'Ian', 'Moreno', '223-234-5678', 'ian.moreno@empresa.com', 'Av. Independencia 234, Mar del Plata', 'MAR_DEL_PLATA'),
('LEG040', 'Alma', 'Suárez', '223-345-6789', 'alma.suarez@empresa.com', 'Av. Luro 345, Mar del Plata', 'MAR_DEL_PLATA'),
('LEG041', 'Bruno', 'Carrasco', '223-456-7890', 'bruno.carrasco@empresa.com', 'Av. Colón 456, Mar del Plata', 'MAR_DEL_PLATA'),
('LEG042', 'Abril', 'Espinoza', '223-567-8901', 'abril.espinoza@empresa.com', 'Av. Jara 567, Mar del Plata', 'MAR_DEL_PLATA'),
('LEG043', 'Dante', 'Contreras', '223-678-9012', 'dante.contreras@empresa.com', 'Av. Martínez de Hoz 678, Mar del Plata', 'MAR_DEL_PLATA'),
('LEG044', 'Mía', 'Sandoval', '223-789-0123', 'mia.sandoval@empresa.com', 'Av. Alem 789, Mar del Plata', 'MAR_DEL_PLATA'),
('LEG045', 'León', 'Cáceres', '223-890-1234', 'leon.caceres@empresa.com', 'Av. Güemes 890, Mar del Plata', 'MAR_DEL_PLATA'),

-- Perú (5 usuarios)
('LEG046', 'Emma', 'Valdez', '+51-1-234-5678', 'emma.valdez@empresa.com', 'Av. Javier Prado 123, Lima', 'PERU'),
('LEG047', 'Enzo', 'Montoya', '+51-1-345-6789', 'enzo.montoya@empresa.com', 'Av. Arequipa 234, Lima', 'PERU'),
('LEG048', 'Olivia', 'Delgado', '+51-1-456-7890', 'olivia.delgado@empresa.com', 'Av. Brasil 345, Lima', 'PERU'),
('LEG049', 'Matías', 'Rojas', '+51-1-567-8901', 'matias.rojas@empresa.com', 'Av. Universitaria 456, Lima', 'PERU'),
('LEG050', 'Francesca', 'Villanueva', '+51-1-678-9012', 'francesca.villanueva@empresa.com', 'Av. La Marina 567, Lima', 'PERU');

-- ============================================
-- INSERTAR EQUIPOS INFORMÁTICOS (100+ equipos)
-- ============================================

INSERT INTO equipos_informaticos (tipo, marca, modelo, numero_serie, numero_inventario, estado, fecha_asignacion, observaciones, usuario_legajo) VALUES
-- Equipos para Buenos Aires
('CPU', 'Dell', 'OptiPlex 7090', 'DL001234', 'INV001', 'ACTIVO', '2024-01-15', 'Equipo principal de trabajo', 'LEG001'),
('MONITOR', 'Samsung', 'F24T450FQU', 'SM001234', 'INV002', 'ACTIVO', '2024-01-15', 'Monitor 24 pulgadas', 'LEG001'),
('TECLADO', 'Logitech', 'K120', 'LG001234', 'INV003', 'ACTIVO', '2024-01-15', 'Teclado USB estándar', 'LEG001'),
('MOUSE', 'Logitech', 'B100', 'LG001235', 'INV004', 'ACTIVO', '2024-01-15', 'Mouse óptico USB', 'LEG001'),

('NOTEBOOK', 'HP', 'EliteBook 840', 'HP001234', 'INV005', 'ACTIVO', '2024-01-16', 'Notebook para trabajo móvil', 'LEG002'),
('MOUSE', 'Microsoft', 'Basic Optical', 'MS001234', 'INV006', 'ACTIVO', '2024-01-16', 'Mouse para notebook', 'LEG002'),

('CPU', 'Lenovo', 'ThinkCentre M720q', 'LN001234', 'INV007', 'ACTIVO', '2024-01-17', 'Mini PC compacto', 'LEG003'),
('MONITOR', 'LG', '22MK430H', 'LG001236', 'INV008', 'ACTIVO', '2024-01-17', 'Monitor IPS 22 pulgadas', 'LEG003'),
('VINCHA', 'Plantronics', 'Blackwire 3220', 'PL001234', 'INV009', 'ACTIVO', '2024-01-17', 'Auriculares con micrófono', 'LEG003'),

('ONLY_ONE', 'HP', 'All-in-One 24-df1023la', 'HP001235', 'INV010', 'ACTIVO', '2024-01-18', 'PC todo en uno', 'LEG004'),
('TECLADO', 'HP', 'Wireless Elite v2', 'HP001236', 'INV011', 'ACTIVO', '2024-01-18', 'Teclado inalámbrico', 'LEG004'),

('NOTEBOOK', 'Asus', 'VivoBook 15', 'AS001234', 'INV012', 'ACTIVO', '2024-01-19', 'Notebook para diseño', 'LEG005'),
('MOUSE', 'Asus', 'WT425', 'AS001235', 'INV013', 'ACTIVO', '2024-01-19', 'Mouse inalámbrico', 'LEG005'),

('CPU', 'Dell', 'Inspiron 3880', 'DL001235', 'INV014', 'EN_MANTENIMIENTO', '2024-01-20', 'En mantenimiento preventivo', 'LEG006'),
('MONITOR', 'Dell', 'S2721DS', 'DL001236', 'INV015', 'ACTIVO', '2024-01-20', 'Monitor QHD 27 pulgadas', 'LEG006'),

('NOTEBOOK', 'Lenovo', 'IdeaPad 3', 'LN001235', 'INV016', 'ACTIVO', '2024-01-21', 'Notebook económico', 'LEG007'),
('CABLES', 'Genérico', 'HDMI 2.0', 'GN001234', 'INV017', 'ACTIVO', '2024-01-21', 'Cable HDMI 2 metros', 'LEG007'),

('CPU', 'HP', 'Pavilion Desktop', 'HP001237', 'INV018', 'ACTIVO', '2024-01-22', 'PC de escritorio', 'LEG008'),
('MONITOR', 'AOC', '24G2U', 'AO001234', 'INV019', 'ACTIVO', '2024-01-22', 'Monitor gaming 24 pulgadas', 'LEG008'),
('TECLADO', 'Redragon', 'K552', 'RD001234', 'INV020', 'ACTIVO', '2024-01-22', 'Teclado mecánico', 'LEG008'),

('ONLY_ONE', 'Lenovo', 'IdeaCentre AIO 3', 'LN001236', 'INV021', 'ACTIVO', '2024-01-23', 'Todo en uno 24 pulgadas', 'LEG009'),
('VINCHA', 'HyperX', 'Cloud Stinger', 'HX001234', 'INV022', 'ACTIVO', '2024-01-23', 'Auriculares gaming', 'LEG009'),

('NOTEBOOK', 'Acer', 'Aspire 5', 'AC001234', 'INV023', 'ACTIVO', '2024-01-24', 'Notebook multimedia', 'LEG010'),
('MOUSE', 'Acer', 'AMR020', 'AC001235', 'INV024', 'ACTIVO', '2024-01-24', 'Mouse óptico', 'LEG010'),

-- Equipos para Córdoba
('CPU', 'Dell', 'Vostro 3681', 'DL001237', 'INV025', 'ACTIVO', '2024-01-25', 'PC empresarial', 'LEG016'),
('MONITOR', 'Philips', '243V7QDSB', 'PH001234', 'INV026', 'ACTIVO', '2024-01-25', 'Monitor IPS 24 pulgadas', 'LEG016'),
('ROUTER', 'TP-Link', 'Archer C6', 'TP001234', 'INV027', 'ACTIVO', '2024-01-25', 'Router WiFi AC1200', 'LEG016'),

('NOTEBOOK', 'HP', 'Pavilion 15', 'HP001238', 'INV028', 'ACTIVO', '2024-01-26', 'Notebook personal', 'LEG017'),
('CABLES', 'Belkin', 'USB-C to HDMI', 'BK001234', 'INV029', 'ACTIVO', '2024-01-26', 'Adaptador USB-C a HDMI', 'LEG017'),

('CPU', 'Asus', 'VivoPC VM40B', 'AS001236', 'INV030', 'ACTIVO', '2024-01-27', 'Mini PC', 'LEG018'),
('MONITOR', 'Asus', 'VA24EHE', 'AS001237', 'INV031', 'ACTIVO', '2024-01-27', 'Monitor 24 pulgadas', 'LEG018'),
('SWITCH', 'Netgear', 'GS108', 'NG001234', 'INV032', 'ACTIVO', '2024-01-27', 'Switch 8 puertos', 'LEG018'),

('NOTEBOOK', 'Lenovo', 'ThinkPad E14', 'LN001237', 'INV033', 'ACTIVO', '2024-01-28', 'Notebook empresarial', 'LEG019'),
('VINCHA', 'Jabra', 'Evolve 20', 'JB001234', 'INV034', 'ACTIVO', '2024-01-28', 'Auriculares profesionales', 'LEG019'),

('ONLY_ONE', 'Dell', 'Inspiron 24 5000', 'DL001238', 'INV035', 'ACTIVO', '2024-01-29', 'All-in-One táctil', 'LEG020'),
('TECLADO', 'Dell', 'KB216', 'DL001239', 'INV036', 'ACTIVO', '2024-01-29', 'Teclado multimedia', 'LEG020'),

-- Equipos para Tucumán
('CPU', 'HP', 'ProDesk 400 G7', 'HP001239', 'INV037', 'ACTIVO', '2024-01-30', 'PC de oficina', 'LEG028'),
('MONITOR', 'HP', 'V24i', 'HP001240', 'INV038', 'ACTIVO', '2024-01-30', 'Monitor IPS 24 pulgadas', 'LEG028'),
('PROYECTORES', 'Epson', 'PowerLite S41+', 'EP001234', 'INV039', 'ACTIVO', '2024-01-30', 'Proyector para presentaciones', 'LEG028'),

('NOTEBOOK', 'Dell', 'Latitude 3420', 'DL001240', 'INV040', 'ACTIVO', '2024-01-31', 'Notebook empresarial', 'LEG029'),
('MOUSE', 'Dell', 'MS116', 'DL001241', 'INV041', 'ACTIVO', '2024-01-31', 'Mouse óptico', 'LEG029'),

('CPU', 'Lenovo', 'ThinkCentre M75s', 'LN001238', 'INV042', 'EN_REPARACION', '2024-02-01', 'En reparación por falla de disco', 'LEG030'),
('MONITOR', 'Lenovo', 'ThinkVision E24-20', 'LN001239', 'INV043', 'ACTIVO', '2024-02-01', 'Monitor profesional', 'LEG030'),

('NOTEBOOK', 'Asus', 'ExpertBook B1400', 'AS001238', 'INV044', 'ACTIVO', '2024-02-02', 'Notebook resistente', 'LEG031'),
('SERVIDORES', 'Dell', 'PowerEdge T140', 'DL001242', 'INV045', 'ACTIVO', '2024-02-02', 'Servidor de archivos', 'LEG031'),

-- Equipos para Mar del Plata
('ONLY_ONE', 'HP', '22-df0013la', 'HP001241', 'INV046', 'ACTIVO', '2024-02-03', 'All-in-One compacto', 'LEG038'),
('VINCHA', 'Logitech', 'H390', 'LG001237', 'INV047', 'ACTIVO', '2024-02-03', 'Auriculares USB', 'LEG038'),

('NOTEBOOK', 'Acer', 'TravelMate P2', 'AC001236', 'INV048', 'ACTIVO', '2024-02-04', 'Notebook para viajes', 'LEG039'),
('CABLES', 'Anker', 'PowerLine USB-C', 'AN001234', 'INV049', 'ACTIVO', '2024-02-04', 'Cable USB-C 3 metros', 'LEG039'),

('CPU', 'Asus', 'ExpertCenter D5', 'AS001239', 'INV050', 'ACTIVO', '2024-02-05', 'PC empresarial', 'LEG040'),
('MONITOR', 'BenQ', 'GW2283', 'BQ001234', 'INV051', 'ACTIVO', '2024-02-05', 'Monitor eye-care', 'LEG040'),

-- Equipos para Perú
('NOTEBOOK', 'HP', 'ProBook 440 G8', 'HP001242', 'INV052', 'ACTIVO', '2024-02-06', 'Notebook profesional', 'LEG046'),
('ROUTER', 'Cisco', 'RV160', 'CS001234', 'INV053', 'ACTIVO', '2024-02-06', 'Router empresarial', 'LEG046'),

('CPU', 'Dell', 'OptiPlex 3080', 'DL001243', 'INV054', 'ACTIVO', '2024-02-07', 'PC compacto', 'LEG047'),
('MONITOR', 'Dell', 'E2220H', 'DL001244', 'INV055', 'ACTIVO', '2024-02-07', 'Monitor básico 22 pulgadas', 'LEG047'),
('OTROS', 'APC', 'UPS BR700G', 'AP001234', 'INV056', 'ACTIVO', '2024-02-07', 'UPS para protección eléctrica', 'LEG047'),

('NOTEBOOK', 'Lenovo', 'V15 G2', 'LN001240', 'INV057', 'DADO_DE_BAJA', '2024-02-08', 'Dado de baja por obsolescencia', 'LEG048'),
('SWITCH', 'D-Link', 'DGS-1016A', 'DL001245', 'INV058', 'ACTIVO', '2024-02-08', 'Switch 16 puertos', 'LEG048'),

('ONLY_ONE', 'Asus', 'Zen AiO 24', 'AS001240', 'INV059', 'ACTIVO', '2024-02-09', 'All-in-One premium', 'LEG049'),
('TECLADO', 'Asus', 'TUF Gaming K1', 'AS001241', 'INV060', 'ACTIVO', '2024-02-09', 'Teclado gaming', 'LEG049'),

-- Equipos adicionales distribuidos
('PROYECTORES', 'BenQ', 'MH535FHD', 'BQ001235', 'INV061', 'ACTIVO', '2024-02-10', 'Proyector Full HD', 'LEG011'),
('SERVIDORES', 'HP', 'ProLiant ML110', 'HP001243', 'INV062', 'ACTIVO', '2024-02-11', 'Servidor de aplicaciones', 'LEG021'),
('ROUTER', 'Ubiquiti', 'Dream Machine', 'UB001234', 'INV063', 'ACTIVO', '2024-02-12', 'Router avanzado', 'LEG032'),
('SWITCH', 'TP-Link', 'TL-SG1024D', 'TP001235', 'INV064', 'ACTIVO', '2024-02-13', 'Switch 24 puertos', 'LEG041'),
('OTROS', 'Synology', 'DS220+', 'SY001234', 'INV065', 'ACTIVO', '2024-02-14', 'NAS para backup', 'LEG050');

-- ============================================
-- VERIFICAR DATOS INSERTADOS
-- ============================================

-- Contar usuarios por site
SELECT site, COUNT(*) as cantidad_usuarios 
FROM usuarios 
GROUP BY site 
ORDER BY cantidad_usuarios DESC;

-- Contar equipos por tipo
SELECT tipo, COUNT(*) as cantidad_equipos 
FROM equipos_informaticos 
GROUP BY tipo 
ORDER BY cantidad_equipos DESC;

-- Contar equipos por estado
SELECT estado, COUNT(*) as cantidad_equipos 
FROM equipos_informaticos 
GROUP BY estado;

-- Usuarios con más equipos
SELECT u.legajo, u.nombre, u.apellido, u.site, COUNT(e.id) as cantidad_equipos
FROM usuarios u
LEFT JOIN equipos_informaticos e ON u.legajo = e.usuario_legajo
GROUP BY u.legajo, u.nombre, u.apellido, u.site
ORDER BY cantidad_equipos DESC
LIMIT 10;

-- ============================================
-- SCRIPT COMPLETADO
-- Total: 50 usuarios, 65+ equipos
-- Distribución: BA(15), CBA(12), TUC(10), MDP(8), PE(5)
-- ============================================
