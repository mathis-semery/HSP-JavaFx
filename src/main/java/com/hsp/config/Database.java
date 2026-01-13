package com.hsp.config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Database {
    public static void main(String[] args) {
        Connection cnx = getConnexion();
        if (cnx != null) {
            System.out.println("Connexion établie avec succès !");
        } else {
            System.out.println("Échec de la connexion à la base de données.");
        }
    }

    private static final String SERVEUR = "localhost";
    private static final String PORT = "3306";
    private static final String NOM_BDD = "hsp_urgences";
    private static final String UTILISATEUR = "root";
    private static final String MOT_DE_PASSE = "";
    private static String getUrl() {
        return "jdbc:mysql://" + SERVEUR + ":" + PORT + "/" + NOM_BDD + "?serverTimezone=UTC";
    } // PORT + "/" +
    public static Connection getConnexion() {
        Connection cnx = null;
        try {
            cnx = DriverManager.getConnection(getUrl(), UTILISATEUR, MOT_DE_PASSE);
            System.out.println("Connexion réussie à la base de données !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
        return cnx;
    }
}