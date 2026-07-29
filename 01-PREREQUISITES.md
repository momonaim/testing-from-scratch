# 🔧 Étape 1 : Prérequis & Installation des outils

## 🎯 Objectif
Installer et vérifier tous les outils nécessaires avant de démarrer le développement.

## 📋 Pré-requis
Aucun - c'est le point de départ.

---

## 🧩 Sous-étapes détaillées

### 1.1 Java 17+

```bash
# Vérifier si Java est déjà installé
java -version

# Sous Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk -y

# Sous macOS (avec Homebrew)
brew install openjdk@17

# Sous Windows : télécharger depuis
# https://adoptium.net/temurin/releases/
```

- [ ] `java -version` affiche bien une version ≥ 17
- [ ] `JAVA_HOME` est bien configuré (`echo $JAVA_HOME`)

### 1.2 Maven 3.8+

```bash
# Vérifier
mvn -version

# Ubuntu/Debian
sudo apt install maven -y

# macOS
brew install maven
```

- [ ] `mvn -version` fonctionne et affiche la version Java associée

### 1.3 Git

```bash
git --version

# Ubuntu/Debian
sudo apt install git -y

# Configuration initiale
git config --global user.name "Votre Nom"
git config --global user.email "vous@example.com"
```

- [ ] `git --version` fonctionne
- [ ] Identité Git configurée

### 1.4 Docker & Docker Compose

```bash
docker --version
docker compose version

# Installation Ubuntu : https://docs.docker.com/engine/install/ubuntu/
# Installation macOS/Windows : Docker Desktop
# https://www.docker.com/products/docker-desktop/
```

- [ ] `docker run hello-world` fonctionne sans erreur
- [ ] `docker compose version` retourne une version ≥ 2.x

### 1.5 IDE (recommandé)

- [ ] IntelliJ IDEA (Community ou Ultimate) **OU** VS Code avec extensions Java
- [ ] Plugin Lombok installé (si vous utilisez Lombok)
- [ ] Plugin Spring Boot / Spring Assistant installé

### 1.6 Node.js & npm (optionnel, pour Playwright en JS)

```bash
node -v
npm -v

# Via nvm (recommandé)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install --lts
```

- [ ] Node ≥ 18 installé (seulement si vous testez la variante JS de Playwright)

### 1.7 Jenkins

Deux options :

**Option A - Jenkins en local via Docker (recommandé pour ce projet)**
```bash
docker run -d --name jenkins \
  -p 8081:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts
```
Puis récupérer le mot de passe initial :
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

**Option B - Installation native**
- Suivre https://www.jenkins.io/doc/book/installing/

- [ ] Jenkins accessible sur `http://localhost:8081`
- [ ] Mot de passe admin initial récupéré et compte admin créé
- [ ] Plugins suggérés installés (Git, Pipeline, Maven Integration, Docker Pipeline)

### 1.8 Navigateurs pour les tests UI

- [ ] Google Chrome installé
- [ ] Firefox installé (optionnel, pour cross-browser)

### 1.9 Postman ou cURL (tests manuels de l'API)

```bash
curl --version
```
- [ ] cURL disponible **OU** Postman installé (https://www.postman.com/downloads/)

---

## 📁 Fichiers à créer/modifier
Aucun fichier projet à cette étape - uniquement configuration de la machine.

---

## ✅ Critères de validation de l'étape

- [ ] Toutes les commandes de vérification (`java -version`, `mvn -version`, `git --version`, `docker --version`) s'exécutent sans erreur
- [ ] Jenkins tourne et est accessible via navigateur
- [ ] Un IDE est installé et prêt à ouvrir un projet Maven

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| `JAVA_HOME` non défini | Ajouter `export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))` dans `.bashrc`/`.zshrc` |
| Docker nécessite `sudo` sous Linux | Ajouter votre utilisateur au groupe docker : `sudo usermod -aG docker $USER` puis relancer la session |
| Port 8080 déjà utilisé par Jenkins | Changer le port Jenkins (`-p 8081:8080`) car Spring Boot utilisera le 8080 |
| Maven télécharge très lentement les dépendances | Vérifier la connexion réseau / configurer un mirror dans `settings.xml` |

---

## ➡️ Prochaine étape
`02-PROJECT-SETUP.md` - Génération du squelette du projet Spring Boot
