package com.finanzapp.dao;

import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

/**
 * Acceso a datos de la tabla {@code usuarios} de Fox Wallet.
 * <p>
 * Gestiona el ciclo de vida completo de la cuenta del usuario:
 * autenticación con BCrypt, registro, actualización de perfil financiero
 * y cambio seguro de contraseña.
 */
public class UsuarioDAO {

    /**
     * Autentica a un usuario en Fox Wallet verificando su email y contraseña.
     * La comprobación de la contraseña se realiza con BCrypt para no exponer
     * el hash almacenado.
     *
     * @param email          email introducido en el formulario de login
     * @param passwordPlano  contraseña en texto plano introducida por el usuario
     * @return el {@link Usuario} autenticado, o {@code null} si las credenciales son incorrectas
     */
    public Usuario autenticarUsuario(String email, String passwordPlano) {
        String consultaUsuarioPorEmail = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaUsuarioPorEmail)) {
            stmt.setString(1, email);
            ResultSet resultados = stmt.executeQuery();
            if (resultados.next()) {
                String hashAlmacenado = resultados.getString("password_hash");
                if (org.mindrot.jbcrypt.BCrypt.checkpw(passwordPlano, hashAlmacenado)) {
                    return mapearUsuario(resultados);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Registra una nueva cuenta de usuario en Fox Wallet.
     * La contraseña se hashea con BCrypt (coste 12) antes de persistirla.
     * Lanza {@link RuntimeException}({@code "EMAIL_DUPLICADO"}) si el email ya existe.
     *
     * @param usuario       objeto con los datos del nuevo usuario
     * @param passwordPlano contraseña en texto plano que se hasheará
     * @return {@code true} si el registro fue exitoso
     */
    public boolean registrarNuevoUsuario(Usuario usuario, String passwordPlano) {
        String hashContraseña = org.mindrot.jbcrypt.BCrypt.hashpw(
                passwordPlano, org.mindrot.jbcrypt.BCrypt.gensalt(12));
        String insertarUsuario =
                "INSERT INTO usuarios " +
                "(nombre, email, password_hash, telefono, fecha_nacimiento, comunidad) " +
                "VALUES (?,?,?,?,?,?)";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(insertarUsuario, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, hashContraseña);
            stmt.setString(4, usuario.getTelefono());
            stmt.setObject(5, usuario.getFechaNacimiento());
            stmt.setString(6, usuario.getComunidad() != null ? usuario.getComunidad() : "Madrid");
            int filasInsertadas = stmt.executeUpdate();
            if (filasInsertadas > 0) {
                ResultSet idGenerado = stmt.getGeneratedKeys();
                if (idGenerado.next()) usuario.setId(idGenerado.getInt(1));
                return true;
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("EMAIL_DUPLICADO");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Actualiza los datos personales y financieros del perfil del usuario.
     * Se llama desde {@code PerfilController} cuando el usuario guarda cambios.
     *
     * @param usuario objeto con los nuevos datos del perfil ya asignados
     * @return {@code true} si se actualizó al menos una fila
     */
    public boolean actualizarPerfilUsuario(Usuario usuario) {
        String actualizarPerfil =
                "UPDATE usuarios SET nombre=?, email=?, telefono=?, fecha_nacimiento=?, comunidad=?, " +
                "situacion_laboral=?, ingresos_netos=?, objetivo_financiero=?, presupuesto_mensual=? " +
                "WHERE id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(actualizarPerfil)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            stmt.setObject(4, usuario.getFechaNacimiento());
            stmt.setString(5, usuario.getComunidad());
            stmt.setString(6, usuario.getSituacionLaboral());
            stmt.setDouble(7, usuario.getIngresosNetos());
            stmt.setString(8, usuario.getObjetivoFinanciero());
            stmt.setDouble(9, usuario.getPresupuestoMensual());
            stmt.setInt(10, usuario.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cambia la contraseña del usuario verificando primero la contraseña actual.
     * Garantiza que solo el propio usuario pueda modificar sus credenciales.
     *
     * @param usuarioId    id del usuario en sesión
     * @param actualPlano  contraseña actual en texto plano para verificación
     * @param nuevaPlano   nueva contraseña en texto plano que se hasheará
     * @return {@code true} si el cambio fue exitoso; {@code false} si la contraseña actual es incorrecta
     */
    public boolean cambiarContrasena(int usuarioId, String actualPlano, String nuevaPlano) {
        String consultaHashActual = "SELECT password_hash FROM usuarios WHERE id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaHashActual)) {
            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();
            if (resultados.next()) {
                String hashActual = resultados.getString("password_hash");
                if (!org.mindrot.jbcrypt.BCrypt.checkpw(actualPlano, hashActual)) return false;
                String nuevoHash = org.mindrot.jbcrypt.BCrypt.hashpw(nuevaPlano, org.mindrot.jbcrypt.BCrypt.gensalt(12));
                String actualizarHash = "UPDATE usuarios SET password_hash=? WHERE id=?";
                try (PreparedStatement stmtUpdate = conexion.prepareStatement(actualizarHash)) {
                    stmtUpdate.setString(1, nuevoHash);
                    stmtUpdate.setInt(2, usuarioId);
                    return stmtUpdate.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Persiste la preferencia de tema visual (claro/oscuro) del usuario.
     * Se llama desde {@code AjustesController} al seleccionar un tema.
     *
     * @param usuarioId id del usuario en sesión
     * @param tema      {@code "claro"} u {@code "oscuro"}
     */
    public void guardarPreferenciaTema(int usuarioId, String tema) {
        String actualizarTema = "UPDATE usuarios SET tema=? WHERE id=?";
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(actualizarTema)) {
            stmt.setString(1, tema);
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Construye un objeto {@link Usuario} a partir de la fila actual del {@link ResultSet}. */
    private Usuario mapearUsuario(ResultSet resultados) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(resultados.getInt("id"));
        usuario.setNombre(resultados.getString("nombre"));
        usuario.setEmail(resultados.getString("email"));
        usuario.setPasswordHash(resultados.getString("password_hash"));
        usuario.setTelefono(resultados.getString("telefono"));
        Date fechaNacimientoSQL = resultados.getDate("fecha_nacimiento");
        usuario.setFechaNacimiento(fechaNacimientoSQL != null ? fechaNacimientoSQL.toLocalDate() : null);
        usuario.setComunidad(resultados.getString("comunidad"));
        usuario.setSituacionLaboral(resultados.getString("situacion_laboral"));
        usuario.setIngresosNetos(resultados.getDouble("ingresos_netos"));
        usuario.setObjetivoFinanciero(resultados.getString("objetivo_financiero"));
        usuario.setPresupuestoMensual(resultados.getDouble("presupuesto_mensual"));
        usuario.setTema(resultados.getString("tema"));
        usuario.setMoneda(resultados.getString("moneda"));
        usuario.setIdioma(resultados.getString("idioma"));
        return usuario;
    }
}
