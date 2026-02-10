package com.hsp.controller.produit;

import com.hsp.dao.ProduitDAO;
import com.hsp.model.Produit;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProduitListController implements Initializable {

    @FXML
    private TableView<Produit> table;

    @FXML
    private TableColumn<Produit, String> idCol;

    @FXML
    private TableColumn<Produit, String> libelleCol;

    @FXML
    private TableColumn<Produit, String> descriptionCol;

    @FXML
    private TableColumn<Produit, String> dangerositeCol;

    @FXML
    private TableColumn<Produit, String> stockCol;

    @FXML
    private TableColumn<Produit, String> dateCreationCol;

    @FXML
    private TextField recherche;

    @FXML
    private Button ajouter;

    @FXML
    private Button modifier;

    @FXML
    private Button supprimer;

    @FXML
    private ComboBox<String> filtreDangerosite;

    private ProduitDAO produitDAO;
    private ObservableList<Produit> produits;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        produitDAO = new ProduitDAO();
        produits = FXCollections.observableArrayList();

        configurerColonnes();
        configurerFiltre();
        chargerProduits();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                modifierProduit();
            }
        });
    }

    private void configurerColonnes() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_produit())));

        libelleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLibelle()));

        descriptionCol.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            if (desc != null && desc.length() > 50) {
                desc = desc.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(desc != null ? desc : "");
        });

        dangerositeCol.setCellValueFactory(cellData -> {
            int niveau = cellData.getValue().getNiveau_dangerosite();
            String texte;
            switch (niveau) {
                case 1:
                    texte = "Faible";
                    break;
                case 2:
                    texte = "Moyen";
                    break;
                case 3:
                    texte = "Élevé";
                    break;
                case 4:
                    texte = "Très élevé";
                    break;
                default:
                    texte = "Non défini";
            }
            return new SimpleStringProperty(texte);
        });

        stockCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantite_stock())));

        dateCreationCol.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getDate_creation() != null ?
                    cellData.getValue().getDate_creation().toLocalDate().toString() : "N/A";
            return new SimpleStringProperty(date);
        });

        table.setItems(produits);
    }

    private void configurerFiltre() {
        if (filtreDangerosite != null) {
            filtreDangerosite.setItems(FXCollections.observableArrayList(
                    "Tous", "Faible", "Moyen", "Élevé", "Très élevé"));
            filtreDangerosite.setValue("Tous");
            filtreDangerosite.setOnAction(event -> filtrerProduits());
        }
    }

    private void chargerProduits() {
        List<Produit> liste = produitDAO.findAll();
        produits.clear();
        produits.addAll(liste);
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
        List<Produit> tous = produitDAO.findAll();

        produits.clear();

        if (texte.isEmpty()) {
            produits.addAll(tous);
        } else {
            for (Produit produit : tous) {
                boolean correspond = false;

                if (String.valueOf(produit.getId_produit()).contains(texte)) {
                    correspond = true;
                }

                if (produit.getLibelle() != null && produit.getLibelle().toLowerCase().contains(texte)) {
                    correspond = true;
                }

                if (produit.getDescription() != null && produit.getDescription().toLowerCase().contains(texte)) {
                    correspond = true;
                }

                if (correspond) {
                    produits.add(produit);
                }
            }
        }

        appliquerFiltre();
    }

    @FXML
    private void filtrerProduits() {
        rechercher();
    }

    private void appliquerFiltre() {
        if (filtreDangerosite == null || "Tous".equals(filtreDangerosite.getValue())) {
            return;
        }

        String valeur = filtreDangerosite.getValue();
        int niveauFiltre;
        switch (valeur) {
            case "Faible":
                niveauFiltre = 1;
                break;
            case "Moyen":
                niveauFiltre = 2;
                break;
            case "Élevé":
                niveauFiltre = 3;
                break;
            case "Très élevé":
                niveauFiltre = 4;
                break;
            default:
                return;
        }

        produits.removeIf(produit -> produit.getNiveau_dangerosite() != niveauFiltre);
    }

    @FXML
    private void ajouterProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ProduitForm.fxml"));
            Parent root = loader.load();

            ProduitFormController controller = loader.getController();
            controller.setMode(ProduitFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouveau produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerProduits();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierProduit() {
        Produit selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ProduitForm.fxml"));
            Parent root = loader.load();

            ProduitFormController controller = loader.getController();
            controller.setMode(ProduitFormController.Mode.MODIFICATION);
            controller.setProduit(selection);

            Stage stage = new Stage();
            stage.setTitle("Modifier le produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerProduits();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerProduit() {
        Produit selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le produit \"" + selection.getLibelle() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = produitDAO.delete(selection.getId_produit());
            if (succes) {
                chargerProduits();
                afficherInfo("Succès", "Le produit a été supprimé.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer le produit.");
            }
        }
    }

    @FXML
    private void rafraichir() {
        recherche.clear();
        if (filtreDangerosite != null) {
            filtreDangerosite.setValue("Tous");
        }
        chargerProduits();
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
