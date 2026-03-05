package com.hsp.controller.produit;

import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.model.Historique;
import com.hsp.model.Produit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ProduitFormController implements Initializable {

    public enum Mode {
        CREATION,
        MODIFICATION
    }

    @FXML private TextField libelleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> dangerositeCombo;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private Button valider;
    @FXML private Button annuler;
    @FXML private Label titre;

    private Mode mode = Mode.CREATION;
    private Produit produit;
    private ProduitDAO produitDAO;
    private HistoriqueDAO historiqueDAO;

    // ID de l'utilisateur connecté, passé depuis ProduitListController
    private int idUtilisateurConnecte = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        produitDAO    = new ProduitDAO();
        historiqueDAO = new HistoriqueDAO();

        configurerDangerosite();
        configurerStockSpinner();
    }

    public void setIdUtilisateurConnecte(int id) {
        this.idUtilisateurConnecte = id;
    }

    private void configurerDangerosite() {
        if (dangerositeCombo != null) {
            dangerositeCombo.getItems().addAll("Faible", "Moyen", "Élevé", "Très élevé");
            dangerositeCombo.setValue("Faible");
        }
    }

    private void configurerStockSpinner() {
        if (stockSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
            stockSpinner.setValueFactory(valueFactory);
            stockSpinner.setEditable(true);
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        mettreAJourTitre();
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        remplirFormulaire();
    }

    private void mettreAJourTitre() {
        if (titre != null) {
            titre.setText(mode == Mode.CREATION ? "Nouveau produit" : "Modifier le produit");
        }
    }

    private void remplirFormulaire() {
        if (produit == null) return;

        if (libelleField != null)     libelleField.setText(produit.getLibelle());
        if (descriptionField != null) descriptionField.setText(produit.getDescription());

        if (dangerositeCombo != null) {
            switch (produit.getNiveau_dangerosite()) {
                case 1: dangerositeCombo.setValue("Faible");     break;
                case 2: dangerositeCombo.setValue("Moyen");      break;
                case 3: dangerositeCombo.setValue("Élevé");      break;
                case 4: dangerositeCombo.setValue("Très élevé"); break;
            }
        }

        if (stockSpinner != null) {
            stockSpinner.getValueFactory().setValue(produit.getQuantite_stock());
        }
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) return;

        String libelle        = libelleField.getText().trim();
        String description    = descriptionField != null ? descriptionField.getText().trim() : "";
        int niveauDangerosite = getNiveauDangerosite();
        int quantiteStock     = stockSpinner != null ? stockSpinner.getValue() : 0;

        boolean succes;

        if (mode == Mode.CREATION) {
            Produit nouveauProduit = new Produit(
                    0, libelle, description, niveauDangerosite, quantiteStock, LocalDateTime.now());
            succes = produitDAO.insert(nouveauProduit);

            if (succes) {
                // Récupération de l'ID généré via findByLibelle
                Produit insere = produitDAO.findByLibelle(libelle);
                int idProduit  = insere != null ? insere.getId_produit() : 0;
                enregistrerHistorique("CREATION", "produit", idProduit,
                        "Création du produit : " + libelle + " (stock : " + quantiteStock + ")");
            }

        } else {
            String ancienLibelle = produit.getLibelle();
            produit.setLibelle(libelle);
            produit.setDescription(description);
            produit.setNiveau_dangerosite(niveauDangerosite);
            produit.setQuantite_stock(quantiteStock);
            succes = produitDAO.update(produit);

            if (succes) {
                enregistrerHistorique("MODIFICATION", "produit", produit.getId_produit(),
                        "Modification produit : " + ancienLibelle + " → " + libelle
                                + " (stock : " + quantiteStock + ")");
            }
        }

        if (succes) {
            fermer();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer le produit.");
        }
    }

    private void enregistrerHistorique(String action, String table, int idEntite, String details) {
        try {
            Historique h = new Historique(
                    0, idUtilisateurConnecte, action, table, idEntite, LocalDateTime.now(), details);
            historiqueDAO.insert(h);
        } catch (Exception e) {
            // L'historique ne doit jamais bloquer l'application
            System.err.println("⚠️ Impossible d'enregistrer l'historique : " + e.getMessage());
        }
    }

    private int getNiveauDangerosite() {
        if (dangerositeCombo == null || dangerositeCombo.getValue() == null) return 1;
        switch (dangerositeCombo.getValue()) {
            case "Faible":     return 1;
            case "Moyen":      return 2;
            case "Élevé":      return 3;
            case "Très élevé": return 4;
            default:           return 1;
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (libelleField == null || libelleField.getText().trim().isEmpty()) {
            erreurs.append("- Le libellé est obligatoire\n");
        }
        if (libelleField != null && libelleField.getText().trim().length() > 100) {
            erreurs.append("- Le libellé ne peut pas dépasser 100 caractères\n");
        }
        if (stockSpinner != null && stockSpinner.getValue() < 0) {
            erreurs.append("- La quantité en stock ne peut pas être négative\n");
        }

        if (erreurs.length() > 0) {
            afficherErreur("Erreurs de validation", erreurs.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void annuler() { fermer(); }

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