
# Projet TinyPet (PétiQuik)

Le projet PétiQuik (pour le retour de Groquik), est un projet réalisé sur Google App Engine. Il est constitué d'un Backend en Java et d'un front en Mithril.js.

### Liste des fonctionnalités

- Affichage des pétitions paginée (limite de 50 par page)
- Possibilité d'afficher les détails d'une pétition
- Connexion avec Google
- Signature d'une pétition en étant connecté
- Création de pétitions (nom, description, objectif de votes, image, tags)
- Affichage du profil avec listes des pétitions signées et créées
- Endpoint permettant l'ajout de données de démonstration (/petition)
- Système de recherche de Pétitions par Tags et par nom (sensible à la casse).

### Les améliorations possibles

- Correction du bouton Se connecter avec Google (il disparait de temps en temps, il faut rafraîchir la page).
- Amélioration de la vue du Profil, avec ajout de pagination en cas de trop nombreuses pétitions signées/créées.
- Connexion qui expire à chaque rafraîchissement de page (ajout d'un cookie? _miam_)

## Installation
Vous devez avoir un compte Google Cloud Platform (GCP), et avoir un projet disponible.
Dans ce projet, lancez Google App Engine et clonez ce répertoire Git.

- Pensez à modifier vos audiences/clients id dans le fichier Java "PetitionEndpoint.java" (Ligne 34 et 35).

- Pensez à modifier votre data-client_id dans le fichier app.js (Ligne 67).

- Configurez les index en utilisant la commande suivante, dans le répertoire racine :
```bash
gcloud datastore indexes create src/main/webapp/WEB-INF/index.yaml
```

- Compilez le code avec la commande suivante :
```bash
mvn package
```

- Déployez le code avec la commande suivante :
```bash
mvn appengine:deploy
```

- Rendez-vous sur https:// [ID_DE_VOTRE_PROJET] .ew.r.appspot.com]