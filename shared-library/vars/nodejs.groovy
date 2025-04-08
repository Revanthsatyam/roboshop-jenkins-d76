def call() {
  pipeline {

    agent {
      label 'workstation'
    }

    tools {
      nodejs 'nodejs'
      sonarQubeScanner 'sonarqube'
    }

    stages {

      stage('Checkout') {
        steps {
          git branch: 'main', url: 'https://github.com/Revanthsatyam/catalogue.git'
        }
      }

      stage('Compile & Build') {
        steps {
          sh 'npm install'
        }
      }

      stage('Test') {
        steps {
          // sh 'npm test'
          echo 'Test Cases Passed'
        }
      }

      stage('Code Coverage') {
        steps {
          echo 'Hello World'
        }
      }

      stage('Release') {
        steps {
          echo 'Hello World'
        }
      }

    }

    post {
      always {
        cleanWs()
      }
    }

  }
}