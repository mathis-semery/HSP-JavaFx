package com.hsp.controller.dossier;

import com.hsp.model.Dossier;
import com.hsp.model.Patient;
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
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DossierListController implements Initializable {

    @FXML private TableView<Dossier> tableDossiers;
    @FXML private TableColumn<Dossier, String> colId;
    @FXML private TableColumn<Dossier, String> colPatient;
    @FXML private TableColumn<Dossier, String> colDateArrivee;
    @FXML private TableColumn<Dossier, String> colSymptomes;
    @FXML private TableColumn<Dossier, String> colGravite;
    @FXML private TableColumn<Dossier, String> colStatut;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private TextField champRecherche;
    @FXML private Button btnNouveau;
    @FXML private Label lblTitre;
    @FXML private Label lblCount;

    private DossierService dossierService;
    private PatientService patientService;
    private ObservableList<Dossier> listeDossiers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dossierService = new DossierService();
        patientService = new PatientService();
        listeDossiers = FXCollections.observableArrayList();

        configurerColonnes();
        configurerFiltre();
        chargerDossiers();
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_dossier())));

        colPatient.setCellValueFactory(cellData -> {
            Patient patient = patientService.getPatientById(cellData.getValue().getId_patient());
            if (patient != null) {
                return new SimpleStringProperty(patient.getNom() + " " + patient.getPrenom());
            }
            return new SimpleStringProperty("Inconnu");
        });

        colDateArrivee.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_arrivee()));

        colSymptomes.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSymptomes()));

        colGravite.setCellValueFactory(cellData -> {
            String gravite = cellData.getValue().getNiveau_gravite();
            return new SimpleStringProperty(gravite != null ? gravite + "/5" : "-");
        });

        colStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(formaterStatut(statut));
        });

        tableDossiers.setItems(listeDossiers);

        // Double-clic pour ouvrir le detail
        tableDossiers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Dossier selected = tableDossiers.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirDetail(selected);
                }
            }
        });
    }

    private void configurerFiltre() {
        comboFiltre.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "En cours", "Termine", "Hospitalise"
        ));
        comboFiltre.setValue("Tous");
        comboFiltre.setOnAction(e -> filtrerDossiers());
    }

    private void chargerDossiers() {
        listeDossiers.clear();
        List<Dossier> dossiers = dossierService.getAllDossiers();
        listeDossiers.addAll(dossiers);
        mettreAJourCompteur();
    }

    @FXML
    private void filtrerDossiers() {
        String filtre = comboFiltre.getValue();
        listeDossiers.clear();

        List<Dossier> dossiers;
        if (filtre == null || "Tous".equals(filtre)) {
            dossiers = dossierService.getAllDossiers();
        } else if ("En attente".equals(filtre)) {
            dossiers = dossierService.getDossiersEnAttente();
        } else if ("En cours".equals(filtre)) {
            dossiers = dossierService.getDossiersEnCours();
        } else if ("Termine".equals(filtre)) {
            dossiers = dossierService.getDossiersParStatut("termine");
        } else if ("Hospitalise".equals(filtre)) {
            dossiers = dossierService.getDossiersParStatut("hospitalise");
        } else {
            dossiers = dossierService.getAllDossiers();
        }

        listeDossiers.addAll(dossiers);
        mettreAJourCompteur();
    }

    @FXML
    private void rechercher() {
        String recherche = champRecherche.getText();
        if (recherche == null || recherche.trim().isEmpty()) {
            chargerDossiers();
            return;
        }

        listeDossiers.clear();
        List<Patient> patients = patientService.rechercherPatients(recherche);
        for (Patient patient : patients) {
            List<Dossier> dossiersPatient = dossierService.getDossiersParPatient(patient.getId_patient());
            listeDossiers.addAll(dossiersPatient);
        }
        mettreAJourCompteur();
    }

    @FXML
    private void nouveauDossier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier/DossierForm.fxml"));
            Parent root = loader.load();

            DossierFormController controller = loader.getController();
            controller.setMode("creation");

            Stage stage = (Stage) tableDossiers.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void voirDetail() {
        Dossier selected = tableDossiers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez selectionner un dossier.");
            return;
        }
        ouvrirDetail(selected);
    }

    private void ouvrirDetail(Dossier dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier/DossierDetail.fxml"));
            Parent root = loader.load();

            DossierDetailController controller = loader.getController();
            controller.chargerDossier(dossier.getId_dossier());

            Stage stage = (Stage) tableDossiers.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le detail: " + e.getMessage());
        }
    }

    @FXML
    private void actualiser() {
        comboFiltre.setValue("Tous");
        champRecherche.clear();
        chargerDossiers();
    }

    private void mettreAJourCompteur() {
        lblCount.setText(listeDossiers.size() + " dossier(s)");
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
