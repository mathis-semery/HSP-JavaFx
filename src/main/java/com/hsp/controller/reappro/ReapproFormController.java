package com.hsp.controller.reappro;

import com.hsp.config.Database;
import com.hsp.dao.FournisseurDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.ProduitFournisseurDAO;
import com.hsp.dao.ReapprovisionnementDAO;
import com.hsp.model.Fournisseur;
import com.hsp.model.Historique;
import com.hsp.model.Produit;
import com.hsp.model.ProduitFournisseur;
import com.hsp.model.Reapprovisionnement;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private ProduitFournisseurDAO produitFournisseurDAO;
    private HistoriqueDAO historiqueDAO;

    private int idGestionnaire = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reapproDAO            = new ReapprovisionnementDAO();
        produitDAO            = new ProduitDAO();
        fournisseurDAO        = new FournisseurDAO();
        produitFournisseurDAO = new ProduitFournisseurDAO();
        historiqueDAO         = new HistoriqueDAO();

        chargerProduits();

        // Quand un produit est sélectionné, filtrer les fournisseurs qui le proposent
        if (produitField != null) {
            produitField.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    chargerFournisseursPourProduit(newVal.getId_produit());
                } else {
                    if (fournisseurField != null) {
                        fournisseurField.getItems().clear();
                        fournisseurField.setValue(null);
                    }
                }
            });
        }

        if (dateCommandeField != null) {
            dateCommandeField.setValue(LocalDate.now());
        }
    }

    private void chargerProduits() {
        if (produitField == null) return;
        // Afficher uniquement les produits qui ont au moins un fournisseur
        List<Produit> produits = produitDAO.findAll().stream()
                .filter(p -> !produitFournisseurDAO.findByProduitId(p.getId_produit()).isEmpty())
                .collect(java.util.stream.Collectors.toList());
        produitField.getItems().addAll(produits);
        produitField.setConverter(new StringConverter<Produit>() {
            @Override public String toString(Produit produit) { return produit != null ? produit.getLibelle() : ""; }
            @Override public Produit fromString(String s)     { return null; }
        });
    }

    private void chargerFournisseursPourProduit(int idProduit) {
        if (fournisseurField == null) return;
        fournisseurField.getItems().clear();
        fournisseurField.setValue(null);

        List<ProduitFournisseur> associations = produitFournisseurDAO.findByProduitId(idProduit);
        for (ProduitFournisseur pf : associations) {
            Fournisseur f = fournisseurDAO.findById(pf.getId_fournisseur());
            if (f != null) fournisseurField.getItems().add(f);
        }

        fournisseurField.setConverter(new StringConverter<Fournisseur>() {
            @Override public String toString(Fournisseur f) {
                if (f == null) return "";
                // Afficher le prix proposé par ce fournisseur
                Produit p = produitField.getValue();
                if (p != null) {
                    ProduitFournisseur pf = produitFournisseurDAO.findById(p.getId_produit(), f.getId_fournisseur());
                    if (pf != null) return f.getNom() + "  —  " + String.format("%.2f", pf.getPrix()) + " €/u";
                }
                return f.getNom();
            }
            @Override public Fournisseur fromString(String s) { return null; }
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

        if (produitField != null)
            produitField.getItems().stream().filter(p -> p.getId_produit() == reappro.getId_produit()).findFirst().ifPresent(produitField::setValue);
        if (fournisseurField != null)
            fournisseurField.getItems().stream().filter(f -> f.getId_fournisseur() == reappro.getId_fournisseur()).findFirst().ifPresent(fournisseurField::setValue);
        if (quantiteField    != null) quantiteField.setText(String.valueOf(reappro.getQuantite()));
        if (dateCommandeField  != null) dateCommandeField.setValue(reappro.getDate_commande());
        if (dateReceptionField != null) dateReceptionField.setValue(reappro.getDate_reception());
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) return;

        Produit produit         = produitField.getValue();
        Fournisseur fournisseur = fournisseurField.getValue();
        int quantite            = Integer.parseInt(quantiteField.getText().trim());
        LocalDate dateCommande  = dateCommandeField.getValue();
        LocalDate dateReception = dateReceptionField != null ? dateReceptionField.getValue() : null;

        // La table reapprovisionnement n'a pas id_produit/id_fournisseur/quantite —
        // ces infos sont dans ligne_reapprovisionnement.
        try (Connection conn = Database.getConnexion()) {
            conn.setAutoCommit(false);

            int idReappro;

            if (mode == Mode.CREATION) {
                // 1. Créer l'en-tête de réappro
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO reapprovisionnement (id_gestionnaire, date_commande, date_reception) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idGestionnaire);
                    ps.setDate(2, Date.valueOf(dateCommande));
                    ps.setDate(3, dateReception != null ? Date.valueOf(dateReception) : null);
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    idReappro = rs.next() ? rs.getInt(1) : 0;
                }

                if (idReappro <= 0) {
                    conn.rollback();
                    afficherErreur("Erreur", "Impossible de créer le réapprovisionnement.");
                    return;
                }

                // 2. Créer la ligne de réappro
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO ligne_reapprovisionnement (id_reappro, id_produit, id_fournisseur, quantite_commandee) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, idReappro);
                    ps.setInt(2, produit.getId_produit());
                    ps.setInt(3, fournisseur.getId_fournisseur());
                    ps.setInt(4, quantite);
                    ps.executeUpdate();
                }

                // 3. Historique
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'CREATION', 'reapprovisionnement', ?, ?)")) {
                    ps.setInt(1, idGestionnaire);
                    ps.setInt(2, idReappro);
                    ps.setString(3, "Réappro. produit : " + produit.getLibelle()
                            + " | fournisseur : " + fournisseur.getNom()
                            + " | qté : " + quantite);
                    ps.executeUpdate();
                }

            } else {
                // Mode modification : mettre à jour l'en-tête
                idReappro = reappro.getId_reappro();
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE reapprovisionnement SET date_commande=?, date_reception=? WHERE id_reappro=?")) {
                    ps.setDate(1, Date.valueOf(dateCommande));
                    ps.setDate(2, dateReception != null ? Date.valueOf(dateReception) : null);
                    ps.setInt(3, idReappro);
                    ps.executeUpdate();
                }

                // Mettre à jour la ligne
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ligne_reapprovisionnement SET id_produit=?, id_fournisseur=?, quantite_commandee=? WHERE id_reappro=?")) {
                    ps.setInt(1, produit.getId_produit());
                    ps.setInt(2, fournisseur.getId_fournisseur());
                    ps.setInt(3, quantite);
                    ps.setInt(4, idReappro);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            fermer();

        } catch (SQLException e) {
            afficherErreur("Erreur BD", e.getMessage());
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        if (produitField    == null || produitField.getValue()    == null) erreurs.append("- Le produit est obligatoire\n");
        if (fournisseurField == null || fournisseurField.getValue() == null) erreurs.append("- Le fournisseur est obligatoire\n");
        if (quantiteField == null || quantiteField.getText().trim().isEmpty()) {
            erreurs.append("- La quantité est obligatoire\n");
        } else {
            try {
                int q = Integer.parseInt(quantiteField.getText().trim());
                if (q <= 0) erreurs.append("- La quantité doit être supérieure à 0\n");
            } catch (NumberFormatException e) { erreurs.append("- La quantité doit être un nombre entier\n"); }
        }
        if (dateCommandeField == null || dateCommandeField.getValue() == null) erreurs.append("- La date de commande est obligatoire\n");
        if (erreurs.length() > 0) { afficherErreur("Erreurs de validation", erreurs.toString()); return false; }
        return true;
    }

    // ← AJOUT : méthode utilitaire historique
    private void enregistrerHistorique(String action, String table, int idEntite, String details) {
        try {
            historiqueDAO.insert(new Historique(
                    0, idGestionnaire, action, table, idEntite, LocalDateTime.now(), details));
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