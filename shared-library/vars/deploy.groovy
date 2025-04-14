def call() {
  pipeline {
    agent any

    options {
      ansiColor('xterm')
    }

    environment {
      SSH = credentials('centos-ssh')
    }

    parameters {
      string(name: 'COMPONENT', defaultValue: '', description: 'Which Component To Deploy?')
      string(name: 'VERSION', defaultValue: '', description: 'Which Version To Deploy?')
      string(name: 'ENV', defaultValue: '', description: 'Which Environment To Deploy?')
    }

    stages {

      stage('Parameter Store Update') {
        steps {
          sh '''
            aws ssm put-parameter --name "${COMPONENT}.${ENV}.version" --type "String" --value "${VERSION}" --overwrite
'''
//          script {
//            addShortText(text: "${ENV}-${COMPONENT}-${VERSION}")
//          }
        }
      }

      stage('Deploy') {
        steps {
          sh '''
            aws ec2 describe-instances --filters "Name=tag:Name,Values=${COMPONENT}-${ENV}" --query 'Reservations[*].Instances[*].PrivateIpAddress' --output text >inv
            ansible-playbook -i inv main.yml -e component=${COMPONENT} -e env=${ENV} -e ansible_user=${SSH_USR} -e ansible_password=${SSH_PSW}
'''
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