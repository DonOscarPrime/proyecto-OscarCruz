-- ============================================================
--  FinanzApp - Schema con datos demo abundantes
--  Ejecutar: mysql -u root -p < schema_demo.sql
-- ============================================================

DROP DATABASE IF EXISTS finanzapp;
CREATE DATABASE finanzapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finanzapp;

-- ─── USUARIOS ─────────────────────────────────────────────
CREATE TABLE usuarios (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(120) NOT NULL,
    email               VARCHAR(200) NOT NULL UNIQUE,
    password_hash       VARCHAR(200) NOT NULL,
    telefono            VARCHAR(30),
    fecha_nacimiento    DATE,
    comunidad           VARCHAR(80)  DEFAULT 'Madrid',
    situacion_laboral   ENUM('Estudiante','Empleado','Autónomo','Funcionario','Desempleado') DEFAULT 'Empleado',
    ingresos_netos      DECIMAL(10,2) DEFAULT 0.00,
    objetivo_financiero VARCHAR(80),
    presupuesto_mensual DECIMAL(10,2) DEFAULT 0.00,
    tema                ENUM('claro','oscuro') DEFAULT 'claro',
    moneda              VARCHAR(10)  DEFAULT '€',
    idioma              VARCHAR(20)  DEFAULT 'Español',
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ─── CATEGORÍAS ───────────────────────────────────────────
CREATE TABLE categorias (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL,
    emoji   VARCHAR(10) NOT NULL,
    tipo    ENUM('gasto','ingreso','ambos') DEFAULT 'ambos',
    color   VARCHAR(20) DEFAULT '#6B6A65'
);

-- ─── MOVIMIENTOS ──────────────────────────────────────────
CREATE TABLE movimientos (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   INT NOT NULL,
    tipo         ENUM('gasto','ingreso') NOT NULL,
    nombre       VARCHAR(200) NOT NULL,
    cantidad     DECIMAL(10,2) NOT NULL,
    categoria_id INT,
    notas        TEXT,
    fecha        DATE NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id)   REFERENCES usuarios(id)   ON DELETE CASCADE,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
);

