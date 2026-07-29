# ⚙️ Étape 8 : Pipeline CI/CD avec Jenkins

## 🎯 Objectif
Mettre en place une pipeline Jenkins complète qui build, teste (API + UI), package, construit l'image Docker, la pousse sur un registre et déploie l'application automatiquement.

## 📋 Pré-requis
- Étapes 1 à 7 validées
- Jenkins installé et accessible (voir `01-PREREQUISITES.md`)
- Un dépôt Git distant (GitHub/GitLab) contenant le projet

---

## 🧩 Sous-étapes détaillées

### 8.1 Pousser le projet sur un dépôt distant

```bash
git remote add origin https://github.com/votre-compte/task-manager.git
git branch -M main
git push -u origin main
```

- [ ] Le code est visible sur GitHub/GitLab

### 8.2 Installer les plugins Jenkins nécessaires

Dans Jenkins → **Manage Jenkins → Plugins → Available plugins**, installer :

- [ ] Git plugin
- [ ] Pipeline
- [ ] Maven Integration
- [ ] Docker Pipeline
- [ ] JUnit
- [ ] HTML Publisher (pour les rapports)
- [ ] Credentials Binding

### 8.3 Configurer les outils globaux

**Manage Jenkins → Tools** :
- [ ] JDK 17 configuré (nom : `jdk17`)
- [ ] Maven 3.9+ configuré (nom : `maven3`)

### 8.4 Configurer les credentials Docker Hub

**Manage Jenkins → Credentials → System → Global credentials → Add Credentials** :
- Type : `Username with password` (ou `Secret text` pour un token)
- ID : `docker-hub-credentials`
- [ ] Credential créée avec votre username/token Docker Hub

### 8.5 Configurer les credentials Git (si dépôt privé)

- Type : `SSH Username with private key` ou `Username with password` (token)
- ID : `git-credentials`
- [ ] Credential créée

### 8.6 Créer le Jenkinsfile complet

Fichier : `Jenkinsfile` (à la racine du projet)

```groovy
pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        DOCKER_IMAGE = "votre-user-dockerhub/task-manager"
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        stage('Unit & API Tests') {
            steps {
                sh 'mvn -B test -Dtest=!*UiTest'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('UI Tests') {
            steps {
                sh '''
                    mvn compile
                    mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"
                    mvn -B test -Dtest=*UiTest
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_IMAGE}:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Push Docker Image') {
            when { branch 'main' }
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', DOCKER_CREDENTIALS_ID) {
                        dockerImage.push("${env.BUILD_NUMBER}")
                        dockerImage.push("latest")
                    }
                }
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                sh '''
                    docker compose down || true
                    docker compose pull
                    docker compose up -d
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline terminée avec succès - Build #${env.BUILD_NUMBER}"
        }
        failure {
            echo "❌ Pipeline en échec - vérifier les logs et les rapports de tests"
        }
        always {
            cleanWs()
        }
    }
}
```

- [ ] Fichier créé et poussé sur le dépôt Git

### 8.7 Créer le Job Jenkins (Pipeline)

1. Jenkins → **New Item**
2. Nom : `task-manager-pipeline`
3. Type : **Pipeline**
4. Dans **Pipeline** → **Definition** : `Pipeline script from SCM`
5. **SCM** : Git
6. **Repository URL** : votre URL de dépôt
7. **Credentials** : sélectionner `git-credentials` si dépôt privé
8. **Script Path** : `Jenkinsfile`

- [ ] Job créé et configuré

### 8.8 Configurer le déclenchement automatique (webhook)

**Option A - Polling SCM (simple, sans webhook)**
Dans la configuration du job → **Build Triggers** → cocher `Poll SCM` → `H/5 * * * *` (toutes les 5 min)

**Option B - Webhook GitHub (recommandé)**
1. Dans GitHub : **Settings → Webhooks → Add webhook**
2. Payload URL : `http://votre-jenkins-url/github-webhook/`
3. Content type : `application/json`
4. Événements : `push`
5. Dans Jenkins job : cocher `GitHub hook trigger for GITScm polling`

- [ ] Déclenchement automatique configuré et testé (faire un `git push` test)

### 8.9 Premier lancement manuel

Dans Jenkins → job → **Build Now**

- [ ] Le pipeline se déclenche
- [ ] Chaque stage passe au vert (Checkout, Build, Unit Tests, UI Tests, Package, Docker Build, Push, Deploy)
- [ ] Les rapports JUnit sont visibles dans l'historique du build

### 8.10 Gérer les échecs et notifications (optionnel mais recommandé)

Ajouter dans le bloc `post` du Jenkinsfile (nécessite le plugin Email Extension ou Slack) :

```groovy
post {
    failure {
        mail to: 'votre-email@example.com',
             subject: "❌ Pipeline échouée: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
             body: "Voir les détails: ${env.BUILD_URL}"
    }
}
```

- [ ] Notifications configurées (email ou Slack)

### 8.11 Commit final

```bash
git add .
git commit -m "ci: pipeline Jenkins complète (build, tests, docker, deploy)"
git push
```

---

## 📁 Fichiers créés/modifiés
```
Jenkinsfile
```

---

## ✅ Critères de validation de l'étape

- [ ] Le pipeline complet passe du checkout jusqu'au déploiement sans intervention manuelle
- [ ] Les tests API et UI sont exécutés et leurs rapports publiés
- [ ] L'image Docker est poussée sur Docker Hub (ou registre choisi)
- [ ] L'application est déployée et accessible après un build réussi
- [ ] Le déclenchement automatique sur push fonctionne

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| `docker: command not found` dans Jenkins | Installer Docker sur l'agent Jenkins, ou monter le socket Docker si Jenkins tourne en conteneur (`-v /var/run/docker.sock:/var/run/docker.sock`) |
| Tests UI échouent en CI (pas de navigateur) | Toujours exécuter `playwright install --with-deps` avant les tests UI dans le pipeline |
| Permission denied sur `docker.sock` | Ajouter l'utilisateur Jenkins au groupe `docker` sur l'agent |
| Webhook GitHub ne déclenche rien | Vérifier que Jenkins est accessible publiquement (ou utiliser ngrok en test local) et que l'URL du webhook est correcte |
| Pipeline très lente | Mettre en cache le repo `.m2` entre builds (volume Docker ou `mvn -o` avec dépendances pré-téléchargées) |

---

## 🎉 Fin de la roadmap

Une fois cette étape validée, le cycle complet est fonctionnel :
**Code → Build → Tests API → Tests UI → Package → Image Docker → Déploiement**, déclenché automatiquement à chaque push.

➡️ Consultez `CHECKLIST.md` pour vérifier que toutes les étapes sont bien cochées de bout en bout.
