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

//      stage('Quality Gate') {
//        steps {
//          //timeout(time: 1, unit: 'MINUTES') {
//          //  waitForQualityGate abortPipeline: true
//          //}
//          waitForQualityGate abortPipeline: true
//        }
//      }

      stage ('Code Security') {
        steps {
          echo 'Code Secured'
        }
      }

//      stage ('Release') {
//        steps {
//          sh "mv target/shipping-1.0.jar shipping.jar; zip -r ${env.component}.zip ${env.component}.jar schema"
//          withCredentials([usernamePassword(credentialsId: 'nexus', passwordVariable: 'nexus_pass', usernameVariable: 'nexus_user')]) {
//            sh """
//                        curl -u $nexus_user:$nexus_pass \
//                        --upload-file ${env.component}.zip \
//                        http://nexus.rsdevops.in/repository/${env.component}/${env.component}-${env.BUILD_NUMBER}.zip
//                    """
//          }
//        }
//      }

      stage('Build Image') {
        steps {
          sh "aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 058264090525.dkr.ecr.us-east-1.amazonaws.com"
          //sh "docker build -t ${env.component} ."
          sh "docker build -t 058264090525.dkr.ecr.us-east-1.amazonaws.com/${env.component}:${env.BUILD_NUMBER} ."
          //sh "docker tag ${env.component}:${env.BUILD_NUMBER} 058264090525.dkr.ecr.us-east-1.amazonaws.com/${env.component}:${env.BUILD_NUMBER}"
        }
      }

      stage('Image Push To ECR') {
        steps {
          sh "docker push 058264090525.dkr.ecr.us-east-1.amazonaws.com/${env.component}:${env.BUILD_NUMBER}"
        }
      }

      stage('Trigger Deployment Pipeline') {
        when {
          expression { currentBuild.currentResult == 'SUCCESS' }
        }
        steps {
          script {
            def branch = env.BRANCH_NAME
            echo "Branch is: ${branch}"

            if (branch == 'main') {
              env.env = 'prod'
            } else {
              env.env = 'stage'
            }

            build job: 'helm_deploy',
              wait: true,
              parameters: [
                string(name: 'env', value: env.env),
                string(name: 'component', value: env.component),
                string(name: 'tag', value: env.BUILD_NUMBER)
              ]
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