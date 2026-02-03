package com.hsp.controller.dossier;

import com.hsp.model.Dossier;
import com.hsp.model.Patient;
import com.hsp.service.AuthService;
import com.hsp.service.DossierService;
import com.hsp.service.PatientService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DossierFormController implements Initializable {

    @FXML private Label lblTitre;
    @FXML private ComboBox<Patient> comboPatient;
    @FXML private TextField champDateArrivee;
    @FXML private TextArea champSymptomes;
    @FXML private ComboBox<String> comboGravite;
    @FXML private Label lblInfoGravite;
    @FXML private Button btnEnregistrer;

    private DossierService dossierService;
    private PatientService patientService;
    private AuthService authService;

    private String mode = "creation";
    private Dossier dossierEnEdition;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dossierService = new DossierService();
        patientService = new PatientService();
        authService = new AuthService();

        configurerComboPatient();
        configurerComboGravite();
    }

    private void configurerComboPatient() {
        List<Patient> patients = patientService.getAllPatients();
        comboPatient.setItems(FXCollections.observableArrayList(patients));

        comboPatient.setConverter(new StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                if (patient == null) return "";
                return patient.getNom() + " " + patient.getPrenom() + " (" + patient.getNum_secu() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });
    }

    private void configurerComboGravite() {
        comboGravite.setItems(FXCollections.observableArrayList(
                "1 - Peu grave",
                "2 - Mineur",
                "3 - Modere",
                "4 - Grave",
                "5 - Urgence vitale"
        ));
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("creation".equals(mode)) {
            lblTitre.setText("Nouveau dossier de prise en charge");
            btnEnregistrer.setText("Creer le dossier");
        } else {
            lblTitre.setText("Modifier le dossier");
            btnEnregistrer.setText("Enregistrer les modifications");
        }
    }

    public void chargerDossier(int idDossier) {
        dossierEnEdition = dossierService.getDossierById(idDossier);
        if (dossierEnEdition == null) return;

        setMode("modification");

        // Pre-remplir le patient
        Patient patient = patientService.getPatientById(dossierEnEdition.getId_patient());
        if (patient != null) {
            for (Patient p : comboPatient.getItems()) {
                if (p.getId_patient() == patient.getId_patient()) {
                    comboPatient.setValue(p);
                    break;
                }
            }
        }

        // Pre-remplir les champs
        champDateArrivee.setText(dossierEnEdition.getDate_arrivee());
        champSymptomes.setText(dossierEnEdition.getSymptomes());

        // Pre-remplir la gravite
        String gravite = dossierEnEdition.getNiveau_gravite();
        if (gravite != null) {
            for (String item : comboGravite.getItems()) {
                if (item.startsWith(gravite)) {
                    comboGravite.setValue(item);
                    break;
                }
            }
        }
    }

    @FXML
    private void enregistrer() {
        // Validation
        if (comboPatient.getValue() == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Champ requis", "Veuillez selectionner un patient.");
            return;
        }
        if (champSymptomes.getText() == null || champSymptomes.getText().trim().isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Champ requis", "Veuillez decrire les symptomes.");
            return;
        }
        if (comboGravite.getValue() == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Champ requis", "Veuillez selectionner le niveau de gravite.");
            return;
        }

        Patient patientSelectionne = comboPatient.getValue();
        String symptomes = champSymptomes.getText().trim();
        String gravite = comboGravite.getValue().substring(0, 1); // Extraire le chiffre

        // Date d'arrivee
        String dateArrivee = champDateArrivee.getText();
        if (dateArrivee == null || dateArrivee.trim().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateArrivee = LocalDateTime.now().format(formatter);
        }

        // ID utilisateur connecte (0 par defaut si pas de session active)
        int idUtilisateur = 0;
        if (authService.isConnecte()) {
            idUtilisateur = authService.getUtilisateurConnecte().getId();
        }

        boolean resultat;

        if ("creation".equals(mode)) {
            Dossier nouveauDossier = new Dossier(
                    0,
                    patientSelectionne.getId_patient(),
                    0,
                    dateArrivee,
                    symptomes,
                    gravite,
                    "en_attente",
                    null
            );
            resultat = dossierService.creerDossier(nouveauDossier, idUtilisateur);
        } else {
            dossierEnEdition.setId_patient(patientSelectionne.getId_patient());
            dossierEnEdition.setDate_arrivee(dateArrivee);
            dossierEnEdition.setSymptomes(symptomes);
            dossierEnEdition.setNiveau_gravite(gravite);
            resultat = dossierService.modifierDossier(dossierEnEdition, idUtilisateur);
        }

        if (resultat) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes",
                    "creation".equals(mode) ? "Le dossier a ete cree avec succes." : "Le dossier a ete modifie avec succes.");
            retourListe();
        } else {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'enregistrer le dossier. Verifiez les donnees.");
        }
    }

    @FXML
    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier/DossierList.fxml"));
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
}
