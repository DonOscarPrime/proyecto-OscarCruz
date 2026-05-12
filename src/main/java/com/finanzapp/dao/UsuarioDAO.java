package com.finanzapp.dao;

import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

/**
 * Gestiona la información y configuración de las cuentas de usuario.
 */
public class UsuarioDAO {


    public Usuario autenticarUsuario(String email, String passwordBase) {
        String consultaPorEmail = "SELECT * FROM usuarios WHERE email = ?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultaPorEmail)) {

            stmt.setString(1, email);
            ResultSet filas = stmt.executeQuery();

            if (filas.next()) {
                String hashAlmacenado = filas.getString("password_hash");
                boolean contrasenaCorrecta = org.mindrot.jbcrypt.BCrypt.checkpw(passwordBase, hashAlmacenado);

                if (contrasenaCorrecta) {
                    return mapearUsuario(filas);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }


    public boolean registrarUsuario(Usuario usuario, String passwordBase) {
        String sal = org.mindrot.jbcrypt.BCrypt.gensalt(12);
        String nuevoHash = org.mindrot.jbcrypt.BCrypt.hashpw(passwordBase, sal);

        String insertar =
                "INSERT INTO usuarios " +
                "(nombre, email, password_hash, telefono, fecha_nacimiento, comunidad) " +
                "VALUES (?,?,?,?,?,?)";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, nuevoHash);
            stmt.setString(4, usuario.getTelefono());
            stmt.setObject(5, usuario.getFechaNacimiento());

            String comunidad = usuario.getComunidad();
            if (comunidad != null) {
                stmt.setString(6, comunidad);
            } else {
                stmt.setString(6, "Madrid");
            }

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet idGenerado = stmt.getGeneratedKeys();
                if (idGenerado.next()) {
                    usuario.setId(idGenerado.getInt(1));
                }
                return true;
            }

        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
            throw new RuntimeException("EMAIL_DUPLICADO");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean actualizarPerfil(Usuario usuario) {
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

            int rowsAct = stmt.executeUpdate();
            boolean actualizacionExitosa = rowsAct > 0;
            return actualizacionExitosa;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public boolean cambiarContrasena(int usuarioId, String actualBase, String nuevoBase) {
        String consultarHash = "SELECT password_hash FROM usuarios WHERE id=?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(consultarHash)) {

            stmt.setInt(1, usuarioId);
            ResultSet resultados = stmt.executeQuery();

            if (resultados.next()) {
                String hashActual = resultados.getString("password_hash");
                boolean contrasenaCorrecta = org.mindrot.jbcrypt.BCrypt.checkpw(actualBase, hashActual);

                if (!contrasenaCorrecta) {
                    return false;
                }

                String salNueva = org.mindrot.jbcrypt.BCrypt.gensalt(12);
                String nuevoHash = org.mindrot.jbcrypt.BCrypt.hashpw(nuevoBase, salNueva);

                String actualizarHash = "UPDATE usuarios SET password_hash=? WHERE id=?";

                try (PreparedStatement update = conexion.prepareStatement(actualizarHash)) {
                    update.setString(1, nuevoHash);
                    update.setInt(2, usuarioId);

                    int rowsAct = update.executeUpdate();
                    boolean cambioExitoso = rowsAct > 0;
                    return cambioExitoso;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }


    public void guardarPreferencia(int usuarioId, String tema) {
        String actualizarTema = "UPDATE usuarios SET tema=? WHERE id=?";

        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(actualizarTema)) {

            stmt.setString(1, tema);
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private Usuario mapearUsuario(ResultSet resultados) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setId(resultados.getInt("id"));
        usuario.setNombre(resultados.getString("nombre"));
        usuario.setEmail(resultados.getString("email"));
        usuario.setPasswordHash(resultados.getString("password_hash"));
        usuario.setTelefono(resultados.getString("telefono"));

        Date fechaNacimientoSQL = resultados.getDate("fecha_nacimiento");
        if (fechaNacimientoSQL != null) {
            LocalDate fechaNacimientoLocal = fechaNacimientoSQL.toLocalDate();
            usuario.setFechaNacimiento(fechaNacimientoLocal);
        } else {
            usuario.setFechaNacimiento(null);
        }

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
