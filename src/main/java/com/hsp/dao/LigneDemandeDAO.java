package com.hsp.dao;

import com.hsp.model.LigneDemande;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneDemandeDAO {

    private Connection connection;

    public LigneDemandeDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public boolean create(LigneDemande ligneDemande) throws SQLException {
        String sql = "INSERT INTO ligne_demande (id_demande, id_produit, quantite_demandee, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ligneDemande.getIdDemande());
            stmt.setInt(2, ligneDemande.getIdProduit());
            stmt.setInt(3, ligneDemande.getQuantiteDemandee());
            stmt.setString(4, ligneDemande.getStatut());

            return stmt.executeUpdate() > 0;
        }
    }

    // READ - Get by id_demande
    public List<LigneDemande> getByIdDemande(int idDemande) throws SQLException {
        List<LigneDemande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_demande WHERE id_demande = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapResultSetToLigneDemande(rs));
                }
            }
        }
        return lignes;
    }

    // READ - Get by id_demande and id_produit
    public LigneDemande getByIds(int idDemande, int idProduit) throws SQLException {
        String sql = "SELECT * FROM ligne_demande WHERE id_demande = ? AND id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            stmt.setInt(2, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLigneDemande(rs);
                }
            }
        }
        return null;
    }

    // READ - Get all
    public List<LigneDemande> getAll() throws SQLException {
        List<LigneDemande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_demande";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lignes.add(mapResultSetToLigneDemande(rs));
            }
        }
        return lignes;
    }

    // READ - Get by statut
    public List<LigneDemande> getByStatut(String statut) throws SQLException {
        List<LigneDemande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM ligne_demande WHERE statut = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statut);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(mapResultSetToLigneDemande(rs));
                }
            }
        }
        return lignes;
    }

    // UPDATE
    public boolean update(LigneDemande ligneDemande) throws SQLException {
        String sql = "UPDATE ligne_demande SET quantite_demandee = ?, statut = ? WHERE id_demande = ? AND id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ligneDemande.getQuantiteDemandee());
            stmt.setString(2, ligneDemande.getStatut());
            stmt.setInt(3, ligneDemande.getIdDemande());
            stmt.setInt(4, ligneDemande.getIdProduit());

            return stmt.executeUpdate() > 0;
        }
    }

    // UPDATE - Update statut
    public boolean updateStatut(int idDemande, int idProduit, String statut) throws SQLException {
        String sql = "UPDATE ligne_demande SET statut = ? WHERE id_demande = ? AND id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setInt(2, idDemande);
            stmt.setInt(3, idProduit);

            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean delete(int idDemande, int idProduit) throws SQLException {
        String sql = "DELETE FROM ligne_demande WHERE id_demande = ? AND id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            stmt.setInt(2, idProduit);
            return stmt.executeUpdate() > 0;
        }
    }

    // DELETE - Delete all lines for a demande
    public boolean deleteByIdDemande(int idDemande) throws SQLException {
        String sql = "DELETE FROM ligne_demande WHERE id_demande = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet to LigneDemande
    private LigneDemande mapResultSetToLigneDemande(ResultSet rs) throws SQLException {
        LigneDemande ligneDemande = new LigneDemande();
        ligneDemande.setIdDemande(rs.getInt("id_demande"));
        ligneDemande.setIdProduit(rs.getInt("id_produit"));
        ligneDemande.setQuantiteDemandee(rs.getInt("quantite_demandee"));
        ligneDemande.setStatut(rs.getString("statut"));
        return ligneDemande;
    }
}
