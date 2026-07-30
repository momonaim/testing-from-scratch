# ⚙️ Étape 8 : Pipeline CI/CD avec Jenkins (backend + frontend React)

## 🎯 Objectif

Mettre en place une pipeline Jenkins complète qui build et teste **le backend Spring Boot ET le frontend React**, construit les deux images Docker, les pousse sur un registre, et déploie l'ensemble.

## 📋 Pré-requis

- Étapes 1 à 7 validées
- Jenkins installé et accessible
- Un dépôt Git distant (GitHub/GitLab) contenant le projet (structure `backend/` + `frontend/` + `e2e-tests/`)

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

**Manage Jenkins → Plugins → Available plugins** :

- [ ] Git plugin
- [ ] Pipeline
- [ ] Maven Integration
- [ ] NodeJS plugin (pour builder le frontend React)
- [ ] Docker Pipeline
- [ ] JUnit
- [ ] HTML Publisher (rapports Playwright)
- [ ] Credentials Binding

### 8.3 Configurer les outils globaux

**Manage Jenkins → Tools** :

- [ ] JDK 17 configuré (nom : `jdk17`)
- [ ] Maven 3.9+ configuré (nom : `maven3`)
- [ ] NodeJS 20.x configuré (nom : `node20`) - via le plugin NodeJS

### 8.4 Configurer les credentials Docker Hub

**Manage Jenkins → Credentials → Global → Add Credentials** :

- Type : `Username with password` (ou token)
- ID : `docker-hub-credentials`
- [ ] Credential créée

### 8.5 Configurer les credentials Git (si dépôt privé)

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
        nodejs 'node20'
    }

    environment {
        DOCKER_REGISTRY = "votre-user-dockerhub"
        BACKEND_IMAGE = "${DOCKER_REGISTRY}/task-manager-backend"
        FRONTEND_IMAGE = "${DOCKER_REGISTRY}/task-manager-frontend"
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

        // ---------- BACKEND ----------

        stage('Backend: Build') {
            steps {
                dir('backend') {
                    sh 'mvn -B clean compile'
                }
            }
        }

        stage('Backend: Unit & API Tests') {
            steps {
                dir('backend') {
                    sh 'mvn -B test -Dtest=!*UiTest'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Backend: Package') {
            steps {
                dir('backend') {
                    sh 'mvn -B package -DskipTests'
                }
            }
        }

        // ---------- FRONTEND ----------

        stage('Frontend: Install & Build') {
            steps {
                dir('frontend') {
                    sh '''
                        npm ci
                        npm run build
                    '''
                }
            }
        }

        stage('Frontend: Lint') {
            steps {
                dir('frontend') {
                    sh 'npm run lint || true'
                }
            }
        }

        // ---------- E2E TESTS (full stack) ----------

        stage('Start Stack for E2E') {
            steps {
                sh 'docker compose up -d --build'
                sh 'sleep 15' // attendre que backend + db soient prêts
            }
        }

        stage('E2E Tests (Playwright)') {
            steps {
                dir('e2e-tests') {
                    sh '''
                        npm ci
                        npx playwright install --with-deps
                        npx playwright test --reporter=html
                    '''
                }
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir: 'e2e-tests/playwright-report',
                        reportFiles: 'index.html',
                        reportName: 'Playwright E2E Report'
                    ])
                }
            }
        }

        stage('Stop E2E Stack') {
            steps {
                sh 'docker compose down'
            }
        }

        // ---------- DOCKER IMAGES ----------

        stage('Build Docker Images') {
            steps {
                script {
                    backendImage = docker.build("${BACKEND_IMAGE}:${env.BUILD_NUMBER}", "./backend")
                    frontendImage = docker.build("${FRONTEND_IMAGE}:${env.BUILD_NUMBER}", "./frontend")
                }
            }
        }

        stage('Push Docker Images') {
            when { branch 'main' }
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', DOCKER_CREDENTIALS_ID) {
                        backendImage.push("${env.BUILD_NUMBER}")
                        backendImage.push("latest")
                        frontendImage.push("${env.BUILD_NUMBER}")
                        frontendImage.push("latest")
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
            sh 'docker compose down || true'
            cleanWs()
        }
    }
}
```

- [ ] Fichier créé et poussé sur le dépôt Git

### 8.7 Créer le Job Jenkins (Pipeline)

1. Jenkins → **New Item** → Nom : `task-manager-pipeline` → Type : **Pipeline**
2. **Pipeline → Definition** : `Pipeline script from SCM`
3. **SCM** : Git → URL du dépôt → credentials si privé
4. **Script Path** : `Jenkinsfile`

- [ ] Job créé et configuré

### 8.8 Configurer le déclenchement automatique (webhook)

**Option A - Polling SCM** : `Build Triggers` → `Poll SCM` → `H/5 * * * *`

**Option B - Webhook GitHub (recommandé)** :

1. GitHub → **Settings → Webhooks → Add webhook**
2. Payload URL : `http://votre-jenkins-url/github-webhook/`
3. Content type : `application/json`, événement : `push`
4. Jenkins job → cocher `GitHub hook trigger for GITScm polling`

