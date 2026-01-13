package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Reapprovisionnement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReapprovisionnementDAO {

    public List<Reapprovisionnement> findAll() {
        List<Reapprovisionnement> reapprovisionnements = new ArrayList<>();
        String sql = "SELECT * FROM reapprovisionnement";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reapprovisionnements.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reapprovisionnements;
    }

    public Reapprovisionnement findById(int id) {
        String sql = "SELECT * FROM reapprovisionnement WHERE id_reappro = ?";
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

    public List<Reapprovisionnement> findByProduitId(int idProduit) {
        List<Reapprovisionnement> reapprovisionnements = new ArrayList<>();
        String sql = "SELECT * FROM reapprovisionnement WHERE id_produit = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reapprovisionnements.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reapprovisionnements;
    }

    public boolean insert(Reapprovisionnement reappro) {
        String sql = "INSERT INTO reapprovisionnement (id_produit, id_fournisseur, id_gestionnaire, quantite, date_commande, date_reception) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, reappro.getId_produit());
            stmt.setInt(2, reappro.getId_fournisseur());
            stmt.setInt(3, reappro.getId_gestionnaire());
            stmt.setInt(4, reappro.getQuantite());
            stmt.setDate(5, reappro.getDate_commande() != null ? Date.valueOf(reappro.getDate_commande()) : null);
            stmt.setDate(6, reappro.getDate_reception() != null ? Date.valueOf(reappro.getDate_reception()) : null);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Reapprovisionnement reappro) {
        String sql = "UPDATE reapprovisionnement SET id_produit = ?, id_fournisseur = ?, id_gestionnaire = ?, quantite = ?, date_commande = ?, date_reception = ? WHERE id_reappro = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, reappro.getId_produit());
            stmt.setInt(2, reappro.getId_fournisseur());
            stmt.setInt(3, reappro.getId_gestionnaire());
            stmt.setInt(4, reappro.getQuantite());
            stmt.setDate(5, reappro.getDate_commande() != null ? Date.valueOf(reappro.getDate_commande()) : null);
            stmt.setDate(6, reappro.getDate_reception() != null ? Date.valueOf(reappro.getDate_reception()) : null);
            stmt.setInt(7, reappro.getId_reappro());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reapprovisionnement WHERE id_reappro = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Reapprovisionnement mapResultSet(ResultSet rs) throws SQLException {
        LocalDate dateCommande = rs.getDate("date_commande") != null ? rs.getDate("date_commande").toLocalDate() : null;
        LocalDate dateReception = rs.getDate("date_reception") != null ? rs.getDate("date_reception").toLocalDate() : null;

        return new Reapprovisionnement(
                rs.getInt("id_reappro"),
                rs.getInt("id_produit"),
                rs.getInt("id_fournisseur"),
                rs.getInt("id_gestionnaire"),
                rs.getInt("quantite"),
                dateCommande,
                dateReception
        );
    }
}
