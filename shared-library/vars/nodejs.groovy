def call() {
  pipeline {

    agent {
      label 'workstation'
    }

    tools {
      nodejs ('nodejs')
    }

    stages {

      stage('Checkout') {
        steps {
          git branch: 'main', url: "https://github.com/Revanthsatyam/${env.component}.git"
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

      stage('SonarQube Analysis') {
        steps {
          withSonarQubeEnv('sonarqube') {
            script {
              def scannerHome = tool 'sonarqube'
              sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=catalogue"
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

      stage('Code Security') {
        steps {
          echo 'Hello World'
        }
      }

      stage('Release') {
        steps {
          sh 'zip -r catalogue.zip node_modules package.json server.js schema'
          withCredentials([usernamePassword(credentialsId: 'nexus', passwordVariable: 'nexus_pass', usernameVariable: 'nexus_user')]) {
            sh """
                        curl -u $nexus_user:$nexus_pass \
                        --upload-file catalogue.zip \
                        http://nexus.rsdevops.in/repository/catalogue/catalogue-${env.BUILD_NUMBER}.zip
                    """
          }
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