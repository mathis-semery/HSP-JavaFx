package com.hsp.service;

import com.hsp.dao.DemandeProduitDAO;
import com.hsp.dao.FournisseurDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.ProduitDAO;
import com.hsp.dao.ProduitFournisseurDAO;
import com.hsp.dao.ReapprovisionnementDAO;
import com.hsp.model.DemandeProduit;
import com.hsp.model.Fournisseur;
import com.hsp.model.Historique;
import com.hsp.model.Produit;
import com.hsp.model.ProduitFournisseur;
import com.hsp.model.Reapprovisionnement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des stocks de produits medicaux.
 * Utilise par :
 * - Le gestionnaire de stock : CRUD produits, validation demandes, reapprovisionnements
 * - Le medecin : envoi de demandes de produits
 */
public class StockService {

    // Statuts des demandes de produits
    public static final String STATUT_DEMANDE_EN_ATTENTE = "en_attente";
    public static final String STATUT_DEMANDE_VALIDEE = "validee";
    public static final String STATUT_DEMANDE_REFUSEE = "refusee";

    // Seuil d'alerte pour stock faible
    public static final int SEUIL_STOCK_FAIBLE = 10;

    private ProduitDAO produitDAO;
    private FournisseurDAO fournisseurDAO;
    private ProduitFournisseurDAO produitFournisseurDAO;
    private DemandeProduitDAO demandeProduitDAO;
    private ReapprovisionnementDAO reapprovisionnementDAO;
    private HistoriqueDAO historiqueDAO;

    public StockService() {
        this.produitDAO = new ProduitDAO();
        this.fournisseurDAO = new FournisseurDAO();
        this.produitFournisseurDAO = new ProduitFournisseurDAO();
        this.demandeProduitDAO = new DemandeProduitDAO();
        this.reapprovisionnementDAO = new ReapprovisionnementDAO();
        this.historiqueDAO = new HistoriqueDAO();
    }

    // ========================================
    // GESTION DES PRODUITS
    // ========================================

    /**
     * Recupere tous les produits
     */
    public List<Produit> getAllProduits() {
        return produitDAO.findAll();
    }

    /**
     * Recupere un produit par son ID
     */
    public Produit getProduitById(int id) {
        return produitDAO.findById(id);
    }

    /**
     * Cree un nouveau produit (par le gestionnaire de stock)
     */
    public boolean creerProduit(Produit produit, int idGestionnaire) {
        boolean resultat = produitDAO.insert(produit);

        if (resultat) {
            String details = "Creation du produit: " + produit.getLibelle();
            enregistrerHistorique(idGestionnaire, "CREATION", "produit", produit.getId_produit(), details);
        }

        return resultat;
    }

    /**
     * Met a jour un produit
     */
    public boolean modifierProduit(Produit produit, int idGestionnaire) {
        boolean resultat = produitDAO.update(produit);

        if (resultat) {
            String details = "Modification du produit: " + produit.getLibelle() + " - Stock: " + produit.getQuantite_stock();
            enregistrerHistorique(idGestionnaire, "MODIFICATION", "produit", produit.getId_produit(), details);
        }

        return resultat;
    }

    /**
     * Supprime un produit
     */
    public boolean supprimerProduit(int idProduit, int idGestionnaire) {
        // Recuperer le produit avant suppression pour l'historique
        Produit produit = produitDAO.findById(idProduit);
        if (produit == null) {
            return false;
        }

        boolean resultat = produitDAO.delete(idProduit);

        if (resultat) {
            String details = "Suppression du produit: " + produit.getLibelle();
            enregistrerHistorique(idGestionnaire, "SUPPRESSION", "produit", idProduit, details);
        }

        return resultat;
    }

    /**
     * Recherche des produits par libelle ou description
     */
    public List<Produit> rechercherProduits(String recherche) {
        // Si la recherche est vide, retourner tous les produits
        if (recherche == null || recherche.trim().isEmpty()) {
            return getAllProduits();
        }

        String rechercheMinuscule = recherche.toLowerCase().trim();
        List<Produit> tousLesProduits = produitDAO.findAll();
        List<Produit> resultats = new ArrayList<>();

        for (Produit produit : tousLesProduits) {
            boolean correspond = false;

            // Verifier si le libelle correspond
            if (produit.getLibelle() != null && produit.getLibelle().toLowerCase().contains(rechercheMinuscule)) {
                correspond = true;
            }

            // Verifier si la description correspond
            if (produit.getDescription() != null && produit.getDescription().toLowerCase().contains(rechercheMinuscule)) {
                correspond = true;
            }

            if (correspond) {
                resultats.add(produit);
            }
        }

        return resultats;
    }

