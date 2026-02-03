package com.hsp.controller.dossier;

import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Dossier;
import com.hsp.model.Patient;
import com.hsp.model.Utilisateur;
import com.hsp.service.AuthService;
import com.hsp.service.DossierService;
import com.hsp.service.PatientService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class DossierDetailController implements Initializable {

    // En-tete
    @FXML private Label lblTitre;
    @FXML private Label lblStatut;

    // Infos dossier
    @FXML private Label lblIdDossier;
    @FXML private Label lblDateArrivee;
    @FXML private Label lblGravite;
    @FXML private Label lblStatutInfo;
    @FXML private Label lblMedecin;
    @FXML private Label lblDateCreation;
    @FXML private Label lblSymptomes;

    // Infos patient
    @FXML private Label lblPatientNom;
    @FXML private Label lblPatientPrenom;
    @FXML private Label lblPatientNumSecu;
    @FXML private Label lblPatientEmail;
    @FXML private Label lblPatientTelephone;
    @FXML private Label lblPatientAdresse;

    // Boutons actions
    @FXML private HBox boxActionsMedicales;
    @FXML private Button btnPrendreEnCharge;
    @FXML private Button btnOrdonnance;
    @FXML private Button btnHospitaliser;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private DossierService dossierService;
    private PatientService patientService;
    private AuthService authService;
    private UtilisateurDAO utilisateurDAO;

    private Dossier dossierCourant;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dossierService = new DossierService();
        patientService = new PatientService();
        authService = new AuthService();
        utilisateurDAO = new UtilisateurDAO();
    }

    public void chargerDossier(int idDossier) {
        dossierCourant = dossierService.getDossierById(idDossier);
        if (dossierCourant == null) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Dossier introuvable.");
            return;
        }

        afficherInfosDossier();
        afficherInfosPatient();
        configurerBoutons();
    }

    private void afficherInfosDossier() {
        lblTitre.setText("Dossier #" + dossierCourant.getId_dossier());
        lblIdDossier.setText(String.valueOf(dossierCourant.getId_dossier()));
        lblDateArrivee.setText(dossierCourant.getDate_arrivee() != null ? dossierCourant.getDate_arrivee() : "-");
        lblDateCreation.setText(dossierCourant.getDate_creation() != null ? dossierCourant.getDate_creation() : "-");
        lblSymptomes.setText(dossierCourant.getSymptomes() != null ? dossierCourant.getSymptomes() : "-");

        // Gravite
        String gravite = dossierCourant.getNiveau_gravite();
        if (gravite != null) {
            String libelleGravite;
            if ("1".equals(gravite)) libelleGravite = "1/5 - Peu grave";
            else if ("2".equals(gravite)) libelleGravite = "2/5 - Mineur";
            else if ("3".equals(gravite)) libelleGravite = "3/5 - Modere";
            else if ("4".equals(gravite)) libelleGravite = "4/5 - Grave";
            else if ("5".equals(gravite)) libelleGravite = "5/5 - Urgence vitale";
            else libelleGravite = gravite + "/5";
            lblGravite.setText(libelleGravite);
        } else {
            lblGravite.setText("-");
        }

        // Statut
        String statut = dossierCourant.getStatut();
        String statutFormate = formaterStatut(statut);
        lblStatutInfo.setText(statutFormate);
        lblStatut.setText(statutFormate);

        // Couleur du badge statut
        if ("en_attente".equals(statut)) {
            lblStatut.setStyle(lblStatut.getStyle() + " -fx-background-color: #FFF3E0; -fx-text-fill: #E65100;");
        } else if ("en_cours".equals(statut)) {
            lblStatut.setStyle(lblStatut.getStyle() + " -fx-background-color: #E3F2FD; -fx-text-fill: #1565C0;");
        } else if ("termine".equals(statut)) {
            lblStatut.setStyle(lblStatut.getStyle() + " -fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;");
        } else if ("hospitalise".equals(statut)) {
            lblStatut.setStyle(lblStatut.getStyle() + " -fx-background-color: #FCE4EC; -fx-text-fill: #C62828;");
        }

        // Medecin
        int idMedecin = dossierCourant.getId_medecin();
        if (idMedecin > 0) {
            Utilisateur medecin = utilisateurDAO.findById(idMedecin);
            if (medecin != null) {
                lblMedecin.setText("Dr. " + medecin.getNom() + " " + medecin.getPrenom());
            } else {
                lblMedecin.setText("ID: " + idMedecin);
            }
        } else {
            lblMedecin.setText("Non assigne");
        }
    }

    private void afficherInfosPatient() {
        Patient patient = patientService.getPatientById(dossierCourant.getId_patient());
        if (patient != null) {
            lblPatientNom.setText(patient.getNom() != null ? patient.getNom() : "-");
            lblPatientPrenom.setText(patient.getPrenom() != null ? patient.getPrenom() : "-");
            lblPatientNumSecu.setText(patient.getNum_secu() != null ? patient.getNum_secu() : "-");
            lblPatientEmail.setText(patient.getEmail() != null ? patient.getEmail() : "-");
            lblPatientTelephone.setText(patient.getTelephone() != null ? patient.getTelephone() : "-");
            lblPatientAdresse.setText(patient.getAdresse() != null ? patient.getAdresse() : "-");
        } else {
            lblPatientNom.setText("Patient introuvable");
            lblPatientPrenom.setText("-");
            lblPatientNumSecu.setText("-");
            lblPatientEmail.setText("-");
            lblPatientTelephone.setText("-");
            lblPatientAdresse.setText("-");
        }
    }

    private void configurerBoutons() {
        String statut = dossierCourant.getStatut();

        // "Prendre en charge" uniquement si en_attente
        btnPrendreEnCharge.setVisible("en_attente".equals(statut));
        btnPrendreEnCharge.setManaged("en_attente".equals(statut));

        // "Ordonnance" et "Hospitaliser" uniquement si en_cours
        btnOrdonnance.setVisible("en_cours".equals(statut));
        btnOrdonnance.setManaged("en_cours".equals(statut));
        btnHospitaliser.setVisible("en_cours".equals(statut));
        btnHospitaliser.setManaged("en_cours".equals(statut));

        // Supprimer desactive si le dossier a des hospitalisations
        boolean aHospitalisations = dossierService.aDesHospitalisations(dossierCourant.getId_dossier());
        btnSupprimer.setDisable(aHospitalisations);
        if (aHospitalisations) {
            btnSupprimer.setTooltip(new Tooltip("Impossible de supprimer : des hospitalisations sont associees"));
        }

        // Modifier desactive si termine ou hospitalise
        boolean estTermineOuHospitalise = "termine".equals(statut) || "hospitalise".equals(statut);
        btnModifier.setDisable(estTermineOuHospitalise);
    }

    @FXML
    private void prendreEnCharge() {
        if (dossierCourant == null) return;

        // Demander confirmation avec saisie de l'ID medecin
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Prise en charge");
        dialog.setHeaderText("Prendre en charge ce dossier");
        dialog.setContentText("Entrez votre ID medecin :");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int idMedecin = Integer.parseInt(result.get().trim());
                boolean succes = dossierService.prendreEnCharge(dossierCourant.getId_dossier(), idMedecin);

                if (succes) {
                    afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Le dossier a ete pris en charge.");
                    chargerDossier(dossierCourant.getId_dossier());
                } else {
                    afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de prendre en charge. Verifiez que le dossier est bien en attente.");
                }
            } catch (NumberFormatException e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "L'ID medecin doit etre un nombre.");
            }
        }
    }

    @FXML
    private void cloturerOrdonnance() {
        if (dossierCourant == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cloture avec ordonnance");
        confirmation.setHeaderText("Cloturer ce dossier avec une ordonnance ?");
        confirmation.setContentText("Le patient sortira de l'hopital. Cette action est irreversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int idMedecin = dossierCourant.getId_medecin();
            boolean succes = dossierService.cloturerAvecOrdonnance(dossierCourant.getId_dossier(), idMedecin);

            if (succes) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succes",
                        "Le dossier a ete cloture. Le patient peut sortir.");
                chargerDossier(dossierCourant.getId_dossier());
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de cloturer le dossier.");
            }
        }
    }

    @FXML
    private void hospitaliser() {
        if (dossierCourant == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Hospitalisation");
        confirmation.setHeaderText("Hospitaliser ce patient ?");
        confirmation.setContentText("Le dossier sera marque comme hospitalise. " +
                "Vous devrez ensuite creer une hospitalisation depuis le module Hospitalisations.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int idMedecin = dossierCourant.getId_medecin();
            boolean succes = dossierService.marquerHospitalise(dossierCourant.getId_dossier(), idMedecin);

            if (succes) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succes",
                        "Le dossier a ete marque comme hospitalise. " +
                                "Pensez a creer l'hospitalisation dans le module correspondant.");
                chargerDossier(dossierCourant.getId_dossier());
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'hospitaliser le patient.");
            }
        }
    }

    @FXML
    private void modifierDossier() {
        if (dossierCourant == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dossier/DossierForm.fxml"));
            Parent root = loader.load();

            DossierFormController controller = loader.getController();
            controller.chargerDossier(dossierCourant.getId_dossier());

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerDossier() {
        if (dossierCourant == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer ce dossier ?");
        confirmation.setContentText("Cette action est irreversible. Le dossier #" +
                dossierCourant.getId_dossier() + " sera definitivement supprime.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int idUtilisateur = 0;
            if (authService.isConnecte()) {
                idUtilisateur = authService.getUtilisateurConnecte().getId();
            }

            boolean succes = dossierService.supprimerDossier(dossierCourant.getId_dossier(), idUtilisateur);

            if (succes) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succes", "Le dossier a ete supprime.");
                retourListe();
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer le dossier. " +
                                "Verifiez qu'il n'a pas d'hospitalisations associees.");
            }
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
