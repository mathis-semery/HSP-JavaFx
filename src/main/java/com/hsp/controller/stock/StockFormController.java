package com.hsp.controller.stock;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour un formulaire rapide d'ajout au stock
 * Utilisé pour les ajustements rapides de quantités
 */
public class StockFormController implements Initializable {

    @FXML
    private Label titre;

    @FXML
    private ComboBox<String> typeMouvement;

    @FXML
    private TextField quantite;

    @FXML
    private TextArea motif;

    @FXML
    private Button valider;

    @FXML
    private Button annuler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (titre != null) {
            titre.setText("Mouvement de Stock");
        }

        if (typeMouvement != null) {
            typeMouvement.getItems().addAll(
                    "Entrée - Réapprovisionnement",
                    "Entrée - Retour",
                    "Entrée - Correction",
                    "Sortie - Demande validée",
                    "Sortie - Perte",
                    "Sortie - Correction"
            );
            typeMouvement.setValue("Entrée - Réapprovisionnement");
        }
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) {
            return;
        }

        // Logique de validation et enregistrement
        afficherInfo("Succès", "Mouvement de stock enregistré.");
        fermer();
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (typeMouvement.getValue() == null) {
            erreurs.append("- Le type de mouvement est obligatoire\n");
        }

        if (quantite.getText() == null || quantite.getText().trim().isEmpty()) {
            erreurs.append("- La quantité est obligatoire\n");
        } else {
            try {
                double qte = Double.parseDouble(quantite.getText().trim());
                if (qte <= 0) {
                    erreurs.append("- La quantité doit être supérieure à 0\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("- La quantité doit être un nombre valide\n");
            }
        }

        if (motif.getText() == null || motif.getText().trim().isEmpty()) {
            erreurs.append("- Le motif est obligatoire\n");
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

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}