package com.hsp.controller.dashboard;

import com.hsp.controller.utilisateur.UtilisateurFormController;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // ===== FXML COMPONENTS =====
    @FXML private Label userNameLabel;
    @FXML private Label totalUsersLabel;

    // Views
    @FXML private VBox dashboardView;
    @FXML private VBox usersView;
    @FXML private VBox systemView;
    @FXML private VBox logsView;
    @FXML private VBox statsView;

    // Dashboard Stats
    @FXML private Label totalUtilisateursLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalHospitalisationsLabel;
    @FXML private Label systemStatusLabel;
    @FXML private TableView<?> recentActivityTable;

    // Users Management - INTÉGRATION AVEC VOTRE SYSTÈME
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, String> userIdCol;
    @FXML private TableColumn<Utilisateur, String> userNomCol;
    @FXML private TableColumn<Utilisateur, String> userPrenomCol;
    @FXML private TableColumn<Utilisateur, String> userEmailCol;
    @FXML private TableColumn<Utilisateur, String> userRoleCol;
    @FXML private TableColumn<Utilisateur, String> userDateCol;
    @FXML private TextField searchUserField;

    // System Settings
    @FXML private TextField hospitalNameField;
    @FXML private TextField hospitalAddressField;
    @FXML private TextField hospitalPhoneField;
    @FXML private TextField maxPatientsField;
    @FXML private CheckBox maintenanceModeCheckBox;

    // Logs
    @FXML private TableView<?> logsTable;
    @FXML private ComboBox<String> logLevelFilter;
    @FXML private DatePicker logDateFilter;

    // Stats
    @FXML private Label statsPatientsMoisLabel;
    @FXML private Label statsConsultationsMoyenneLabel;
    @FXML private Label statsTauxOccupationLabel;

    // ===== DATA =====
    private String currentAdminName = "Administrateur";
    private int currentAdminId = 1;
    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Utilisateur> utilisateurs;

    // DB Config
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INIT ADMIN DASHBOARD ===");

        if (userNameLabel != null) userNameLabel.setText(currentAdminName);

        // Initialiser le DAO
        utilisateurDAO = new UtilisateurDAO();
        utilisateurs = FXCollections.observableArrayList();

        // Configurer la TableView des utilisateurs
        configurerTableUtilisateurs();

        // Initialize ComboBoxes
        if (logLevelFilter != null) {
            logLevelFilter.getItems().addAll("Tous", "INFO", "WARNING", "ERROR", "CRITICAL");
            logLevelFilter.setValue("Tous");
        }

        // Initialize system settings
        if (hospitalNameField != null) hospitalNameField.setText("HSP Urgences");
        if (hospitalAddressField != null) hospitalAddressField.setText("123 Rue de la Santé, 75013 Paris");
        if (hospitalPhoneField != null) hospitalPhoneField.setText("01 23 45 67 89");
        if (maxPatientsField != null) maxPatientsField.setText("100");

        loadDashboardData();
        showDashboard();

        System.out.println("=== INIT TERMINÉE ===");
    }

    // ===== CONFIGURATION TABLE UTILISATEURS =====

    private void configurerTableUtilisateurs() {
        if (usersTable == null) return;

        userIdCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        userNomCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom() != null ? cellData.getValue().getNom() : ""));

        userPrenomCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrenom() != null ? cellData.getValue().getPrenom() : ""));

        userEmailCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : ""));

        userRoleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole() != null ? cellData.getValue().getRole() : ""));

        userDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_creation() != null ?
                        cellData.getValue().getDate_creation() : ""));

        usersTable.setItems(utilisateurs);

        // Double-clic pour modifier
        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && usersTable.getSelectionModel().getSelectedItem() != null) {
                onEditUser();
            }
        });
    }

    // ===== NAVIGATION =====

    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) {
            dashboardView.setVisible(true);
            dashboardView.setManaged(true);
        }
        loadDashboardData();
    }

    @FXML
    private void showUsers() {
        hideAllViews();
        if (usersView != null) {
            usersView.setVisible(true);
            usersView.setManaged(true);
        }
        loadUsers();
    }

    @FXML
    private void showSystem() {
        hideAllViews();
        if (systemView != null) {
            systemView.setVisible(true);
            systemView.setManaged(true);
        }
    }

    @FXML
    private void showLogs() {
        hideAllViews();
        if (logsView != null) {
            logsView.setVisible(true);
            logsView.setManaged(true);
        }
        loadLogs();
    }

    @FXML
    private void showStats() {
        hideAllViews();
        if (statsView != null) {
            statsView.setVisible(true);
            statsView.setManaged(true);
        }
        loadStats();
    }

    private void hideAllViews() {
        if (dashboardView != null) { dashboardView.setVisible(false); dashboardView.setManaged(false); }
        if (usersView != null) { usersView.setVisible(false); usersView.setManaged(false); }
        if (systemView != null) { systemView.setVisible(false); systemView.setManaged(false); }
        if (logsView != null) { logsView.setVisible(false); logsView.setManaged(false); }
        if (statsView != null) { statsView.setVisible(false); statsView.setManaged(false); }
    }

    // ===== DATA LOADING =====

    private void loadDashboardData() {
        Connection conn = null;
        try {
            conn = getConnection();

            // Compter les utilisateurs
            String queryUsers = "SELECT COUNT(*) as count FROM utilisateur";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(queryUsers);
            if (rs.next()) {
                int count = rs.getInt("count");
                if (totalUtilisateursLabel != null) totalUtilisateursLabel.setText(String.valueOf(count));
                if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(count));
            }
            rs.close();

            // Compter les patients
            String queryPatients = "SELECT COUNT(*) as count FROM patient";
            rs = stmt.executeQuery(queryPatients);
            if (rs.next()) {
                if (totalPatientsLabel != null) totalPatientsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            // Compter les hospitalisations actives
            String queryHosp = "SELECT COUNT(*) as count FROM hospitalisation WHERE date_fin IS NULL";
            rs = stmt.executeQuery(queryHosp);
            if (rs.next()) {
                if (totalHospitalisationsLabel != null) totalHospitalisationsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            stmt.close();

            if (systemStatusLabel != null) systemStatusLabel.setText("Opérationnel");

            System.out.println("✓ Données admin chargées");

        } catch (SQLException e) {
            System.err.println("Erreur chargement données : " + e.getMessage());
            if (totalUtilisateursLabel != null) totalUtilisateursLabel.setText("0");
            if (totalPatientsLabel != null) totalPatientsLabel.setText("0");
            if (totalHospitalisationsLabel != null) totalHospitalisationsLabel.setText("0");
        } finally {
            closeConnection(conn);
        }
    }

    private void loadUsers() {
        List<Utilisateur> liste = utilisateurDAO.findAll();
        utilisateurs.clear();
        utilisateurs.addAll(liste);
        System.out.println("✓ " + liste.size() + " utilisateurs chargés");
    }

    private void loadLogs() {
        // TODO: Charger les logs depuis la table historique
        System.out.println("Chargement des logs...");
    }

    private void loadStats() {
        Connection conn = null;
        try {
            conn = getConnection();

            // Patients ce mois
            String queryMonth = "SELECT COUNT(*) as count FROM dossier WHERE MONTH(date_arrivee) = MONTH(CURDATE()) AND YEAR(date_arrivee) = YEAR(CURDATE())";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(queryMonth);
            if (rs.next()) {
                if (statsPatientsMoisLabel != null) statsPatientsMoisLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            stmt.close();

            // Données fictives pour les autres stats
            if (statsConsultationsMoyenneLabel != null) statsConsultationsMoyenneLabel.setText("2h 15min");
            if (statsTauxOccupationLabel != null) statsTauxOccupationLabel.setText("78%");

            System.out.println("✓ Statistiques chargées");

        } catch (SQLException e) {
            System.err.println("Erreur stats : " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    // ===== USER MANAGEMENT - INTÉGRATION AVEC VOTRE SYSTÈME =====

    @FXML
    private void onCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateur/UtilisateurForm.fxml"));
            Parent root = loader.load();

            UtilisateurFormController controller = loader.getController();
            controller.setMode(UtilisateurFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouvel utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUsers();
            loadDashboardData(); // Mettre à jour les stats

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEditUser() {
        Utilisateur selection = usersTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateur/UtilisateurForm.fxml"));
            Parent root = loader.load();

            UtilisateurFormController controller = loader.getController();
            controller.setMode(UtilisateurFormController.Mode.MODIFICATION);
            controller.setUtilisateur(selection);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUsers();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeleteUser() {
        Utilisateur selection = usersTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'utilisateur \"" + selection.getPrenom() + " " + selection.getNom() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = utilisateurDAO.delete(selection.getId());
            if (succes) {
                loadUsers();
                loadDashboardData();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'utilisateur");
            }
        }
    }

    @FXML
    private void onResetPassword() {
        Utilisateur selection = usersTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur");
            return;
        }

        String newPassword = "HSP" + (int)(Math.random() * 10000);

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Réinitialisation mot de passe");
        confirmation.setHeaderText("Réinitialiser le mot de passe de " + selection.getPrenom() + " " + selection.getNom() + " ?");
        confirmation.setContentText("Nouveau mot de passe : " + newPassword);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Implémenter la réinitialisation avec BCrypt
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Nouveau mot de passe : " + newPassword + "\nN'oubliez pas de le communiquer à l'utilisateur !");
            }
        });
    }

    @FXML
    private void onSearchUser() {
        String texte = searchUserField.getText().toLowerCase().trim();
        List<Utilisateur> tous = utilisateurDAO.findAll();
        utilisateurs.clear();

        if (texte.isEmpty()) {
            utilisateurs.addAll(tous);
        } else {
            for (Utilisateur u : tous) {
                boolean correspond =
                        (u.getNom() != null && u.getNom().toLowerCase().contains(texte)) ||
                                (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(texte)) ||
                                (u.getEmail() != null && u.getEmail().toLowerCase().contains(texte)) ||
                                (u.getRole() != null && u.getRole().toLowerCase().contains(texte)) ||
                                String.valueOf(u.getId()).contains(texte);

                if (correspond) {
                    utilisateurs.add(u);
                }
            }
        }
    }

    @FXML
    private void onRefreshUsers() {
        searchUserField.clear();
        loadUsers();
    }

    // ===== SYSTEM SETTINGS =====

    @FXML
    private void onSaveSettings() {
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Paramètres système sauvegardés !");
    }

    @FXML
    private void onToggleMaintenance() {
        if (maintenanceModeCheckBox.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Mode maintenance");
            alert.setHeaderText("Activer le mode maintenance ?");
            alert.setContentText("Cela empêchera les utilisateurs de se connecter.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    showAlert(Alert.AlertType.INFORMATION, "Mode maintenance", "Mode maintenance activé");
                } else {
                    maintenanceModeCheckBox.setSelected(false);
                }
            });
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Mode maintenance", "Mode maintenance désactivé");
        }
    }

    @FXML
    private void onBackupDatabase() {
        showAlert(Alert.AlertType.INFORMATION, "Sauvegarde",
                "Sauvegarde de la base de données en cours...\nFichier: backup_" +
                        java.time.LocalDate.now() + ".sql");
    }

    @FXML
    private void onClearCache() {
        showAlert(Alert.AlertType.INFORMATION, "Cache", "Cache système vidé !");
    }

    // ===== LOGS =====

    @FXML
    private void onRefreshLogs() {
        loadLogs();
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Logs actualisés");
    }

    @FXML
    private void onExportLogs() {
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Logs exportés : logs_" + java.time.LocalDate.now() + ".csv");
    }

    @FXML
    private void onClearLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer tous les logs ?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert(Alert.AlertType.INFORMATION, "Suppression", "Logs supprimés");
            }
        });
    }

    // ===== STATS =====

    @FXML
    private void onExportStats() {
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Statistiques exportées : stats_" + java.time.LocalDate.now() + ".pdf");
    }

    @FXML
    private void onGenerateReport() {
        showAlert(Alert.AlertType.INFORMATION, "Rapport",
                "Rapport mensuel généré : rapport_" + java.time.LocalDate.now() + ".pdf");
    }

    // ===== LOGOUT =====

    @FXML
    private void onLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) userNameLabel.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 700));
                    stage.setTitle("Connexion - HSP Urgences");
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la connexion");
                }
            }
        });
    }

    // ===== UTILS =====

    public void setAdminInfo(int id, String name) {
        this.currentAdminId = id;
        this.currentAdminName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erreur fermeture : " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}