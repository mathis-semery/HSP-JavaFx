package com.hsp.util;

import com.hsp.config.Database;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utilitaire pour creer le compte administrateur dans la base de donnees.
 * Executez cette classe une seule fois pour inserer l'admin.
 *
 * Identifiants par defaut :
 *   Email    : admin@hsp.fr
 *   Mot de passe : admin123
 */
public class CreateAdmin {

    public static void main(String[] args) {
        String email = "admin@hsp.fr";
        String motDePasse = "admin123";
        String nom = "Admin";
        String prenom = "Super";
        String role = "Admin";

        String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());

        try (Connection cnx = Database.getConnexion()) {
            if (cnx == null) {
                System.err.println("Impossible de se connecter a la base de donnees.");
                return;
            }

            // Verifier si l'admin existe deja
            String checkSql = "SELECT id_utilisateur FROM utilisateur WHERE email = ?";
            try (PreparedStatement checkStmt = cnx.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("L'administrateur existe deja (id=" + rs.getInt(1) + ").");
                    return;
                }
            }

            // Inserer l'admin
            String sql = "INSERT INTO utilisateur (nom, prenom, email, mdp, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, email);
                stmt.setString(4, hash);
                stmt.setString(5, role);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Administrateur cree avec succes !");
                    System.out.println("  Email    : " + email);
                    System.out.println("  Mot de passe : " + motDePasse);
                    System.out.println("  Role     : " + role);
                } else {
                    System.err.println("Echec de la creation de l'administrateur.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
