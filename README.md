# FinanzApp · JavaFX + Maven + MySQL

Aplicación de finanzas personales desarrollada en JavaFX 21 con Maven y base de datos MySQL.

---

## Requisitos previos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java JDK    | 17 (recomendado 21) |
| Maven       | 3.8+           |
| MySQL       | 8.0+           |

---

## 1 · Configurar la base de datos

```sql
-- En tu cliente MySQL (MySQL Workbench, DBeaver, terminal…)
mysql -u root -p < schema.sql
```

El script `schema.sql` crea la base de datos `finanzapp`, todas las tablas
y carga datos de demostración (usuario de prueba incluido).

**Usuario demo:**
- Email: `oscar@email.com`
- Contraseña: `Demo1234!`

---

## 2 · Configurar la conexión

Edita el fichero:
```
src/main/java/com/finanzapp/util/DatabaseConnection.java
```

Cambia las constantes al inicio:
```java
private static final String HOST     = "localhost";
private static final String PORT     = "3306";
private static final String DATABASE = "finanzapp";
private static final String USER     = "root";
private static final String PASSWORD = "TU_CONTRASEÑA";
```

---

## 3 · Ejecutar la aplicación

```bash
# Desde la raíz del proyecto:
mvn clean javafx:run
```

O compila y genera el JAR:
```bash
mvn clean package
java --module-path $PATH_TO_JAVAFX_SDK/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/finanzapp-1.0.0.jar
```

---

## Estructura del proyecto

```
finanzapp/
├── schema.sql                          ← Script SQL (crear primero)
├── pom.xml                             ← Configuración Maven
└── src/main/
    ├── java/com/finanzapp/
    │   ├── MainApp.java                ← Punto de entrada
    │   ├── controller/                 ← Controladores de cada vista
    │   │   ├── LoginController.java
    │   │   ├── MainController.java     ← Navegación principal
    │   │   ├── DashboardController.java
    │   │   ├── GastosController.java
    │   │   ├── SimuladorController.java
    │   │   ├── PrestamosController.java
    │   │   ├── ObjetivosController.java
    │   │   ├── HistorialController.java
    │   │   ├── RentaController.java
    │   │   ├── NotificacionesController.java
    │   │   ├── PerfilController.java
    │   │   └── AjustesController.java
    │   ├── model/                      ← Entidades / POJOs
    │   │   ├── Usuario.java
    │   │   ├── Movimiento.java
    │   │   ├── Objetivo.java
    │   │   ├── Habito.java
    │   │   ├── Categoria.java
    │   │   └── Notificacion.java
    │   ├── dao/                        ← Acceso a datos (JDBC puro)
    │   │   ├── UsuarioDAO.java
    │   │   ├── MovimientoDAO.java
    │   │   ├── ObjetivoDAO.java
    │   │   ├── HabitoDAO.java
    │   │   ├── CategoriaDAO.java
    │   │   └── NotificacionDAO.java
    │   └── util/
    │       ├── DatabaseConnection.java ← Singleton de conexión
    │       └── Session.java            ← Usuario en sesión
    └── resources/com/finanzapp/
        ├── fxml/                       ← Vistas FXML
        │   ├── login.fxml
        │   ├── main.fxml
        │   ├── dashboard.fxml
        │   ├── gastos.fxml
        │   ├── simulador.fxml
        │   ├── prestamos.fxml
        │   ├── objetivos.fxml
        │   ├── historial.fxml
        │   ├── renta.fxml
        │   ├── notificaciones.fxml
        │   ├── perfil.fxml
        │   └── ajustes.fxml
        └── css/
            └── styles.css              ← Estilos globales
```

---

## Funcionalidades implementadas

| Módulo | Descripción |
|--------|-------------|
| 🔑 **Login / Registro** | Autenticación con BCrypt (contraseñas hasheadas) |
| 📊 **Dashboard** | Resumen con gráfico de dona y barras, últimos movimientos, objetivos |
| 💳 **Ingresos/Gastos** | CRUD de movimientos con filtros y estadísticas en tiempo real |
| 🎯 **Objetivos** | Tarjetas de ahorro con barra de progreso y aportaciones |
| 📅 **Historial** | Listado filtrable por mes, año y tipo con búsqueda |
| 🏷 **Simulador hábitos** | Análisis de gasto en hábitos con ahorro posible mensual/anual |
| 🏦 **Simulador préstamos** | Cálculo de cuota, intereses, tabla de amortización y consejo |
| 📋 **Calculadora Renta** | Estimación IRPF con tramos estatales y autonómicos |
| 🔔 **Notificaciones** | Centro de alertas con badge de no leídas |
| 👤 **Perfil** | Edición de datos personales, cambio de contraseña, situación financiera |
| ⚙ **Ajustes** | Tema, moneda, idioma y preferencias de notificaciones |

---

## Tecnologías

- **JavaFX 21** — interfaz gráfica
- **Maven** — gestión de dependencias y build
- **MySQL 8** — base de datos relacional
- **mysql-connector-j 8.3** — driver JDBC
- **jBCrypt 0.4** — hash seguro de contraseñas

---

*Desarrollado como proyecto de 2º DAM · Óscar Cruz Vázquez*
