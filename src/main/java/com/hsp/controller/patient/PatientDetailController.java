package com.hsp.controller.patient;

import com.hsp.controller.dossier.DossierDetailController;
import com.hsp.controller.dossier.DossierFormController;
import com.hsp.model.Dossier;
import com.hsp.model.Patient;
import com.hsp.service.AuthService;
import com.hsp.service.DossierService;
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
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PatientDetailController implements Initializable {

    // En-tete
    @FXML private Label lblTitre;
    @FXML private Label lblDateCreation;

    // Infos patient
    @FXML private Label lblId;
    @FXML private Label lblNom;
    @FXML private Label lblPrenom;
    @FXML private Label lblNumSecu;
    @FXML private Label lblEmail;
    @FXML private Label lblTelephone;
    @FXML private Label lblAdresse;

    // Tableau dossiers
    @FXML private TableView<Dossier> tableDossiers;
    @FXML private TableColumn<Dossier, String> colDossierId;
    @FXML private TableColumn<Dossier, String> colDossierDate;
    @FXML private TableColumn<Dossier, String> colDossierSymptomes;
    @FXML private TableColumn<Dossier, String> colDossierGravite;
    @FXML private TableColumn<Dossier, String> colDossierStatut;
    @FXML private Label lblDossierCount;

    // Boutons
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private PatientService patientService;
    private DossierService dossierService;
    private AuthService authService;

    private Patient patientCourant;
    private ObservableList<Dossier> listeDossiers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        patientService = new PatientService();
        dossierService = new DossierService();
        authService = new AuthService();
        listeDossiers = FXCollections.observableArrayList();

        configurerColonnesDossiers();
    }

    private void configurerColonnesDossiers() {
        colDossierId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_dossier())));

        colDossierDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_arrivee() != null ? cellData.getValue().getDate_arrivee() : "-"));

        colDossierSymptomes.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSymptomes() != null ? cellData.getValue().getSymptomes() : "-"));

        colDossierGravite.setCellValueFactory(cellData -> {
            String gravite = cellData.getValue().getNiveau_gravite();
            return new SimpleStringProperty(gravite != null ? gravite + "/5" : "-");
        });

        colDossierStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(formaterStatut(statut));
        });

        tableDossiers.setItems(listeDossiers);

        // Double-clic pour ouvrir le detail du dossier
        tableDossiers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Dossier selected = tableDossiers.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirDetailDossier(selected);
                }
            }
        });
    }

    public void chargerPatient(int idPatient) {
        patientCourant = patientService.getPatientById(idPatient);
        if (patientCourant == null) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Patient introuvable.");
            return;
        }

        afficherInfosPatient();
        chargerDossiers();
        configurerBoutons();
    }

    private void afficherInfosPatient() {
        lblTitre.setText(patientCourant.getNom() + " " + patientCourant.getPrenom());
        lblId.setText(String.valueOf(patientCourant.getId_patient()));
        lblNom.setText(patientCourant.getNom() != null ? patientCourant.getNom() : "-");
        lblPrenom.setText(patientCourant.getPrenom() != null ? patientCourant.getPrenom() : "-");
        lblNumSecu.setText(patientCourant.getNum_secu() != null ? patientCourant.getNum_secu() : "-");
        lblEmail.setText(patientCourant.getEmail() != null && !patientCourant.getEmail().isEmpty() ? patientCourant.getEmail() : "-");
        lblTelephone.setText(patientCourant.getTelephone() != null && !patientCourant.getTelephone().isEmpty() ? patientCourant.getTelephone() : "-");
        lblAdresse.setText(patientCourant.getAdresse() != null && !patientCourant.getAdresse().isEmpty() ? patientCourant.getAdresse() : "-");

        // Date de creation
        if (patientCourant.getDate_creation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lblDateCreation.setText("Cree le " + patientCourant.getDate_creation().format(formatter));
        } else {
            lblDateCreation.setText("");
        }
    }

    private void chargerDossiers() {
        listeDossiers.clear();
        List<Dossier> dossiers = patientService.getDossiersPatient(patientCourant.getId_patient());
        if (dossiers != null) {
            listeDossiers.addAll(dossiers);
        }
        lblDossierCount.setText(listeDossiers.size() + " dossier(s)");
    }

    private void configurerBoutons() {
        // Desactiver la suppression si le patient a des dossiers
        boolean aDossiers = !listeDossiers.isEmpty();
        btnSupprimer.setDisable(aDossiers);
        if (aDossiers) {
            btnSupprimer.setTooltip(new Tooltip("Impossible de supprimer : des dossiers sont associes"));
        }
    }

    @FXML
    private void modifierPatient() {
        if (patientCourant == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient/PatientForm.fxml"));
            Parent root = loader.load();

            PatientFormController controller = loader.getController();
            controller.chargerPatient(patientCourant.getId_patient());

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerPatient() {
        if (patientCourant == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer ce patient ?");
        confirmation.setContentText("Le patient " + patientCourant.getNom() + " " + patientCourant.getPrenom() +
                " sera definitivement supprime.\nCette action est irreversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int idUtilisateur = 0;
            if (authService.isConnecte()) {
                idUtilisateur = authService.getUtilisateurConnecte().getId();
            }

            boolean succes = patientService.supprimerPatient(patientCourant.getId_patient(), idUtilisateur);

            if (succes) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Le patient a ete supprime.");
                retourListe();
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer le patient.\nVerifiez qu'il n'a pas de dossiers associes.");
            }
        }
    }

    @FXML
    private void nouveauDossier() {
        if (patientCourant == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier/DossierForm.fxml"));
            Parent root = loader.load();

            DossierFormController controller = loader.getController();
            controller.setMode("creation");
            controller.preselectionnerPatient(patientCourant.getId_patient());

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire dossier: " + e.getMessage());
        }
    }

    private void ouvrirDetailDossier(Dossier dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier/DossierDetail.fxml"));
            Parent root = loader.load();

            DossierDetailController controller = loader.getController();
            controller.chargerDossier(dossier.getId_dossier());

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le detail du dossier: " + e.getMessage());
        }
    }

    @FXML
    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/patient/PatientList.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner a la liste: " + e.getMessage());
        }
    }

    private String formaterStatut(String statut) {
        if (statut == null) return "-";
        if ("en_attente".equals(statut)) return "En attente";
        if ("en_cours".equals(statut)) return "En cours";
        if ("termine".equals(statut)) return "Termine";
        if ("hospitalise".equals(statut)) return "Hospitalise";
        return statut;
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
