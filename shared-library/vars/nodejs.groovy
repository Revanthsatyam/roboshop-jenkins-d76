def call() {
  pipeline {

    agent {
      label 'workstation'
    }

    tools {
      nodejs 'nodejs'
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
          sh 'npm test'
        }
      }

    }

  }
}