- [ ] Déclenchement automatique configuré et testé

### 8.9 Premier lancement manuel

Jenkins → job → **Build Now**

- [ ] Checkout ✅
- [ ] Backend: Build ✅
- [ ] Backend: Unit & API Tests ✅
- [ ] Backend: Package ✅
- [ ] Frontend: Install & Build ✅
- [ ] Frontend: Lint ✅
- [ ] Start Stack for E2E ✅
- [ ] E2E Tests (Playwright) ✅
- [ ] Stop E2E Stack ✅
- [ ] Build Docker Images ✅
- [ ] Push Docker Images ✅ (branche `main` uniquement)
- [ ] Deploy ✅ (branche `main` uniquement)

### 8.10 Notifications (optionnel)

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
git commit -m "ci: pipeline Jenkins complète (backend + frontend + e2e + docker + deploy)"
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
- [ ] Les tests backend (Rest Assured) ET les tests E2E (Playwright sur React) sont exécutés
- [ ] Les rapports de tests sont publiés et consultables dans Jenkins
- [ ] Les deux images Docker (backend, frontend) sont poussées sur le registre
- [ ] L'application complète est déployée et accessible après un build réussi sur `main`
- [ ] Le déclenchement automatique sur push fonctionne

---

## ⚠️ Pièges courants

| Problème                                                           | Solution                                                                                                         |
| ------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------- |
| `npm: command not found` sur l'agent Jenkins                       | Vérifier la configuration du plugin NodeJS dans `Tools`, et bien déclarer `nodejs 'node20'` dans le bloc `tools` |
| `docker: command not found`                                        | Installer Docker sur l'agent, ou monter le socket si Jenkins tourne en conteneur                                 |
| E2E tests échouent en CI (pas de navigateur)                       | Toujours exécuter `npx playwright install --with-deps` avant `npx playwright test`                               |
| Stack Docker Compose ne se lève pas assez vite avant les tests E2E | Augmenter le `sleep` ou ajouter un vrai healthcheck/wait-for-it sur le endpoint `/api/tasks`                     |
| Webhook GitHub ne déclenche rien                                   | Vérifier l'accessibilité publique de Jenkins et l'URL exacte du webhook                                          |
| Pipeline lente (npm install + mvn install à chaque fois)           | Mettre en cache `~/.m2` et `node_modules`/`~/.npm` entre builds (volumes Docker ou cache Jenkins)                |

---

## 🎉 Fin de la roadmap

Cycle complet fonctionnel : **Code → Build backend & frontend → Tests unitaires/API → Tests E2E React → Images Docker → Déploiement**, déclenché automatiquement à chaque push.

➡️ Consultez `CHECKLIST.md` pour vérifier que toutes les étapes sont bien cochées de bout en bout.