    /**
     * Recupere les produits avec un stock faible (inferieur ou egal au seuil)
     */
    public List<Produit> getProduitsStockFaible() {
        List<Produit> tousLesProduits = produitDAO.findAll();
        List<Produit> produitsStockFaible = new ArrayList<>();

        for (Produit produit : tousLesProduits) {
            if (produit.getQuantite_stock() <= SEUIL_STOCK_FAIBLE) {
                produitsStockFaible.add(produit);
            }
        }

        return produitsStockFaible;
    }

    /**
     * Recupere les produits par niveau de dangerosite
     */
    public List<Produit> getProduitsParDangerosite(int niveau) {
        List<Produit> tousLesProduits = produitDAO.findAll();
        List<Produit> resultats = new ArrayList<>();

        for (Produit produit : tousLesProduits) {
            if (produit.getNiveau_dangerosite() == niveau) {
                resultats.add(produit);
            }
        }

        return resultats;
    }

    /**
     * Met a jour la quantite en stock d'un produit
     */
    public boolean mettreAJourStock(int idProduit, int nouvelleQuantite, int idGestionnaire) {
        Produit produit = produitDAO.findById(idProduit);
        if (produit == null) {
            return false;
        }

        int ancienneQuantite = produit.getQuantite_stock();
        produit.setQuantite_stock(nouvelleQuantite);

        boolean resultat = produitDAO.update(produit);

        if (resultat) {
            String details = "Mise a jour stock: " + ancienneQuantite + " -> " + nouvelleQuantite;
            enregistrerHistorique(idGestionnaire, "MISE_A_JOUR_STOCK", "produit", idProduit, details);
        }

        return resultat;
    }

    /**
     * Verifie si un produit a un stock faible
     */
    public boolean estStockFaible(int idProduit) {
        Produit produit = produitDAO.findById(idProduit);
        if (produit == null) {
            return false;
        }
        return produit.getQuantite_stock() <= SEUIL_STOCK_FAIBLE;
    }

    // ========================================
    // GESTION DES FOURNISSEURS
    // ========================================

