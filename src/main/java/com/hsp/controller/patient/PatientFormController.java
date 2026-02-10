package com.hsp.controller.patient;

import com.hsp.model.Patient;
import com.hsp.service.AuthService;
import com.hsp.service.PatientService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PatientFormController implements Initializable {

    @FXML private Label lblTitre;
    @FXML private TextField champNom;
    @FXML private TextField champPrenom;
    @FXML private TextField champNumSecu;
    @FXML private TextField champEmail;
    @FXML private TextField champTelephone;
    @FXML private TextArea champAdresse;
    @FXML private Label lblMessage;
    @FXML private Button btnEnregistrer;

    private PatientService patientService;
    private AuthService authService;

    private String mode = "creation";
    private Patient patientEnEdition;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        patientService = new PatientService();
        authService = new AuthService();
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("creation".equals(mode)) {
            lblTitre.setText("Nouveau patient");
            btnEnregistrer.setText("Creer le patient");
        } else {
            lblTitre.setText("Modifier le patient");
            btnEnregistrer.setText("Enregistrer les modifications");
        }
    }

    public void chargerPatient(int idPatient) {
        patientEnEdition = patientService.getPatientById(idPatient);
        if (patientEnEdition == null) return;

        setMode("modification");

        // Pre-remplir les champs
        champNom.setText(patientEnEdition.getNom());
        champPrenom.setText(patientEnEdition.getPrenom());
        champNumSecu.setText(patientEnEdition.getNum_secu());
        champEmail.setText(patientEnEdition.getEmail());
        champTelephone.setText(patientEnEdition.getTelephone());
        champAdresse.setText(patientEnEdition.getAdresse());
    }

    @FXML
    private void enregistrer() {
        // Reinitialiser le message
        lblMessage.setText("");

        // Validation des champs obligatoires
        String nom = champNom.getText();
        String prenom = champPrenom.getText();
        String numSecu = champNumSecu.getText();
        String email = champEmail.getText();
        String telephone = champTelephone.getText();
        String adresse = champAdresse.getText();

        if (nom == null || nom.trim().isEmpty()) {
            lblMessage.setText("Le nom est obligatoire.");
            return;
        }
        if (prenom == null || prenom.trim().isEmpty()) {
            lblMessage.setText("Le prenom est obligatoire.");
            return;
        }
        if (numSecu == null || numSecu.trim().isEmpty()) {
            lblMessage.setText("Le numero de securite sociale est obligatoire.");
            return;
        }

        // Verification unicite du numero de securite sociale
        int excludeId = (patientEnEdition != null) ? patientEnEdition.getId_patient() : -1;
        if (patientService.numSecuExiste(numSecu.trim(), excludeId)) {
            lblMessage.setText("Ce numero de securite sociale est deja utilise par un autre patient.");
            return;
        }

        // Validation format email si renseigne
        if (email != null && !email.trim().isEmpty()) {
            if (!email.contains("@") || !email.contains(".")) {
                lblMessage.setText("Le format de l'email est invalide.");
                return;
            }
        }

        // ID utilisateur connecte
        int idUtilisateur = 0;
        if (authService.isConnecte()) {
            idUtilisateur = authService.getUtilisateurConnecte().getId();
        }

        boolean resultat;

        if ("creation".equals(mode)) {
            Patient nouveauPatient = new Patient(
                    0,
                    nom.trim(),
                    prenom.trim(),
                    numSecu.trim(),
                    email != null ? email.trim() : "",
                    telephone != null ? telephone.trim() : "",
                    adresse != null ? adresse.trim() : "",
                    null
            );
            resultat = patientService.creerPatient(nouveauPatient, idUtilisateur);
        } else {
            patientEnEdition.setNom(nom.trim());
            patientEnEdition.setPrenom(prenom.trim());
            patientEnEdition.setNum_secu(numSecu.trim());
            patientEnEdition.setEmail(email != null ? email.trim() : "");
            patientEnEdition.setTelephone(telephone != null ? telephone.trim() : "");
            patientEnEdition.setAdresse(adresse != null ? adresse.trim() : "");
            resultat = patientService.modifierPatient(patientEnEdition, idUtilisateur);
        }

        if (resultat) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Succes",
                    "creation".equals(mode) ? "Le patient a ete cree avec succes." : "Le patient a ete modifie avec succes.");
            retourListe();
        } else {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'enregistrer le patient. Verifiez les donnees.");
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

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
