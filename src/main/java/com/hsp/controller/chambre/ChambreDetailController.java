package com.hsp.controller.chambre;

import com.hsp.dao.HospitalisationDAO;
import com.hsp.model.Chambre;
import com.hsp.model.Hospitalisation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChambreDetailController implements Initializable {

    @FXML
    private Label numero;

    @FXML
    private Label etage;

    @FXML
    private Label nbLits;

    @FXML
    private Label disponible;

    @FXML
    private ListView<String> hospitalisations;

    private Chambre chambre;
    private HospitalisationDAO hospitalisationDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hospitalisationDAO = new HospitalisationDAO();
    }

    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
        afficherDetails();
    }

    private void afficherDetails() {
        if (chambre == null) {
            return;
        }

        numero.setText(chambre.getNumero());
        etage.setText(chambre.getEtage());
        nbLits.setText(chambre.getNb_lits());

        String dispo = chambre.getDisponible();
        if ("1".equals(dispo) || "true".equalsIgnoreCase(dispo)) {
            disponible.setText("Oui");
            disponible.setStyle("-fx-text-fill: green;");
        } else {
            disponible.setText("Non");
            disponible.setStyle("-fx-text-fill: red;");
        }

        chargerHospitalisations();
    }

    private void chargerHospitalisations() {
        if (hospitalisations == null || chambre == null) {
            return;
        }

        hospitalisations.getItems().clear();

        List<Hospitalisation> liste = hospitalisationDAO.findByChambreId(chambre.getId_chambre());
        if (liste == null || liste.isEmpty()) {
            hospitalisations.getItems().add("Aucune hospitalisation en cours");
        } else {
            for (Hospitalisation h : liste) {
                String info = String.format("Dossier ID: %d - Du %s au %s",
                        h.getId_dossier(),
                        h.getDate_debut() != null ? h.getDate_debut().toString() : "N/A",
                        h.getDate_fin() != null ? h.getDate_fin().toString() : "En cours");
                hospitalisations.getItems().add(info);
            }
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) numero.getScene().getWindow();
        stage.close();
    }
}