    /**
     * Recupere tous les fournisseurs
     */
    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurDAO.findAll();
    }

    /**
     * Recupere un fournisseur par son ID
     */
    public Fournisseur getFournisseurById(int id) {
        return fournisseurDAO.findById(id);
    }

    /**
     * Cree un nouveau fournisseur
     */
    public boolean creerFournisseur(Fournisseur fournisseur, int idGestionnaire) {
        boolean resultat = fournisseurDAO.insert(fournisseur);

        if (resultat) {
            String details = "Creation du fournisseur: " + fournisseur.getNom();
            enregistrerHistorique(idGestionnaire, "CREATION", "fournisseur", fournisseur.getId_fournisseur(), details);
        }

        return resultat;
    }

    /**
     * Met a jour un fournisseur
     */
    public boolean modifierFournisseur(Fournisseur fournisseur, int idGestionnaire) {
        boolean resultat = fournisseurDAO.update(fournisseur);

        if (resultat) {
            String details = "Modification du fournisseur: " + fournisseur.getNom();
            enregistrerHistorique(idGestionnaire, "MODIFICATION", "fournisseur", fournisseur.getId_fournisseur(), details);
        }

        return resultat;
    }

    /**
     * Supprime un fournisseur
     */
    public boolean supprimerFournisseur(int idFournisseur, int idGestionnaire) {
        Fournisseur fournisseur = fournisseurDAO.findById(idFournisseur);
        if (fournisseur == null) {
            return false;
        }

        boolean resultat = fournisseurDAO.delete(idFournisseur);

        if (resultat) {
            String details = "Suppression du fournisseur: " + fournisseur.getNom();
            enregistrerHistorique(idGestionnaire, "SUPPRESSION", "fournisseur", idFournisseur, details);
        }

        return resultat;
    }

    // ========================================
    // GESTION DES RELATIONS PRODUIT-FOURNISSEUR
    // ========================================

    /**
     * Recupere les fournisseurs d'un produit avec leurs prix
     */
    public List<ProduitFournisseur> getFournisseursDuProduit(int idProduit) {
        return produitFournisseurDAO.findByProduitId(idProduit);
    }

    /**
     * Recupere les produits d'un fournisseur
     */
    public List<ProduitFournisseur> getProduitsDuFournisseur(int idFournisseur) {
        return produitFournisseurDAO.findByFournisseurId(idFournisseur);
    }

    /**
     * Associe un fournisseur a un produit avec un prix
     */
    public boolean associerFournisseurProduit(int idProduit, int idFournisseur, double prix, int idGestionnaire) {
        ProduitFournisseur pf = new ProduitFournisseur(idProduit, idFournisseur, prix);
        boolean resultat = produitFournisseurDAO.insert(pf);

        if (resultat) {
            String details = "Association produit ID: " + idProduit + " avec fournisseur ID: " + idFournisseur + " - Prix: " + prix;
            enregistrerHistorique(idGestionnaire, "ASSOCIATION", "produit_fournisseur", idProduit, details);
        }

        return resultat;
    }

    /**
     * Met a jour le prix d'un produit chez un fournisseur
     */
    public boolean mettreAJourPrixFournisseur(int idProduit, int idFournisseur, double nouveauPrix, int idGestionnaire) {
        ProduitFournisseur pf = new ProduitFournisseur(idProduit, idFournisseur, nouveauPrix);
        boolean resultat = produitFournisseurDAO.update(pf);

        if (resultat) {
            String details = "Modification prix - Produit ID: " + idProduit + " Fournisseur ID: " + idFournisseur + " - Nouveau prix: " + nouveauPrix;
            enregistrerHistorique(idGestionnaire, "MODIFICATION_PRIX", "produit_fournisseur", idProduit, details);
        }

        return resultat;
    }

    /**
     * Supprime l'association entre un produit et un fournisseur
     */
    public boolean supprimerAssociationFournisseur(int idProduit, int idFournisseur, int idGestionnaire) {
        boolean resultat = produitFournisseurDAO.delete(idProduit, idFournisseur);

        if (resultat) {
            String details = "Suppression association produit ID: " + idProduit + " avec fournisseur ID: " + idFournisseur;
            enregistrerHistorique(idGestionnaire, "SUPPRESSION_ASSOCIATION", "produit_fournisseur", idProduit, details);
        }

        return resultat;
    }

    // ========================================
    // GESTION DES DEMANDES DE PRODUITS
    // ========================================

    /**
     * Recupere toutes les demandes de produits
     */
    public List<DemandeProduit> getAllDemandes() {
        return demandeProduitDAO.findAll();
    }

    /**
     * Recupere une demande par son ID
     */
    public DemandeProduit getDemandeById(int id) {
        return demandeProduitDAO.findById(id);
    }

    /**
     * Recupere les demandes d'un medecin
     */
    public List<DemandeProduit> getDemandesParMedecin(int idMedecin) {
        return demandeProduitDAO.findByMedecinId(idMedecin);
    }

    /**
     * Recupere les demandes en attente de validation
     */
    public List<DemandeProduit> getDemandesEnAttente() {
        return demandeProduitDAO.findByStatut(STATUT_DEMANDE_EN_ATTENTE);
    }

    /**
     * Cree une nouvelle demande de produit (par le medecin)
     */
    public boolean creerDemandeProduit(DemandeProduit demande, int idMedecin) {
        // Verifier que le produit existe
        Produit produit = produitDAO.findById(demande.getId_produit());
        if (produit == null) {
            return false;
        }

        // Definir le medecin et le statut initial
        demande.setId_medecin(idMedecin);
        demande.setStatut(STATUT_DEMANDE_EN_ATTENTE);

        // Definir la date de demande si non specifiee
        if (demande.getDate_demande() == null || demande.getDate_demande().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            demande.setDate_demande(LocalDateTime.now().format(formatter));
        }

        boolean resultat = demandeProduitDAO.insert(demande);

        if (resultat) {
            String details = "Demande de " + demande.getQuantite() + " unites du produit: " + produit.getLibelle();
            enregistrerHistorique(idMedecin, "DEMANDE_PRODUIT", "demande_produit", demande.getId_demande(), details);
        }

        return resultat;
    }

    /**
     * Valide une demande de produit (par le gestionnaire de stock)
     * Deduit la quantite du stock si la validation reussit
     */
    public boolean validerDemande(int idDemande, int idGestionnaire) {
        // Recuperer la demande
        DemandeProduit demande = demandeProduitDAO.findById(idDemande);
        if (demande == null) {
            return false;
        }

        // Verifier que la demande est en attente
        if (!STATUT_DEMANDE_EN_ATTENTE.equals(demande.getStatut())) {
            return false;
        }

        // Recuperer le produit
        Produit produit = produitDAO.findById(demande.getId_produit());
        if (produit == null) {
            return false;
        }

        // Verifier le stock disponible
        int quantiteDemandee = (int) demande.getQuantite();
        if (produit.getQuantite_stock() < quantiteDemandee) {
            return false; // Stock insuffisant
        }

        // Mettre a jour le statut de la demande
        demande.setStatut(STATUT_DEMANDE_VALIDEE);
        demande.setId_gestionnaire(idGestionnaire);

        boolean resultat = demandeProduitDAO.update(demande);

        if (resultat) {
            // Deduire la quantite du stock
            int nouveauStock = produit.getQuantite_stock() - quantiteDemandee;
            produit.setQuantite_stock(nouveauStock);
            produitDAO.update(produit);

            String details = "Validation demande - " + quantiteDemandee + " unites du produit ID: " + demande.getId_produit();
            enregistrerHistorique(idGestionnaire, "VALIDATION_DEMANDE", "demande_produit", idDemande, details);
        }

        return resultat;
    }

    /**
     * Refuse une demande de produit (par le gestionnaire de stock)
     */
    public boolean refuserDemande(int idDemande, String motifRefus, int idGestionnaire) {
        // Recuperer la demande
        DemandeProduit demande = demandeProduitDAO.findById(idDemande);
        if (demande == null) {
            return false;
        }

        // Verifier que la demande est en attente
        if (!STATUT_DEMANDE_EN_ATTENTE.equals(demande.getStatut())) {
            return false;
        }

        // Mettre a jour le statut de la demande
        demande.setStatut(STATUT_DEMANDE_REFUSEE);
        demande.setMotif_refus(motifRefus);
        demande.setId_gestionnaire(idGestionnaire);

        boolean resultat = demandeProduitDAO.update(demande);

        if (resultat) {
            String details = "Refus demande - Motif: " + motifRefus;
            enregistrerHistorique(idGestionnaire, "REFUS_DEMANDE", "demande_produit", idDemande, details);
        }

        return resultat;
    }

    // ========================================
    // GESTION DES REAPPROVISIONNEMENTS
    // ========================================

    /**
     * Recupere tous les reapprovisionnements
     */
    public List<Reapprovisionnement> getAllReapprovisionnements() {
        return reapprovisionnementDAO.findAll();
    }

    /**
     * Recupere un reapprovisionnement par son ID
     */
    public Reapprovisionnement getReapprovisionnementById(int id) {
        return reapprovisionnementDAO.findById(id);
    }

    /**
     * Recupere les reapprovisionnements d'un produit
     */
    public List<Reapprovisionnement> getReapprovisionnementsParProduit(int idProduit) {
        return reapprovisionnementDAO.findByProduitId(idProduit);
    }

    /**
     * Recupere les reapprovisionnements en attente de reception
     */
    public List<Reapprovisionnement> getReapprovisionnementsEnAttente() {
        List<Reapprovisionnement> tous = reapprovisionnementDAO.findAll();
        List<Reapprovisionnement> enAttente = new ArrayList<>();

        for (Reapprovisionnement reappro : tous) {
            // Si pas de date de reception, le reapprovisionnement est en attente
            if (reappro.getDate_reception() == null) {
                enAttente.add(reappro);
            }
        }

        return enAttente;
    }

    /**
     * Cree une commande de reapprovisionnement (par le gestionnaire de stock)
     */
    public boolean creerReapprovisionnement(Reapprovisionnement reappro, int idGestionnaire) {
        // Verifier que le produit existe
        Produit produit = produitDAO.findById(reappro.getId_produit());
        if (produit == null) {
            return false;
        }

        // Verifier que le fournisseur existe
        Fournisseur fournisseur = fournisseurDAO.findById(reappro.getId_fournisseur());
        if (fournisseur == null) {
            return false;
        }

        // Definir le gestionnaire et la date de commande
        reappro.setId_gestionnaire(idGestionnaire);
        if (reappro.getDate_commande() == null) {
            reappro.setDate_commande(LocalDate.now());
        }

        boolean resultat = reapprovisionnementDAO.insert(reappro);

        if (resultat) {
            String details = "Commande de reapprovisionnement - " + reappro.getQuantite() +
                    " unites du produit: " + produit.getLibelle() +
                    " chez fournisseur: " + fournisseur.getNom();
            enregistrerHistorique(idGestionnaire, "CREATION", "reapprovisionnement", reappro.getId_reappro(), details);
        }

        return resultat;
    }

    /**
     * Confirme la reception d'un reapprovisionnement et met a jour le stock
     */
    public boolean confirmerReceptionReapprovisionnement(int idReappro, LocalDate dateReception, int idGestionnaire) {
        // Recuperer le reapprovisionnement
        Reapprovisionnement reappro = reapprovisionnementDAO.findById(idReappro);
        if (reappro == null) {
            return false;
        }

        // Verifier que la reception n'a pas deja ete confirmee
        if (reappro.getDate_reception() != null) {
            return false;
        }

        // Definir la date de reception (aujourd'hui si non specifiee)
        if (dateReception == null) {
            dateReception = LocalDate.now();
        }
        reappro.setDate_reception(dateReception);

        boolean resultat = reapprovisionnementDAO.update(reappro);

        if (resultat) {
            // Mettre a jour le stock du produit
            Produit produit = produitDAO.findById(reappro.getId_produit());
            if (produit != null) {
                int nouveauStock = produit.getQuantite_stock() + reappro.getQuantite();
                produit.setQuantite_stock(nouveauStock);
                produitDAO.update(produit);
            }

            String details = "Reception confirmee - " + reappro.getQuantite() + " unites ajoutees au stock";
            enregistrerHistorique(idGestionnaire, "RECEPTION", "reapprovisionnement", idReappro, details);
        }

        return resultat;
    }

    /**
     * Supprime un reapprovisionnement
     */
    public boolean supprimerReapprovisionnement(int idReappro, int idGestionnaire) {
        boolean resultat = reapprovisionnementDAO.delete(idReappro);

        if (resultat) {
            String details = "Suppression du reapprovisionnement ID: " + idReappro;
            enregistrerHistorique(idGestionnaire, "SUPPRESSION", "reapprovisionnement", idReappro, details);
        }

        return resultat;
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * Compte le nombre de produits en stock faible
     */
    public int compterProduitsStockFaible() {
        List<Produit> produitsStockFaible = getProduitsStockFaible();
        return produitsStockFaible.size();
    }

    /**
     * Compte le nombre de demandes en attente
     */
    public int compterDemandesEnAttente() {
        List<DemandeProduit> demandesEnAttente = getDemandesEnAttente();
        return demandesEnAttente.size();
    }

    /**
     * Compte le nombre de reapprovisionnements en attente
     */
    public int compterReapprovisionnementsEnAttente() {
        List<Reapprovisionnement> enAttente = getReapprovisionnementsEnAttente();
        return enAttente.size();
    }

    /**
     * Enregistre une action dans l'historique pour la tracabilite (RGPD)
     */
    private void enregistrerHistorique(int idUtilisateur, String action, String table, int idEnregistrement, String details) {
        Historique historique = new Historique(
                0,                      // ID auto-genere
                idUtilisateur,          // Qui a fait l'action
                action,                 // Type d'action
                table,                  // Table concernee
                idEnregistrement,       // ID de l'enregistrement concerne
                LocalDateTime.now(),    // Date et heure de l'action
                details                 // Details de l'action
        );
        historiqueDAO.insert(historique);
    }
}
