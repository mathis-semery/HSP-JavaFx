package com.hsp.controller.produit_fournisseur;

import com.hsp.dao.FournisseurDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.ProduitFournisseurDAO;
import com.hsp.model.Fournisseur;
import com.hsp.model.Historique;
import com.hsp.model.Produit;
import com.hsp.model.ProduitFournisseur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProduitFournisseurListController implements Initializable {

    @FXML private TableView<ProduitFournisseur> table;
    @FXML private TableColumn<ProduitFournisseur, String> produitCol;
    @FXML private TableColumn<ProduitFournisseur, String> fournisseurCol;
    @FXML private TableColumn<ProduitFournisseur, String> prixCol;
    @FXML private TextField recherche;
    @FXML private Button ajouter;
    @FXML private Button modifier;
    @FXML private Button supprimer;

    private ProduitFournisseurDAO pfDAO;
    private ProduitDAO produitDAO;
    private FournisseurDAO fournisseurDAO;
    private HistoriqueDAO historiqueDAO;   // ← AJOUT
    private ObservableList<ProduitFournisseur> liens;

    private int idUtilisateurConnecte = 1;   // ← AJOUT

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pfDAO          = new ProduitFournisseurDAO();
        produitDAO     = new ProduitDAO();
        fournisseurDAO = new FournisseurDAO();
        historiqueDAO  = new HistoriqueDAO();   // ← AJOUT
        liens          = FXCollections.observableArrayList();

        configurerColonnes();
        chargerLiens();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null)
                modifierLien();
        });
    }

    public void setIdUtilisateurConnecte(int id) { this.idUtilisateurConnecte = id; }   // ← AJOUT

    private void configurerColonnes() {
        produitCol.setCellValueFactory(cellData -> {
            Produit p = produitDAO.findById(cellData.getValue().getId_produit());
            return new SimpleStringProperty(p != null ? p.getLibelle() : "Produit #" + cellData.getValue().getId_produit());
        });
        fournisseurCol.setCellValueFactory(cellData -> {
            Fournisseur f = fournisseurDAO.findById(cellData.getValue().getId_fournisseur());
            return new SimpleStringProperty(f != null ? f.getNom() : "Fournisseur #" + cellData.getValue().getId_fournisseur());
        });
        prixCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f €", cellData.getValue().getPrix())));
        table.setItems(liens);
    }

    private void chargerLiens() {
        liens.clear();
        liens.addAll(pfDAO.findAll());
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
        List<ProduitFournisseur> tous = pfDAO.findAll();
        liens.clear();

        if (texte.isEmpty()) { liens.addAll(tous); return; }

        for (ProduitFournisseur pf : tous) {
            boolean correspond = String.valueOf(pf.getPrix()).contains(texte);
            Produit p = produitDAO.findById(pf.getId_produit());
            if (p != null && p.getLibelle().toLowerCase().contains(texte)) correspond = true;
            Fournisseur f = fournisseurDAO.findById(pf.getId_fournisseur());
            if (f != null && f.getNom().toLowerCase().contains(texte)) correspond = true;
            if (correspond) liens.add(pf);
        }
    }

    @FXML
    private void ajouterLien() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ProduitFournisseurForm.fxml"));
            Parent root = loader.load();
            ProduitFournisseurFormController controller = loader.getController();
            controller.setMode(ProduitFournisseurFormController.Mode.CREATION);
            controller.setIdUtilisateurConnecte(idUtilisateurConnecte);   // ← AJOUT
            Stage stage = new Stage();
            stage.setTitle("Associer un fournisseur à un produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerLiens();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierLien() {
        ProduitFournisseur selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ProduitFournisseurForm.fxml"));
            Parent root = loader.load();
            ProduitFournisseurFormController controller = loader.getController();
            controller.setMode(ProduitFournisseurFormController.Mode.MODIFICATION);
            controller.setProduitFournisseur(selection);
            controller.setIdUtilisateurConnecte(idUtilisateurConnecte);   // ← AJOUT
            Stage stage = new Stage();
            stage.setTitle("Modifier le prix");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerLiens();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerLien() {
        ProduitFournisseur selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        Produit p    = produitDAO.findById(selection.getId_produit());
        Fournisseur f = fournisseurDAO.findById(selection.getId_fournisseur());
        String nomProduit     = p != null ? p.getLibelle() : "#" + selection.getId_produit();
        String nomFournisseur = f != null ? f.getNom()     : "#" + selection.getId_fournisseur();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Dissocier \"" + nomProduit + "\" de \"" + nomFournisseur + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = pfDAO.delete(selection.getId_produit(), selection.getId_fournisseur());
            if (succes) {
                enregistrerHistorique("SUPPRESSION", "produit_fournisseur", selection.getId_produit(),   // ← AJOUT
                        "Dissociation : " + nomProduit + " ↔ " + nomFournisseur);
                chargerLiens();
                afficherInfo("Succès", "L'association a été supprimée.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer l'association.");
            }
        }
    }

    @FXML private void rafraichir() { recherche.clear(); chargerLiens(); }

    // ← AJOUT
    private void enregistrerHistorique(String action, String table, int idEntite, String details) {
        try {
            historiqueDAO.insert(new Historique(
                    0, idUtilisateurConnecte, action, table, idEntite, LocalDateTime.now(), details));
        } catch (Exception e) {
            System.err.println("⚠️ Historique non enregistré : " + e.getMessage());
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}