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

    // La table demande_produit n'a pas de colonnes id_produit ni quantite
    // (elles sont dans ligne_demande). On n'insère que les colonnes existantes.
    public boolean insert(DemandeProduit demande) {
        String sql = "INSERT INTO demande_produit (id_medecin, id_gestionnaire, date_demande, statut, motif_refus) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, demande.getId_medecin());
            if (demande.getId_gestionnaire() > 0) stmt.setInt(2, demande.getId_gestionnaire());
            else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, demande.getDate_demande());
            stmt.setString(4, demande.getStatut());
            stmt.setString(5, demande.getMotif_refus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Insère une demande et retourne l'id généré (-1 en cas d'échec). */
    public int insertAndGetId(DemandeProduit demande) {
        String sql = "INSERT INTO demande_produit (id_medecin, id_gestionnaire, date_demande, statut, motif_refus) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, demande.getId_medecin());
            if (demande.getId_gestionnaire() > 0) stmt.setInt(2, demande.getId_gestionnaire());
            else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, demande.getDate_demande());
            stmt.setString(4, demande.getStatut());
            stmt.setString(5, demande.getMotif_refus());
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(DemandeProduit demande) {
        String sql = "UPDATE demande_produit SET id_medecin = ?, id_gestionnaire = ?, date_demande = ?, statut = ?, motif_refus = ? WHERE id_demande = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, demande.getId_medecin());
            if (demande.getId_gestionnaire() > 0) stmt.setInt(2, demande.getId_gestionnaire());
            else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, demande.getDate_demande());
            stmt.setString(4, demande.getStatut());
            stmt.setString(5, demande.getMotif_refus());
            stmt.setInt(6, demande.getId_demande());
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
        // id_produit et quantite ne sont pas dans demande_produit (elles sont dans ligne_demande)
        return new DemandeProduit(
                rs.getInt("id_demande"),
                rs.getInt("id_medecin"),
                0,
                rs.getInt("id_gestionnaire"),
                0,
                rs.getString("date_demande"),
                rs.getString("statut"),
                rs.getString("motif_refus")
        );
    }
}
