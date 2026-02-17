-- Script pour inserer un utilisateur Admin dans la base de donnees hsp_urgences
-- Le mot de passe est 'admin123' hashe avec BCrypt
-- Le hash BCrypt est genere par l'utilitaire CreateAdmin.java

-- IMPORTANT : Executez d'abord CreateAdmin.java pour inserer l'admin via BCrypt,
-- ou utilisez cette requete si vous avez deja un hash BCrypt :

-- INSERT INTO utilisateur (nom, prenom, email, mdp, role)
-- VALUES ('Admin', 'Super', 'admin@hsp.fr', '<BCRYPT_HASH>', 'Admin');

-- Pour generer le hash et inserer l'admin, executez :
-- java com.hsp.util.CreateAdmin
