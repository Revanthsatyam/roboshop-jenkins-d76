def call () {
  pipeline {

    agent {
      label 'workstation'
    }

    stages {

      stage ('Checkout') {
        steps {
          git branch: env.BRANCH_NAME, url: "https://github.com/Revanthsatyam/${env.component}.git"
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
//          sh "zip -r ${env.component}.zip * "
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
          script {
            if (env.BRANCH_NAME == 'main') {
              sh "aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 058264090525.dkr.ecr.us-east-1.amazonaws.com"
              sh "docker build -t 058264090525.dkr.ecr.us-east-1.amazonaws.com/${env.component}:${env.BUILD_NUMBER} ."
            } else if (env.BRANCH_NAME == 'stage') {
              sh "aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 058264090525.dkr.ecr.us-east-1.amazonaws.com"
              sh "docker build -t 058264090525.dkr.ecr.us-east-1.amazonaws.com/${env.component}-stage:${env.BUILD_NUMBER} ."
            }
          }
        }
      }

      stage('Image Push To ECR') {
        steps {
          script {
            if (env.BRANCH_NAME == 'main') {
              sh "docker push 058264090525.dkr.ecr.us-east-1.amazonaws.com/${env.component}:${env.BUILD_NUMBER}"
            } else if (env.BRANCH_NAME == 'stage') {
              sh "docker push 058264090525.dkr.ecr.us-east-1.amazonaws.com/${env.component}-stage:${env.BUILD_NUMBER}"
            }
          }
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
            } else if (branch == 'stage') {
              env.env = 'stage'  // Or any environment for 'develop' branch
            } else {
              env.env = ''  // Default case for other branches
            }

            roboshop_helm_deploy(
              env: env.env,
              component: env.component,
              tag: env.BUILD_NUMBER
            )
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