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
import java.util.ResourceBundle;

public class SecretaireDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label todayCountLabel;
    @FXML private VBox dashboardView;
    @FXML private VBox registrationView;
    @FXML private VBox patientListView;
    @FXML private VBox triageView;
    @FXML private Label todayPatientsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private Label triagedPatientsLabel;
    @FXML private TableView<?> recentPatientsTable;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextArea motifTextArea;
    @FXML private TextField searchField;
    @FXML private TableView<?> allPatientsTable;
    @FXML private TableView<?> waitingTriageTable;

    private String currentSecretaireName = "Secrétaire";
    private int currentSecretaireId = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (userNameLabel != null) userNameLabel.setText(currentSecretaireName);
        if (sexeComboBox != null) sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");
        loadDashboardData();
        showDashboard();
    }

    @FXML private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) { dashboardView.setVisible(true); dashboardView.setManaged(true); }
        loadDashboardData();
    }

    @FXML private void showPatientRegistration() {
        hideAllViews();
        if (registrationView != null) { registrationView.setVisible(true); registrationView.setManaged(true); }
        clearRegistrationForm();
    }

    @FXML private void showPatientList() {
        hideAllViews();
        if (patientListView != null) { patientListView.setVisible(true); patientListView.setManaged(true); }
    }

    @FXML private void showTriage() {
        hideAllViews();
        if (triageView != null) { triageView.setVisible(true); triageView.setManaged(true); }
    }

    private void hideAllViews() {
        if (dashboardView != null) { dashboardView.setVisible(false); dashboardView.setManaged(false); }
        if (registrationView != null) { registrationView.setVisible(false); registrationView.setManaged(false); }
        if (patientListView != null) { patientListView.setVisible(false); patientListView.setManaged(false); }
        if (triageView != null) { triageView.setVisible(false); triageView.setManaged(false); }
    }

    private void loadDashboardData() {
        if (todayPatientsLabel != null) todayPatientsLabel.setText("24");
        if (waitingPatientsLabel != null) waitingPatientsLabel.setText("8");
        if (triagedPatientsLabel != null) triagedPatientsLabel.setText("16");
        if (todayCountLabel != null) todayCountLabel.setText("24");
    }

    @FXML
    private void onSavePatient() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire");
            return;
        }
        if (prenomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le prénom est obligatoire");
            return;
        }
        if (dateNaissancePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La date de naissance est obligatoire");
            return;
        }
        if (sexeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le sexe est obligatoire");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Patient " + prenomField.getText() + " " + nomField.getText() + " enregistré !");
        clearRegistrationForm();
        showDashboard();
    }

    private void clearRegistrationForm() {
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (dateNaissancePicker != null) dateNaissancePicker.setValue(null);
        if (sexeComboBox != null) sexeComboBox.setValue(null);
        if (telephoneField != null) telephoneField.clear();
        if (adresseField != null) adresseField.clear();
        if (motifTextArea != null) motifTextArea.clear();
    }

    @FXML private void onRefreshList() {
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Liste actualisée");
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
                    stage.setScene(new Scene(root, 900, 700));
                    stage.setTitle("Connexion - HSP Urgences");
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion");
                }
            }
        });
    }

    public void setSecretaireInfo(int id, String name) {
        this.currentSecretaireId = id;
        this.currentSecretaireName = name;
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
