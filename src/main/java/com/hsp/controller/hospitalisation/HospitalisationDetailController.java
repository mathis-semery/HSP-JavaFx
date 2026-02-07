package com.hsp.controller.hospitalisation;

import com.hsp.dao.ChambreDAO;
import com.hsp.dao.DossierDAO;
import com.hsp.dao.PatientDAO;
import com.hsp.model.Chambre;
import com.hsp.model.Dossier;
import com.hsp.model.Hospitalisation;
import com.hsp.model.Patient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HospitalisationDetailController implements Initializable {

    @FXML
    private Label idHospitalisation;

    @FXML
    private Label dossier;

    @FXML
    private Label chambre;

    @FXML
    private Label idMedecin;

    @FXML
    private Label dateDebut;

    @FXML
    private Label dateFin;

    @FXML
    private Label statut;

    @FXML
    private Label descriptionMaladie;

    private Hospitalisation hospitalisation;
    private DossierDAO dossierDAO;
    private ChambreDAO chambreDAO;
    private PatientDAO patientDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dossierDAO = new DossierDAO();
        chambreDAO = new ChambreDAO();
        patientDAO = new PatientDAO();
    }

    public void setHospitalisation(Hospitalisation hospitalisation) {
        this.hospitalisation = hospitalisation;
        afficherDetails();
    }

    private void afficherDetails() {
        if (hospitalisation == null) {
            return;
        }

        idHospitalisation.setText(String.valueOf(hospitalisation.getId_hospitalisation()));

        // Afficher les informations du dossier et du patient
        Dossier d = dossierDAO.findById(hospitalisation.getId_dossier());
        if (d != null) {
            Patient patient = patientDAO.findById(d.getId_patient());
            String nomPatient = "N/A";
            if (patient != null) {
                nomPatient = patient.getNom() + " " + patient.getPrenom();
            }
            dossier.setText("Dossier #" + d.getId_dossier() + " - Patient: " + nomPatient);
        } else {
            dossier.setText("Dossier #" + hospitalisation.getId_dossier());
        }

        // Afficher les informations de la chambre
        Chambre c = chambreDAO.findById(hospitalisation.getId_chambre());
        if (c != null) {
            chambre.setText("Chambre " + c.getNumero() + " - Étage " + c.getEtage());
        } else {
            chambre.setText("Chambre #" + hospitalisation.getId_chambre());
        }

        // Afficher l'ID du médecin
        if (idMedecin != null) {
            idMedecin.setText("Médecin #" + hospitalisation.getId_medecin());
        }

        // Afficher les dates
        dateDebut.setText(hospitalisation.getDate_debut() != null ?
                hospitalisation.getDate_debut().toString() : "N/A");

        if (hospitalisation.getDate_fin() != null) {
            dateFin.setText(hospitalisation.getDate_fin().toString());
            statut.setText("Terminée");
            statut.setStyle("-fx-text-fill: gray;");
        } else {
            dateFin.setText("En cours");
            statut.setText("En cours");
            statut.setStyle("-fx-text-fill: green;");
        }

        // Afficher la description
        if (descriptionMaladie != null) {
            String description = hospitalisation.getDescription_maladie();
            descriptionMaladie.setText(description != null && !description.trim().isEmpty() ?
                    description : "Aucune description");
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) idHospitalisation.getScene().getWindow();
        stage.close();
    }
}