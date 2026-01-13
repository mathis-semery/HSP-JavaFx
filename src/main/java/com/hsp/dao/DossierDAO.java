package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Dossier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DossierDAO {

    public List<Dossier> findAll() {
        List<Dossier> dossiers = new ArrayList<>();
        String sql = "SELECT * FROM dossier";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dossiers.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dossiers;
    }

    public Dossier findById(int id) {
        String sql = "SELECT * FROM dossier WHERE id_dossier = ?";
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

    public List<Dossier> findByPatientId(int idPatient) {
        List<Dossier> dossiers = new ArrayList<>();
        String sql = "SELECT * FROM dossier WHERE id_patient = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idPatient);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dossiers.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dossiers;
    }

    public boolean insert(Dossier dossier) {
        String sql = "INSERT INTO dossier (id_patient, id_medecin, date_arrivee, symptomes, niveau_gravite, statut) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, dossier.getId_patient());
            stmt.setInt(2, dossier.getId_medecin());
            stmt.setString(3, dossier.getDate_arrivee());
            stmt.setString(4, dossier.getSymptomes());
            stmt.setString(5, dossier.getNiveau_gravite());
            stmt.setString(6, dossier.getStatut());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Dossier dossier) {
        String sql = "UPDATE dossier SET id_patient = ?, id_medecin = ?, date_arrivee = ?, symptomes = ?, niveau_gravite = ?, statut = ? WHERE id_dossier = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, dossier.getId_patient());
            stmt.setInt(2, dossier.getId_medecin());
            stmt.setString(3, dossier.getDate_arrivee());
            stmt.setString(4, dossier.getSymptomes());
            stmt.setString(5, dossier.getNiveau_gravite());
            stmt.setString(6, dossier.getStatut());
            stmt.setInt(7, dossier.getId_dossier());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM dossier WHERE id_dossier = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Dossier mapResultSet(ResultSet rs) throws SQLException {
        return new Dossier(
                rs.getInt("id_dossier"),
                rs.getInt("id_patient"),
                rs.getInt("id_medecin"),
                rs.getString("date_arrivee"),
                rs.getString("symptomes"),
                rs.getString("niveau_gravite"),
                rs.getString("statut"),
                rs.getString("date_creation")
        );
    }
}
