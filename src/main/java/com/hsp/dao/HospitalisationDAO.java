package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Hospitalisation;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HospitalisationDAO {

    public List<Hospitalisation> findAll() {
        List<Hospitalisation> hospitalisations = new ArrayList<>();
        String sql = "SELECT * FROM hospitalisation";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                hospitalisations.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hospitalisations;
    }

    public Hospitalisation findById(int id) {
        String sql = "SELECT * FROM hospitalisation WHERE id_hospitalisation = ?";
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

    public List<Hospitalisation> findByDossierId(int idDossier) {
        List<Hospitalisation> hospitalisations = new ArrayList<>();
        String sql = "SELECT * FROM hospitalisation WHERE id_dossier = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idDossier);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hospitalisations.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hospitalisations;
    }

    public List<Hospitalisation> findByChambreId(int idChambre) {
        List<Hospitalisation> hospitalisations = new ArrayList<>();
        String sql = "SELECT * FROM hospitalisation WHERE id_chambre = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idChambre);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hospitalisations.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hospitalisations;
    }

    public boolean insert(Hospitalisation hospitalisation) {
        String sql = "INSERT INTO hospitalisation (id_dossier, id_chambre, id_medecin, date_debut, date_fin, description_maladie) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, hospitalisation.getId_dossier());
            stmt.setInt(2, hospitalisation.getId_chambre());
            stmt.setInt(3, hospitalisation.getId_medecin());
            stmt.setDate(4, hospitalisation.getDate_debut() != null ? Date.valueOf(hospitalisation.getDate_debut()) : null);
            stmt.setDate(5, hospitalisation.getDate_fin() != null ? Date.valueOf(hospitalisation.getDate_fin()) : null);
            stmt.setString(6, hospitalisation.getDescription_maladie());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Hospitalisation hospitalisation) {
        String sql = "UPDATE hospitalisation SET id_dossier = ?, id_chambre = ?, id_medecin = ?, date_debut = ?, date_fin = ?, description_maladie = ? WHERE id_hospitalisation = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, hospitalisation.getId_dossier());
            stmt.setInt(2, hospitalisation.getId_chambre());
            stmt.setInt(3, hospitalisation.getId_medecin());
            stmt.setDate(4, hospitalisation.getDate_debut() != null ? Date.valueOf(hospitalisation.getDate_debut()) : null);
            stmt.setDate(5, hospitalisation.getDate_fin() != null ? Date.valueOf(hospitalisation.getDate_fin()) : null);
            stmt.setString(6, hospitalisation.getDescription_maladie());
            stmt.setInt(7, hospitalisation.getId_hospitalisation());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM hospitalisation WHERE id_hospitalisation = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Hospitalisation mapResultSet(ResultSet rs) throws SQLException {
        LocalDate dateDebut = rs.getDate("date_debut") != null ? rs.getDate("date_debut").toLocalDate() : null;
        LocalDate dateFin = rs.getDate("date_fin") != null ? rs.getDate("date_fin").toLocalDate() : null;
        LocalDateTime dateCreation = rs.getTimestamp("date_creation") != null ? rs.getTimestamp("date_creation").toLocalDateTime() : null;

        return new Hospitalisation(
                rs.getInt("id_hospitalisation"),
                rs.getInt("id_dossier"),
                rs.getInt("id_chambre"),
                rs.getInt("id_medecin"),
                dateDebut,
                dateFin,
                rs.getString("description_maladie"),
                dateCreation
        );
    }
}
