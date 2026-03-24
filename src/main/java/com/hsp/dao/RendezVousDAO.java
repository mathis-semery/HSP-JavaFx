package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.RendezVous;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    public List<RendezVous> findAll() {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous ORDER BY date_rdv DESC";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public RendezVous findById(int id) {
        String sql = "SELECT * FROM rendez_vous WHERE id_rdv = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RendezVous> findByPatientId(int idPatient) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE id_patient = ? ORDER BY date_rdv DESC";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idPatient);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RendezVous> findByMedecinId(int idMedecin) {
        List<RendezVous> list = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE id_medecin = ? ORDER BY date_rdv DESC";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idMedecin);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(RendezVous rdv) {
        String sql = "INSERT INTO rendez_vous (id_patient, id_medecin, id_secretaire, date_rdv, motif, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, rdv.getId_patient());
            stmt.setInt(2, rdv.getId_medecin());
            stmt.setInt(3, rdv.getId_secretaire());
            stmt.setString(4, rdv.getDate_rdv());
            stmt.setString(5, rdv.getMotif());
            stmt.setString(6, rdv.getStatut() != null ? rdv.getStatut() : "Planifie");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(RendezVous rdv) {
        String sql = "UPDATE rendez_vous SET id_patient=?, id_medecin=?, id_secretaire=?, " +
                     "date_rdv=?, motif=?, statut=? WHERE id_rdv=?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, rdv.getId_patient());
            stmt.setInt(2, rdv.getId_medecin());
            stmt.setInt(3, rdv.getId_secretaire());
            stmt.setString(4, rdv.getDate_rdv());
            stmt.setString(5, rdv.getMotif());
            stmt.setString(6, rdv.getStatut());
            stmt.setInt(7, rdv.getId_rdv());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatut(int idRdv, String statut) {
        String sql = "UPDATE rendez_vous SET statut=? WHERE id_rdv=?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setInt(2, idRdv);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM rendez_vous WHERE id_rdv = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private RendezVous mapResultSet(ResultSet rs) throws SQLException {
        return new RendezVous(
                rs.getInt("id_rdv"),
                rs.getInt("id_patient"),
                rs.getInt("id_medecin"),
                rs.getInt("id_secretaire"),
                rs.getString("date_rdv"),
                rs.getString("motif"),
                rs.getString("statut"),
                rs.getString("date_creation")
        );
    }
}
