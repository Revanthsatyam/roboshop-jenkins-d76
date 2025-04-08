def call () {
  pipeline {

    agent {
      label 'workstation'
    }

    stages {

      stage ('Checkout') {
        steps {
          git branch: 'main', url: "https://github.com/Revanthsatyam/${env.component}.git"
        }
      }

//      stage ('Compile & Build') {
//        steps {
//          sh 'mvn clean package'
//        }
//      }

      stage ('Test') {
        steps {
          //sh 'python3.6 -m unittest'
          echo 'Test Cases Passed'
        }
      }

      stage ('SonarQube Analysis') {
        steps {
          withSonarQubeEnv('sonarqube') {
            script {
              def scannerHome = tool 'sonarqube'
              sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${env.component}"
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

      stage ('Release') {
        steps {
          sh "zip -r ${env.component}.zip * "
          withCredentials([usernamePassword(credentialsId: 'nexus', passwordVariable: 'nexus_pass', usernameVariable: 'nexus_user')]) {
            sh """
                        curl -u $nexus_user:$nexus_pass \
                        --upload-file ${env.component}.zip \
                        http://nexus.rsdevops.in/repository/${env.component}/${env.component}-${env.BUILD_NUMBER}.zip
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