package com.hsp.controller.ordonnance;

import com.hsp.model.Dossier;
import com.hsp.model.Ordonnance;
import com.hsp.model.Patient;
import com.hsp.model.Utilisateur;
import com.hsp.service.AuthService;
import com.hsp.service.OrdonnanceService;
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

public class OrdonnanceListController implements Initializable {

    @FXML private TableView<Ordonnance> tableOrdonnances;
    @FXML private TableColumn<Ordonnance, String> colId;
    @FXML private TableColumn<Ordonnance, String> colPatient;
    @FXML private TableColumn<Ordonnance, String> colMedecin;
    @FXML private TableColumn<Ordonnance, String> colDate;
    @FXML private TableColumn<Ordonnance, String> colMedicaments;
    @FXML private TableColumn<Ordonnance, String> colDuree;
    @FXML private TableColumn<Ordonnance, String> colStatut;
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private Label lblTitre;
    @FXML private Label lblCount;

    private OrdonnanceService ordonnanceService;
    private AuthService authService;
    private ObservableList<Ordonnance> listeOrdonnances;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ordonnanceService = new OrdonnanceService();
        authService = new AuthService();
        listeOrdonnances = FXCollections.observableArrayList();

        configurerColonnes();
        configurerFiltre();
        chargerOrdonnances();
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_ordonnance())));

        colPatient.setCellValueFactory(cellData -> {
            Patient patient = ordonnanceService.getPatientDeLOrdonnance(cellData.getValue());
            if (patient != null) {
                return new SimpleStringProperty(patient.getNom() + " " + patient.getPrenom());
            }
            return new SimpleStringProperty("Inconnu");
        });

        colMedecin.setCellValueFactory(cellData -> {
            Utilisateur medecin = ordonnanceService.getMedecinDeLOrdonnance(cellData.getValue());
            if (medecin != null) {
                return new SimpleStringProperty("Dr. " + medecin.getNom() + " " + medecin.getPrenom());
            }
            return new SimpleStringProperty("-");
        });

        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_ordonnance() != null
                        ? cellData.getValue().getDate_ordonnance() : "-"));

        colMedicaments.setCellValueFactory(cellData -> {
            String med = cellData.getValue().getMedicaments();
            if (med != null && med.length() > 50) {
                med = med.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(med != null ? med : "-");
        });

        colDuree.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDuree_traitement() != null
                        ? cellData.getValue().getDuree_traitement() : "-"));

        colStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(formaterStatut(statut));
        });

        tableOrdonnances.setItems(listeOrdonnances);

        tableOrdonnances.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirModification(selected);
                }
            }
        });
    }

    private void configurerFiltre() {
        comboFiltre.setItems(FXCollections.observableArrayList("Toutes", "Active", "Terminee"));
        comboFiltre.setValue("Toutes");
        comboFiltre.setOnAction(e -> filtrerOrdonnances());
    }

    private void chargerOrdonnances() {
        listeOrdonnances.clear();
        try {
            List<Ordonnance> ordonnances = ordonnanceService.getAllOrdonnances();
            listeOrdonnances.addAll(ordonnances);
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur de connexion",
                    "Impossible de se connecter a la base de donnees.\nVerifiez que MySQL est demarre.");
        }
        mettreAJourCompteur();
    }

    @FXML
    private void filtrerOrdonnances() {
        String filtre = comboFiltre.getValue();
        listeOrdonnances.clear();

        List<Ordonnance> toutes = ordonnanceService.getAllOrdonnances();

        if (filtre == null || "Toutes".equals(filtre)) {
            listeOrdonnances.addAll(toutes);
        } else {
            String statutFiltre = "Active".equals(filtre) ? "active" : "terminee";
            for (Ordonnance o : toutes) {
                if (statutFiltre.equals(o.getStatut())) {
                    listeOrdonnances.add(o);
                }
            }
        }
        mettreAJourCompteur();
    }

    @FXML
    private void rechercher() {
        String recherche = champRecherche.getText();
        if (recherche == null || recherche.trim().isEmpty()) {
            chargerOrdonnances();
            return;
        }

        listeOrdonnances.clear();
        List<Ordonnance> resultats = ordonnanceService.rechercherOrdonnances(recherche);
        listeOrdonnances.addAll(resultats);
        mettreAJourCompteur();
    }

    @FXML
    private void nouvelleOrdonnance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ordonnance/OrdonnanceForm.fxml"));
            Parent root = loader.load();

            OrdonnanceFormController controller = loader.getController();
            controller.setMode("creation");

            Stage stage = (Stage) tableOrdonnances.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void modifierOrdonnance() {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez selectionner une ordonnance.");
            return;
        }
        ouvrirModification(selected);
    }

    private void ouvrirModification(Ordonnance ordonnance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ordonnance/OrdonnanceForm.fxml"));
            Parent root = loader.load();

            OrdonnanceFormController controller = loader.getController();
            controller.chargerOrdonnance(ordonnance.getId_ordonnance());

            Stage stage = (Stage) tableOrdonnances.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerOrdonnance() {
        Ordonnance selected = tableOrdonnances.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez selectionner une ordonnance.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer cette ordonnance ?");
        confirmation.setContentText("L'ordonnance #" + selected.getId_ordonnance() + " sera definitivement supprimee.\nCette action est irreversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int idUtilisateur = 0;
            if (authService.isConnecte()) {
                idUtilisateur = authService.getUtilisateurConnecte().getId();
            }

            boolean succes = ordonnanceService.supprimerOrdonnance(selected.getId_ordonnance(), idUtilisateur);

            if (succes) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "L'ordonnance a ete supprimee.");
                chargerOrdonnances();
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'ordonnance.");
            }
        }
    }

    @FXML
    private void actualiser() {
        champRecherche.clear();
        comboFiltre.setValue("Toutes");
        chargerOrdonnances();
    }

    private void mettreAJourCompteur() {
        lblCount.setText(listeOrdonnances.size() + " ordonnance(s)");
    }

    private String formaterStatut(String statut) {
        if (statut == null) return "-";
        if ("active".equals(statut)) return "Active";
        if ("terminee".equals(statut)) return "Terminee";
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
