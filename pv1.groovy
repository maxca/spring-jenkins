pipeline {
    agent any
    tools {
        maven 'maven_3_9'
    }
    environment {
        DOCKER_HOME = '/opt/homebrew/bin/docker'  // Path to the Docker executable
        KUBECTL_HOME = '/opt/homebrew/bin/kubectl'
    }
    stages {
        stage('Clean Workspace') {
            steps {
                deleteDir() // Clean the workspace before starting
            }
        }
       
        stage('Build Maven') {
            steps {
                checkout([$class: 'GitSCM', credentialsId: 'githubpwd', branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/maxca/spring-jenkins.git']]])
                sh 'mvn clean install'
            }
        }
        stage('Clean Docker State') {
            steps {
                script {
                    sh '${DOCKER_HOME} system prune -af --volumes' // Clean all Docker resources
                }
            }
        }
        stage('Build Image') {
            steps {
                script {
                    // sh '${DOCKER_HOME} logout'
                    sh '${DOCKER_HOME} build -t app-service:latest .'
                }
            }
        }
        stage('Push image to Hub'){
    steps{
        script{
            withCredentials([usernamePassword(credentialsId: 'dockerpwd', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                sh '${DOCKER_HOME} login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}'
                sh '${DOCKER_HOME} tag app-service:latest maxca789/app-service:latest'
                sh '${DOCKER_HOME} push maxca789/app-service:latest'
                
                // Check for dangling images and remove them if any are found
                script {
                    def danglingImages = sh(script: "${DOCKER_HOME} images -f 'dangling=true' -q", returnStdout: true).trim()
                    if (danglingImages) {
                        sh "${DOCKER_HOME} rmi -f ${danglingImages}"
                    } else {
                        echo "No dangling images to remove."
                    }
                }
            }
        }
    }
}

       
        stage('Deploy to k8s') {
            steps {
                withKubeConfig([credentialsId: 'kubectlpwd', serverUrl: 'https://127.0.0.1:58558']) {
                    sh '${KUBECTL_HOME} get pods -n minikube-local'
                    sh '${KUBECTL_HOME} apply -f k8s/deployment.yaml -n minikube-local'
                    sh '${KUBECTL_HOME} apply -f k8s/service.yaml -n minikube-local'
                }
            }
        }
    }
}
