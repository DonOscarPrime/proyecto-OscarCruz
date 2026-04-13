-- ============================================================
--  FinanzApp - Esquema de base de datos MySQL
--  Ejecutar como: mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS finanzapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finanzapp;

-- ─── USUARIOS ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    password_hash   VARCHAR(200) NOT NULL,
    telefono        VARCHAR(30),
    fecha_nacimiento DATE,
    comunidad       VARCHAR(80) DEFAULT 'Madrid',
    situacion_laboral ENUM('Estudiante','Empleado','Autónomo','Funcionario','Desempleado') DEFAULT 'Empleado',
    ingresos_netos  DECIMAL(10,2) DEFAULT 0.00,
    objetivo_financiero VARCHAR(80),
    presupuesto_mensual DECIMAL(10,2) DEFAULT 0.00,
    tema            ENUM('claro','oscuro') DEFAULT 'claro',
    moneda          VARCHAR(10) DEFAULT '€',
    idioma          VARCHAR(20) DEFAULT 'Español',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─── CATEGORÍAS ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categorias (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL,
    emoji   VARCHAR(10) NOT NULL,
    tipo    ENUM('gasto','ingreso','ambos') DEFAULT 'ambos',
    color   VARCHAR(20) DEFAULT '#6B6A65'
);

INSERT INTO categorias (nombre, emoji, tipo, color) VALUES
('Vivienda',        '🏠', 'gasto',   '#1D9E75'),
('Alimentación',    '🍔', 'gasto',   '#BA7517'),
('Transporte',      '🚗', 'gasto',   '#185FA5'),
('Ocio',            '🎮', 'gasto',   '#8B5CF6'),
('Servicios',       '📱', 'gasto',   '#D85A30'),
('Extraordinario',  '⚡', 'gasto',   '#6B6A65'),
('Nómina / Ingreso','💼', 'ingreso', '#1D9E75'),
('Otro',            '💳', 'ambos',   '#A09F9B');

-- ─── MOVIMIENTOS (ingresos & gastos) ──────────────────────
CREATE TABLE IF NOT EXISTS movimientos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      INT NOT NULL,
    tipo            ENUM('gasto','ingreso') NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    cantidad        DECIMAL(10,2) NOT NULL,
    categoria_id    INT,
    notas           TEXT,
    fecha           DATE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id)   REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
);

-- ─── OBJETIVOS DE AHORRO ──────────────────────────────────
CREATE TABLE IF NOT EXISTS objetivos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      INT NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    objetivo        DECIMAL(10,2) NOT NULL,
    actual          DECIMAL(10,2) DEFAULT 0.00,
    emoji           VARCHAR(10) DEFAULT '🎯',
    fecha_limite    DATE,
    completado      TINYINT(1) DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─── HÁBITOS (simulador) ──────────────────────────────────
CREATE TABLE IF NOT EXISTS habitos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      INT NOT NULL,
    emoji           VARCHAR(10) NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    frecuencia_actual INT DEFAULT 0,
    frecuencia_obj  INT DEFAULT 0,
    unidad          ENUM('semana','mes') DEFAULT 'semana',
    coste           DECIMAL(8,2) NOT NULL,
    descripcion     VARCHAR(100),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─── NOTIFICACIONES ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS notificaciones (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      INT NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    mensaje         TEXT,
    tipo            ENUM('info','success','warning','danger') DEFAULT 'info',
    leida           TINYINT(1) DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─── SIMULACIONES DE PRÉSTAMO (historial) ─────────────────
CREATE TABLE IF NOT EXISTS simulaciones_prestamo (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      INT NOT NULL,
    tipo_prestamo   VARCHAR(50),
    capital         DECIMAL(12,2),
    plazo_meses     INT,
    tin             DECIMAL(5,2),
    cuota_mensual   DECIMAL(10,2),
    total_pagar     DECIMAL(12,2),
    total_intereses DECIMAL(12,2),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─── USUARIO DEMO ─────────────────────────────────────────
-- Contraseña: Demo1234!
INSERT INTO usuarios (nombre, email, password_hash, telefono, fecha_nacimiento, comunidad,
                      situacion_laboral, ingresos_netos, objetivo_financiero, presupuesto_mensual)
VALUES ('Óscar Cruz Vázquez', 'oscar@email.com',
        '$2a$12$7zBx8GkBk0hL6S.0iXj/iuWxJr7P/GwE3l4SZr6c1NeEL2NTFZK.C',
        '+34 612 345 678', '1999-05-14', 'Madrid',
        'Empleado', 1400.00, 'Controlar mis gastos', 1100.00);

-- Movimientos demo
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, fecha) VALUES
(1, 'gasto',   'Alquiler',      450.00,  1, CURDATE() - INTERVAL 24 DAY),
(1, 'ingreso', 'Nómina',       1400.00,  7, CURDATE() - INTERVAL 24 DAY),
(1, 'gasto',   'Mercadona',      78.00,  2, CURDATE() - INTERVAL 20 DAY),
(1, 'gasto',   'Gasolina',       55.00,  3, CURDATE() - INTERVAL 17 DAY),
(1, 'gasto',   'Netflix',        13.00,  5, CURDATE() - INTERVAL 15 DAY),
(1, 'gasto',   'Cena amigos',    42.00,  4, CURDATE() - INTERVAL 10 DAY),
(1, 'gasto',   'Supermercado',   92.00,  2, CURDATE() - INTERVAL 7 DAY),
(1, 'gasto',   'Gym',            40.00,  6, CURDATE() - INTERVAL 5 DAY);

-- Objetivos demo
INSERT INTO objetivos (usuario_id, nombre, objetivo, actual, emoji) VALUES
(1, 'Viaje a Japón',         3000.00, 1860.00, '✈️'),
(1, 'Fondo de emergencia',   2000.00,  950.00, '🛡️'),
(1, 'Portátil nuevo',         800.00,  320.00, '💻');

-- Hábitos demo
INSERT INTO habitos (usuario_id, emoji, nombre, frecuencia_actual, frecuencia_obj, unidad, coste, descripcion) VALUES
(1, '☕', 'Café fuera de casa',     5, 2, 'semana', 2.50,  'por café'),
(1, '🍕', 'Comida a domicilio',     3, 1, 'semana', 12.00, 'por pedido'),
(1, '🚗', 'Transporte privado',     5, 3, 'semana', 8.00,  'por viaje'),
(1, '🎮', 'Suscripciones / ocio',  35, 20,'mes',    1.00,  '€/mes'),
(1, '🍺', 'Salidas nocturnas',      2, 1, 'semana', 22.00, 'por salida');

-- Notificaciones demo
INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo) VALUES
(1, '¡Presupuesto al 89%!', 'Llevas 982€ de los 1.100€ de tu presupuesto mensual.', 'warning'),
(1, 'Nómina recibida',      'Se ha registrado un ingreso de 1.400€.', 'success'),
(1, 'Objetivo casi completado', 'Tu objetivo "Viaje a Japón" está al 62%.', 'info');
