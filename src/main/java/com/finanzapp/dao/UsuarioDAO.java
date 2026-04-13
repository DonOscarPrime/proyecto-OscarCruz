package com.finanzapp.dao;

import com.finanzapp.model.Usuario;
import com.finanzapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

public class UsuarioDAO {

    /** Busca un usuario por email y comprueba la contraseña con BCrypt. */
    public Usuario login(String email, String password) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                if (org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Registra un nuevo usuario. */
    public boolean registrar(Usuario u, String passwordPlano) {
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(passwordPlano, org.mindrot.jbcrypt.BCrypt.gensalt(12));
        String sql = "INSERT INTO usuarios (nombre,email,password_hash,telefono,fecha_nacimiento,comunidad) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, hash);
            ps.setString(4, u.getTelefono());
            ps.setObject(5, u.getFechaNacimiento());
            ps.setString(6, u.getComunidad() != null ? u.getComunidad() : "Madrid");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) u.setId(keys.getInt(1));
                return true;
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("EMAIL_DUPLICADO");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Actualiza datos personales del usuario. */
    public boolean actualizarPerfil(Usuario u) {
        String sql = "UPDATE usuarios SET nombre=?, email=?, telefono=?, fecha_nacimiento=?, comunidad=?, " +
                     "situacion_laboral=?, ingresos_netos=?, objetivo_financiero=?, presupuesto_mensual=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getTelefono());
            ps.setObject(4, u.getFechaNacimiento());
            ps.setString(5, u.getComunidad());
            ps.setString(6, u.getSituacionLaboral());
            ps.setDouble(7, u.getIngresosNetos());
            ps.setString(8, u.getObjetivoFinanciero());
            ps.setDouble(9, u.getPresupuestoMensual());
            ps.setInt(10, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Cambia la contraseña del usuario tras verificar la actual. */
    public boolean cambiarPassword(int usuarioId, String actualPlano, String nuevaPlano) {
        String sqlGet = "SELECT password_hash FROM usuarios WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlGet)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                if (!org.mindrot.jbcrypt.BCrypt.checkpw(actualPlano, hash)) return false;
                String nuevoHash = org.mindrot.jbcrypt.BCrypt.hashpw(nuevaPlano, org.mindrot.jbcrypt.BCrypt.gensalt(12));
                String sqlUpd = "UPDATE usuarios SET password_hash=? WHERE id=?";
                try (PreparedStatement ps2 = c.prepareStatement(sqlUpd)) {
                    ps2.setString(1, nuevoHash);
                    ps2.setInt(2, usuarioId);
                    return ps2.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Actualiza la preferencia de tema del usuario. */
    public void guardarTema(int usuarioId, String tema) {
        String sql = "UPDATE usuarios SET tema=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tema);
            ps.setInt(2, usuarioId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setTelefono(rs.getString("telefono"));
        Date fn = rs.getDate("fecha_nacimiento");
        u.setFechaNacimiento(fn != null ? fn.toLocalDate() : null);
        u.setComunidad(rs.getString("comunidad"));
        u.setSituacionLaboral(rs.getString("situacion_laboral"));
        u.setIngresosNetos(rs.getDouble("ingresos_netos"));
        u.setObjetivoFinanciero(rs.getString("objetivo_financiero"));
        u.setPresupuestoMensual(rs.getDouble("presupuesto_mensual"));
        u.setTema(rs.getString("tema"));
        u.setMoneda(rs.getString("moneda"));
        u.setIdioma(rs.getString("idioma"));
        return u;
    }
}
