def call() {
  pipeline {
    agent {
      label 'workstation'
    }

    options {
      ansiColor('xterm')
    }

    parameters {
      choice(name: 'env', choices: ['dev', 'prod', 'stage'], description: 'Select The Environment')
      choice(name: 'component', choices: ['frontend', 'catalogue', 'user', 'cart', 'shipping', 'payment'], description: 'Select Component')
      string(name: 'tag', defaultValue: '', description: 'Select Image Tag')
    }

    stages {
      stage('Update KubeConfig') {
        steps {
          sh "aws eks update-kubeconfig --name ${params.env}-eks-cluster --region us-east-1"
        }
      }

      stage('Get APP Code') {
        steps {
          dir('APP') {
            git branch: 'main', url: "https://github.com/Revanthsatyam/${params.component}"
          }
          dir('CHART') {
            git branch: 'main', url: 'https://github.com/Revanthsatyam/roboshop-helm-d76'
          }
        }
      }

      stage('Deployment') {
        steps {
          sh "helm upgrade --install ${params.component} ./CHART -f APP/helm/${params.env}.yaml --set image_tag=${params.tag}"
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