-- ─── OBJETIVOS ────────────────────────────────────────────
CREATE TABLE objetivos (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   INT NOT NULL,
    nombre       VARCHAR(200) NOT NULL,
    objetivo     DECIMAL(10,2) NOT NULL,
    actual       DECIMAL(10,2) DEFAULT 0.00,
    emoji        VARCHAR(10)   DEFAULT '🎯',
    fecha_limite DATE,
    completado   TINYINT(1)    DEFAULT 0,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─── HÁBITOS ──────────────────────────────────────────────
CREATE TABLE habitos (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id        INT NOT NULL,
    emoji             VARCHAR(10) NOT NULL,
    nombre            VARCHAR(200) NOT NULL,
    frecuencia_actual INT          DEFAULT 0,
    frecuencia_obj    INT          DEFAULT 0,
    unidad            ENUM('semana','mes') DEFAULT 'semana',
    coste             DECIMAL(8,2) NOT NULL,
    descripcion       VARCHAR(100),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─── NOTIFICACIONES ───────────────────────────────────────
CREATE TABLE notificaciones (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    titulo     VARCHAR(200) NOT NULL,
    mensaje    TEXT,
    tipo       ENUM('info','success','warning','danger') DEFAULT 'info',
    leida      TINYINT(1)   DEFAULT 0,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─── SIMULACIONES PRÉSTAMO ────────────────────────────────
CREATE TABLE simulaciones_prestamo (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id       INT NOT NULL,
    tipo_prestamo    VARCHAR(50),
    capital          DECIMAL(12,2),
    plazo_meses      INT,
    tin              DECIMAL(5,2),
    cuota_mensual    DECIMAL(10,2),
    total_pagar      DECIMAL(12,2),
    total_intereses  DECIMAL(12,2),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);


-- ══════════════════════════════════════════════════════════
--  DATOS
-- ══════════════════════════════════════════════════════════

-- ─── CATEGORÍAS ───────────────────────────────────────────
INSERT INTO categorias (nombre, emoji, tipo, color) VALUES
('Vivienda',         '🏠', 'gasto',   '#1D9E75'),
('Alimentación',     '🍔', 'gasto',   '#BA7517'),
('Transporte',       '🚗', 'gasto',   '#185FA5'),
('Ocio',             '🎮', 'gasto',   '#8B5CF6'),
('Servicios',        '📱', 'gasto',   '#D85A30'),
('Extraordinario',   '⚡', 'gasto',   '#6B6A65'),
('Nómina / Ingreso', '💼', 'ingreso', '#1D9E75'),
('Salud',            '🏥', 'gasto',   '#EF4444'),
('Ropa / Moda',      '👗', 'gasto',   '#EC4899'),
('Educación',        '📚', 'gasto',   '#F59E0B'),
('Viajes',           '✈️',  'gasto',   '#06B6D4'),
('Freelance',        '💻', 'ingreso', '#10B981'),
('Inversión',        '📈', 'ingreso', '#3B82F6'),
('Otro',             '💳', 'ambos',   '#A09F9B');

-- ─── USUARIOS ─────────────────────────────────────────────
-- Contraseña para todos: Demo1234!
-- Hash BCrypt generado con jBCrypt gensalt(12)
INSERT INTO usuarios
    (nombre, email, password_hash, telefono, fecha_nacimiento, comunidad,
     situacion_laboral, ingresos_netos, objetivo_financiero, presupuesto_mensual)
VALUES
-- Usuario 1 – empleado madrileño (el principal)
('Óscar Cruz Vázquez',
 'oscar@email.com',
 '$2a$12$eImiTXuWVxfM37uY4JANjOe5XtSYmREfBj0IpuTm4jR2wOPSsa3Gy',
 '+34 612 345 678', '1999-05-14', 'Madrid',
 'Empleado', 1400.00, 'Controlar mis gastos', 1100.00),

-- Usuario 2 – autónoma catalana
('Laura Martínez Roca',
 'laura@email.com',
 '$2a$12$eImiTXuWVxfM37uY4JANjOe5XtSYmREfBj0IpuTm4jR2wOPSsa3Gy',
 '+34 633 210 987', '1995-11-22', 'Cataluña',
 'Autónomo', 2200.00, 'Iniciarme en inversión', 1600.00);


-- ══════════════════════════════════════════════════════════
--  MOVIMIENTOS – USUARIO 1 (Óscar, últimos 6 meses)
-- ══════════════════════════════════════════════════════════

-- ── Octubre (hace 5 meses) ────────────────────────────────
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES
(1,'ingreso','Nómina octubre',        1400.00, 7, 'Nómina mensual bruta',                        DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-01')),
(1,'gasto',  'Alquiler octubre',       450.00, 1, 'Piso compartido Lavapiés',                    DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-02')),
(1,'gasto',  'Luz y gas',              68.50,  5, 'Endesa — bimestral',                          DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-03')),
(1,'gasto',  'Mercadona',              92.30,  2, 'Compra semanal',                              DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-05')),
(1,'gasto',  'Abono transporte',       54.60,  3, 'Zona A Madrid',                               DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-06')),
(1,'gasto',  'Netflix',                13.99,  5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-07')),
(1,'gasto',  'Spotify',                10.99,  5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-07')),
(1,'gasto',  'Supermercado Lidl',      47.80,  2, 'Compra segunda semana',                       DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-12')),
(1,'gasto',  'Cena cumpleaños Ana',    38.00,  4, 'Restaurante La Pepita',                       DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-14')),
(1,'gasto',  'Farmacia',               21.40,  8, 'Antihistamínico + vitaminas',                 DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-16')),
(1,'gasto',  'Amazon — auriculares',   35.99,  6, 'Auriculares inalámbricos básicos',            DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-18')),
(1,'gasto',  'Mercadona',              88.50,  2, 'Compra tercera semana',                       DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-19')),
(1,'gasto',  'Cine + palomitas',       19.50,  4, 'Película con amigos',                         DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-21')),
(1,'gasto',  'Gasolina coche compartido',28.00,3, 'Viaje fin de semana',                         DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-24')),
(1,'gasto',  'Mercadona',              65.20,  2, 'Compra cuarta semana',                        DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-26')),
(1,'gasto',  'Gym Octubre',            40.00,  6, 'Mensualidad gym Holmes Place',                DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-28')),
(1,'ingreso','Transferencia amigo',    50.00,  7, 'Me devuelve cena de agosto',                  DATE_FORMAT(NOW() - INTERVAL 5 MONTH, '%Y-%m-29'));

-- ── Noviembre ─────────────────────────────────────────────
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES
(1,'ingreso','Nómina noviembre',      1400.00, 7, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-01')),
(1,'gasto',  'Alquiler noviembre',    450.00,  1, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-02')),
(1,'gasto',  'Mercadona',             79.60,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-04')),
(1,'gasto',  'Abono transporte',      54.60,   3, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-05')),
(1,'gasto',  'Netflix',               13.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-07')),
(1,'gasto',  'Spotify',               10.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-07')),
(1,'gasto',  'Clases inglés',         60.00,  10, 'Preparación B2 — academia',                   DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-08')),
(1,'gasto',  'Lidl',                  53.10,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-11')),
(1,'gasto',  'Bar con compañeros',    27.50,   4, 'Afterwork viernes',                           DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-15')),
(1,'gasto',  'Ropa El Corte Inglés',  89.95,   9, 'Camisa + pantalón Black Friday',              DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-24')),
(1,'gasto',  'Zapatillas Nike',       75.00,   9, 'Nike Air Max outlet',                         DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-24')),
(1,'gasto',  'Mercadona',             91.30,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-25')),
(1,'gasto',  'Gym noviembre',         40.00,   6, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-28')),
(1,'gasto',  'Cena Acción de Gracias',45.00,   4, 'Restaurante americano con amigos',            DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-28')),
(1,'gasto',  'Seguro móvil',          8.99,    5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 4 MONTH, '%Y-%m-30'));

-- ── Diciembre ─────────────────────────────────────────────
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES
(1,'ingreso','Nómina diciembre',      1400.00, 7, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-01')),
(1,'ingreso','Paga extra diciembre',  700.00,  7, 'Media paga extra convenio',                   DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-01')),
(1,'gasto',  'Alquiler diciembre',    450.00,  1, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-02')),
(1,'gasto',  'Regalos Navidad',       180.00,  9, 'Familia + amigos cercanos',                   DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-10')),
(1,'gasto',  'Netflix',               13.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-07')),
(1,'gasto',  'Spotify',               10.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-07')),
(1,'gasto',  'Abono transporte',      54.60,   3, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-05')),
(1,'gasto',  'Mercadona',             105.40,  2, 'Compra especial Navidad',                     DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-20')),
(1,'gasto',  'Cena Nochebuena',       65.00,   4, 'Cena familiar restaurante',                   DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-24')),
(1,'gasto',  'Cotillón Nochevieja',   55.00,   4, 'Fiesta fin de año',                           DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-31')),
(1,'gasto',  'Vuelo Madrid-Málaga',   89.00,  11, 'Visita familia en vacaciones',                DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-22')),
(1,'gasto',  'Hotel Málaga 3 noches',135.00,  11, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-22')),
(1,'gasto',  'Gym diciembre',         40.00,   6, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-28')),
(1,'gasto',  'Plataforma HBO Max',    9.99,    5, 'Promo 3 meses',                               DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-15')),
(1,'ingreso','Transferencia navidad', 200.00,  7, 'Regalo abuelos',                              DATE_FORMAT(NOW() - INTERVAL 3 MONTH, '%Y-%m-25'));

-- ── Enero ─────────────────────────────────────────────────
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES
(1,'ingreso','Nómina enero',          1400.00, 7, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-01')),
(1,'gasto',  'Alquiler enero',        450.00,  1, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-02')),
(1,'gasto',  'Netflix',               13.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-07')),
(1,'gasto',  'Spotify',               10.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-07')),
(1,'gasto',  'Abono transporte',      54.60,   3, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-06')),
(1,'gasto',  'Mercadona',             83.70,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-05')),
(1,'gasto',  'Reyes — Nintendo Switch',289.00, 6, 'Me la regalo a mí mismo',                    DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-06')),
(1,'gasto',  'Juegos Switch',         49.99,   4, 'Mario Kart 8 + Zelda',                       DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-08')),
(1,'gasto',  'Clases inglés',         60.00,  10, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-09')),
(1,'gasto',  'Supermercado Carrefour',61.20,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-14')),
(1,'gasto',  'Dentista revisión',     45.00,   8, 'Revisión anual + limpieza',                   DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-17')),
(1,'gasto',  'Bar tapas fin de semana',22.00,  4, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-19')),
(1,'gasto',  'Mercadona',             77.30,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-21')),
(1,'gasto',  'Gym enero',             40.00,   6, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-28')),
(1,'gasto',  'Suscripción ChatGPT',   22.00,   5, 'Plus mensual',                               DATE_FORMAT(NOW() - INTERVAL 2 MONTH, '%Y-%m-15'));

-- ── Febrero ───────────────────────────────────────────────
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES
(1,'ingreso','Nómina febrero',        1400.00, 7, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-01')),
(1,'gasto',  'Alquiler febrero',      450.00,  1, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-02')),
(1,'gasto',  'Netflix',               13.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-07')),
(1,'gasto',  'Spotify',               10.99,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-07')),
(1,'gasto',  'Abono transporte',      54.60,   3, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-05')),
(1,'gasto',  'Mercadona',             86.40,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-04')),
(1,'gasto',  'San Valentín — cena',   72.00,   4, 'Restaurante con pareja',                      DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-14')),
(1,'gasto',  'Roses + regalo',        35.00,   9, 'Detalles San Valentín',                       DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-14')),
(1,'gasto',  'Clases inglés',         60.00,  10, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-10')),
(1,'gasto',  'Lidl semanal',          49.80,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-11')),
(1,'gasto',  'Camisetas Zara',        42.00,   9, '3x2 rebajas',                                 DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-18')),
(1,'gasto',  'Mercadona',             94.10,   2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-22')),
(1,'gasto',  'Amazon — libro técnico',18.99,  10, 'Clean Code — R. Martin',                      DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-20')),
(1,'gasto',  'Gym febrero',           40.00,   6, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-28')),
(1,'gasto',  'Suscripción ChatGPT',   22.00,   5, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-15')),
(1,'gasto',  'Taxi noche sábado',     14.50,   3, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-22')),
(1,'ingreso','Venta objeto segunda mano',80.00,7, 'Xbox vendida en Wallapop',                    DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-25'));

-- ── Marzo (mes actual) ────────────────────────────────────
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES
(1,'ingreso','Nómina marzo',          1400.00, 7, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-01')),
(1,'gasto',  'Alquiler marzo',        450.00,  1, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-02')),
(1,'gasto',  'Netflix',               13.99,   5, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-07')),
(1,'gasto',  'Spotify',               10.99,   5, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-07')),
(1,'gasto',  'Abono transporte',      54.60,   3, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-05')),
(1,'gasto',  'Mercadona',             91.20,   2, 'Compra grande mensual',                       DATE_FORMAT(NOW(), '%Y-%m-03')),
(1,'gasto',  'Suscripción ChatGPT',   22.00,   5, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-08')),
(1,'gasto',  'Gasolina viaje',        48.00,   3, 'Viaje fin de semana Toledo',                  DATE_FORMAT(NOW(), '%Y-%m-09')),
(1,'gasto',  'Restaurante Toledo',    33.50,   4, 'Menú turístico x2',                           DATE_FORMAT(NOW(), '%Y-%m-09')),
(1,'gasto',  'Clases inglés',         60.00,  10, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-11')),
(1,'gasto',  'Mercadona',             78.30,   2, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-15')),
(1,'gasto',  'Gym marzo',             40.00,   6, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-10')),
(1,'gasto',  'Farmacia — proteína',   34.99,   8, 'Suplemento deportivo',                        DATE_FORMAT(NOW(), '%Y-%m-12')),
(1,'gasto',  'Concierto Vetusta Morla',52.00,  4, 'Entradas WiZink Center',                      DATE_FORMAT(NOW(), '%Y-%m-14')),
(1,'gasto',  'Cena preconcierto',     28.00,   4, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-14')),
(1,'ingreso','Freelance diseño logo', 250.00, 12, 'Logo para amigo emprendedor',                 DATE_FORMAT(NOW(), '%Y-%m-16')),
(1,'gasto',  'Supermercado Carrefour',55.40,   2, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-18')),
(1,'gasto',  'Libro — Padre Rico',    13.99,  10, 'Padre Rico Padre Pobre',                      DATE_FORMAT(NOW(), '%Y-%m-19')),
(1,'gasto',  'Pago reparación bici',  65.00,   6, 'Cambio cámara + frenos',                      DATE_FORMAT(NOW(), '%Y-%m-20')),
(1,'gasto',  'Bar fin de semana',     19.50,   4, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-22'));


-- ══════════════════════════════════════════════════════════
--  OBJETIVOS – USUARIO 1
-- ══════════════════════════════════════════════════════════
INSERT INTO objetivos (usuario_id, nombre, objetivo, actual, emoji, fecha_limite, completado) VALUES
(1, 'Viaje a Japón',            3000.00, 1860.00, '🗾', DATE_ADD(NOW(), INTERVAL 8  MONTH), 0),
(1, 'Fondo de emergencia',      3000.00,  950.00, '🛡️',  DATE_ADD(NOW(), INTERVAL 18 MONTH), 0),
(1, 'Portátil nuevo',            900.00,  620.00, '💻', DATE_ADD(NOW(), INTERVAL 3  MONTH), 0),
(1, 'Matrícula máster online',  1200.00,  200.00, '🎓', DATE_ADD(NOW(), INTERVAL 10 MONTH), 0),
(1, 'Bicicleta de carretera',    800.00,  800.00, '🚴', NULL,                                1),
(1, 'Curso de inglés B2',        400.00,  400.00, '🇬🇧', NULL,                                1);


-- ══════════════════════════════════════════════════════════
--  HÁBITOS – USUARIO 1
-- ══════════════════════════════════════════════════════════
INSERT INTO habitos (usuario_id, emoji, nombre, frecuencia_actual, frecuencia_obj, unidad, coste, descripcion) VALUES
(1, '☕', 'Café fuera de casa',       5, 2, 'semana', 2.50,  'por café'),
(1, '🍕', 'Comida a domicilio',       3, 1, 'semana', 12.00, 'por pedido'),
(1, '🚗', 'Taxi / Uber',              3, 1, 'semana',  9.00, 'por viaje'),
(1, '🎮', 'Suscripciones digitales', 73, 40,'mes',     1.00, '€/mes'),
(1, '🍺', 'Salidas nocturnas',        2, 1, 'semana', 22.00, 'por salida'),
(1, '🍔', 'Comida rápida',            4, 2, 'semana',  7.50, 'por comida'),
(1, '🛒', 'Compras impulsivas online',3, 1, 'mes',    35.00, 'por compra'),
(1, '🚬', 'Tabaco',                   0, 0, 'semana',  5.50, 'por paquete');


-- ══════════════════════════════════════════════════════════
--  NOTIFICACIONES – USUARIO 1
-- ══════════════════════════════════════════════════════════
INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo, leida, created_at) VALUES
(1, '¡Presupuesto casi al límite!',
    'Llevas 982€ gastados de tu límite mensual de 1.100€. Quedan solo 118€.',
    'warning', 0, NOW() - INTERVAL 2 HOUR),

(1, 'Nómina recibida ✓',
    'Se ha registrado un ingreso de 1.400€. ¡Buen comienzo de mes!',
    'success', 0, NOW() - INTERVAL 3 DAY),

(1, 'Objetivo "Viaje a Japón" al 62%',
    'Te faltan 1.140€ para tu meta. Con tu ritmo actual llegarás en agosto.',
    'info', 0, NOW() - INTERVAL 5 DAY),

(1, '¡Objetivo completado! 🎉',
    'Has alcanzado tu objetivo "Curso de inglés B2". ¡Enhorabuena!',
    'success', 1, NOW() - INTERVAL 10 DAY),

(1, 'Gasto inusual detectado',
    'Has gastado 289€ en un solo movimiento (Nintendo Switch). ¿Todo correcto?',
    'warning', 1, NOW() - INTERVAL 65 DAY),

(1, 'Resumen enero 2025',
    'Enero cerró con -430€ de balance. Tasa de ahorro: 0%. Revisa tus hábitos.',
    'danger', 1, NOW() - INTERVAL 55 DAY),

(1, 'Ingreso extra registrado',
    'Has recibido 250€ por trabajo freelance. ¡Considera destinarlo a tus objetivos!',
    'success', 0, NOW() - INTERVAL 7 DAY),

(1, 'Hábito con alto impacto',
    'Tus suscripciones digitales suman 73€/mes. Podrías ahorrar 33€ reduciendo a tu objetivo.',
    'info', 1, NOW() - INTERVAL 20 DAY),

(1, 'Recordatorio — transferencia pendiente',
    'Tu amigo Carlos aún no te ha devuelto los 38€ de la cena de octubre.',
    'info', 1, NOW() - INTERVAL 30 DAY),

(1, 'Nuevo mes, nuevo presupuesto',
    'Comienza marzo. Tu presupuesto se ha reiniciado a 1.100€. ¡Suerte!',
    'info', 0, DATE_FORMAT(NOW(), '%Y-%m-01'));


-- ══════════════════════════════════════════════════════════
--  MOVIMIENTOS – USUARIO 2 (Laura, autónoma) — muestra
-- ══════════════════════════════════════════════════════════
INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES
(2,'ingreso','Factura cliente A',     1800.00,12, 'Diseño web corporativo',                      DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-01')),
(2,'ingreso','Factura cliente B',      900.00,12, 'Mantenimiento mensual',                       DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-05')),
(2,'gasto',  'Alquiler oficina',       350.00, 1, 'Coworking Barcelona centro',                  DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-02')),
(2,'gasto',  'Seguridad Social autónomo',294.00,6,'Cuota mensual SS',                            DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-20')),
(2,'gasto',  'Adobe Creative Cloud',   54.99, 5, 'Licencia anual / 12',                         DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-07')),
(2,'gasto',  'Supermercado',           110.00, 2, NULL,                                          DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-08')),
(2,'gasto',  'Transporte público',      45.00, 3, 'T-Usual metro Barcelona',                     DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-06')),
(2,'ingreso','Nómina marzo',          2200.00, 7, 'Transferencia mes en curso',                  DATE_FORMAT(NOW(), '%Y-%m-01')),
(2,'gasto',  'Alquiler piso',          780.00, 1, 'Piso Gracia, Barcelona',                      DATE_FORMAT(NOW(), '%Y-%m-03')),
(2,'gasto',  'Gym + pilates',           55.00, 6, NULL,                                          DATE_FORMAT(NOW(), '%Y-%m-05'));

INSERT INTO objetivos (usuario_id, nombre, objetivo, actual, emoji, fecha_limite) VALUES
(2, 'Colchón autónoma (6 meses)',  13200.00, 4800.00, '🏦', DATE_ADD(NOW(), INTERVAL 14 MONTH)),
(2, 'Viaje Estados Unidos',         4500.00, 1200.00, '🗽', DATE_ADD(NOW(), INTERVAL 12 MONTH)),
(2, 'Cámara mirrorless Sony A7',    2200.00, 2200.00, '📷', NULL);

UPDATE objetivos SET completado=1 WHERE usuario_id=2 AND nombre='Cámara mirrorless Sony A7';

INSERT INTO habitos (usuario_id, emoji, nombre, frecuencia_actual, frecuencia_obj, unidad, coste, descripcion) VALUES
(2,'☕','Café en coworking',    5, 3, 'semana',  3.00, 'por café'),
(2,'🍱','Delivery comida',      4, 2, 'semana', 14.00, 'por pedido'),
(2,'🛍','Ropa y accesorios',    2, 1, 'mes',    65.00, 'por compra');

INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo, leida) VALUES
(2,'Factura cobrada ✓',  'El cliente A ha pagado 1.800€. Balance mensual positivo.',       'success', 0),
(2,'Cuota SS pendiente', 'Recuerda pagar los 294€ de Seguridad Social antes del día 20.', 'warning', 0),
(2,'Objetivo al 36%',   '"Colchón autónoma" va por el 36%. Mantén el ritmo de ahorro.',   'info',    1);


-- ══════════════════════════════════════════════════════════
--  SIMULACIONES PRÉSTAMO – USUARIO 1 (historial)
-- ══════════════════════════════════════════════════════════
INSERT INTO simulaciones_prestamo
    (usuario_id, tipo_prestamo, capital, plazo_meses, tin, cuota_mensual, total_pagar, total_intereses)
VALUES
(1,'Personal',  10000.00, 36,  6.50, 307.07, 11054.52, 1054.52),
(1,'Coche',     15000.00, 60,  5.50, 286.86, 17211.60, 2211.60),
(1,'Hipoteca', 150000.00,300,  3.00, 711.74,213522.00,63522.00);
