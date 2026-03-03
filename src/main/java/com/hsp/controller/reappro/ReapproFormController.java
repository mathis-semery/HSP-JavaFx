package com.hsp.controller.reappro;

import com.hsp.dao.FournisseurDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.ReapprovisionnementDAO;
import com.hsp.model.Fournisseur;
import com.hsp.model.Produit;
import com.hsp.model.Reapprovisionnement;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReapproFormController implements Initializable {

    public enum Mode {
        CREATION,
        MODIFICATION
    }

    @FXML private ComboBox<Produit> produitField;
    @FXML private ComboBox<Fournisseur> fournisseurField;
    @FXML private TextField quantiteField;
    @FXML private DatePicker dateCommandeField;
    @FXML private DatePicker dateReceptionField;
    @FXML private Button valider;
    @FXML private Button annuler;
    @FXML private Label titre;

    private Mode mode = Mode.CREATION;
    private Reapprovisionnement reappro;
    private ReapprovisionnementDAO reapproDAO;
    private ProduitDAO produitDAO;
    private FournisseurDAO fournisseurDAO;

    private int idGestionnaire = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reapproDAO = new ReapprovisionnementDAO();
        produitDAO = new ProduitDAO();
        fournisseurDAO = new FournisseurDAO();

        chargerProduits();
        chargerFournisseurs();

        if (dateCommandeField != null) {
            dateCommandeField.setValue(LocalDate.now());
        }
    }

    private void chargerProduits() {
        if (produitField == null) return;
        List<Produit> produits = produitDAO.findAll();
        produitField.getItems().addAll(produits);
        produitField.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit produit) {
                return produit != null ? produit.getLibelle() : "";
            }
            @Override
            public Produit fromString(String s) { return null; }
        });
    }

    private void chargerFournisseurs() {
        if (fournisseurField == null) return;
        List<Fournisseur> fournisseurs = fournisseurDAO.findAll();
        fournisseurField.getItems().addAll(fournisseurs);
        fournisseurField.setConverter(new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur fournisseur) {
                return fournisseur != null ? fournisseur.getNom() : "";
            }
            @Override
            public Fournisseur fromString(String s) { return null; }
        });
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        mettreAJourTitre();
    }

    public void setReappro(Reapprovisionnement reappro) {
        this.reappro = reappro;
        remplirFormulaire();
    }

    public void setIdGestionnaire(int idGestionnaire) {
        this.idGestionnaire = idGestionnaire;
    }

    private void mettreAJourTitre() {
        if (titre != null) {
            titre.setText(mode == Mode.CREATION ? "Nouveau réapprovisionnement" : "Modifier le réapprovisionnement");
        }
    }

    private void remplirFormulaire() {
        if (reappro == null) return;

        if (produitField != null) {
            produitField.getItems().stream()
                    .filter(p -> p.getId_produit() == reappro.getId_produit())
                    .findFirst()
                    .ifPresent(produitField::setValue);
        }

        if (fournisseurField != null) {
            fournisseurField.getItems().stream()
                    .filter(f -> f.getId_fournisseur() == reappro.getId_fournisseur())
                    .findFirst()
                    .ifPresent(fournisseurField::setValue);
        }

        if (quantiteField != null) {
            quantiteField.setText(String.valueOf(reappro.getQuantite()));
        }

        if (dateCommandeField != null) {
            dateCommandeField.setValue(reappro.getDate_commande());
        }

        if (dateReceptionField != null) {
            dateReceptionField.setValue(reappro.getDate_reception());
        }
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) return;

        Produit produit = produitField.getValue();
        Fournisseur fournisseur = fournisseurField.getValue();
        int quantite = Integer.parseInt(quantiteField.getText().trim());
        LocalDate dateCommande = dateCommandeField.getValue();
        LocalDate dateReception = dateReceptionField != null ? dateReceptionField.getValue() : null;

        boolean succes;

        if (mode == Mode.CREATION) {
            Reapprovisionnement nouveau = new Reapprovisionnement(
                    0,
                    produit.getId_produit(),
                    fournisseur.getId_fournisseur(),
                    idGestionnaire,
                    quantite,
                    dateCommande,
                    dateReception
            );
            succes = reapproDAO.insert(nouveau);
        } else {
            reappro.setId_produit(produit.getId_produit());
            reappro.setId_fournisseur(fournisseur.getId_fournisseur());
            reappro.setQuantite(quantite);
            reappro.setDate_commande(dateCommande);
            reappro.setDate_reception(dateReception);
            succes = reapproDAO.update(reappro);
        }

        if (succes) {
            fermer();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer le réapprovisionnement.");
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (produitField == null || produitField.getValue() == null) {
            erreurs.append("- Le produit est obligatoire\n");
        }

        if (fournisseurField == null || fournisseurField.getValue() == null) {
            erreurs.append("- Le fournisseur est obligatoire\n");
        }

        if (quantiteField == null || quantiteField.getText().trim().isEmpty()) {
            erreurs.append("- La quantité est obligatoire\n");
        } else {
            try {
                int q = Integer.parseInt(quantiteField.getText().trim());
                if (q <= 0) {
                    erreurs.append("- La quantité doit être supérieure à 0\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("- La quantité doit être un nombre entier\n");
            }
        }

        if (dateCommandeField == null || dateCommandeField.getValue() == null) {
            erreurs.append("- La date de commande est obligatoire\n");
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
