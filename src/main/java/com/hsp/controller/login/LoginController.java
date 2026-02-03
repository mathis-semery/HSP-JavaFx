package com.hsp.controller.login;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Button loginButton;

    // Configuration de la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER = "root";  // À modifier selon votre configuration
    private static final String DB_PASSWORD = "";  // À modifier selon votre configuration

    @FXML
    private void initialize() {
        System.out.println("LoginController initialisé");

        // Tester la connexion à la base de données
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL chargé avec succès");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL introuvable : " + e.getMessage());
        }
    }

    @FXML
    private void onLoginClicked() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckbox.isSelected();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Erreur", "Adresse email invalide", Alert.AlertType.ERROR);
            return;
        }

        // Désactiver le bouton pendant la connexion
        loginButton.setDisable(true);

        // Tentative de connexion
        authenticateUser(email, password, rememberMe);

        // Réactiver le bouton
        loginButton.setDisable(false);
    }

    private void authenticateUser(String email, String password, boolean rememberMe) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base de données
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Requête pour récupérer l'utilisateur
            String query = "SELECT id_utilisateur, nom, prenom, mot_de_passe, role FROM utilisateur WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id_utilisateur");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String hashedPassword = rs.getString("mot_de_passe");
                String role = rs.getString("role");

                // Vérification du mot de passe avec BCrypt
                if (BCrypt.checkpw(password, hashedPassword)) {
                    // Connexion réussie
                    System.out.println("Connexion réussie pour : " + prenom + " " + nom + " (" + role + ")");

                    // Enregistrer dans l'historique
                    enregistrerHistorique(conn, userId, "Connexion", "utilisateur", userId, "Connexion réussie");

                    // Rediriger vers le tableau de bord approprié selon le rôle
                    redirectToDashboard(role);

                } else {
                    // Mot de passe incorrect
                    showAlert("Erreur", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
                    System.out.println("Échec de connexion pour : " + email);
                }
            } else {
                // Utilisateur non trouvé
                showAlert("Erreur", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
                System.out.println("Utilisateur non trouvé : " + email);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de connexion à la base de données : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            // Fermeture des ressources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void enregistrerHistorique(Connection conn, int userId, String action, String table, int recordId, String details) {
        try {
            String query = "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, table);
            stmt.setInt(4, recordId);
            stmt.setString(5, details);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de l'historique : " + e.getMessage());
        }
    }

    private void redirectToDashboard(String role) {
        try {
            String fxmlFile = "";
            String windowTitle = "";

            // Choisir le tableau de bord selon le rôle
            switch (role) {
                case "Secretaire":
                    fxmlFile = "dashboard_secretaire.fxml";
                    windowTitle = "Tableau de bord - Secrétaire";
                    break;
                case "Medecin":
                    fxmlFile = "dashboard_medecin.fxml";
                    windowTitle = "Tableau de bord - Médecin";
                    break;
                case "Gestionnaire":
                    fxmlFile = "dashboard_gestionnaire.fxml";
                    windowTitle = "Tableau de bord - Gestionnaire";
                    break;
                default:
                    showAlert("Erreur", "Rôle utilisateur non reconnu", Alert.AlertType.ERROR);
                    return;
            }

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle et la remplacer
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.show();

            showAlert("Succès", "Connexion réussie ! Bienvenue.", Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void onForgotPassword() {
        System.out.println("Mot de passe oublié cliqué");
        showAlert("Information", "Un email de réinitialisation vous sera envoyé", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void onSignUpClicked() {
        System.out.println("S'inscrire cliqué");
        redirectToSignIn();
    }

    private void redirectToSignIn() {
        try {
            // Charger la page d'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/Signin.fxml"));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle et la remplacer
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 700);
            stage.setScene(scene);
            stage.setTitle("Inscription - HSP Urgences");
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'inscription : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    private void onFieldHover(MouseEvent event) {
        Control field = (Control) event.getSource();
        field.setStyle(field.getStyle() + "-fx-border-color: #667eea;");
    }

    @FXML
    private void onFieldExit(MouseEvent event) {
        Control field = (Control) event.getSource();
        field.setStyle(field.getStyle().replace("-fx-border-color: #667eea;", "-fx-border-color: #e2e8f0;"));
    }

    @FXML
    private void onButtonHover() {
        loginButton.setStyle(loginButton.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.5), 15, 0, 0, 5);");
    }

    @FXML
    private void onButtonExit() {
        loginButton.setStyle(loginButton.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.5), 15, 0, 0, 5);", ""));
    }

    // Méthodes utilitaires
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}