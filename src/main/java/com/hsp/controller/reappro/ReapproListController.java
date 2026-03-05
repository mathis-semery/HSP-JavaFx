package com.hsp.controller.reappro;

import com.hsp.dao.FournisseurDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.ReapprovisionnementDAO;
import com.hsp.model.Fournisseur;
import com.hsp.model.Historique;
import com.hsp.model.Produit;
import com.hsp.model.Reapprovisionnement;
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

public class ReapproListController implements Initializable {

    @FXML private TableView<Reapprovisionnement> table;
    @FXML private TableColumn<Reapprovisionnement, String> idCol;
    @FXML private TableColumn<Reapprovisionnement, String> produitCol;
    @FXML private TableColumn<Reapprovisionnement, String> fournisseurCol;
    @FXML private TableColumn<Reapprovisionnement, String> quantiteCol;
    @FXML private TableColumn<Reapprovisionnement, String> dateCommandeCol;
    @FXML private TableColumn<Reapprovisionnement, String> dateReceptionCol;
    @FXML private TextField recherche;
    @FXML private Button ajouter;
    @FXML private Button modifier;
    @FXML private Button supprimer;

    private ReapprovisionnementDAO reapproDAO;
    private ProduitDAO produitDAO;
    private FournisseurDAO fournisseurDAO;
    private HistoriqueDAO historiqueDAO;   // ← AJOUT
    private ObservableList<Reapprovisionnement> reappros;

    private int idUtilisateurConnecte = 1;   // ← AJOUT

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reapproDAO     = new ReapprovisionnementDAO();
        produitDAO     = new ProduitDAO();
        fournisseurDAO = new FournisseurDAO();
        historiqueDAO  = new HistoriqueDAO();   // ← AJOUT
        reappros       = FXCollections.observableArrayList();

        configurerColonnes();
        chargerReappros();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null)
                modifierReappro();
        });
    }

    public void setIdUtilisateurConnecte(int id) { this.idUtilisateurConnecte = id; }   // ← AJOUT

    private void configurerColonnes() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_reappro())));

        produitCol.setCellValueFactory(cellData -> {
            Produit produit = produitDAO.findById(cellData.getValue().getId_produit());
            return new SimpleStringProperty(produit != null ? produit.getLibelle() : "ID: " + cellData.getValue().getId_produit());
        });

        fournisseurCol.setCellValueFactory(cellData -> {
            Fournisseur fournisseur = fournisseurDAO.findById(cellData.getValue().getId_fournisseur());
            return new SimpleStringProperty(fournisseur != null ? fournisseur.getNom() : "ID: " + cellData.getValue().getId_fournisseur());
        });

        quantiteCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantite())));

        dateCommandeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_commande() != null ?
                        cellData.getValue().getDate_commande().toString() : ""));

        dateReceptionCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_reception() != null ?
                        cellData.getValue().getDate_reception().toString() : "En attente"));

        table.setItems(reappros);
    }

    private void chargerReappros() {
        List<Reapprovisionnement> liste = reapproDAO.findAll();
        reappros.clear();
        reappros.addAll(liste);
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
        List<Reapprovisionnement> tous = reapproDAO.findAll();
        reappros.clear();

        if (texte.isEmpty()) { reappros.addAll(tous); return; }

        for (Reapprovisionnement r : tous) {
            boolean correspond = String.valueOf(r.getId_reappro()).contains(texte)
                    || String.valueOf(r.getQuantite()).contains(texte);
            Produit produit = produitDAO.findById(r.getId_produit());
            if (produit != null && produit.getLibelle().toLowerCase().contains(texte)) correspond = true;
            Fournisseur fournisseur = fournisseurDAO.findById(r.getId_fournisseur());
            if (fournisseur != null && fournisseur.getNom().toLowerCase().contains(texte)) correspond = true;
            if (r.getDate_commande() != null && r.getDate_commande().toString().contains(texte)) correspond = true;
            if (correspond) reappros.add(r);
        }
    }

    @FXML
    private void ajouterReappro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ReapproForm.fxml"));
            Parent root = loader.load();

            ReapproFormController controller = loader.getController();
            controller.setMode(ReapproFormController.Mode.CREATION);
            controller.setIdGestionnaire(idUtilisateurConnecte);   // ← AJOUT

            Stage stage = new Stage();
            stage.setTitle("Nouveau réapprovisionnement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerReappros();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierReappro() {
        Reapprovisionnement selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ReapproForm.fxml"));
            Parent root = loader.load();

            ReapproFormController controller = loader.getController();
            controller.setMode(ReapproFormController.Mode.MODIFICATION);
            controller.setReappro(selection);
            controller.setIdGestionnaire(idUtilisateurConnecte);   // ← AJOUT

            Stage stage = new Stage();
            stage.setTitle("Modifier le réapprovisionnement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerReappros();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerReappro() {
        Reapprovisionnement selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le réapprovisionnement #" + selection.getId_reappro() + " ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = reapproDAO.delete(selection.getId_reappro());
            if (succes) {
                // ← AJOUT
                enregistrerHistorique("SUPPRESSION", "reapprovisionnement", selection.getId_reappro(),
                        "Suppression du réapprovisionnement #" + selection.getId_reappro());
                chargerReappros();
                afficherInfo("Succès", "Le réapprovisionnement a été supprimé.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer le réapprovisionnement.");
            }
        }
    }

    @FXML
    private void rafraichir() { recherche.clear(); chargerReappros(); }

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