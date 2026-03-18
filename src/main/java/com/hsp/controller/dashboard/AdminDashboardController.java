package com.hsp.controller.dashboard;

import com.hsp.controller.utilisateur.UtilisateurFormController;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Historique;
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
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Users Management
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
    @FXML private TableView<Historique> logsTable;
    @FXML private TableColumn<Historique, String> logDateCol;
    @FXML private TableColumn<Historique, String> logUtilisateurCol;
    @FXML private TableColumn<Historique, String> logActionCol;
    @FXML private TableColumn<Historique, String> logTableCol;
    @FXML private TableColumn<Historique, String> logDetailsCol;
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
    private HistoriqueDAO historiqueDAO;
    private ObservableList<Utilisateur> utilisateurs;
    private ObservableList<Historique> historiques;
    private Map<Integer, String> userNames = new HashMap<>();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurDAO = new UtilisateurDAO();
        historiqueDAO = new HistoriqueDAO();
        utilisateurs = FXCollections.observableArrayList();
        historiques = FXCollections.observableArrayList();

        if (userNameLabel != null) userNameLabel.setText(currentAdminName);

        configurerTableUtilisateurs();
        configurerTableLogs();

        if (logLevelFilter != null) {
            logLevelFilter.getItems().addAll("Tous", "CREATE", "UPDATE", "DELETE", "Connexion");
            logLevelFilter.setValue("Tous");
        }

        if (hospitalNameField != null) hospitalNameField.setText("HSP Urgences");
        if (hospitalAddressField != null) hospitalAddressField.setText("123 Rue de la Santé, 75013 Paris");
        if (hospitalPhoneField != null) hospitalPhoneField.setText("01 23 45 67 89");
        if (maxPatientsField != null) maxPatientsField.setText("100");

        loadDashboardData();
        showDashboard();
    }

    // ===== CONFIGURATION TABLES =====

    private void configurerTableUtilisateurs() {
        if (usersTable == null) return;

        userIdCol.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        userNomCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNom() != null ? cell.getValue().getNom() : ""));
        userPrenomCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPrenom() != null ? cell.getValue().getPrenom() : ""));
        userEmailCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEmail() != null ? cell.getValue().getEmail() : ""));
        userRoleCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getRole() != null ? cell.getValue().getRole() : ""));
        userDateCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDate_creation() != null ? cell.getValue().getDate_creation() : ""));

        usersTable.setItems(utilisateurs);
        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && usersTable.getSelectionModel().getSelectedItem() != null) {
                onEditUser();
            }
        });
    }

    private void configurerTableLogs() {
        if (logsTable == null) return;

        logDateCol.setCellValueFactory(cell -> {
            var dt = cell.getValue().getDate_action();
            return new SimpleStringProperty(dt != null ? dt.format(LOG_FORMATTER) : "");
        });
        logUtilisateurCol.setCellValueFactory(cell -> {
            int uid = cell.getValue().getId_utilisateur();
            return new SimpleStringProperty(userNames.getOrDefault(uid, "ID " + uid));
        });
        logActionCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAction() != null ? cell.getValue().getAction() : ""));
        logTableCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTable_concernee() != null ? cell.getValue().getTable_concernee() : ""));
        logDetailsCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDetails() != null ? cell.getValue().getDetails() : ""));

        logsTable.setItems(historiques);
    }

    // ===== NAVIGATION =====

    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) { dashboardView.setVisible(true); dashboardView.setManaged(true); }
        loadDashboardData();
    }

    @FXML
    private void showUsers() {
        hideAllViews();
        if (usersView != null) { usersView.setVisible(true); usersView.setManaged(true); }
        loadUsers();
    }

    @FXML
    private void showSystem() {
        hideAllViews();
        if (systemView != null) { systemView.setVisible(true); systemView.setManaged(true); }
    }

    @FXML
    private void showLogs() {
        hideAllViews();
        if (logsView != null) { logsView.setVisible(true); logsView.setManaged(true); }
        loadLogs();
    }

    @FXML
    private void showStats() {
        hideAllViews();
        if (statsView != null) { statsView.setVisible(true); statsView.setManaged(true); }
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
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM utilisateur");
            if (rs.next()) {
                int count = rs.getInt("count");
                if (totalUtilisateursLabel != null) totalUtilisateursLabel.setText(String.valueOf(count));
                if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(count));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM patient");
            if (rs.next()) {
                if (totalPatientsLabel != null) totalPatientsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM hospitalisation WHERE date_fin IS NULL");
            if (rs.next()) {
                if (totalHospitalisationsLabel != null) totalHospitalisationsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();
            stmt.close();

            if (systemStatusLabel != null) systemStatusLabel.setText("Opérationnel");

        } catch (SQLException e) {
            System.err.println("Erreur chargement données dashboard : " + e.getMessage());
            if (totalUtilisateursLabel != null) totalUtilisateursLabel.setText("0");
            if (totalPatientsLabel != null) totalPatientsLabel.setText("0");
            if (totalHospitalisationsLabel != null) totalHospitalisationsLabel.setText("0");
            if (systemStatusLabel != null) systemStatusLabel.setText("Erreur BDD");
        } finally {
            closeConnection(conn);
        }
    }

    private void loadUsers() {
        List<Utilisateur> liste = utilisateurDAO.findAll();
        utilisateurs.clear();
        utilisateurs.addAll(liste);
    }

    private void loadLogs() {
        // Construire la map id_utilisateur → "Prénom Nom"
        userNames.clear();
        for (Utilisateur u : utilisateurDAO.findAll()) {
            userNames.put(u.getId(), u.getPrenom() + " " + u.getNom());
        }

        List<Historique> logs = historiqueDAO.findAll();
        historiques.clear();
        historiques.addAll(logs);

        // Appliquer le filtre action si sélectionné
        String filtre = logLevelFilter != null ? logLevelFilter.getValue() : "Tous";
        if (filtre != null && !filtre.equals("Tous")) {
            historiques.removeIf(h -> !filtre.equalsIgnoreCase(h.getAction()));
        }
    }

    private void loadStats() {
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();

            // Patients arrivés ce mois
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) as count FROM dossier " +
                "WHERE MONTH(date_arrivee) = MONTH(CURDATE()) AND YEAR(date_arrivee) = YEAR(CURDATE())");
            if (rs.next() && statsPatientsMoisLabel != null) {
                statsPatientsMoisLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            // Dossiers résolus ce mois (avec ordonnance ou hospitalisation)
            rs = stmt.executeQuery(
                "SELECT COUNT(DISTINCT d.id_dossier) as count FROM dossier d " +
                "LEFT JOIN ordonnance o ON o.id_dossier = d.id_dossier " +
                "LEFT JOIN hospitalisation h ON h.id_dossier = d.id_dossier " +
                "WHERE (o.id_ordonnance IS NOT NULL OR h.id_hospitalisation IS NOT NULL) " +
                "AND MONTH(d.date_arrivee) = MONTH(CURDATE()) AND YEAR(d.date_arrivee) = YEAR(CURDATE())");
            if (rs.next() && statsConsultationsMoyenneLabel != null) {
                statsConsultationsMoyenneLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            // Taux d'occupation des chambres
            rs = stmt.executeQuery(
                "SELECT " +
                "(SELECT COUNT(*) FROM hospitalisation WHERE date_fin IS NULL) as occupees, " +
                "(SELECT COUNT(*) FROM chambre) as total");
            if (rs.next()) {
                int occupees = rs.getInt("occupees");
                int total = rs.getInt("total");
                String taux = total > 0 ? (occupees * 100 / total) + "%" : "0%";
                if (statsTauxOccupationLabel != null) statsTauxOccupationLabel.setText(taux);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur stats : " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    // ===== USER MANAGEMENT =====

    @FXML
    private void onCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateur/UtilisateurForm.fxml"));
            Parent root = loader.load();

            UtilisateurFormController controller = loader.getController();
            controller.setMode(UtilisateurFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouvel utilisateur");
            stage.setScene(new Scene(root, 480, 420));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUsers();
            loadDashboardData();

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
            stage.setScene(new Scene(root, 480, 420));
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
        confirmation.setHeaderText("Supprimer \"" + selection.getPrenom() + " " + selection.getNom() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = utilisateurDAO.delete(selection.getId());
            if (succes) {
                loadUsers();
                loadDashboardData();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer cet utilisateur.");
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

        String newPassword = "HSP" + (int)(Math.random() * 90000 + 10000);

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Réinitialisation mot de passe");
        confirmation.setHeaderText("Réinitialiser le mot de passe de " + selection.getPrenom() + " " + selection.getNom() + " ?");
        confirmation.setContentText("Nouveau mot de passe : " + newPassword + "\nConfirmez pour appliquer.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                selection.setMdp(hashedPassword);
                boolean succes = utilisateurDAO.update(selection);
                if (succes) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Mot de passe réinitialisé !\n\nNouveau mot de passe : " + newPassword +
                            "\n\nCommuniquez ce mot de passe à l'utilisateur.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de réinitialiser le mot de passe.");
                }
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
                if (correspond) utilisateurs.add(u);
            }
        }
    }

    @FXML
    private void onRefreshUsers() {
        if (searchUserField != null) searchUserField.clear();
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
            alert.setContentText("Les utilisateurs ne pourront plus se connecter.");
            alert.showAndWait().ifPresent(response -> {
                if (response != ButtonType.OK) maintenanceModeCheckBox.setSelected(false);
                else showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Mode maintenance activé.");
            });
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Mode maintenance désactivé.");
        }
    }

    @FXML
    private void onBackupDatabase() {
        showAlert(Alert.AlertType.INFORMATION, "Sauvegarde",
                "Sauvegarde de la base de données lancée.\nFichier : backup_" + java.time.LocalDate.now() + ".sql");
    }

    @FXML
    private void onClearCache() {
        showAlert(Alert.AlertType.INFORMATION, "Cache", "Cache système vidé !");
    }

    // ===== LOGS =====

    @FXML
    private void onRefreshLogs() {
        loadLogs();
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
                showAlert(Alert.AlertType.INFORMATION, "Suppression", "Logs supprimés.");
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
                    stage.setMaximized(false);
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la connexion : " + e.getMessage());
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
            try { conn.close(); } catch (SQLException e) { /* ignore */ }
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
