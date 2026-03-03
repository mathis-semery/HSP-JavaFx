package com.hsp.controller.ordonnance;

import com.hsp.model.Dossier;
import com.hsp.model.Ordonnance;
import com.hsp.model.Patient;
import com.hsp.model.Utilisateur;
import com.hsp.service.AuthService;
import com.hsp.service.OrdonnanceService;
import com.hsp.service.PatientService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class OrdonnanceFormController implements Initializable {

    @FXML private Label lblTitre;
    @FXML private ComboBox<DossierItem> comboDossier;
    @FXML private DatePicker dateOrdonnance;
    @FXML private TextArea champMedicaments;
    @FXML private TextArea champPosologie;
    @FXML private TextField champDuree;
    @FXML private TextArea champNotes;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Label lblMessage;
    @FXML private Button btnEnregistrer;

    private OrdonnanceService ordonnanceService;
    private AuthService authService;

    private String mode = "creation";
    private Ordonnance ordonnanceEnEdition;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ordonnanceService = new OrdonnanceService();
        authService = new AuthService();

        configurerComboDossier();
        configurerComboStatut();
        dateOrdonnance.setValue(LocalDate.now());
    }

    private void configurerComboDossier() {
        List<Dossier> dossiers = ordonnanceService.getTousDossiers();
        comboDossier.getItems().clear();
        for (Dossier d : dossiers) {
            comboDossier.getItems().add(new DossierItem(d, ordonnanceService));
        }
    }

    private void configurerComboStatut() {
        comboStatut.setItems(FXCollections.observableArrayList("active", "terminee"));
        comboStatut.setValue("active");
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("creation".equals(mode)) {
            lblTitre.setText("Nouvelle ordonnance");
            btnEnregistrer.setText("Creer l'ordonnance");
        } else {
            lblTitre.setText("Modifier l'ordonnance");
            btnEnregistrer.setText("Enregistrer les modifications");
        }
    }

    public void chargerOrdonnance(int idOrdonnance) {
        ordonnanceEnEdition = ordonnanceService.getOrdonnanceById(idOrdonnance);
        if (ordonnanceEnEdition == null) return;

        setMode("modification");

        // Selectionner le dossier correspondant
        for (DossierItem item : comboDossier.getItems()) {
            if (item.getDossier().getId_dossier() == ordonnanceEnEdition.getId_dossier()) {
                comboDossier.setValue(item);
                break;
            }
        }

        // Remplir la date
        if (ordonnanceEnEdition.getDate_ordonnance() != null && !ordonnanceEnEdition.getDate_ordonnance().isEmpty()) {
            try {
                String datePart = ordonnanceEnEdition.getDate_ordonnance().split(" ")[0];
                dateOrdonnance.setValue(LocalDate.parse(datePart));
            } catch (Exception e) {
                dateOrdonnance.setValue(LocalDate.now());
            }
        }

        champMedicaments.setText(ordonnanceEnEdition.getMedicaments());
        champPosologie.setText(ordonnanceEnEdition.getPosologie());
        champDuree.setText(ordonnanceEnEdition.getDuree_traitement());
        champNotes.setText(ordonnanceEnEdition.getNotes());
        comboStatut.setValue(ordonnanceEnEdition.getStatut() != null ? ordonnanceEnEdition.getStatut() : "active");
    }

    public void setDossierPreselectionne(int idDossier) {
        for (DossierItem item : comboDossier.getItems()) {
            if (item.getDossier().getId_dossier() == idDossier) {
                comboDossier.setValue(item);
                comboDossier.setDisable(true);
                break;
            }
        }
    }

    @FXML
    private void enregistrer() {
        lblMessage.setText("");

        DossierItem dossierItem = comboDossier.getValue();
        String medicaments = champMedicaments.getText();
        String posologie = champPosologie.getText();
        String duree = champDuree.getText();
        String notes = champNotes.getText();
        String statut = comboStatut.getValue();
        LocalDate date = dateOrdonnance.getValue();

        if (dossierItem == null) {
            lblMessage.setText("Veuillez selectionner un dossier.");
            return;
        }
        if (medicaments == null || medicaments.trim().isEmpty()) {
            lblMessage.setText("Les medicaments sont obligatoires.");
            return;
        }
        if (posologie == null || posologie.trim().isEmpty()) {
            lblMessage.setText("La posologie est obligatoire.");
            return;
        }
        if (date == null) {
            lblMessage.setText("La date de l'ordonnance est obligatoire.");
            return;
        }

        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";

        int idUtilisateur = 0;
        int idMedecin = 0;
        if (authService.isConnecte()) {
            idUtilisateur = authService.getUtilisateurConnecte().getId();
            idMedecin = idUtilisateur;
        }

        boolean resultat;

        if ("creation".equals(mode)) {
            Ordonnance nouvelle = new Ordonnance(
                    0,
                    dossierItem.getDossier().getId_dossier(),
                    idMedecin,
                    dateStr,
                    medicaments.trim(),
                    posologie.trim(),
                    duree != null ? duree.trim() : "",
                    notes != null ? notes.trim() : "",
                    statut != null ? statut : "active",
                    null
            );
            resultat = ordonnanceService.creerOrdonnance(nouvelle, idUtilisateur);
        } else {
            ordonnanceEnEdition.setId_dossier(dossierItem.getDossier().getId_dossier());
            ordonnanceEnEdition.setDate_ordonnance(dateStr);
            ordonnanceEnEdition.setMedicaments(medicaments.trim());
            ordonnanceEnEdition.setPosologie(posologie.trim());
            ordonnanceEnEdition.setDuree_traitement(duree != null ? duree.trim() : "");
            ordonnanceEnEdition.setNotes(notes != null ? notes.trim() : "");
            ordonnanceEnEdition.setStatut(statut != null ? statut : "active");
            resultat = ordonnanceService.modifierOrdonnance(ordonnanceEnEdition, idUtilisateur);
        }

        if (resultat) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes",
                    "creation".equals(mode) ? "L'ordonnance a ete creee avec succes." : "L'ordonnance a ete modifiee avec succes.");
            retourListe();
        } else {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer l'ordonnance. Verifiez les donnees.");
        }
    }

    @FXML
    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ordonnance/OrdonnanceList.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner a la liste: " + e.getMessage());
        }
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe interne pour afficher les dossiers dans le ComboBox
    public static class DossierItem {
        private Dossier dossier;
        private String label;

        public DossierItem(Dossier dossier, OrdonnanceService service) {
            this.dossier = dossier;
            Patient patient = service.getPatientParIdDossier(dossier.getId_dossier());
            if (patient != null) {
                this.label = "Dossier #" + dossier.getId_dossier() + " - " + patient.getNom() + " " + patient.getPrenom();
            } else {
                this.label = "Dossier #" + dossier.getId_dossier();
            }
        }

        public Dossier getDossier() {
            return dossier;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
