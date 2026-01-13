package com.hsp.dao;

import com.hsp.config.Database;
import com.hsp.model.Chambre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChambreDAO {

    public List<Chambre> findAll() {
        List<Chambre> chambres = new ArrayList<>();
        String sql = "SELECT * FROM chambre";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                chambres.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chambres;
    }

    public Chambre findById(int id) {
        String sql = "SELECT * FROM chambre WHERE id_chambre = ?";
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

    public List<Chambre> findDisponibles() {
        List<Chambre> chambres = new ArrayList<>();
        String sql = "SELECT * FROM chambre WHERE disponible = '1' OR disponible = 'true'";
        try (Connection cnx = Database.getConnexion();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                chambres.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chambres;
    }

    public boolean insert(Chambre chambre) {
        String sql = "INSERT INTO chambre (numero, etage, nb_lits, disponible) VALUES (?, ?, ?, ?)";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, chambre.getNumero());
            stmt.setString(2, chambre.getEtage());
            stmt.setString(3, chambre.getNb_lits());
            stmt.setString(4, chambre.getDisponible());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Chambre chambre) {
        String sql = "UPDATE chambre SET numero = ?, etage = ?, nb_lits = ?, disponible = ? WHERE id_chambre = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, chambre.getNumero());
            stmt.setString(2, chambre.getEtage());
            stmt.setString(3, chambre.getNb_lits());
            stmt.setString(4, chambre.getDisponible());
            stmt.setInt(5, chambre.getId_chambre());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM chambre WHERE id_chambre = ?";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Chambre mapResultSet(ResultSet rs) throws SQLException {
        return new Chambre(
                rs.getInt("id_chambre"),
                rs.getString("numero"),
                rs.getString("etage"),
                rs.getString("nb_lits"),
                rs.getString("disponible")
        );
    }
}
