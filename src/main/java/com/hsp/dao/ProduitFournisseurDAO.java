package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.ProduitFournisseur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitFournisseurDAO {

    public List<ProduitFournisseur> findAll() {
        List<ProduitFournisseur> produitFournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM produit_fournisseur";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produitFournisseurs.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produitFournisseurs;
    }

    public ProduitFournisseur findById(int idProduit, int idFournisseur) {
        String sql = "SELECT * FROM produit_fournisseur WHERE id_produit = ? AND id_fournisseur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.setInt(2, idFournisseur);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ProduitFournisseur> findByProduitId(int idProduit) {
        List<ProduitFournisseur> produitFournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM produit_fournisseur WHERE id_produit = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                produitFournisseurs.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produitFournisseurs;
    }

    public List<ProduitFournisseur> findByFournisseurId(int idFournisseur) {
        List<ProduitFournisseur> produitFournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM produit_fournisseur WHERE id_fournisseur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                produitFournisseurs.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produitFournisseurs;
    }

    public boolean insert(ProduitFournisseur pf) {
        String sql = "INSERT INTO produit_fournisseur (id_produit, id_fournisseur, prix) VALUES (?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, pf.getId_produit());
            stmt.setInt(2, pf.getId_fournisseur());
            stmt.setDouble(3, pf.getPrix());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ProduitFournisseur pf) {
        String sql = "UPDATE produit_fournisseur SET prix = ? WHERE id_produit = ? AND id_fournisseur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setDouble(1, pf.getPrix());
            stmt.setInt(2, pf.getId_produit());
            stmt.setInt(3, pf.getId_fournisseur());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int idProduit, int idFournisseur) {
        String sql = "DELETE FROM produit_fournisseur WHERE id_produit = ? AND id_fournisseur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.setInt(2, idFournisseur);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ProduitFournisseur mapResultSet(ResultSet rs) throws SQLException {
        return new ProduitFournisseur(
                rs.getInt("id_produit"),
                rs.getInt("id_fournisseur"),
                rs.getDouble("prix")
        );
    }
}
