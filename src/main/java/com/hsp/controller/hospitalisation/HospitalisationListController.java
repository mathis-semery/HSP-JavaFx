package com.hsp.controller.hospitalisation;

import com.hsp.dao.ChambreDAO;
import com.hsp.dao.DossierDAO;
import com.hsp.dao.HospitalisationDAO;
import com.hsp.dao.PatientDAO;
import com.hsp.model.Chambre;
import com.hsp.model.Dossier;
import com.hsp.model.Hospitalisation;
import com.hsp.model.Patient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class HospitalisationListController implements Initializable {

    @FXML
    private TableView<Hospitalisation> table;

    @FXML
    private TableColumn<Hospitalisation, String> idCol;

    @FXML
    private TableColumn<Hospitalisation, String> dossierCol;

    @FXML
    private TableColumn<Hospitalisation, String> chambreCol;

    @FXML
    private TableColumn<Hospitalisation, String> idMedecinCol;

    @FXML
    private TableColumn<Hospitalisation, String> dateDebutCol;

    @FXML
    private TableColumn<Hospitalisation, String> dateFinCol;

    @FXML
    private TableColumn<Hospitalisation, String> statutCol;

    @FXML
    private TextField recherche;

    @FXML
    private Button ajouter;

    @FXML
    private Button modifier;

    @FXML
    private Button supprimer;

    @FXML
    private Button details;

    @FXML
    private ComboBox<String> filtre;

    private HospitalisationDAO hospitalisationDAO;
    private DossierDAO dossierDAO;
    private ChambreDAO chambreDAO;
    private PatientDAO patientDAO;
    private ObservableList<Hospitalisation> hospitalisations;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hospitalisationDAO = new HospitalisationDAO();
        dossierDAO = new DossierDAO();
        chambreDAO = new ChambreDAO();
        patientDAO = new PatientDAO();
        hospitalisations = FXCollections.observableArrayList();

        configurerColonnes();
        configurerFiltre();
        chargerHospitalisations();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        details.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                afficherDetails();
            }
        });
    }

    private void configurerColonnes() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_hospitalisation())));

        dossierCol.setCellValueFactory(cellData -> {
            int idDossier = cellData.getValue().getId_dossier();
            Dossier dossier = dossierDAO.findById(idDossier);
            String texte = "Dossier #" + idDossier;
            if (dossier != null) {
                Patient patient = patientDAO.findById(dossier.getId_patient());
                if (patient != null) {
                    texte += " - " + patient.getNom() + " " + patient.getPrenom();
                }
            }
            return new SimpleStringProperty(texte);
        });

        chambreCol.setCellValueFactory(cellData -> {
            int idChambre = cellData.getValue().getId_chambre();
            Chambre chambre = chambreDAO.findById(idChambre);
            String texte = "Chambre #" + idChambre;
            if (chambre != null && chambre.getNumero() != null) {
                texte = "Chambre " + chambre.getNumero();
            }
            return new SimpleStringProperty(texte);
        });

        idMedecinCol.setCellValueFactory(cellData -> {
            int idMedecin = cellData.getValue().getId_medecin();
            return new SimpleStringProperty("Médecin #" + idMedecin);
        });

        dateDebutCol.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getDate_debut() != null ?
                    cellData.getValue().getDate_debut().toString() : "N/A";
            return new SimpleStringProperty(date);
        });

        dateFinCol.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getDate_fin() != null ?
                    cellData.getValue().getDate_fin().toString() : "En cours";
            return new SimpleStringProperty(date);
        });

        statutCol.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getDate_fin() != null ? "Terminée" : "En cours";
            return new SimpleStringProperty(statut);
        });

        table.setItems(hospitalisations);
    }

    private void configurerFiltre() {
        if (filtre != null) {
            filtre.setItems(FXCollections.observableArrayList("Toutes", "En cours", "Terminées"));
            filtre.setValue("Toutes");
            filtre.setOnAction(event -> filtrerHospitalisations());
        }
    }

    private void chargerHospitalisations() {
        List<Hospitalisation> liste = hospitalisationDAO.findAll();
        hospitalisations.clear();
        hospitalisations.addAll(liste);
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
        List<Hospitalisation> toutes = hospitalisationDAO.findAll();

        hospitalisations.clear();

        if (texte.isEmpty()) {
            hospitalisations.addAll(toutes);
        } else {
            for (Hospitalisation hosp : toutes) {
                boolean correspond = false;

                // Recherche par ID
                if (String.valueOf(hosp.getId_hospitalisation()).contains(texte)) {
                    correspond = true;
                }

                // Recherche par numéro de dossier
                if (String.valueOf(hosp.getId_dossier()).contains(texte)) {
                    correspond = true;
                }

                // Recherche par nom de patient
                Dossier dossier = dossierDAO.findById(hosp.getId_dossier());
                if (dossier != null) {
                    Patient patient = patientDAO.findById(dossier.getId_patient());
                    if (patient != null) {
                        String nomComplet = (patient.getNom() + " " + patient.getPrenom()).toLowerCase();
                        if (nomComplet.contains(texte)) {
                            correspond = true;
                        }
                    }
                }

                // Recherche par numéro de chambre
                Chambre chambre = chambreDAO.findById(hosp.getId_chambre());
                if (chambre != null && chambre.getNumero() != null &&
                        chambre.getNumero().toLowerCase().contains(texte)) {
                    correspond = true;
                }

                if (correspond) {
                    hospitalisations.add(hosp);
                }
            }
        }

        appliquerFiltre();
    }

    @FXML
    private void filtrerHospitalisations() {
        rechercher();
    }

    private void appliquerFiltre() {
        if (filtre == null || "Toutes".equals(filtre.getValue())) {
            return;
        }

        String valeur = filtre.getValue();
        hospitalisations.removeIf(hosp -> {
            boolean estEnCours = hosp.getDate_fin() == null;
            if ("En cours".equals(valeur)) {
                return !estEnCours;
            } else if ("Terminées".equals(valeur)) {
                return estEnCours;
            }
            return false;
        });
    }

    @FXML
    private void ajouterHospitalisation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hsp/view/hospitalisation/hospitalisation-form.fxml"));
            Parent root = loader.load();

            HospitalisationFormController controller = loader.getController();
            controller.setMode(HospitalisationFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle hospitalisation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerHospitalisations();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierHospitalisation() {
        Hospitalisation selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hsp/view/hospitalisation/hospitalisation-form.fxml"));
            Parent root = loader.load();

            HospitalisationFormController controller = loader.getController();
            controller.setMode(HospitalisationFormController.Mode.MODIFICATION);
            controller.setHospitalisation(selection);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'hospitalisation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerHospitalisations();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerHospitalisation() {
        Hospitalisation selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'hospitalisation #" + selection.getId_hospitalisation() + " ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = hospitalisationDAO.delete(selection.getId_hospitalisation());
            if (succes) {
                chargerHospitalisations();
                afficherInfo("Succès", "L'hospitalisation a été supprimée.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer l'hospitalisation.");
            }
        }
    }

    @FXML
    private void afficherDetails() {
        Hospitalisation selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hsp/view/hospitalisation/hospitalisation-detail.fxml"));
            Parent root = loader.load();

            HospitalisationDetailController controller = loader.getController();
            controller.setHospitalisation(selection);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'hospitalisation #" + selection.getId_hospitalisation());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture des détails", e.getMessage());
        }
    }

    @FXML
    private void rafraichir() {
        recherche.clear();
        if (filtre != null) {
            filtre.setValue("Toutes");
        }
        chargerHospitalisations();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}