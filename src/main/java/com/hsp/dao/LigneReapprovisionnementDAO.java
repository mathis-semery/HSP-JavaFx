package com.hsp.dao;

import com.hsp.model.LigneReapprovisionnement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneReapprovisionnementDAO {

    private Connection connection;

    public LigneReapprovisionnementDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public boolean create(LigneReapprovisionnement ligneReappro) throws SQLException {
        String sql = "INSERT INTO ligne_reapprovisionnement (id_reappro, id_fournisseur, id_produit, quantite_commandee, prix_unitaire) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ligneReappro.getIdReappro());
            stmt.setInt(2, ligneReappro.getIdFournisseur());
            stmt.setInt(3, ligneReappro.getIdProduit());
            stmt.setInt(4, ligneReappro.getQuantiteCommandee());
            stmt.setBigDecimal(5, ligneReappro.getPrixUnitaire());

            return stmt.executeUpdate() > 0;
        }
    }

    // READ - Get by id_reappro
    public List<LigneReapprovisionnement> getByIdReappro(int idReappro) throws SQLException {
        List<LigneReapprovisionnement> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_reapprovisionnement WHERE id_reappro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReappro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapResultSetToLigneReapprovisionnement(rs));
                }
            }
        }
        return lignes;
    }


    // READ - Get by ids (clé primaire composite)
    public LigneReapprovisionnement getByIds(int idReappro, int idFournisseur, int idProduit) throws SQLException {
        String sql = "SELECT * FROM ligne_reapprovisionnement WHERE id_reappro = ? AND id_fournisseur = ? AND id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReappro);
            stmt.setInt(2, idFournisseur);
            stmt.setInt(3, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLigneReapprovisionnement(rs);
                }
            }
        }
        return null;
    }

    // READ - Get by id_fournisseur
    public List<LigneReapprovisionnement> getByIdFournisseur(int idFournisseur) throws SQLException {
        List<LigneReapprovisionnement> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_reapprovisionnement WHERE id_fournisseur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idFournisseur);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapResultSetToLigneReapprovisionnement(rs));
                }
            }
        }
        return lignes;
    }

    // READ - Get by id_produit
    public List<LigneReapprovisionnement> getByIdProduit(int idProduit) throws SQLException {
        List<LigneReapprovisionnement> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_reapprovisionnement WHERE id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapResultSetToLigneReapprovisionnement(rs));
                }
            }
        }
        return lignes;
    }

    // READ - Get all
    public List<LigneReapprovisionnement> getAll() throws SQLException {
        List<LigneReapprovisionnement> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_reapprovisionnement";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lignes.add(mapResultSetToLigneReapprovisionnement(rs));
            }
        }
        return lignes;
    }

    // UPDATE
    public boolean update(LigneReapprovisionnement ligneReappro) throws SQLException {
        String sql = "UPDATE ligne_reapprovisionnement SET quantite_commandee = ?, prix_unitaire = ? WHERE id_reappro = ? AND id_fournisseur = ? AND id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ligneReappro.getQuantiteCommandee());
            stmt.setBigDecimal(2, ligneReappro.getPrixUnitaire());
            stmt.setInt(3, ligneReappro.getIdReappro());
            stmt.setInt(4, ligneReappro.getIdFournisseur());
            stmt.setInt(5, ligneReappro.getIdProduit());

            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean delete(int idReappro, int idFournisseur, int idProduit) throws SQLException {
        String sql = "DELETE FROM ligne_reapprovisionnement WHERE id_reappro = ? AND id_fournisseur = ? AND id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReappro);
            stmt.setInt(2, idFournisseur);
            stmt.setInt(3, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Delete all lines for a reapprovisionnement
    public boolean deleteByIdReappro(int idReappro) throws SQLException {
        String sql = "DELETE FROM ligne_reapprovisionnement WHERE id_reappro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReappro);
            return stmt.executeUpdate() > 0;
        }
    }

    // CALCULATE - Get total amount for a reapprovisionnement
    public double getTotalAmount(int idReappro) throws SQLException {
        String sql = "SELECT SUM(quantite_commandee * prix_unitaire) as total FROM ligne_reapprovisionnement WHERE id_reappro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReappro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    // Helper method to map ResultSet to LigneReapprovisionnement
    private LigneReapprovisionnement mapResultSetToLigneReapprovisionnement(ResultSet rs) throws SQLException {
        LigneReapprovisionnement ligneReappro = new LigneReapprovisionnement();
        ligneReappro.setIdReappro(rs.getInt("id_reappro"));
        ligneReappro.setIdFournisseur(rs.getInt("id_fournisseur"));
        ligneReappro.setIdProduit(rs.getInt("id_produit"));
        ligneReappro.setQuantiteCommandee(rs.getInt("quantite_commandee"));
        ligneReappro.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
        return ligneReappro;
    }
}