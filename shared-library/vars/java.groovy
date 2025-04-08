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

      stage ('Compile & Build') {
        steps {
          sh 'mvn clean package'
        }
      }

      stage ('Test') {
        steps {
          sh 'mvn test'
        }
      }

      stage ('SonarQube Analysis') {
        steps {
          withSonarQubeEnv('sonarqube') {
            script {
              def scannerHome = tool 'sonarqube'
              sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${env.component} -Dsonar.java.binaries=target"
            }
          }
        }
      }

      stage('Quality Gate') {
        steps {
          //timeout(time: 1, unit: 'MINUTES') {
          //  waitForQualityGate abortPipeline: true
          //}
          waitForQualityGate abortPipeline: true
        }
      }

      stage ('Code Security') {
        steps {
          echo 'Code Secured'
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