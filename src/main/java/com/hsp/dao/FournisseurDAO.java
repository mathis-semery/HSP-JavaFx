package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Fournisseur;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAO {

    public List<Fournisseur> findAll() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                fournisseurs.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fournisseurs;
    }

    public Fournisseur findById(int id) {
        String sql = "SELECT * FROM fournisseur WHERE id_fournisseur = ?";
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

    public boolean insert(Fournisseur fournisseur) {
        String sql = "INSERT INTO fournisseur (nom, contact, email, telephone, adresse) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getContact());
            stmt.setString(3, fournisseur.getEmail());
            stmt.setString(4, fournisseur.getTelephone());
            stmt.setString(5, fournisseur.getAdresse());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Fournisseur fournisseur) {
        String sql = "UPDATE fournisseur SET nom = ?, contact = ?, email = ?, telephone = ?, adresse = ? WHERE id_fournisseur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getContact());
            stmt.setString(3, fournisseur.getEmail());
            stmt.setString(4, fournisseur.getTelephone());
            stmt.setString(5, fournisseur.getAdresse());
            stmt.setInt(6, fournisseur.getId_fournisseur());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM fournisseur WHERE id_fournisseur = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Fournisseur mapResultSet(ResultSet rs) throws SQLException {
        LocalDate dateCreation = rs.getDate("date_creation") != null ? rs.getDate("date_creation").toLocalDate() : null;

        return new Fournisseur(
                rs.getInt("id_fournisseur"),
                rs.getString("nom"),
                rs.getString("contact"),
                rs.getString("email"),
                rs.getString("telephone"),
                rs.getString("adresse"),
                dateCreation
        );
    }
}
