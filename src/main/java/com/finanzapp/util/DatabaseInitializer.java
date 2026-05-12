package com.finanzapp.util;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 Inicializa la base de datos  al arrancar la aplicación.
 Crea tablas, procedimientos almacenados y datos de prueba.
 */
public class DatabaseInitializer {

    public static void inicializarEsquemaFoxWallet(Connection conexion) {
        try {
            crearEsquemaTablas(conexion);
            verificarProcedimientosAlmacenados(conexion);

            boolean baseDeDatosVacia = tablaUsuariosVacia(conexion);

            if (baseDeDatosVacia) {
                System.out.println("[FoxWallet] BD vacía detectada → cargando datos de demostración...");
                insertarDatosDemo(conexion);
                System.out.println("[FoxWallet] Datos demo cargados correctamente.");
            } else {
                System.out.println("[FoxWallet] BD ya contiene datos; se omite la carga demo.");
            }

        } catch (SQLException e) {
            System.err.println("[FoxWallet] ERROR inicializando la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static boolean tablaUsuariosVacia(Connection conexion) {
        try (Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {

            if (rs.next()) {
                int numeroDeUsuarios = rs.getInt(1);
                boolean sinUsuarios = (numeroDeUsuarios == 0);
                return sinUsuarios;
            }
            return true;

        } catch (SQLException e) {
            return true;
        }
    }


    public static void verificarProcedimientosAlmacenados(Connection conexion) {
        try {
            crearProcedimientos(conexion);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1304) {
                e.printStackTrace();
            }
        }
    }

    private static void crearEsquemaTablas(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id                  INT AUTO_INCREMENT PRIMARY KEY,
                    nombre              VARCHAR(120)  NOT NULL,
                    email               VARCHAR(200)  NOT NULL UNIQUE,
                    password_hash       VARCHAR(200)  NOT NULL,
                    telefono            VARCHAR(30),
                    fecha_nacimiento    DATE,
                    comunidad           VARCHAR(80)   DEFAULT 'Madrid',
                    situacion_laboral   ENUM('Estudiante','Empleado','Autónomo','Funcionario','Desempleado')
                                                      DEFAULT 'Empleado',
                    ingresos_netos      DECIMAL(10,2) DEFAULT 0.00,
                    objetivo_financiero VARCHAR(80),
                    presupuesto_mensual DECIMAL(10,2) DEFAULT 0.00,
                    tema                ENUM('claro','oscuro') DEFAULT 'claro',
                    moneda              VARCHAR(10)   DEFAULT '€',
                    idioma              VARCHAR(20)   DEFAULT 'Español',
                    created_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS categorias (
                    id      INT AUTO_INCREMENT PRIMARY KEY,
                    nombre  VARCHAR(80)  NOT NULL,
                    emoji   VARCHAR(10)  NOT NULL,
                    tipo    ENUM('gasto','ingreso','ambos') DEFAULT 'ambos',
                    color   VARCHAR(20)  DEFAULT '#6B6A65'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS movimientos (
                    id           INT AUTO_INCREMENT PRIMARY KEY,
                    usuario_id   INT  NOT NULL,
                    tipo         ENUM('gasto','ingreso') NOT NULL,
                    nombre       VARCHAR(200) NOT NULL,
                    cantidad     DECIMAL(10,2) NOT NULL,
                    categoria_id INT,
                    notas        TEXT,
                    fecha        DATE NOT NULL,
                    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (usuario_id)   REFERENCES usuarios(id)   ON DELETE CASCADE,
                    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS objetivos (
                    id           INT AUTO_INCREMENT PRIMARY KEY,
                    usuario_id   INT NOT NULL,
                    nombre       VARCHAR(200)  NOT NULL,
                    objetivo     DECIMAL(10,2) NOT NULL,
                    actual       DECIMAL(10,2) DEFAULT 0.00,
                    emoji        VARCHAR(10)   DEFAULT '🎯',
                    fecha_limite DATE,
                    completado   TINYINT(1)    DEFAULT 0,
                    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
               ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS habitos (
                    id                INT AUTO_INCREMENT PRIMARY KEY,
                    usuario_id        INT  NOT NULL,
                    emoji             VARCHAR(10)  NOT NULL,
                    nombre            VARCHAR(200) NOT NULL,
                    frecuencia_actual INT          DEFAULT 0,
                    frecuencia_obj    INT          DEFAULT 0,
                    unidad            ENUM('semana','mes') DEFAULT 'semana',
                    coste             DECIMAL(8,2) NOT NULL,
                    descripcion       VARCHAR(100),
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS notificaciones (
                    id         INT AUTO_INCREMENT PRIMARY KEY,
                    usuario_id INT NOT NULL,
                    titulo     VARCHAR(200) NOT NULL,
                    mensaje    TEXT,
                    tipo       ENUM('info','success','warning','danger') DEFAULT 'info',
                    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        }
    }

    private static void crearProcedimientos(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {

            st.execute(
                "CREATE PROCEDURE IF NOT EXISTS actualizarProgresoObjetivo(" +
                "    IN p_id           INT," +
                "    IN p_nuevo_actual DECIMAL(10,2)" +
                ") " +
                "BEGIN " +
                "    UPDATE objetivos " +
                "    SET actual     = p_nuevo_actual, " +
                "        completado = IF(p_nuevo_actual >= objetivo, 1, 0) " +
                "    WHERE id = p_id; " +
                "END"
            );

            st.execute(
                "CREATE PROCEDURE IF NOT EXISTS obtenerEstadisticasMes(" +
                "    IN p_uid  INT," +
                "    IN p_anio INT," +
                "    IN p_mes  INT" +
                ") " +
                "BEGIN " +
                "    SELECT " +
                "        IFNULL(SUM(CASE WHEN tipo = 'ingreso' THEN cantidad ELSE 0 END), 0) AS total_ingresos, " +
                "        IFNULL(SUM(CASE WHEN tipo = 'gasto'   THEN cantidad ELSE 0 END), 0) AS total_gastos " +
                "    FROM movimientos " +
                "    WHERE usuario_id = p_uid " +
                "      AND YEAR(fecha)  = p_anio " +
                "      AND MONTH(fecha) = p_mes; " +
                "END"
            );
        }
    }

    private static void insertarDatosDemo(Connection c) throws SQLException {
        insertCategorias(c);
        insertUsuarios(c);
        insertMovimientos(c);
        insertObjetivos(c);
        insertHabitos(c);
        insertNotificaciones(c);
    }

    private static void insertCategorias(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute("""
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
                ('Otro',             '💳', 'ambos',   '#A09F9B')
                """);
        }
    }

    private static void insertUsuarios(Connection c) throws SQLException {
        String passwordDemo = "Demo1234!";
        int costeFactor = 12;
        String sal = BCrypt.gensalt(costeFactor);
        String hash = BCrypt.hashpw(passwordDemo, sal);
        String sql  = """
            INSERT INTO usuarios
                (nombre, email, password_hash, telefono, fecha_nacimiento, comunidad,
                 situacion_laboral, ingresos_netos, objetivo_financiero, presupuesto_mensual)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "Óscar Cruz Vázquez");
            ps.setString(2, "oscar@email.com");
            ps.setString(3, hash);
            ps.setString(4, "+34 612 345 678");
            ps.setDate(5, Date.valueOf("1999-05-14"));
            ps.setString(6, "Madrid");
            ps.setString(7, "Empleado");
            ps.setDouble(8, 1400.00);
            ps.setString(9, "Controlar mis gastos");
            ps.setDouble(10, 1100.00);
            ps.executeUpdate();

            ps.setString(1, "Laura Martínez Roca");
            ps.setString(2, "laura@email.com");
            ps.setString(3, hash);
            ps.setString(4, "+34 633 210 987");
            ps.setDate(5, Date.valueOf("1995-11-22"));
            ps.setString(6, "Cataluña");
            ps.setString(7, "Autónomo");
            ps.setDouble(8, 2200.00);
            ps.setString(9, "Iniciarme en inversión");
            ps.setDouble(10, 1600.00);
            ps.executeUpdate();
        }
    }

    private static void insertMovimientos(Connection c) throws SQLException {
        String sql = "INSERT INTO movimientos (usuario_id, tipo, nombre, cantidad, categoria_id, notas, fecha) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {

            mov(ps,1,"ingreso","Nómina",                   1400.00, 7,"Nómina mensual bruta",             -5, 1);
            mov(ps,1,"gasto", "Alquiler",                   450.00, 1,"Piso compartido Lavapiés",         -5, 2);
            mov(ps,1,"gasto", "Luz y gas",                   68.50, 5,"Endesa — bimestral",               -5, 3);
            mov(ps,1,"gasto", "Mercadona",                   92.30, 2,"Compra semanal",                   -5, 5);
            mov(ps,1,"gasto", "Abono transporte",            54.60, 3,"Zona A Madrid",                    -5, 6);
            mov(ps,1,"gasto", "Netflix",                     13.99, 5, null,                              -5, 7);
            mov(ps,1,"gasto", "Spotify",                     10.99, 5, null,                              -5, 7);
            mov(ps,1,"gasto", "Supermercado Lidl",           47.80, 2,"Compra segunda semana",            -5,12);
            mov(ps,1,"gasto", "Cena cumpleaños Ana",         38.00, 4,"Restaurante La Pepita",            -5,14);
            mov(ps,1,"gasto", "Farmacia",                    21.40, 8,"Antihistamínico + vitaminas",      -5,16);
            mov(ps,1,"gasto", "Amazon — auriculares",        35.99, 6,"Auriculares inalámbricos básicos", -5,18);
            mov(ps,1,"gasto", "Mercadona",                   88.50, 2,"Compra tercera semana",            -5,19);
            mov(ps,1,"gasto", "Cine + palomitas",            19.50, 4,"Película con amigos",              -5,21);
            mov(ps,1,"gasto", "Gasolina coche compartido",   28.00, 3,"Viaje fin de semana",              -5,24);
            mov(ps,1,"gasto", "Mercadona",                   65.20, 2,"Compra cuarta semana",             -5,26);
            mov(ps,1,"gasto", "Gym",                         40.00, 6,"Mensualidad gym Holmes Place",     -5,28);
            mov(ps,1,"ingreso","Transferencia amigo",        50.00, 7,"Me devuelve cena de agosto",       -5,29);

            mov(ps,1,"ingreso","Nómina",                   1400.00, 7, null,                              -4, 1);
            mov(ps,1,"gasto", "Alquiler",                   450.00, 1, null,                              -4, 2);
            mov(ps,1,"gasto", "Mercadona",                   79.60, 2, null,                              -4, 4);
            mov(ps,1,"gasto", "Abono transporte",            54.60, 3, null,                              -4, 5);
            mov(ps,1,"gasto", "Netflix",                     13.99, 5, null,                              -4, 7);
            mov(ps,1,"gasto", "Spotify",                     10.99, 5, null,                              -4, 7);
            mov(ps,1,"gasto", "Clases inglés",               60.00,10,"Preparación B2 — academia",        -4, 8);
            mov(ps,1,"gasto", "Lidl",                        53.10, 2, null,                              -4,11);
            mov(ps,1,"gasto", "Bar con compañeros",          27.50, 4,"Afterwork viernes",                -4,15);
            mov(ps,1,"gasto", "Ropa El Corte Inglés",        89.95, 9,"Camisa + pantalón Black Friday",   -4,24);
            mov(ps,1,"gasto", "Zapatillas Nike",             75.00, 9,"Nike Air Max outlet",              -4,24);
            mov(ps,1,"gasto", "Mercadona",                   91.30, 2, null,                              -4,25);
            mov(ps,1,"gasto", "Gym",                         40.00, 6, null,                              -4,28);
            mov(ps,1,"gasto", "Cena con amigos",             45.00, 4,"Restaurante americano",            -4,28);
            mov(ps,1,"gasto", "Seguro móvil",                 8.99, 5, null,                              -4,28);

            mov(ps,1,"ingreso","Nómina",                   1400.00, 7, null,                              -3, 1);
            mov(ps,1,"ingreso","Paga extra",                700.00, 7,"Media paga extra convenio",        -3, 1);
            mov(ps,1,"gasto", "Alquiler",                   450.00, 1, null,                              -3, 2);
            mov(ps,1,"gasto", "Netflix",                     13.99, 5, null,                              -3, 7);
            mov(ps,1,"gasto", "Spotify",                     10.99, 5, null,                              -3, 7);
            mov(ps,1,"gasto", "Abono transporte",            54.60, 3, null,                              -3, 5);
            mov(ps,1,"gasto", "Regalos Navidad",            180.00, 9,"Familia + amigos cercanos",        -3,10);
            mov(ps,1,"gasto", "Plataforma HBO Max",           9.99, 5,"Promo 3 meses",                    -3,15);
            mov(ps,1,"gasto", "Mercadona",                  105.40, 2,"Compra especial Navidad",          -3,20);
            mov(ps,1,"gasto", "Vuelo Madrid-Málaga",         89.00,11,"Visita familia en vacaciones",     -3,22);
            mov(ps,1,"gasto", "Hotel Málaga 3 noches",      135.00,11, null,                              -3,22);
            mov(ps,1,"gasto", "Cena Nochebuena",             65.00, 4,"Cena familiar restaurante",        -3,24);
            mov(ps,1,"ingreso","Transferencia navidad",      200.00, 7,"Regalo abuelos",                  -3,25);
            mov(ps,1,"gasto", "Gym",                         40.00, 6, null,                              -3,28);
            mov(ps,1,"gasto", "Cotillón Nochevieja",         55.00, 4,"Fiesta fin de año",                -3,31);

            mov(ps,1,"ingreso","Nómina",                   1400.00, 7, null,                              -2, 1);
            mov(ps,1,"gasto", "Alquiler",                   450.00, 1, null,                              -2, 2);
            mov(ps,1,"gasto", "Netflix",                     13.99, 5, null,                              -2, 7);
            mov(ps,1,"gasto", "Spotify",                     10.99, 5, null,                              -2, 7);
            mov(ps,1,"gasto", "Abono transporte",            54.60, 3, null,                              -2, 6);
            mov(ps,1,"gasto", "Mercadona",                   83.70, 2, null,                              -2, 5);
            mov(ps,1,"gasto", "Reyes — Nintendo Switch",    289.00, 6,"Me la regalo a mí mismo",          -2, 6);
            mov(ps,1,"gasto", "Juegos Switch",               49.99, 4,"Mario Kart 8 + Zelda",             -2, 8);
            mov(ps,1,"gasto", "Clases inglés",               60.00,10, null,                              -2, 9);
            mov(ps,1,"gasto", "Supermercado Carrefour",      61.20, 2, null,                              -2,14);
            mov(ps,1,"gasto", "Dentista revisión",           45.00, 8,"Revisión anual + limpieza",        -2,17);
            mov(ps,1,"gasto", "Bar tapas fin de semana",     22.00, 4, null,                              -2,19);
            mov(ps,1,"gasto", "Mercadona",                   77.30, 2, null,                              -2,21);
            mov(ps,1,"gasto", "Suscripción ChatGPT",         22.00, 5,"Plus mensual",                     -2,15);
            mov(ps,1,"gasto", "Gym",                         40.00, 6, null,                              -2,28);

            mov(ps,1,"ingreso","Nómina",                   1400.00, 7, null,                              -1, 1);
            mov(ps,1,"gasto", "Alquiler",                   450.00, 1, null,                              -1, 2);
            mov(ps,1,"gasto", "Netflix",                     13.99, 5, null,                              -1, 7);
            mov(ps,1,"gasto", "Spotify",                     10.99, 5, null,                              -1, 7);
            mov(ps,1,"gasto", "Abono transporte",            54.60, 3, null,                              -1, 5);
            mov(ps,1,"gasto", "Mercadona",                   86.40, 2, null,                              -1, 4);
            mov(ps,1,"gasto", "San Valentín — cena",         72.00, 4,"Restaurante con pareja",           -1,14);
            mov(ps,1,"gasto", "Roses + regalo",              35.00, 9,"Detalles San Valentín",            -1,14);
            mov(ps,1,"gasto", "Clases inglés",               60.00,10, null,                              -1,10);
            mov(ps,1,"gasto", "Lidl semanal",                49.80, 2, null,                              -1,11);
            mov(ps,1,"gasto", "Camisetas Zara",              42.00, 9,"3x2 rebajas",                      -1,18);
            mov(ps,1,"gasto", "Mercadona",                   94.10, 2, null,                              -1,22);
            mov(ps,1,"gasto", "Amazon — libro técnico",      18.99,10,"Clean Code — R. Martin",           -1,20);
            mov(ps,1,"gasto", "Gym",                         40.00, 6, null,                              -1,28);
            mov(ps,1,"gasto", "Suscripción ChatGPT",         22.00, 5, null,                              -1,15);
            mov(ps,1,"gasto", "Taxi noche sábado",           14.50, 3, null,                              -1,22);
            mov(ps,1,"ingreso","Venta objeto segunda mano",  80.00, 7,"Xbox vendida en Wallapop",         -1,25);

            mov(ps,1,"ingreso","Nómina",                   1400.00, 7, null,                               0, 1);
            mov(ps,1,"gasto", "Alquiler",                   450.00, 1, null,                               0, 2);
            mov(ps,1,"gasto", "Netflix",                     13.99, 5, null,                               0, 7);
            mov(ps,1,"gasto", "Spotify",                     10.99, 5, null,                               0, 7);
            mov(ps,1,"gasto", "Abono transporte",            54.60, 3, null,                               0, 5);
            mov(ps,1,"gasto", "Mercadona",                   91.20, 2,"Compra grande mensual",             0, 3);
            mov(ps,1,"gasto", "Suscripción ChatGPT",         22.00, 5, null,                               0, 8);
            mov(ps,1,"gasto", "Gasolina viaje",              48.00, 3,"Viaje fin de semana Toledo",        0, 9);
            mov(ps,1,"gasto", "Restaurante Toledo",          33.50, 4,"Menú turístico x2",                 0, 9);
            mov(ps,1,"gasto", "Clases inglés",               60.00,10, null,                               0,11);
            mov(ps,1,"gasto", "Mercadona",                   78.30, 2, null,                               0,13);
            mov(ps,1,"gasto", "Gym",                         40.00, 6, null,                               0,10);
            mov(ps,1,"gasto", "Farmacia — proteína",         34.99, 8,"Suplemento deportivo",              0,12);
            mov(ps,1,"gasto", "Concierto Vetusta Morla",     52.00, 4,"Entradas WiZink Center",            0,14);
            mov(ps,1,"gasto", "Cena preconcierto",           28.00, 4, null,                               0,14);
            mov(ps,1,"ingreso","Freelance diseño logo",     250.00,12,"Logo para amigo emprendedor",       0,16);
            mov(ps,1,"gasto", "Supermercado Carrefour",      55.40, 2, null,                               0,18);
            mov(ps,1,"gasto", "Libro — Padre Rico",          13.99,10,"Padre Rico Padre Pobre",            0,19);
            mov(ps,1,"gasto", "Pago reparación bici",        65.00, 6,"Cambio cámara + frenos",            0,20);
            mov(ps,1,"gasto", "Bar fin de semana",           19.50, 4, null,                               0,22);

            mov(ps,2,"ingreso","Factura cliente A",        1800.00,12,"Diseño web corporativo",            -1, 1);
            mov(ps,2,"ingreso","Factura cliente B",         900.00,12,"Mantenimiento mensual",             -1, 5);
            mov(ps,2,"gasto", "Alquiler oficina",           350.00, 1,"Coworking Barcelona centro",        -1, 2);
            mov(ps,2,"gasto", "Seguridad Social autónomo",  294.00, 6,"Cuota mensual SS",                  -1,20);
            mov(ps,2,"gasto", "Adobe Creative Cloud",        54.99, 5,"Licencia anual / 12",               -1, 7);
            mov(ps,2,"gasto", "Supermercado",               110.00, 2, null,                               -1, 8);
            mov(ps,2,"gasto", "Transporte público",          45.00, 3,"T-Usual metro Barcelona",           -1, 6);
            mov(ps,2,"ingreso","Factura mes actual",       2200.00, 7,"Transferencia mes en curso",         0, 1);
            mov(ps,2,"gasto", "Alquiler piso",              780.00, 1,"Piso Gracia, Barcelona",             0, 3);
            mov(ps,2,"gasto", "Gym + pilates",               55.00, 6, null,                                0, 5);
        }
    }

    private static void insertObjetivos(Connection c) throws SQLException {
        LocalDate hoy = LocalDate.now();
        String sql = "INSERT INTO objetivos (usuario_id, nombre, objetivo, actual, emoji, fecha_limite, completado) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            obj(ps,1,"Viaje a Japón",            3000.00, 1860.00,"🗾", hoy.plusMonths(8),  0);
            obj(ps,1,"Fondo de emergencia",       3000.00,  950.00,"🛡️", hoy.plusMonths(18), 0);
            obj(ps,1,"Portátil nuevo",             900.00,  620.00,"💻", hoy.plusMonths(3),  0);
            obj(ps,1,"Matrícula máster online",   1200.00,  200.00,"🎓", hoy.plusMonths(10), 0);
            obj(ps,1,"Bicicleta de carretera",     800.00,  800.00,"🚴", null,               1);
            obj(ps,1,"Curso de inglés B2",         400.00,  400.00,"🇬🇧", null,              1);
            obj(ps,2,"Colchón autónoma (6 meses)",13200.00,4800.00,"🏦", hoy.plusMonths(14), 0);
            obj(ps,2,"Viaje Estados Unidos",       4500.00, 1200.00,"🗽", hoy.plusMonths(12), 0);
            obj(ps,2,"Cámara mirrorless Sony A7",  2200.00, 2200.00,"📷", null,              1);
        }
    }

    private static void insertHabitos(Connection c) throws SQLException {
        String sql = "INSERT INTO habitos (usuario_id, emoji, nombre, frecuencia_actual, frecuencia_obj, unidad, coste, descripcion) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            hab(ps,1,"☕","Café fuera de casa",         5, 2,"semana",  2.50,"por café");
            hab(ps,1,"🍕","Comida a domicilio",          3, 1,"semana", 12.00,"por pedido");
            hab(ps,1,"🚗","Taxi / Uber",                 3, 1,"semana",  9.00,"por viaje");
            hab(ps,1,"🎮","Suscripciones digitales",    73,40,"mes",     1.00,"€/mes");
            hab(ps,1,"🍺","Salidas nocturnas",           2, 1,"semana", 22.00,"por salida");
            hab(ps,1,"🍔","Comida rápida",               4, 2,"semana",  7.50,"por comida");
            hab(ps,1,"🛒","Compras impulsivas online",   3, 1,"mes",    35.00,"por compra");
            hab(ps,1,"🚬","Tabaco",                      0, 0,"semana",  5.50,"por paquete");
            hab(ps,2,"☕","Café en coworking",           5, 3,"semana",  3.00,"por café");
            hab(ps,2,"🍱","Delivery comida",             4, 2,"semana", 14.00,"por pedido");
            hab(ps,2,"🛍","Ropa y accesorios",           2, 1,"mes",    65.00,"por compra");
        }
    }

    private static void insertNotificaciones(Connection c) throws SQLException {
        String sql = "INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo) VALUES (?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            noti(ps,1,"¡Presupuesto casi al límite!",
                    "Llevas 982€ gastados de tu límite mensual de 1.100€. Quedan solo 118€.",
                    "warning");
            noti(ps,1,"Nómina recibida ✓",
                    "Se ha registrado un ingreso de 1.400€. ¡Buen comienzo de mes!",
                    "success");
            noti(ps,1,"Objetivo \"Viaje a Japón\" al 62%",
                    "Te faltan 1.140€ para tu meta. Con tu ritmo actual llegarás en agosto.",
                    "info");
            noti(ps,1,"¡Objetivo completado! 🎉",
                    "Has alcanzado tu objetivo \"Curso de inglés B2\". ¡Enhorabuena!",
                    "success");
            noti(ps,1,"Gasto inusual detectado",
                    "Has gastado 289€ en un solo movimiento (Nintendo Switch). ¿Todo correcto?",
                    "warning");
            noti(ps,1,"Ingreso extra registrado",
                    "Has recibido 250€ por trabajo freelance. ¡Considera destinarlo a tus objetivos!",
                    "success");
            noti(ps,1,"Hábito con alto impacto",
                    "Tus suscripciones digitales suman 73€/mes. Podrías ahorrar 33€ reduciendo a tu objetivo.",
                    "info");
            noti(ps,1,"Nuevo mes, nuevo presupuesto",
                    "Comienza el mes. Tu presupuesto se ha reiniciado a 1.100€. ¡Suerte!",
                    "info");
            noti(ps,2,"Factura cobrada ✓",
                    "El cliente A ha pagado 1.800€. Balance mensual positivo.",
                    "success");
            noti(ps,2,"Cuota SS pendiente",
                    "Recuerda pagar los 294€ de Seguridad Social antes del día 20.",
                    "warning");
            noti(ps,2,"Objetivo al 36%",
                    "\"Colchón autónoma\" va por el 36%. Mantén el ritmo de ahorro.",
                    "info");
        }
    }

    private static void mov(PreparedStatement ps, int uid, String tipo, String nombre,
                             double cantidad, int catId, String notas,
                             int mesesAtras, int dia) throws SQLException {
        ps.setInt(1, uid);
        ps.setString(2, tipo);
        ps.setString(3, nombre);
        ps.setDouble(4, cantidad);

        if (catId > 0) {
            ps.setInt(5, catId);
        } else {
            ps.setNull(5, Types.INTEGER);
        }

        ps.setString(6, notas);
        LocalDate fechaMovimiento = safeDate(mesesAtras, dia);
        Date fechaSQL = Date.valueOf(fechaMovimiento);
        ps.setDate(7, fechaSQL);
        ps.executeUpdate();
    }

    private static void obj(PreparedStatement ps, int uid, String nombre,
                             double objetivo, double actual, String emoji,
                             LocalDate limite, int completado) throws SQLException {
        ps.setInt(1, uid);
        ps.setString(2, nombre);
        ps.setDouble(3, objetivo);
        ps.setDouble(4, actual);
        ps.setString(5, emoji);

        if (limite != null) {
            ps.setDate(6, Date.valueOf(limite));
        } else {
            ps.setNull(6, Types.DATE);
        }

        ps.setInt(7, completado);
        ps.executeUpdate();
    }

    private static void hab(PreparedStatement ps, int uid, String emoji, String nombre,
                             int frecActual, int frecObj, String unidad,
                             double coste, String desc) throws SQLException {
        ps.setInt(1, uid);
        ps.setString(2, emoji);
        ps.setString(3, nombre);
        ps.setInt(4, frecActual);
        ps.setInt(5, frecObj);
        ps.setString(6, unidad);
        ps.setDouble(7, coste);
        ps.setString(8, desc);
        ps.executeUpdate();
    }

    private static void noti(PreparedStatement ps, int uid, String titulo,
                              String mensaje, String tipo) throws SQLException {
        ps.setInt(1, uid);
        ps.setString(2, titulo);
        ps.setString(3, mensaje);
        ps.setString(4, tipo);
        ps.executeUpdate();
    }

    private static LocalDate safeDate(int mesesAtras, int dia) {
        LocalDate hoyParaFecha = LocalDate.now();
        LocalDate mesMenosN = hoyParaFecha.minusMonths(mesesAtras);
        LocalDate primerDiaMes = mesMenosN.with(TemporalAdjusters.firstDayOfMonth());

        LocalDate ultimoDiaDelMes = primerDiaMes.with(TemporalAdjusters.lastDayOfMonth());

        int diaFinal;
        if (dia > ultimoDiaDelMes.getDayOfMonth()) {
            diaFinal = ultimoDiaDelMes.getDayOfMonth();
        } else {
            diaFinal = dia;
        }

        return primerDiaMes.withDayOfMonth(diaFinal);
    }
}
