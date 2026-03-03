package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Ordonnance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceDAO {

    public List<Ordonnance> findAll() {
        List<Ordonnance> ordonnances = new ArrayList<>();
        String sql = "SELECT * FROM ordonnance ORDER BY date_ordonnance DESC";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ordonnances.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ordonnances;
    }

    public Ordonnance findById(int id) {
        String sql = "SELECT * FROM ordonnance WHERE id_ordonnance = ?";
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

    public List<Ordonnance> findByDossierId(int idDossier) {
        List<Ordonnance> ordonnances = new ArrayList<>();
        String sql = "SELECT * FROM ordonnance WHERE id_dossier = ? ORDER BY date_ordonnance DESC";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idDossier);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ordonnances.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ordonnances;
    }

    public List<Ordonnance> findByMedecinId(int idMedecin) {
        List<Ordonnance> ordonnances = new ArrayList<>();
        String sql = "SELECT * FROM ordonnance WHERE id_medecin = ? ORDER BY date_ordonnance DESC";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idMedecin);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ordonnances.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ordonnances;
    }

    public boolean insert(Ordonnance ordonnance) {
        String sql = "INSERT INTO ordonnance (id_dossier, id_medecin, date_ordonnance, medicaments, posologie, duree_traitement, notes, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, ordonnance.getId_dossier());
            stmt.setInt(2, ordonnance.getId_medecin());
            stmt.setString(3, ordonnance.getDate_ordonnance());
            stmt.setString(4, ordonnance.getMedicaments());
            stmt.setString(5, ordonnance.getPosologie());
            stmt.setString(6, ordonnance.getDuree_traitement());
            stmt.setString(7, ordonnance.getNotes());
            stmt.setString(8, ordonnance.getStatut());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Ordonnance ordonnance) {
        String sql = "UPDATE ordonnance SET id_dossier = ?, id_medecin = ?, date_ordonnance = ?, medicaments = ?, posologie = ?, duree_traitement = ?, notes = ?, statut = ? WHERE id_ordonnance = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, ordonnance.getId_dossier());
            stmt.setInt(2, ordonnance.getId_medecin());
            stmt.setString(3, ordonnance.getDate_ordonnance());
            stmt.setString(4, ordonnance.getMedicaments());
            stmt.setString(5, ordonnance.getPosologie());
            stmt.setString(6, ordonnance.getDuree_traitement());
            stmt.setString(7, ordonnance.getNotes());
            stmt.setString(8, ordonnance.getStatut());
            stmt.setInt(9, ordonnance.getId_ordonnance());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ordonnance WHERE id_ordonnance = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Ordonnance mapResultSet(ResultSet rs) throws SQLException {
        return new Ordonnance(
                rs.getInt("id_ordonnance"),
                rs.getInt("id_dossier"),
                rs.getInt("id_medecin"),
                rs.getString("date_ordonnance"),
                rs.getString("medicaments"),
                rs.getString("posologie"),
                rs.getString("duree_traitement"),
                rs.getString("notes"),
                rs.getString("statut"),
                rs.getString("date_creation")
        );
    }
}
