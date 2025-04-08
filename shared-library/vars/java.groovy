def call () {
  pipeline {

    agent {
      label 'workstation'
    }

    tools{
      maven 'maven'
    }

    stages {

      stage ('Checkout') {
        steps {
          git branch: 'main', url: "https://github.com/Revanthsatyam/${env.component}.git"
        }
      }

    }

  }
}