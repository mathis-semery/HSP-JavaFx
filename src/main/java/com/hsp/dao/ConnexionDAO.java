package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Connexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConnexionDAO {

    public List<Connexion> findAll() {
        List<Connexion> connexions = new ArrayList<>();
        String sql = "SELECT * FROM connexion";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                connexions.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connexions;
    }

    public Connexion findById(int id) {
        String sql = "SELECT * FROM connexion WHERE id_connexion = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Connexion> findByUtilisateurId(int idUtilisateur) {
        List<Connexion> connexions = new ArrayList<>();
        String sql = "SELECT * FROM connexion WHERE id_utilisateur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                connexions.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connexions;
    }

    public boolean insert(Connexion connexion) {
        String sql = "INSERT INTO connexion (id_utilisateur, date_connexion, ip_adresse) VALUES (?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, connexion.getId_utilisateur());
            stmt.setString(2, connexion.getDate_connexion());
            stmt.setString(3, connexion.getIp_adresse());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Connexion connexion) {
        String sql = "UPDATE connexion SET id_utilisateur = ?, date_connexion = ?, ip_adresse = ? WHERE id_connexion = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, connexion.getId_utilisateur());
            stmt.setString(2, connexion.getDate_connexion());
            stmt.setString(3, connexion.getIp_adresse());
            stmt.setInt(4, connexion.getId_connexion());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM connexion WHERE id_connexion = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Connexion mapResultSet(ResultSet rs) throws SQLException {
        return new Connexion(
                rs.getInt("id_connexion"),
                rs.getInt("id_utilisateur"),
                rs.getString("date_connexion"),
                rs.getString("ip_adresse")
        );
    }
}
