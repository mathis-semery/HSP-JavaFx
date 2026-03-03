package com.hsp.controller.demande;

import com.hsp.dao.DemandeProduitDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.DemandeProduit;
import com.hsp.model.Produit;
import com.hsp.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class DemandeFormController implements Initializable {

    public enum Mode {
        CREATION,
        MODIFICATION
    }

    @FXML private Label titre;
    @FXML private ComboBox<Utilisateur> medecinField;
    @FXML private ComboBox<Produit> produitField;
    @FXML private TextField quantiteField;
    @FXML private ComboBox<String> statutField;
    @FXML private Label motifRefusLabel;
    @FXML private TextArea motifRefusField;
    @FXML private Button valider;
    @FXML private Button annuler;

    private Mode mode = Mode.CREATION;
    private DemandeProduit demande;
    private DemandeProduitDAO demandeDAO;
    private ProduitDAO produitDAO;
    private UtilisateurDAO utilisateurDAO;

    // ID du gestionnaire connecté (à adapter selon votre système de session)
    private int idGestionnaire = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        demandeDAO     = new DemandeProduitDAO();
        produitDAO     = new ProduitDAO();
        utilisateurDAO = new UtilisateurDAO();

        chargerMedecins();
        chargerProduits();
        configurerStatut();
    }

    private void chargerMedecins() {
        if (medecinField == null) return;
        // Charge uniquement les utilisateurs avec role = 'Medecin'
        List<Utilisateur> medecins = utilisateurDAO.findMedecins();
        System.out.printf(medecins.size() + " medecins found");
        medecinField.getItems().addAll(medecins);
        medecinField.setConverter(new StringConverter<Utilisateur>() {
            @Override
            public String toString(Utilisateur u) {
                return u != null ? "Dr. " + u.getNom() + " " + u.getPrenom() : "";
            }
            @Override
            public Utilisateur fromString(String s) { return null; }
        });
    }

    private void chargerProduits() {
        if (produitField == null) return;
        List<Produit> produits = produitDAO.findAll();
        produitField.getItems().addAll(produits);
        produitField.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit p) {
                return p != null ? p.getLibelle() : "";
            }
            @Override
            public Produit fromString(String s) { return null; }
        });
    }

    private void configurerStatut() {
        if (statutField != null) {
            statutField.getItems().addAll("En attente", "Validée", "Refusée");
            statutField.setValue("En attente");
            statutField.setOnAction(event -> {
                boolean estRefus = "Refusée".equals(statutField.getValue());
                if (motifRefusField != null) motifRefusField.setVisible(estRefus);
                if (motifRefusLabel != null) motifRefusLabel.setVisible(estRefus);
            });
        }
        // Masqué par défaut
        if (motifRefusField != null) motifRefusField.setVisible(false);
        if (motifRefusLabel != null) motifRefusLabel.setVisible(false);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (titre != null) {
            titre.setText(mode == Mode.CREATION
                    ? "Nouvelle demande de produit"
                    : "Modifier la demande");
        }
    }

    public void setDemande(DemandeProduit demande) {
        this.demande = demande;
        remplirFormulaire();
    }

    public void setIdGestionnaire(int idGestionnaire) {
        this.idGestionnaire = idGestionnaire;
    }

    private void remplirFormulaire() {
        if (demande == null) return;

        // Sélectionne le bon médecin dans la ComboBox via getId()
        if (medecinField != null) {
            medecinField.getItems().stream()
                    .filter(u -> u.getId() == demande.getId_medecin())
                    .findFirst()
                    .ifPresent(medecinField::setValue);
        }

        if (produitField != null) {
            produitField.getItems().stream()
                    .filter(p -> p.getId_produit() == demande.getId_produit())
                    .findFirst()
                    .ifPresent(produitField::setValue);
        }

        if (quantiteField != null) {
            quantiteField.setText(String.valueOf(demande.getQuantite()));
        }

        if (statutField != null) {
            statutField.setValue(demande.getStatut());
            boolean estRefus = "Refusée".equals(demande.getStatut());
            if (motifRefusField != null) motifRefusField.setVisible(estRefus);
            if (motifRefusLabel != null) motifRefusLabel.setVisible(estRefus);
        }

        if (motifRefusField != null && demande.getMotif_refus() != null) {
            motifRefusField.setText(demande.getMotif_refus());
        }
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) return;

        Utilisateur medecin = medecinField.getValue();
        Produit produit     = produitField.getValue();
        double quantite     = Double.parseDouble(quantiteField.getText().trim());
        String statut       = statutField.getValue();
        String motifRefus   = motifRefusField != null ? motifRefusField.getText().trim() : "";
        String dateDemande  = LocalDate.now().toString();

        boolean succes;

        if (mode == Mode.CREATION) {
            DemandeProduit nouvelle = new DemandeProduit(
                    0,
                    medecin.getId(),         // getId() du model Utilisateur
                    produit.getId_produit(),
                    idGestionnaire,
                    quantite,
                    dateDemande,
                    statut,
                    motifRefus
            );
            succes = demandeDAO.insert(nouvelle);
        } else {
            demande.setId_medecin(medecin.getId());
            demande.setId_produit(produit.getId_produit());
            demande.setQuantite(quantite);
            demande.setDate_demande(dateDemande);
            demande.setStatut(statut);
            demande.setMotif_refus(motifRefus);
            succes = demandeDAO.update(demande);
        }

        if (succes) {
            fermer();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer la demande.");
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (medecinField == null || medecinField.getValue() == null) {
            erreurs.append("- Le médecin est obligatoire\n");
        }
        if (produitField == null || produitField.getValue() == null) {
            erreurs.append("- Le produit est obligatoire\n");
        }
        if (quantiteField == null || quantiteField.getText().trim().isEmpty()) {
            erreurs.append("- La quantité est obligatoire\n");
        } else {
            try {
                double q = Double.parseDouble(quantiteField.getText().trim());
                if (q <= 0) erreurs.append("- La quantité doit être supérieure à 0\n");
            } catch (NumberFormatException e) {
                erreurs.append("- La quantité doit être un nombre valide\n");
            }
        }
        if (statutField == null || statutField.getValue() == null) {
            erreurs.append("- Le statut est obligatoire\n");
        }
        if ("Refusée".equals(statutField != null ? statutField.getValue() : null)) {
            if (motifRefusField == null || motifRefusField.getText().trim().isEmpty()) {
                erreurs.append("- Le motif de refus est obligatoire pour une demande refusée\n");
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