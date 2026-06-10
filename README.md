# SmartRDV - Système de Gestion de Rendez-vous

##  Description

SmartRDV est une application web de gestion de rendez-vous développée dans le cadre du module **Génie Logiciel Avancé**.

L'application permet aux utilisateurs de réserver des rendez-vous en ligne tandis que les administrateurs peuvent gérer les disponibilités, les créneaux horaires et les réservations à travers une interface intuitive.

Le projet met en œuvre plusieurs concepts avancés du génie logiciel tels que les Design Patterns, les tests automatisés, l'intégration continue (CI/CD), l'analyse de qualité avec SonarCloud et le déploiement automatique.



##  Fonctionnalités

###  Utilisateur

* Consultation des disponibilités
* Réservation de rendez-vous
* Annulation de rendez-vous
* Suggestions automatiques de créneaux proches
* Liste d'attente lorsque les créneaux sont complets

###  Administrateur

* Gestion des horaires de travail
* Gestion du calendrier
* Génération automatique des créneaux
* Blocage de dates avec motif
* Gestion des réservations
* Gestion des utilisateurs



##  Architecture

Le projet suit une architecture en couches :

```text
Frontend (React)
       ↓
API REST (Spring Boot)
       ↓
Services Métier
       ↓
Repositories (JPA)
       ↓
PostgreSQL
```

Cette architecture permet une meilleure maintenabilité et une séparation claire des responsabilités.



##  Technologies Utilisées

### Backend

* Java 21
* Spring Boot 3
* Spring Security
* JWT
* Hibernate / JPA
* PostgreSQL

### Frontend

* React 18
* Vite
* Tailwind CSS
* Axios

### Qualité Logicielle

* JUnit 5
* Mockito
* JaCoCo
* SonarCloud

### DevOps

* Docker
* GitHub Actions
* Railway
* GitHub Pages



##  Design Patterns Implémentés

Le projet utilise plusieurs Design Patterns :

| Pattern   | Description                                    |
| --------- | ---------------------------------------------- |
| Bridge    | Séparation entre abstraction et implémentation |
| Composite | Gestion uniforme des objets composites         |
| Decorator | Ajout dynamique de fonctionnalités             |
| Facade    | Simplification de l'accès aux services         |
| Flyweight | Réduction de la consommation mémoire           |
| Strategy  | Encapsulation des algorithmes métier           |
| DTO       | Transfert sécurisé des données                 |



##  Tests

Les tests ont été réalisés avec :

* JUnit 5
* Mockito
* Spring Boot Test

Les objectifs de qualité sont :

* Couverture supérieure à 80 %
* Aucune vulnérabilité critique
* Quality Gate SonarCloud validé



##  CI/CD

Le projet utilise GitHub Actions pour automatiser :

1. Compilation
2. Tests
3. Analyse SonarCloud
4. Génération des rapports
5. Déploiement automatique



##  Sécurité

Le système utilise :

* Authentification JWT
* Contrôle d'accès basé sur les rôles
* Hachage des mots de passe avec BCrypt
* Validation des données côté serveur



##  Déploiement

### Backend

Déployé sur Railway.

### Frontend

Déployé sur GitHub Pages.



##  Installation Locale

### Cloner le projet

```bash
git clone https://github.com/fatimezhra/Smart_rdv.git
```

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```



##  Réalisé par

* Fatima Ezzahra Khalil
* Ouiame Tayibi

### Encadrant

Pr. Fahd Kalloubi



##  Module

Génie Logiciel Avancé

Université Cadi Ayyad – Faculté des Sciences Semlalia Marrakech

Année Universitaire 2025-2026
