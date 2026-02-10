package com.hsp.controller.patient;

import com.hsp.model.Patient;
import com.hsp.service.AuthService;
import com.hsp.service.PatientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PatientListController implements Initializable {

    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient, String> colId;
    @FXML private TableColumn<Patient, String> colNom;
    @FXML private TableColumn<Patient, String> colPrenom;
    @FXML private TableColumn<Patient, String> colNumSecu;
    @FXML private TableColumn<Patient, String> colEmail;
    @FXML private TableColumn<Patient, String> colTelephone;
    @FXML private TextField champRecherche;
    @FXML private Button btnNouveau;
    @FXML private Label lblTitre;
    @FXML private Label lblCount;

    private PatientService patientService;
    private AuthService authService;
    private ObservableList<Patient> listePatients;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        patientService = new PatientService();
        authService = new AuthService();
        listePatients = FXCollections.observableArrayList();

        configurerColonnes();
        chargerPatients();
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_patient())));

        colNom.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom() != null ? cellData.getValue().getNom() : "-"));

        colPrenom.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrenom() != null ? cellData.getValue().getPrenom() : "-"));

        colNumSecu.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNum_secu() != null ? cellData.getValue().getNum_secu() : "-"));

        colEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : "-"));

        colTelephone.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTelephone() != null ? cellData.getValue().getTelephone() : "-"));

        tablePatients.setItems(listePatients);

        // Double-clic pour ouvrir le detail
        tablePatients.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Patient selected = tablePatients.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirDetail(selected);
                }
            }
        });
    }

    private void chargerPatients() {
        listePatients.clear();
        try {
            List<Patient> patients = patientService.getAllPatients();
            listePatients.addAll(patients);
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur de connexion",
                    "Impossible de se connecter a la base de donnees.\nVerifiez que MySQL est demarre.");
        }
        mettreAJourCompteur();
    }

    @FXML
    private void rechercher() {
        String recherche = champRecherche.getText();
        if (recherche == null || recherche.trim().isEmpty()) {
            chargerPatients();
            return;
        }

        listePatients.clear();
        List<Patient> resultats = patientService.rechercherPatients(recherche);
        listePatients.addAll(resultats);
        mettreAJourCompteur();
    }

    @FXML
    private void nouveauPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient/PatientForm.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.setMode("creation");

            Stage stage = (Stage) tablePatients.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void modifierPatient() {
        Patient selected = tablePatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez selectionner un patient.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient/PatientForm.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.chargerPatient(selected.getId_patient());

            Stage stage = (Stage) tablePatients.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerPatient() {
        Patient selected = tablePatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez selectionner un patient.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer ce patient ?");
        confirmation.setContentText("Le patient " + selected.getNom() + " " + selected.getPrenom() +
                " sera definitivement supprime.\nCette action est irreversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int idUtilisateur = 0;
            if (authService.isConnecte()) {
                idUtilisateur = authService.getUtilisateurConnecte().getId();
            }

            boolean succes = patientService.supprimerPatient(selected.getId_patient(), idUtilisateur);

            if (succes) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Le patient a ete supprime.");
                chargerPatients();
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer le patient.\nVerifiez qu'il n'a pas de dossiers associes.");
            }
        }
    }

    @FXML
    private void voirDetail() {
        Patient selected = tablePatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez selectionner un patient.");
            return;
        }
        ouvrirDetail(selected);
    }

    private void ouvrirDetail(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient/PatientDetail.fxml"));
            Parent root = loader.load();

            PatientDetailController controller = loader.getController();
            controller.chargerPatient(patient.getId_patient());

            Stage stage = (Stage) tablePatients.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le detail: " + e.getMessage());
        }
    }

    @FXML
    private void actualiser() {
        champRecherche.clear();
        chargerPatients();
    }

    private void mettreAJourCompteur() {
        lblCount.setText(listePatients.size() + " patient(s)");
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
