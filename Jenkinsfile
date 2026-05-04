// Jenkinsfile - Complete GitFlow CI/CD Pipeline with GitHub Polling
pipeline {
    agent {
        kubernetes {
                    yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          nodeSelector:
            role: secondary
          containers:
          - name: jnlp
            image: jenkins/inbound-agent:latest
            args: ["$(JENKINS_SECRET)", "$(JENKINS_NAME)"]
          restartPolicy: Never
        '''
                }
    }

    // ============ POLLING TRIGGER CONFIGURATION ============
    triggers {
        // Poll GitHub every 5 minutes for changes on all branches
        pollSCM('H/5 * * * *')

        // For testing, use every minute: '* * * * *'
        // For production, use: 'H */4 * * *' (every 4 hours)
    }

    environment {
        // Application configuration
        APP_NAME = 'spring-boot3-elasticsearch'
        DOCKER_HUB_REPO = 'skariaj/spring-boot3-elasticsearch'

        // Different namespaces for different environments
        STAGING_NAMESPACE = 'staging'
        PRODUCTION_NAMESPACE = 'production'

        // Version calculation
        BUILD_VERSION = "${env.BUILD_NUMBER}"
        GIT_COMMIT_SHORT = ''

        // Branch detection - will be set in first stage
        IS_FEATURE = 'false'
        IS_DEVELOP = 'false'
        IS_RELEASE = 'false'
        IS_MAIN = 'false'
        IS_HOTFIX = 'false'
    }



    stages {

    stage('Checkout') {

            steps {
                git branch: "${BRANCH_NAME}",
                    url: "https://github.com/skariajampi/spring-boot3-elasticsearch.git",
                    credentialsId: "github-credentials"
            }
        }


        // ============ STAGE 0: BRANCH DETECTION (Runs first on all branches) ============
        stage('Detect Branch Type') {

            steps {
                script {
                    def branch = env.BRANCH_NAME

                    // Set branch type flags
                    IS_FEATURE = branch.startsWith('feature/')
                    IS_DEVELOP = (branch == 'develop' || branch == 'dev')
                    IS_RELEASE = branch.startsWith('release/')
                    IS_MAIN = (branch == 'main' || branch == 'master')
                    IS_HOTFIX = branch.startsWith('hotfix/')

                    // Print branch information
                    echo "=========================================="
                    echo "Branch: ${branch}"
                    echo "Build Number: ${BUILD_VERSION}"
                    echo "Git Commit: ${env.GIT_COMMIT_SHORT}"
                    echo "=========================================="
                    echo "Branch Type Detection:"
                    echo "  - Feature branch: ${IS_FEATURE}"
                    echo "  - Develop branch: ${IS_DEVELOP}"
                    echo "  - Release branch: ${IS_RELEASE}"
                    echo "  - Main branch: ${IS_MAIN}"
                    echo "  - Hotfix branch: ${IS_HOTFIX}"
                    echo "=========================================="

                    // Set environment variables for later stages
                    env.IS_FEATURE = IS_FEATURE.toString()
                    env.IS_DEVELOP = IS_DEVELOP.toString()
                    env.IS_RELEASE = IS_RELEASE.toString()
                    env.IS_MAIN = IS_MAIN.toString()
                    env.IS_HOTFIX = IS_HOTFIX.toString()

                    env.GIT_COMMIT_SHORT = sh(
                            script: "git rev-parse --short HEAD",
                            returnStdout: true
                        ).trim()
                }
            }
        }

        // ============ STAGE 1: UNIT TESTS (Runs on ALL branches) ============
        stage('Unit Tests') {
            when {
                        expression { return true }  // Always run on all branches
                    }
            agent {
                kubernetes {
                    yaml '''
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    role: secondary
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    args: ["$(JENKINS_SECRET)", "$(JENKINS_NAME)"]
  - name: maven
    image: maven:3.9.6-eclipse-temurin-21
    command: ["sleep"]
    args: ["infinity"]
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
    resources:
      requests:
        memory: "2Gi"
        cpu: "1000m"
      limits:
        memory: "4Gi"
        cpu: "2000m"
  restartPolicy: Never
  volumes:
  - name: maven-cache
    persistentVolumeClaim:
      claimName: maven-cache-pvc
'''
                }
            }
            steps {
                container('maven') {
                    script {
                        echo "Running Unit Tests on branch: ${env.BRANCH_NAME}"
                        echo "Branch type - Feature: ${IS_FEATURE}, Develop: ${IS_DEVELOP}, Release: ${IS_RELEASE}, Main: ${IS_MAIN}, Hotfix: ${IS_HOTFIX}"
                        echo "Branch type - Feature: ${env.IS_FEATURE}, Develop: ${env.IS_DEVELOP}, Release: ${env.IS_RELEASE}, Main: ${env.IS_MAIN}, Hotfix: ${env.IS_HOTFIX}"
                    }
                    sh '''
                        mvn clean test
                        mvn surefire-report:report
                    '''
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // ============ STAGE 2: INTEGRATION TESTS (develop, release, main, hotfix) ============
        stage('Integration Tests') {
            when {
                expression {
                        return (
                            IS_DEVELOP?.toBoolean() ||
                            IS_MAIN?.toBoolean() ||
                            IS_RELEASE?.toBoolean() ||
                            IS_HOTFIX?.toBoolean()
                        )
                    }
            }
            agent {
                kubernetes {
                    yaml '''
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    role: secondary
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    args: ["$(JENKINS_SECRET)", "$(JENKINS_NAME)"]
  - name: maven
    image: maven:3.9.6-eclipse-temurin-21
    command: ["sleep"]
    args: ["infinity"]
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
    resources:
      requests:
        memory: "2Gi"
        cpu: "1000m"
      limits:
        memory: "4Gi"
        cpu: "2000m"
  restartPolicy: Never
  volumes:
  - name: maven-cache
    persistentVolumeClaim:
      claimName: maven-cache-pvc
'''
                }
            }
            steps {
                container('maven') {
                    sh '''
                        echo "Running integration tests on branch: ${BRANCH_NAME}"
                        mvn verify
                    '''
                }
            }
//             post {
//                 always {
//                     junit 'target/failsafe-reports/*.xml'
//                 }
//             }
        }

        // ============ STAGE 3: BUILD & PACKAGE (develop, release, main, hotfix) ============
        stage('Build & Package') {
            when {
                expression {
                        return (
                            IS_DEVELOP?.toBoolean() ||
                            IS_RELEASE?.toBoolean() ||
                            IS_MAIN?.toBoolean() ||
                            IS_HOTFIX?.toBoolean()
                        )
                    }
            }
            agent {
                kubernetes {
                    yaml '''
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    role: secondary
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    args: ["$(JENKINS_SECRET)", "$(JENKINS_NAME)"]
  - name: maven
    image: maven:3.9.6-eclipse-temurin-21
    command: ["sleep"]
    args: ["infinity"]
    resources:
      requests:
        memory: "2Gi"
        cpu: "1000m"
      limits:
        memory: "4Gi"
        cpu: "2000m"
  restartPolicy: Never
'''
                }
            }
            steps {
                container('maven') {
                    sh 'mvn package -DskipTests'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        // ============ STAGE 4: BUILD DOCKER IMAGE (release, main, hotfix) ============
        stage('Build Docker Image') {
            when {
                expression {
                        return (
                            IS_RELEASE?.toBoolean() ||
                            IS_MAIN?.toBoolean() ||
                            IS_HOTFIX?.toBoolean()
                        )
                    }
            }
            agent {
                kubernetes {
                    yaml '''
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    role: secondary
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    args: ["$(JENKINS_SECRET)", "$(JENKINS_NAME)"]
  - name: docker
    image: docker:24-dind
    command: ["dockerd-entrypoint.sh"]
    securityContext:
      privileged: true
    resources:
      requests:
        memory: "1Gi"
        cpu: "500m"
      limits:
        memory: "2Gi"
        cpu: "1000m"
  restartPolicy: Never
'''
                }
            }
            steps {
                container('docker') {
                withCredentials([
                                         usernamePassword(
                                             credentialsId: 'dockerhub-credentials',
                                             usernameVariable: 'DOCKER_USER',
                                             passwordVariable: 'DOCKER_PASS'
                                         )
                                     ])
                                 {
                    script {
                        def imageTag = ""
                        def pomVersion = readMavenPom().version

                        if (IS_MAIN?.toBoolean()) {
                            // Production tag from pom.xml version
                            imageTag = pomVersion
                            echo "Building production image with tag: ${imageTag}"
                        } else if (IS_RELEASE?.toBoolean()) {
                            // Release candidate tag
                            def releaseVersion = env.BRANCH_NAME.replace('release/', '')
                            imageTag = "${releaseVersion}-rc.${BUILD_VERSION}"
                            echo "Building release candidate image with tag: ${imageTag}"
                        } else if (IS_HOTFIX?.toBoolean()) {
                            // Hotfix tag
                            def hotfixVersion = BRANCH_NAME.replace('hotfix/', '')
                            imageTag = "${pomVersion}-hotfix-${BUILD_VERSION}"
                            echo "Building hotfix image with tag: ${imageTag}"
                        }

                        // Build Docker image
                        sh """
                            docker build -t ${DOCKER_HUB_REPO}:${imageTag} .
                            docker tag ${DOCKER_HUB_REPO}:${imageTag} ${DOCKER_HUB_REPO}:latest

                        """

                        // Save image tag for later stages
                        env.IMAGE_TAG = imageTag

                        // Push docker image
                        sh """
                            echo "${DOCKER_PASS}" | docker login -u skariaj --password-stdin
                            docker push ${DOCKER_HUB_REPO}:${imageTag}
                            docker push ${DOCKER_HUB_REPO}:latest
                            echo "Successfully pushed: ${DOCKER_HUB_REPO}:${imageTag}"
                        """
                    }
                    }
                }
            }
        }


        // ============ STAGE 6: DEPLOY TO STAGING (release branches) ============
        stage('Deploy to Staging') {
            when {
                expression { return (IS_RELEASE?.toBoolean()) }
            }
            agent {
                kubernetes {
                    yaml '''
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    role: secondary
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    args: ["$(JENKINS_SECRET)", "$(JENKINS_NAME)"]
  - name: kubectl
    image: bitnami/kubectl:latest
    command: ["sleep"]
    args: ["infinity"]
  restartPolicy: Never
'''
                }
            }
            steps {
                container('kubectl') {
                    script {
                        def releaseVersion = env.BRANCH_NAME.replace('release/', '')
                        def imageTag = env.IMAGE_TAG

                        sh """
                            kubectl apply -f deployment.yaml
                            kubectl set image deployment/${APP_NAME} \
                              ${APP_NAME}=${DOCKER_HUB_REPO}:${IMAGE_TAG} \
                              -n ${STAGING_NAMESPACE}

                            kubectl rollout status deployment/${APP_NAME} -n ${STAGING_NAMESPACE}
                        """
                    }
                }
            }
        }

        /* stage('Approve Production Deploy') {
            when {
                expression { return (IS_MAIN?.toBoolean()) }
            }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
            }
        } */


        // ============ STAGE 7: DEPLOY TO PRODUCTION (main branch with approval) ============
        stage('Deploy to Production') {
            when {
                expression { return (IS_MAIN?.toBoolean()) }
            }
            agent {
                kubernetes {
                    yaml '''
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    role: secondary
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    args: ["$(JENKINS_SECRET)", "$(JENKINS_NAME)"]
  - name: kubectl
    image: alpine/k8s:1.29.2
    command:
    - cat
    tty: true
  restartPolicy: Never
'''
                }
            }
            steps {

                container('kubectl') {
                    script {
                        def pomVersion = readMavenPom().version
                        def imageTag = env.IMAGE_TAG ?: pomVersion
                        echo "deploy to production with image with tag: ${imageTag} and pomVersion: ${pomVersion}"
                        sh """
                            # Create production namespace if not exists
                            #kubectl create namespace ${PRODUCTION_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                            # Deploy to production
                            kubectl apply -f k8s/production/deployment.yaml

                            # Wait for rollout
                            kubectl rollout status deployment/${APP_NAME} -n ${PRODUCTION_NAMESPACE} --timeout=5m

                            # Verify deployment
                            kubectl get pods -n ${PRODUCTION_NAMESPACE}
                        """
                    }
                }
            }
        }

        // ============ STAGE 8: CREATE GIT TAG (main branch) ============
        stage('Create Git Tag') {
            when {
                expression { return (IS_MAIN?.toBoolean()) }
            }
            steps {
                script {
                    def pomVersion = readMavenPom().version
                    withCredentials([
                                                                             usernamePassword(
                                                                                 credentialsId: 'github-credentials',
                                                                                 usernameVariable: 'GITHUB_USER',
                                                                                 passwordVariable: 'GITHUB_TOKEN'
                                                                             )
                                                                         ]) {
                        sh """
                            git config user.email "jenkins@homelab.local"
                            git config user.name "Jenkins CI"
                            git tag -a v${pomVersion} -m "Release version ${pomVersion}"
                            git push https://skariajampi:\${GITHUB_TOKEN}@github.com/skariajampi/spring-boot3-elasticsearch.git v${pomVersion}
                            echo "Created tag: v${pomVersion}"
                        """
                    }
                }
            }
        }

        // ============ STAGE 9: CLEANUP (feature branches after merge) ============
        stage('Cleanup') {
            when {
                expression { return (IS_FEATURE?.toBoolean()) }
            }
            steps {
                echo "Feature branch ${env.BRANCH_NAME} build completed successfully."
                echo "This branch will be cleaned up after merge to develop."

                script {
                    // Optional: Add logic to delete feature branch from remote after merge
                    // This would typically be done manually after PR is merged
                    echo "No cleanup logic yet"
                }
            }
        }
    }

    post {
        success {
            echo "=========================================="
            echo "Pipeline completed successfully!"
            echo "Branch: ${env.BRANCH_NAME}"
            echo "Build: ${env.BUILD_URL}"
            echo "=========================================="
        }
        failure {
            echo "=========================================="
            echo "Pipeline failed!"
            echo "Branch: ${env.BRANCH_NAME}"
            echo "Build: ${env.BUILD_URL}"
            echo "=========================================="

            // Optional: Send email notification
            // emailext (
            //     subject: "Pipeline Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            //     body: "The pipeline failed. Check console output at ${env.BUILD_URL}",
            //     to: 'team@example.com'
            // )
        }
        always {
            // Clean up workspace
            cleanWs()
        }
    }
}