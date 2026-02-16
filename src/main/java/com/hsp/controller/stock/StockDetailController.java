package com.hsp.controller.stock;

import com.hsp.dao.ProduitDAO;
import com.hsp.dao.DemandeProduitDAO;
import com.hsp.model.Produit;
import com.hsp.model.DemandeProduit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour afficher les détails et statistiques du stock
 * Vue d'ensemble de l'état du stock
 */
public class StockDetailController implements Initializable {

    @FXML
    private Label titre;

    @FXML
    private Label totalProduits;

    @FXML
    private Label produitsStockBas;

    @FXML
    private Label demandesEnAttente;

    @FXML
    private Label valeurTotaleStock;

    @FXML
    private ListView<String> alertesStock;

    @FXML
    private ListView<String> dernieresActions;

    private ProduitDAO produitDAO;
    private DemandeProduitDAO demandeDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        produitDAO = new ProduitDAO();
        demandeDAO = new DemandeProduitDAO();

        if (titre != null) {
            titre.setText("Vue d'ensemble du Stock");
        }

        chargerStatistiques();
    }

    private void chargerStatistiques() {
        // Charger le nombre total de produits
        List<Produit> produits = produitDAO.findAll();
        if (totalProduits != null) {
            totalProduits.setText(String.valueOf(produits.size()));
        }

        // Compter les produits en stock bas (≤ 10)
        int stockBas = 0;
        for (Produit p : produits) {
            if (p.getQuantite_stock() <= 10) {
                stockBas++;
            }
        }
        if (produitsStockBas != null) {
            produitsStockBas.setText(String.valueOf(stockBas));
            if (stockBas > 0) {
                produitsStockBas.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }

        // Charger les demandes en attente
        List<DemandeProduit> demandes = demandeDAO.findByStatut("En attente");
        if (demandesEnAttente != null) {
            demandesEnAttente.setText(String.valueOf(demandes.size()));
            if (demandes.size() > 0) {
                demandesEnAttente.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            }
        }

        // Calculer la valeur totale du stock (exemple fictif)
        if (valeurTotaleStock != null) {
            valeurTotaleStock.setText("N/A");
        }

        // Charger les alertes de stock
        chargerAlertes(produits);

        // Charger les dernières actions (exemple)
        chargerDernieresActions();
    }

    private void chargerAlertes(List<Produit> produits) {
        if (alertesStock == null) {
            return;
        }

        alertesStock.getItems().clear();

        for (Produit p : produits) {
            if (p.getQuantite_stock() <= 10) {
                String alerte = String.format("⚠️ %s - Stock critique : %d unités",
                        p.getLibelle(), p.getQuantite_stock());
                alertesStock.getItems().add(alerte);
            }
        }

        if (alertesStock.getItems().isEmpty()) {
            alertesStock.getItems().add("✅ Aucune alerte de stock");
        }
    }

    private void chargerDernieresActions() {
        if (dernieresActions == null) {
            return;
        }

        dernieresActions.getItems().clear();
        dernieresActions.getItems().add("📦 Réapprovisionnement validé - Paracétamol (50 unités)");
        dernieresActions.getItems().add("📋 Demande créée - Dr. Martin - Seringues (20 unités)");
        dernieresActions.getItems().add("✅ Demande validée - Dr. Dubois - Gants (100 unités)");
        dernieresActions.getItems().add("➕ Nouveau produit ajouté - Compresses stériles");
    }

    @FXML
    private void rafraichir() {
        chargerStatistiques();
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) titre.getScene().getWindow();
        stage.close();
    }
}