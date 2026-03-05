package com.hsp.controller.demande;

import com.hsp.dao.DemandeProduitDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.DemandeProduit;
import com.hsp.model.Historique;
import com.hsp.model.Produit;
import com.hsp.model.Utilisateur;
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

public class DemandeListController implements Initializable {

    @FXML private TableView<DemandeProduit> table;
    @FXML private TableColumn<DemandeProduit, String> idCol;
    @FXML private TableColumn<DemandeProduit, String> medecinCol;
    @FXML private TableColumn<DemandeProduit, String> produitCol;
    @FXML private TableColumn<DemandeProduit, String> quantiteCol;
    @FXML private TableColumn<DemandeProduit, String> dateDemandecol;
    @FXML private TableColumn<DemandeProduit, String> statutCol;
    @FXML private TableColumn<DemandeProduit, String> motifRefusCol;
    @FXML private TextField recherche;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private Button ajouter;
    @FXML private Button modifier;
    @FXML private Button supprimer;

    private DemandeProduitDAO demandeDAO;
    private ProduitDAO produitDAO;
    private UtilisateurDAO utilisateurDAO;
    private HistoriqueDAO historiqueDAO;   // ← AJOUT
    private ObservableList<DemandeProduit> demandes;

    private int idUtilisateurConnecte = 1;   // ← AJOUT

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        demandeDAO     = new DemandeProduitDAO();
        produitDAO     = new ProduitDAO();
        utilisateurDAO = new UtilisateurDAO();
        historiqueDAO  = new HistoriqueDAO();   // ← AJOUT
        demandes       = FXCollections.observableArrayList();

        configurerColonnes();
        configurerFiltre();
        chargerDemandes();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null)
                modifierDemande();
        });
    }

    public void setIdUtilisateurConnecte(int id) { this.idUtilisateurConnecte = id; }   // ← AJOUT

    private void configurerColonnes() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_demande())));

        medecinCol.setCellValueFactory(cellData -> {
            int idMedecin = cellData.getValue().getId_medecin();
            Utilisateur medecin = utilisateurDAO.findById(idMedecin);
            return new SimpleStringProperty(medecin != null
                    ? "Dr. " + medecin.getNom() + " " + medecin.getPrenom()
                    : "Médecin #" + idMedecin);
        });

        produitCol.setCellValueFactory(cellData -> {
            int idProduit = cellData.getValue().getId_produit();
            Produit produit = produitDAO.findById(idProduit);
            return new SimpleStringProperty(produit != null ? produit.getLibelle() : "Produit #" + idProduit);
        });

        quantiteCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantite())));

        dateDemandecol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_demande() != null
                        ? cellData.getValue().getDate_demande() : "N/A"));

        statutCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut() != null
                        ? cellData.getValue().getStatut() : ""));

        statutCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Validée":    setStyle("-fx-text-fill: #155724; -fx-font-weight: bold;"); break;
                    case "Refusée":    setStyle("-fx-text-fill: #721c24; -fx-font-weight: bold;"); break;
                    case "En attente": setStyle("-fx-text-fill: #856404; -fx-font-weight: bold;"); break;
                    default:           setStyle("");
                }
            }
        });

        motifRefusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMotif_refus() != null
                        ? cellData.getValue().getMotif_refus() : ""));

        table.setItems(demandes);
    }

    private void configurerFiltre() {
        if (filtreStatut != null) {
            filtreStatut.setItems(FXCollections.observableArrayList("Tous", "En attente", "Validée", "Refusée"));
            filtreStatut.setValue("Tous");
            filtreStatut.setOnAction(event -> filtrerDemandes());
        }
    }

    private void chargerDemandes() {
        List<DemandeProduit> liste = demandeDAO.findAll();
        demandes.clear();
        demandes.addAll(liste);
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
        List<DemandeProduit> toutes = demandeDAO.findAll();
        demandes.clear();

        if (texte.isEmpty()) { demandes.addAll(toutes); appliquerFiltre(); return; }

        for (DemandeProduit d : toutes) {
            boolean correspond = false;
            if (String.valueOf(d.getId_demande()).contains(texte))                               correspond = true;
            if (String.valueOf(d.getQuantite()).contains(texte))                                 correspond = true;
            if (d.getDate_demande() != null && d.getDate_demande().toLowerCase().contains(texte)) correspond = true;
            if (d.getStatut() != null && d.getStatut().toLowerCase().contains(texte))           correspond = true;
            Produit produit = produitDAO.findById(d.getId_produit());
            if (produit != null && produit.getLibelle().toLowerCase().contains(texte))           correspond = true;
            Utilisateur medecin = utilisateurDAO.findById(d.getId_medecin());
            if (medecin != null && (medecin.getNom() + " " + medecin.getPrenom()).toLowerCase().contains(texte)) correspond = true;
            if (correspond) demandes.add(d);
        }
        appliquerFiltre();
    }

    @FXML private void filtrerDemandes() { rechercher(); }

    private void appliquerFiltre() {
        if (filtreStatut == null || "Tous".equals(filtreStatut.getValue())) return;
        demandes.removeIf(d -> !filtreStatut.getValue().equals(d.getStatut()));
    }

    @FXML
    private void ajouterDemande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/DemandeForm.fxml"));
            Parent root = loader.load();
            DemandeFormController controller = loader.getController();
            controller.setMode(DemandeFormController.Mode.CREATION);
            controller.setIdGestionnaire(idUtilisateurConnecte);   // ← AJOUT
            Stage stage = new Stage();
            stage.setTitle("Nouvelle demande");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerDemandes();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierDemande() {
        DemandeProduit selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/DemandeForm.fxml"));
            Parent root = loader.load();
            DemandeFormController controller = loader.getController();
            controller.setMode(DemandeFormController.Mode.MODIFICATION);
            controller.setDemande(selection);
            controller.setIdGestionnaire(idUtilisateurConnecte);   // ← AJOUT
            Stage stage = new Stage();
            stage.setTitle("Modifier la demande");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerDemandes();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerDemande() {
        DemandeProduit selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la demande #" + selection.getId_demande() + " ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = demandeDAO.delete(selection.getId_demande());
            if (succes) {
                enregistrerHistorique("SUPPRESSION", "demande_produit", selection.getId_demande(),   // ← AJOUT
                        "Suppression de la demande #" + selection.getId_demande());
                chargerDemandes();
                afficherInfo("Succès", "La demande a été supprimée.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer la demande.");
            }
        }
    }

    @FXML
    private void rafraichir() {
        recherche.clear();
        if (filtreStatut != null) filtreStatut.setValue("Tous");
        chargerDemandes();
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

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}