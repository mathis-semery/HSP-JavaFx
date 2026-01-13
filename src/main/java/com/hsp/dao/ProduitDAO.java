package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Produit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    public List<Produit> findAll() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produits.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }

    public Produit findById(int id) {
        String sql = "SELECT * FROM produit WHERE id_produit = ?";
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

    public boolean insert(Produit produit) {
        String sql = "INSERT INTO produit (libelle, description, niveau_dangerosite, quantite_stock) VALUES (?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, produit.getLibelle());
            stmt.setString(2, produit.getDescription());
            stmt.setInt(3, produit.getNiveau_dangerosite());
            stmt.setInt(4, produit.getQuantite_stock());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Produit produit) {
        String sql = "UPDATE produit SET libelle = ?, description = ?, niveau_dangerosite = ?, quantite_stock = ? WHERE id_produit = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, produit.getLibelle());
            stmt.setString(2, produit.getDescription());
            stmt.setInt(3, produit.getNiveau_dangerosite());
            stmt.setInt(4, produit.getQuantite_stock());
            stmt.setInt(5, produit.getId_produit());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM produit WHERE id_produit = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Produit mapResultSet(ResultSet rs) throws SQLException {
        LocalDateTime dateCreation = rs.getTimestamp("date_creation") != null ? rs.getTimestamp("date_creation").toLocalDateTime() : null;

        return new Produit(
                rs.getInt("id_produit"),
                rs.getString("libelle"),
                rs.getString("description"),
                rs.getInt("niveau_dangerosite"),
                rs.getInt("quantite_stock"),
                dateCreation
        );
    }
}
