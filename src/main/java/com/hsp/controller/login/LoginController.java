package com.hsp.controller.login;

import com.hsp.controller.dashboard.AdminDashboardController;
import com.hsp.controller.dashboard.GestionnaireDashboardController;
import com.hsp.controller.dashboard.MedecinDashboardController;
import com.hsp.controller.dashboard.SecretaireDashboardController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
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
    public static final String DB_URL = "jdbc:mysql://localhost:3306/hsp_urgences";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "";

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
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // On récupère l'utilisateur par email uniquement, puis on vérifie le MDP côté Java
            String query = "SELECT id_utilisateur, nom, prenom, role, mot_de_passe FROM utilisateur WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("mot_de_passe");

                if (!verifyPassword(password, storedPassword)) {
                    showAlert("Erreur", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
                    System.out.println("Mot de passe incorrect pour : " + email);
                    return;
                }

                int userId = rs.getInt("id_utilisateur");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String role = rs.getString("role");

                System.out.println("Connexion réussie pour : " + prenom + " " + nom + " (" + role + ")");
                enregistrerHistorique(conn, userId, "Connexion", "utilisateur", userId, "Connexion réussie");
                redirectToDashboard(role, userId, prenom + " " + nom);

            } else {
                showAlert("Erreur", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
                System.out.println("Utilisateur introuvable : " + email);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de connexion à la base de données : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Vérifie le mot de passe : BCrypt en priorité, fallback texte clair pour les comptes de dev.
     */
    private boolean verifyPassword(String inputPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) return false;
        try {
            return BCrypt.checkpw(inputPassword, storedHash);
        } catch (Exception e) {
            // Le hash n'est pas BCrypt (compte dev) : comparaison directe
            return inputPassword.equals(storedHash);
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

    private void redirectToDashboard(String role, int userId, String userName) {
        try {
            String fxmlFile = "";
            String windowTitle = "";

            switch (role) {
                case "Admin":
                    fxmlFile = "/view/dashboard/DashboardAdmin.fxml";
                    windowTitle = "Tableau de bord - Administrateur";
                    break;
                case "Secretaire":
                    fxmlFile = "/view/dashboard/DashboardSecretaire.fxml";
                    windowTitle = "Tableau de bord - Secrétaire";
                    break;
                case "Medecin":
                    fxmlFile = "/view/dashboard/DashboardMedecin.fxml";
                    windowTitle = "Tableau de bord - Médecin";
                    break;
                case "Gestionnaire":
                    fxmlFile = "/view/dashboard/DashboardGestionnaire.fxml";
                    windowTitle = "Tableau de bord - Gestionnaire";
                    break;
                default:
                    showAlert("Erreur", "Rôle utilisateur non reconnu: " + role, Alert.AlertType.ERROR);
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Passer les infos selon le rôle
            if (role.equals("Admin")) {
                AdminDashboardController controller = loader.getController();
                controller.setAdminInfo(userId, userName);
            } else if (role.equals("Medecin")) {
                MedecinDashboardController controller = loader.getController();
                controller.setMedecinInfo(userId, "Dr. " + userName);
            } else if (role.equals("Secretaire")) {
                SecretaireDashboardController controller = loader.getController();
                controller.setSecretaireInfo(userId, userName);
            } else if (role.equals("Gestionnaire")) {
                GestionnaireDashboardController controller = loader.getController();
                controller.setGestionnaireInfo(userId, userName);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.setMaximized(true);
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