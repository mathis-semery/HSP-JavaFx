package com.hsp.controller.fournisseur;

import com.hsp.config.Database;
import com.hsp.dao.FournisseurDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.model.Fournisseur;
import com.hsp.model.Historique;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class FournisseurFormController implements Initializable {

    public enum Mode { CREATION, MODIFICATION }

    // ===== Modèle interne pour une ligne du catalogue =====
    public static class CatalogueEntry {
        public int    idProduit;       // 0 = nouveau produit à créer
        public String libelle;
        public int    dangerosite;
        public String description;
        public double prix;

        public CatalogueEntry(int idProduit, String libelle, int dangerosite, String description, double prix) {
            this.idProduit   = idProduit;
            this.libelle     = libelle;
            this.dangerosite = dangerosite;
            this.description = description;
            this.prix        = prix;
        }
    }

    // ===== Champs fournisseur =====
    @FXML private TextField  nomField;
    @FXML private TextField  contactField;
    @FXML private TextField  emailField;
    @FXML private TextField  telephoneField;
    @FXML private TextArea   adresseField;
    @FXML private Button     valider;
    @FXML private Button     annuler;
    @FXML private Label      titre;

    // ===== Champs catalogue =====
    @FXML private TextField          nomProduitCatalogueField;
    @FXML private TextField          descriptionCatalogueField;
    @FXML private ComboBox<Integer>  dangerositeCatalogueBox;
    @FXML private TextField          prixCatalogueField;
    @FXML private TableView<CatalogueEntry> catalogueTable;

    private final ObservableList<CatalogueEntry> catalogueEntries = FXCollections.observableArrayList();

    private Mode       mode               = Mode.CREATION;
    private Fournisseur fournisseur;
    private FournisseurDAO  fournisseurDAO;
    private HistoriqueDAO   historiqueDAO;
    private int idUtilisateurConnecte = 1;

    // ===== INIT =====

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fournisseurDAO = new FournisseurDAO();
        historiqueDAO  = new HistoriqueDAO();

        if (dangerositeCatalogueBox != null)
            dangerositeCatalogueBox.getItems().addAll(1, 2, 3, 4, 5);

        setupCatalogueTable();
    }

    @SuppressWarnings("unchecked")
    private void setupCatalogueTable() {
        if (catalogueTable == null) return;
        catalogueTable.getColumns().clear();
        catalogueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CatalogueEntry, String> colNom = new TableColumn<>("Produit");
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().libelle));
        colNom.setMaxWidth(1f * Integer.MAX_VALUE * 30);

        TableColumn<CatalogueEntry, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().description != null ? data.getValue().description : ""));
        colDesc.setMaxWidth(1f * Integer.MAX_VALUE * 45);

        TableColumn<CatalogueEntry, String> colDanger = new TableColumn<>("Danger");
        colDanger.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().dangerosite)));
        colDanger.setMaxWidth(1f * Integer.MAX_VALUE * 10);

        TableColumn<CatalogueEntry, String> colPrix = new TableColumn<>("Prix (€)");
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().prix)));
        colPrix.setMaxWidth(1f * Integer.MAX_VALUE * 15);

        catalogueTable.getColumns().addAll(colNom, colDesc, colDanger, colPrix);
        catalogueTable.setItems(catalogueEntries);
    }

    // ===== SETTERS =====

    public void setIdUtilisateurConnecte(int id) { this.idUtilisateurConnecte = id; }

    public void setMode(Mode mode) {
        this.mode = mode;
        mettreAJourTitre();
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
        remplirFormulaire();
        chargerCatalogueExistant(fournisseur.getId_fournisseur());
    }

    private void mettreAJourTitre() {
        if (titre != null)
            titre.setText(mode == Mode.CREATION ? "Nouveau fournisseur" : "Modifier le fournisseur");
    }

    private void remplirFormulaire() {
        if (fournisseur == null) return;
        if (nomField       != null) nomField.setText(fournisseur.getNom());
        if (contactField   != null) contactField.setText(fournisseur.getContact());
        if (emailField     != null) emailField.setText(fournisseur.getEmail());
        if (telephoneField != null) telephoneField.setText(fournisseur.getTelephone());
        if (adresseField   != null) adresseField.setText(fournisseur.getAdresse());
    }

    /** Charge les produits déjà liés à ce fournisseur (mode MODIFICATION). */
    private void chargerCatalogueExistant(int idFournisseur) {
        catalogueEntries.clear();
        String sql = """
                SELECT pf.id_produit, p.libelle, p.niveau_dangerosite, p.description, pf.prix
                FROM produit_fournisseur pf
                JOIN produit p ON pf.id_produit = p.id_produit
                WHERE pf.id_fournisseur = ?
                ORDER BY p.libelle
                """;
        try (Connection conn = Database.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFournisseur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                catalogueEntries.add(new CatalogueEntry(
                        rs.getInt("id_produit"),
                        rs.getString("libelle"),
                        rs.getInt("niveau_dangerosite"),
                        rs.getString("description"),
                        rs.getDouble("prix")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement catalogue : " + e.getMessage());
        }
    }

    // ===== ACTIONS CATALOGUE =====

    @FXML
    private void onAjouterProduit() {
        String nom = nomProduitCatalogueField != null ? nomProduitCatalogueField.getText().trim() : "";
        String prixTxt = prixCatalogueField != null ? prixCatalogueField.getText().trim() : "";

        if (nom.isEmpty()) {
            afficherErreur("Champ manquant", "Le nom du produit est obligatoire.");
            return;
        }
        if (dangerositeCatalogueBox == null || dangerositeCatalogueBox.getValue() == null) {
            afficherErreur("Champ manquant", "Le niveau de dangerosité est obligatoire.");
            return;
        }
        double prix;
        try {
            prix = Double.parseDouble(prixTxt.replace(',', '.'));
            if (prix <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherErreur("Prix invalide", "Le prix doit être un nombre positif (ex : 12.50).");
            return;
        }

        // Vérifier que ce produit n'est pas déjà dans la liste
        boolean dejaPresent = catalogueEntries.stream()
                .anyMatch(e -> e.libelle.equalsIgnoreCase(nom));
        if (dejaPresent) {
            afficherErreur("Doublon", "\"" + nom + "\" est déjà dans le catalogue.");
            return;
        }

        int dangerosite = dangerositeCatalogueBox.getValue();
        String description = descriptionCatalogueField != null ? descriptionCatalogueField.getText().trim() : "";

        // Chercher si le produit existe déjà en base (même libellé)
        int idProduitExistant = trouverIdProduitParLibelle(nom);

        catalogueEntries.add(new CatalogueEntry(idProduitExistant, nom, dangerosite, description, prix));

        // Vider les champs
        nomProduitCatalogueField.clear();
        if (descriptionCatalogueField != null) descriptionCatalogueField.clear();
        dangerositeCatalogueBox.setValue(null);
        prixCatalogueField.clear();
    }

    @FXML
    private void onRetirerProduit() {
        if (catalogueTable == null) return;
        CatalogueEntry selected = catalogueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner un produit à retirer.");
            return;
        }
        catalogueEntries.remove(selected);
    }

    /** Retourne l'id_produit si un produit avec ce libellé existe, sinon 0. */
    private int trouverIdProduitParLibelle(String libelle) {
        String sql = "SELECT id_produit FROM produit WHERE libelle = ?";
        try (Connection conn = Database.getConnexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libelle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_produit");
        } catch (SQLException e) {
            System.err.println("Erreur recherche produit : " + e.getMessage());
        }
        return 0;
    }

    // ===== VALIDATION ET SAUVEGARDE =====

    @FXML
    private void valider() {
        if (!validerFormulaire()) return;

        String nom       = nomField.getText().trim();
        String contact   = contactField   != null ? contactField.getText().trim()   : "";
        String email     = emailField     != null ? emailField.getText().trim()     : "";
        String telephone = telephoneField != null ? telephoneField.getText().trim() : "";
        String adresse   = adresseField   != null ? adresseField.getText().trim()   : "";

        try (Connection conn = Database.getConnexion()) {
            conn.setAutoCommit(false);

            int idFournisseur;

            if (mode == Mode.CREATION) {
                idFournisseur = insererFournisseur(conn, nom, contact, email, telephone, adresse);
                if (idFournisseur <= 0) {
                    conn.rollback();
                    afficherErreur("Erreur", "Impossible d'enregistrer le fournisseur.");
                    return;
                }
                enregistrerHistoriqueConn(conn, "CREATION", "fournisseur", idFournisseur,
                        "Création fournisseur : " + nom);
            } else {
                fournisseur.setNom(nom);
                fournisseur.setContact(contact);
                fournisseur.setEmail(email);
                fournisseur.setTelephone(telephone);
                fournisseur.setAdresse(adresse);
                mettreAJourFournisseur(conn, fournisseur);
                idFournisseur = fournisseur.getId_fournisseur();
                enregistrerHistoriqueConn(conn, "MODIFICATION", "fournisseur", idFournisseur,
                        "Modification fournisseur : " + nom);
            }

            // Sauvegarder le catalogue (suppression + réinsertion)
            sauvegarderCatalogue(conn, idFournisseur);

            conn.commit();
            fermer();

        } catch (SQLException e) {
            afficherErreur("Erreur BD", e.getMessage());
        }
    }

    /** Insère un fournisseur et retourne son ID généré. */
    private int insererFournisseur(Connection conn, String nom, String contact,
                                   String email, String telephone, String adresse) throws SQLException {
        String sql = "INSERT INTO fournisseur (nom, contact, email, telephone, adresse, date_creation) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, contact);
            ps.setString(3, email);
            ps.setString(4, telephone);
            ps.setString(5, adresse);
            ps.setDate(6, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Met à jour un fournisseur existant. */
    private void mettreAJourFournisseur(Connection conn, Fournisseur f) throws SQLException {
        String sql = "UPDATE fournisseur SET nom=?, contact=?, email=?, telephone=?, adresse=? WHERE id_fournisseur=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getContact());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getTelephone());
            ps.setString(5, f.getAdresse());
            ps.setInt(6, f.getId_fournisseur());
            ps.executeUpdate();
        }
    }

    /**
     * Supprime toutes les associations produit_fournisseur pour ce fournisseur,
     * puis réinsère les entrées du catalogue courant.
     * Pour chaque entrée : si le produit n'existe pas encore (idProduit == 0), il est créé.
     */
    private void sauvegarderCatalogue(Connection conn, int idFournisseur) throws SQLException {
        // Supprimer les associations existantes
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM produit_fournisseur WHERE id_fournisseur = ?")) {
            ps.setInt(1, idFournisseur);
            ps.executeUpdate();
        }

        // Réinsérer chaque entrée du catalogue
        for (CatalogueEntry entry : catalogueEntries) {
            int idProduit = entry.idProduit;

            if (idProduit == 0) {
                // Créer le produit (quantite_stock = 0 à la création)
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO produit (libelle, description, niveau_dangerosite, quantite_stock) VALUES (?, ?, ?, 0)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, entry.libelle);
                    ps.setString(2, entry.description != null ? entry.description : "");
                    ps.setInt(3, entry.dangerosite);
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    idProduit = rs.next() ? rs.getInt(1) : 0;
                }
            }

            if (idProduit > 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO produit_fournisseur (id_produit, id_fournisseur, prix) VALUES (?, ?, ?)")) {
                    ps.setInt(1, idProduit);
                    ps.setInt(2, idFournisseur);
                    ps.setDouble(3, entry.prix);
                    ps.executeUpdate();
                }
            }
        }
    }

    private void enregistrerHistoriqueConn(Connection conn, String action, String table,
                                           int idEntite, String details) {
        String sql = "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateurConnecte);
            ps.setString(2, action);
            ps.setString(3, table);
            ps.setInt(4, idEntite);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Historique non enregistré : " + e.getMessage());
        }
    }

    // ===== VALIDATION FORMULAIRE =====

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (nomField == null || nomField.getText().trim().isEmpty())
            erreurs.append("- Le nom est obligatoire\n");
        if (nomField != null && nomField.getText().trim().length() > 100)
            erreurs.append("- Le nom ne peut pas dépasser 100 caractères\n");
        if (emailField != null && !emailField.getText().trim().isEmpty()) {
            if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
                erreurs.append("- L'adresse email n'est pas valide\n");
        }
        if (telephoneField != null && !telephoneField.getText().trim().isEmpty()) {
            if (!telephoneField.getText().trim().matches("^[0-9+\\-\\s()]{8,20}$"))
                erreurs.append("- Le numéro de téléphone n'est pas valide\n");
        }

        if (erreurs.length() > 0) {
            afficherErreur("Erreurs de validation", erreurs.toString());
            return false;
        }
        return true;
    }

    // ===== UTILS =====

    @FXML private void annuler() { fermer(); }

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
