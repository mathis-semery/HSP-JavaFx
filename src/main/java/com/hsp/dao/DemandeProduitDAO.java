package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.DemandeProduit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeProduitDAO {

    public List<DemandeProduit> findAll() {
        List<DemandeProduit> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demande_produit";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                demandes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return demandes;
    }

    public DemandeProduit findById(int id) {
        String sql = "SELECT * FROM demande_produit WHERE id_demande = ?";
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

    public List<DemandeProduit> findByMedecinId(int idMedecin) {
        List<DemandeProduit> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demande_produit WHERE id_medecin = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idMedecin);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                demandes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return demandes;
    }

    public List<DemandeProduit> findByStatut(String statut) {
        List<DemandeProduit> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demande_produit WHERE statut = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                demandes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return demandes;
    }

    public boolean insert(DemandeProduit demande) {
        String sql = "INSERT INTO demande_produit (id_medecin, id_produit, id_gestionnaire, quantite, date_demande, statut, motif_refus) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, demande.getId_medecin());
            stmt.setInt(2, demande.getId_produit());
            stmt.setInt(3, demande.getId_gestionnaire());
            stmt.setDouble(4, demande.getQuantite());
            stmt.setString(5, demande.getDate_demande());
            stmt.setString(6, demande.getStatut());
            stmt.setString(7, demande.getMotif_refus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(DemandeProduit demande) {
        String sql = "UPDATE demande_produit SET id_medecin = ?, id_produit = ?, id_gestionnaire = ?, quantite = ?, date_demande = ?, statut = ?, motif_refus = ? WHERE id_demande = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, demande.getId_medecin());
            stmt.setInt(2, demande.getId_produit());
            stmt.setInt(3, demande.getId_gestionnaire());
            stmt.setDouble(4, demande.getQuantite());
            stmt.setString(5, demande.getDate_demande());
            stmt.setString(6, demande.getStatut());
            stmt.setString(7, demande.getMotif_refus());
            stmt.setInt(8, demande.getId_demande());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM demande_produit WHERE id_demande = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private DemandeProduit mapResultSet(ResultSet rs) throws SQLException {
        return new DemandeProduit(
                rs.getInt("id_demande"),
                rs.getInt("id_medecin"),
                rs.getInt("id_produit"),
                rs.getInt("id_gestionnaire"),
                rs.getDouble("quantite"),
                rs.getString("date_demande"),
                rs.getString("statut"),
                rs.getString("motif_refus")
        );
    }
}
