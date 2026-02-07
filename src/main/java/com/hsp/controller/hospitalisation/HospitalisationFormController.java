package com.hsp.controller.hospitalisation;

import com.hsp.dao.ChambreDAO;
import com.hsp.dao.DossierDAO;
import com.hsp.dao.HospitalisationDAO;
import com.hsp.dao.PatientDAO;
import com.hsp.model.Chambre;
import com.hsp.model.Dossier;
import com.hsp.model.Hospitalisation;
import com.hsp.model.Patient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class HospitalisationFormController implements Initializable {

    public enum Mode {
        CREATION,
        MODIFICATION
    }

    @FXML
    private ComboBox<Dossier> comboDossier;

    @FXML
    private ComboBox<Chambre> comboChambre;

    @FXML
    private TextField idMedecin;

    @FXML
    private DatePicker dateDebut;

    @FXML
    private DatePicker dateFin;

    @FXML
    private TextArea descriptionMaladie;

    @FXML
    private CheckBox enCours;

    @FXML
    private Button valider;

    @FXML
    private Button annuler;

    @FXML
    private Label titre;

    private Mode mode = Mode.CREATION;
    private Hospitalisation hospitalisation;
    private HospitalisationDAO hospitalisationDAO;
    private DossierDAO dossierDAO;
    private ChambreDAO chambreDAO;
    private PatientDAO patientDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hospitalisationDAO = new HospitalisationDAO();
        dossierDAO = new DossierDAO();
        chambreDAO = new ChambreDAO();
        patientDAO = new PatientDAO();

        chargerDossiers();
        chargerChambres();
        configurerDatePickers();
    }

    private void chargerDossiers() {
        if (comboDossier != null) {
            List<Dossier> dossiers = dossierDAO.findAll();
            comboDossier.getItems().addAll(dossiers);

            // Configurer l'affichage des dossiers
            comboDossier.setCellFactory(param -> new ListCell<Dossier>() {
                @Override
                protected void updateItem(Dossier item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        Patient patient = patientDAO.findById(item.getId_patient());
                        String nomPatient = "Patient";
                        if (patient != null) {
                            nomPatient = patient.getNom() + " " + patient.getPrenom();
                        }
                        setText("Dossier #" + item.getId_dossier() + " - " + nomPatient);
                    }
                }
            });

            comboDossier.setButtonCell(new ListCell<Dossier>() {
                @Override
                protected void updateItem(Dossier item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        Patient patient = patientDAO.findById(item.getId_patient());
                        String nomPatient = "Patient";
                        if (patient != null) {
                            nomPatient = patient.getNom() + " " + patient.getPrenom();
                        }
                        setText("Dossier #" + item.getId_dossier() + " - " + nomPatient);
                    }
                }
            });
        }
    }

    private void chargerChambres() {
        if (comboChambre != null) {
            List<Chambre> chambres = chambreDAO.findAll();
            comboChambre.getItems().addAll(chambres);

            // Configurer l'affichage des chambres
            comboChambre.setCellFactory(param -> new ListCell<Chambre>() {
                @Override
                protected void updateItem(Chambre item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String disponibilite = ("1".equals(item.getDisponible()) ||
                                "true".equalsIgnoreCase(item.getDisponible())) ?
                                "Disponible" : "Occupée";
                        setText("Chambre " + item.getNumero() + " - Étage " +
                                item.getEtage() + " (" + disponibilite + ")");
                    }
                }
            });

            comboChambre.setButtonCell(new ListCell<Chambre>() {
                @Override
                protected void updateItem(Chambre item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String disponibilite = ("1".equals(item.getDisponible()) ||
                                "true".equalsIgnoreCase(item.getDisponible())) ?
                                "Disponible" : "Occupée";
                        setText("Chambre " + item.getNumero() + " - Étage " +
                                item.getEtage() + " (" + disponibilite + ")");
                    }
                }
            });
        }
    }

    private void configurerDatePickers() {
        if (dateDebut != null) {
            dateDebut.setValue(LocalDate.now());
        }

        if (enCours != null && dateFin != null) {
            enCours.selectedProperty().addListener((obs, oldVal, newVal) -> {
                dateFin.setDisable(newVal);
                if (newVal) {
                    dateFin.setValue(null);
                }
            });
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        mettreAJourTitre();
    }

    public void setHospitalisation(Hospitalisation hospitalisation) {
        this.hospitalisation = hospitalisation;
        remplirFormulaire();
    }

    private void mettreAJourTitre() {
        if (titre != null) {
            if (mode == Mode.CREATION) {
                titre.setText("Nouvelle hospitalisation");
            } else {
                titre.setText("Modifier l'hospitalisation");
            }
        }
    }

    private void remplirFormulaire() {
        if (hospitalisation == null) {
            return;
        }

        // Sélectionner le dossier
        if (comboDossier != null) {
            for (Dossier d : comboDossier.getItems()) {
                if (d.getId_dossier() == hospitalisation.getId_dossier()) {
                    comboDossier.setValue(d);
                    break;
                }
            }
        }

        // Sélectionner la chambre
        if (comboChambre != null) {
            for (Chambre c : comboChambre.getItems()) {
                if (c.getId_chambre() == hospitalisation.getId_chambre()) {
                    comboChambre.setValue(c);
                    break;
                }
            }
        }

        // Remplir l'ID du médecin
        if (idMedecin != null) {
            idMedecin.setText(String.valueOf(hospitalisation.getId_medecin()));
        }

        // Remplir les dates
        if (dateDebut != null && hospitalisation.getDate_debut() != null) {
            dateDebut.setValue(hospitalisation.getDate_debut());
        }

        if (dateFin != null && hospitalisation.getDate_fin() != null) {
            dateFin.setValue(hospitalisation.getDate_fin());
            if (enCours != null) {
                enCours.setSelected(false);
            }
        } else {
            if (enCours != null) {
                enCours.setSelected(true);
            }
        }

        // Remplir la description
        if (descriptionMaladie != null && hospitalisation.getDescription_maladie() != null) {
            descriptionMaladie.setText(hospitalisation.getDescription_maladie());
        }
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) {
            return;
        }

        int idDossier = comboDossier.getValue().getId_dossier();
        int idChambre = comboChambre.getValue().getId_chambre();
        int medecinId = Integer.parseInt(idMedecin.getText().trim());
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = (enCours != null && enCours.isSelected()) ? null : dateFin.getValue();
        String description = descriptionMaladie != null ? descriptionMaladie.getText().trim() : "";

        boolean succes;

        if (mode == Mode.CREATION) {
            Hospitalisation nouvelleHospitalisation = new Hospitalisation(
                    0,
                    idDossier,
                    idChambre,
                    medecinId,
                    debut,
                    fin,
                    description,
                    LocalDateTime.now()
            );
            succes = hospitalisationDAO.insert(nouvelleHospitalisation);
        } else {
            hospitalisation.setId_dossier(idDossier);
            hospitalisation.setId_chambre(idChambre);
            hospitalisation.setId_medecin(medecinId);
            hospitalisation.setDate_debut(debut);
            hospitalisation.setDate_fin(fin);
            hospitalisation.setDescription_maladie(description);
            succes = hospitalisationDAO.update(hospitalisation);
        }

        if (succes) {
            fermer();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer l'hospitalisation.");
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (comboDossier == null || comboDossier.getValue() == null) {
            erreurs.append("- Le dossier est obligatoire\n");
        }

        if (comboChambre == null || comboChambre.getValue() == null) {
            erreurs.append("- La chambre est obligatoire\n");
        }

        if (idMedecin == null || idMedecin.getText().trim().isEmpty()) {
            erreurs.append("- L'ID du médecin est obligatoire\n");
        } else {
            try {
                int id = Integer.parseInt(idMedecin.getText().trim());
                if (id <= 0) {
                    erreurs.append("- L'ID du médecin doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("- L'ID du médecin doit être un nombre valide\n");
            }
        }

        if (dateDebut == null || dateDebut.getValue() == null) {
            erreurs.append("- La date de début est obligatoire\n");
        }

        if (enCours != null && !enCours.isSelected()) {
            if (dateFin != null && dateFin.getValue() != null) {
                if (dateDebut != null && dateDebut.getValue() != null) {
                    if (dateFin.getValue().isBefore(dateDebut.getValue())) {
                        erreurs.append("- La date de fin doit être après la date de début\n");
                    }
                }
            } else {
                erreurs.append("- La date de fin est obligatoire si l'hospitalisation n'est pas en cours\n");
            }
        }

        if (erreurs.length() > 0) {
            afficherErreur("Erreurs de validation", erreurs.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) valider.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}