package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Patient;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patient";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    public Patient findById(int id) {
        String sql = "SELECT * FROM patient WHERE id_patient = ?";
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

    public boolean insert(Patient patient) {
        String sql = "INSERT INTO patient (nom, prenom, num_secu, email, telephone, adresse) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, patient.getNom());
            stmt.setString(2, patient.getPrenom());
            stmt.setString(3, patient.getNum_secu());
            stmt.setString(4, patient.getEmail());
            stmt.setString(5, patient.getTelephone());
            stmt.setString(6, patient.getAdresse());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Patient patient) {
        String sql = "UPDATE patient SET nom = ?, prenom = ?, num_secu = ?, email = ?, telephone = ?, adresse = ? WHERE id_patient = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, patient.getNom());
            stmt.setString(2, patient.getPrenom());
            stmt.setString(3, patient.getNum_secu());
            stmt.setString(4, patient.getEmail());
            stmt.setString(5, patient.getTelephone());
            stmt.setString(6, patient.getAdresse());
            stmt.setInt(7, patient.getId_patient());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM patient WHERE id_patient = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Patient mapResultSet(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id_patient"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("num_secu"),
                rs.getString("email"),
                rs.getString("telephone"),
                rs.getString("adresse"),
                rs.getTimestamp("date_creation") != null ? rs.getTimestamp("date_creation").toLocalDateTime() : null
        );
    }
}
