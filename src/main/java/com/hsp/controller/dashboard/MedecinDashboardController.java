package com.hsp.controller.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MedecinDashboardController implements Initializable {

    // ============= FXML COMPONENTS =============
    @FXML private Label userNameLabel;
    @FXML private Label waitingCountLabel;
    @FXML private Label hospitalizationCountLabel;
    @FXML private VBox dashboardView;
    @FXML private VBox waitingPatientsView;
    @FXML private VBox hospitalizationsView;
    @FXML private VBox productRequestsView;
    @FXML private VBox treatmentView;
    @FXML private Label todayPatientsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private Label hospitalizationsLabel;
    @FXML private Label productRequestsLabel;
    @FXML private TableView<?> urgentPatientsTable;
    @FXML private ComboBox<String> gravityFilterComboBox;
    @FXML private TableView<?> waitingPatientsTable;
    @FXML private TableView<?> activeHospitalizationsTable;
    @FXML private TableView<?> completedHospitalizationsTable;
    @FXML private TableView<?> myRequestsTable;
    @FXML private Label patientNameLabel;
    @FXML private Label arrivalDateLabel;
    @FXML private Label gravityLevelLabel;
    @FXML private TextArea symptomsTextArea;
    @FXML private RadioButton ordonnanceRadio;
    @FXML private RadioButton hospitalizationRadio;
    @FXML private VBox hospitalizationDetails;
    @FXML private ComboBox<String> chambreComboBox;
    @FXML private TextArea maladieTextArea;

    // ============= DATA =============
    private ToggleGroup treatmentGroup;
    private String currentMedecinName = "Dr. Martin";
    private int currentMedecinId = 1;

    // Base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALISATION DASHBOARD ===");

        // Initialiser les composants
        if (userNameLabel != null) userNameLabel.setText(currentMedecinName);

        // ToggleGroup
        if (ordonnanceRadio != null && hospitalizationRadio != null) {
            treatmentGroup = new ToggleGroup();
            ordonnanceRadio.setToggleGroup(treatmentGroup);
            hospitalizationRadio.setToggleGroup(treatmentGroup);
            hospitalizationRadio.selectedProperty().addListener((obs, old, val) -> {
                if (hospitalizationDetails != null) hospitalizationDetails.setVisible(val);
            });
        }

        // ComboBox
        if (gravityFilterComboBox != null) {
            gravityFilterComboBox.getItems().addAll("Tous", "Gravité 5", "Gravité 4", "Gravité 3", "Gravité 2", "Gravité 1");
            gravityFilterComboBox.setValue("Tous");
        }

        if (chambreComboBox != null) {
            chambreComboBox.getItems().addAll("101", "102", "103", "201", "202", "203");
        }

        // Charger les données
        loadDashboardData();
        showDashboard();

        System.out.println("=== INITIALISATION TERMINÉE ===");
    }

    // ============= NAVIGATION =============

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
    private void showWaitingPatients() {
        hideAllViews();
        if (waitingPatientsView != null) {
            waitingPatientsView.setVisible(true);
            waitingPatientsView.setManaged(true);
        }
    }

    @FXML
    private void showHospitalizations() {
        hideAllViews();
        if (hospitalizationsView != null) {
            hospitalizationsView.setVisible(true);
            hospitalizationsView.setManaged(true);
        }
    }

    @FXML
    private void showProductRequests() {
        hideAllViews();
        if (productRequestsView != null) {
            productRequestsView.setVisible(true);
            productRequestsView.setManaged(true);
        }
    }

    @FXML
    private void showHistory() {
        showAlert(Alert.AlertType.INFORMATION, "Historique", "Fonctionnalité en cours de développement");
    }

    private void hideAllViews() {
        if (dashboardView != null) { dashboardView.setVisible(false); dashboardView.setManaged(false); }
        if (waitingPatientsView != null) { waitingPatientsView.setVisible(false); waitingPatientsView.setManaged(false); }
        if (hospitalizationsView != null) { hospitalizationsView.setVisible(false); hospitalizationsView.setManaged(false); }
        if (productRequestsView != null) { productRequestsView.setVisible(false); productRequestsView.setManaged(false); }
        if (treatmentView != null) { treatmentView.setVisible(false); treatmentView.setManaged(false); }
    }

    // ============= DATA LOADING =============

    private void loadDashboardData() {
        // Pour l'instant : données statiques
        if (todayPatientsLabel != null) todayPatientsLabel.setText("12");
        if (waitingPatientsLabel != null) waitingPatientsLabel.setText("5");
        if (hospitalizationsLabel != null) hospitalizationsLabel.setText("3");
        if (productRequestsLabel != null) productRequestsLabel.setText("2");
        if (waitingCountLabel != null) waitingCountLabel.setText("5");
        if (hospitalizationCountLabel != null) hospitalizationCountLabel.setText("3");

        System.out.println("✓ Données chargées (mode démonstration)");
    }

    @FXML
    private void onRefreshWaiting() {
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Liste actualisée");
    }

    @FXML
    private void onNewHospitalization() {
        showAlert(Alert.AlertType.INFORMATION, "Nouvelle hospitalisation", "Sélectionnez un patient en attente");
    }

    @FXML
    private void onNewProductRequest() {
        showAlert(Alert.AlertType.INFORMATION, "Nouvelle demande", "Formulaire en développement");
    }

    @FXML
    private void onValidateTreatment() {
        if (treatmentGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une décision");
            return;
        }

        if (hospitalizationRadio.isSelected()) {
            if (chambreComboBox.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une chambre");
                return;
            }
            if (maladieTextArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Décrivez la maladie");
                return;
            }
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient hospitalisé");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance délivrée");
        }

        showDashboard();
    }

    @FXML
    private void onCancelTreatment() {
        showWaitingPatients();
    }

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
                    Scene scene = new Scene(root, 900, 700);
                    stage.setScene(scene);
                    stage.setTitle("Connexion - HSP Urgences");
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de connexion");
                    e.printStackTrace();
                }
            }
        });
    }

    // ============= UTILS =============

    public void setMedecinInfo(int id, String name) {
        this.currentMedecinId = id;
        this.currentMedecinName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}