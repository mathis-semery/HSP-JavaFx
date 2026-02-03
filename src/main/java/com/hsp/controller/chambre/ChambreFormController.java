package com.hsp.controller.chambre;

import com.hsp.dao.ChambreDAO;
import com.hsp.model.Chambre;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ChambreFormController implements Initializable {

    public enum Mode {
        CREATION,
        MODIFICATION
    }

    @FXML
    private TextField numero;

    @FXML
    private TextField etage;

    @FXML
    private TextField nbLits;

    @FXML
    private CheckBox disponible;

    @FXML
    private ComboBox<String> comboDisponible;

    @FXML
    private Button valider;

    @FXML
    private Button annuler;

    @FXML
    private Label titre;

    private Mode mode = Mode.CREATION;
    private Chambre chambre;
    private ChambreDAO chambreDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chambreDAO = new ChambreDAO();

        if (comboDisponible != null) {
            comboDisponible.getItems().addAll("Disponible", "Occupee");
            comboDisponible.setValue("Disponible");
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        mettreAJourTitre();
    }

    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
        remplirFormulaire();
    }

    private void mettreAJourTitre() {
        if (titre != null) {
            if (mode == Mode.CREATION) {
                titre.setText("Nouvelle chambre");
            } else {
                titre.setText("Modifier la chambre");
            }
        }
    }

    private void remplirFormulaire() {
        if (chambre == null) {
            return;
        }

        numero.setText(chambre.getNumero());
        etage.setText(chambre.getEtage());
        nbLits.setText(chambre.getNb_lits());

        boolean estDisponible = "1".equals(chambre.getDisponible()) || "true".equalsIgnoreCase(chambre.getDisponible());

        if (disponible != null) {
            disponible.setSelected(estDisponible);
        }

        if (comboDisponible != null) {
            comboDisponible.setValue(estDisponible ? "Disponible" : "Occupee");
        }
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) {
            return;
        }

        String numChambre = numero.getText().trim();
        String etageChambre = etage.getText().trim();
        String lits = nbLits.getText().trim();
        String dispo = getValeurDisponible();

        boolean succes;

        if (mode == Mode.CREATION) {
            Chambre nouvelleChambre = new Chambre(0, numChambre, etageChambre, lits, dispo);
            succes = chambreDAO.insert(nouvelleChambre);
        } else {
            chambre.setNumero(numChambre);
            chambre.setEtage(etageChambre);
            chambre.setNb_lits(lits);
            chambre.setDisponible(dispo);
            succes = chambreDAO.update(chambre);
        }

        if (succes) {
            fermer();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer la chambre.");
        }
    }

    private String getValeurDisponible() {
        if (disponible != null) {
            return disponible.isSelected() ? "1" : "0";
        }

        if (comboDisponible != null) {
            return "Disponible".equals(comboDisponible.getValue()) ? "1" : "0";
        }

        return "1";
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (numero.getText() == null || numero.getText().trim().isEmpty()) {
            erreurs.append("- Le numero de chambre est obligatoire\n");
        }

        if (etage.getText() == null || etage.getText().trim().isEmpty()) {
            erreurs.append("- L'etage est obligatoire\n");
        }

        if (nbLits.getText() == null || nbLits.getText().trim().isEmpty()) {
            erreurs.append("- Le nombre de lits est obligatoire\n");
        } else {
            try {
                int nb = Integer.parseInt(nbLits.getText().trim());
                if (nb <= 0) {
                    erreurs.append("- Le nombre de lits doit etre superieur a 0\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("- Le nombre de lits doit etre un nombre valide\n");
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
        Stage stage = (Stage) numero.getScene().getWindow();
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
