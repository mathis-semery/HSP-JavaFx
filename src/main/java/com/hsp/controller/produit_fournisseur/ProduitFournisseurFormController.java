package com.hsp.controller.produit_fournisseur;

import com.hsp.dao.FournisseurDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.ProduitFournisseurDAO;
import com.hsp.model.Fournisseur;
import com.hsp.model.Historique;
import com.hsp.model.Produit;
import com.hsp.model.ProduitFournisseur;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class ProduitFournisseurFormController implements Initializable {

    public enum Mode { CREATION, MODIFICATION }

    @FXML private Label titre;
    @FXML private ComboBox<Produit> produitField;
    @FXML private ComboBox<Fournisseur> fournisseurField;
    @FXML private TextField prixField;
    @FXML private Button valider;
    @FXML private Button annuler;

    private Mode mode = Mode.CREATION;
    private ProduitFournisseur produitFournisseur;
    private ProduitFournisseurDAO pfDAO;
    private ProduitDAO produitDAO;
    private FournisseurDAO fournisseurDAO;
    private HistoriqueDAO historiqueDAO;   // ← AJOUT

    private int idUtilisateurConnecte = 1;   // ← AJOUT

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pfDAO          = new ProduitFournisseurDAO();
        produitDAO     = new ProduitDAO();
        fournisseurDAO = new FournisseurDAO();
        historiqueDAO  = new HistoriqueDAO();   // ← AJOUT

        chargerProduits();
        chargerFournisseurs();
    }

    public void setIdUtilisateurConnecte(int id) { this.idUtilisateurConnecte = id; }   // ← AJOUT

    private void chargerProduits() {
        if (produitField == null) return;
        List<Produit> produits = produitDAO.findAll();
        produitField.getItems().addAll(produits);
        produitField.setConverter(new StringConverter<Produit>() {
            @Override public String toString(Produit p)   { return p != null ? p.getLibelle() : ""; }
            @Override public Produit fromString(String s) { return null; }
        });
    }

    private void chargerFournisseurs() {
        if (fournisseurField == null) return;
        List<Fournisseur> fournisseurs = fournisseurDAO.findAll();
        fournisseurField.getItems().addAll(fournisseurs);
        fournisseurField.setConverter(new StringConverter<Fournisseur>() {
            @Override public String toString(Fournisseur f)   { return f != null ? f.getNom() : ""; }
            @Override public Fournisseur fromString(String s) { return null; }
        });
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (titre != null) {
            titre.setText(mode == Mode.CREATION ? "Associer un fournisseur à un produit" : "Modifier le prix");
        }
        if (mode == Mode.MODIFICATION) {
            if (produitField    != null) produitField.setDisable(true);
            if (fournisseurField != null) fournisseurField.setDisable(true);
        }
    }

    public void setProduitFournisseur(ProduitFournisseur pf) {
        this.produitFournisseur = pf;
        remplirFormulaire();
    }

    private void remplirFormulaire() {
        if (produitFournisseur == null) return;
        if (produitField != null)
            produitField.getItems().stream().filter(p -> p.getId_produit() == produitFournisseur.getId_produit()).findFirst().ifPresent(produitField::setValue);
        if (fournisseurField != null)
            fournisseurField.getItems().stream().filter(f -> f.getId_fournisseur() == produitFournisseur.getId_fournisseur()).findFirst().ifPresent(fournisseurField::setValue);
        if (prixField != null)
            prixField.setText(String.valueOf(produitFournisseur.getPrix()));
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) return;

        Produit produit         = produitField.getValue();
        Fournisseur fournisseur = fournisseurField.getValue();
        double prix             = Double.parseDouble(prixField.getText().trim());

        boolean succes;

        if (mode == Mode.CREATION) {
            ProduitFournisseur existant = pfDAO.findById(produit.getId_produit(), fournisseur.getId_fournisseur());
            if (existant != null) { afficherErreur("Doublon", "Ce produit est déjà associé à ce fournisseur."); return; }
            succes = pfDAO.insert(new ProduitFournisseur(produit.getId_produit(), fournisseur.getId_fournisseur(), prix));

            if (succes) {
                enregistrerHistorique("CREATION", "produit_fournisseur", produit.getId_produit(),
                        "Association : " + produit.getLibelle() + " ↔ " + fournisseur.getNom() + " à " + prix + " €");
            }
        } else {
            double ancienPrix = produitFournisseur.getPrix();
            produitFournisseur.setPrix(prix);
            succes = pfDAO.update(produitFournisseur);

            if (succes) {
                enregistrerHistorique("MODIFICATION", "produit_fournisseur", produitFournisseur.getId_produit(),
                        "Prix modifié : " + produit.getLibelle() + " ↔ " + fournisseur.getNom()
                                + " | " + ancienPrix + " € → " + prix + " €");
            }
        }

        if (succes) fermer();
        else afficherErreur("Erreur", "Impossible d'enregistrer l'association.");
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        if (produitField    == null || produitField.getValue()    == null) erreurs.append("- Le produit est obligatoire\n");
        if (fournisseurField == null || fournisseurField.getValue() == null) erreurs.append("- Le fournisseur est obligatoire\n");
        if (prixField == null || prixField.getText().trim().isEmpty()) {
            erreurs.append("- Le prix est obligatoire\n");
        } else {
            try {
                double p = Double.parseDouble(prixField.getText().trim());
                if (p < 0) erreurs.append("- Le prix ne peut pas être négatif\n");
            } catch (NumberFormatException e) { erreurs.append("- Le prix doit être un nombre valide (ex: 12.50)\n"); }
        }
        if (erreurs.length() > 0) { afficherErreur("Erreurs de validation", erreurs.toString()); return false; }
        return true;
    }

    // ← AJOUT
    private void enregistrerHistorique(String action, String table, int idEntite, String details) {
        try {
            historiqueDAO.insert(new Historique(
                    0, idUtilisateurConnecte, action, table, idEntite, LocalDateTime.now(), details));
        } catch (Exception e) {
            System.err.println("⚠️ Historique non enregistré : " + e.getMessage());
        }
    }

    @FXML private void annuler() { fermer(); }

    private void fermer() {
        Stage stage = (Stage) valider.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}