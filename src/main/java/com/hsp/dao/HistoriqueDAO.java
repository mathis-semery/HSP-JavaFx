package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Historique;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueDAO {

    public List<Historique> findAll() {
        List<Historique> historiques = new ArrayList<>();
        String sql = "SELECT * FROM historique";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                historiques.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historiques;
    }

    public Historique findById(int id) {
        String sql = "SELECT * FROM historique WHERE id_historique = ?";
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

    public List<Historique> findByUtilisateurId(int idUtilisateur) {
        List<Historique> historiques = new ArrayList<>();
        String sql = "SELECT * FROM historique WHERE id_utilisateur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                historiques.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historiques;
    }

    public List<Historique> findByTable(String tableConcernee) {
        List<Historique> historiques = new ArrayList<>();
        String sql = "SELECT * FROM historique WHERE table_concernee = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, tableConcernee);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                historiques.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historiques;
    }

    public boolean insert(Historique historique) {
        String sql = "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, date_action, details) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, historique.getId_utilisateur());
            stmt.setString(2, historique.getAction());
            stmt.setString(3, historique.getTable_concernee());
            stmt.setInt(4, historique.getId_enregistrement());
            stmt.setTimestamp(5, historique.getDate_action() != null ? Timestamp.valueOf(historique.getDate_action()) : null);
            stmt.setString(6, historique.getDetails());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Historique historique) {
        String sql = "UPDATE historique SET id_utilisateur = ?, action = ?, table_concernee = ?, id_enregistrement = ?, date_action = ?, details = ? WHERE id_historique = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, historique.getId_utilisateur());
            stmt.setString(2, historique.getAction());
            stmt.setString(3, historique.getTable_concernee());
            stmt.setInt(4, historique.getId_enregistrement());
            stmt.setTimestamp(5, historique.getDate_action() != null ? Timestamp.valueOf(historique.getDate_action()) : null);
            stmt.setString(6, historique.getDetails());
            stmt.setInt(7, historique.getId_historique());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM historique WHERE id_historique = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Historique mapResultSet(ResultSet rs) throws SQLException {
        LocalDateTime dateAction = rs.getTimestamp("date_action") != null ? rs.getTimestamp("date_action").toLocalDateTime() : null;

        return new Historique(
                rs.getInt("id_historique"),
                rs.getInt("id_utilisateur"),
                rs.getString("action"),
                rs.getString("table_concernee"),
                rs.getInt("id_enregistrement"),
                dateAction,
                rs.getString("details")
        );
    }
}
