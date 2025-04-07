pipeline {
  agent { label 'workstation' }

  stages {

    stage('Compile Code') {
      steps {
        echo "Compile Stage"
      }
    }

    stage('Test') {
      steps {
        echo "Test Stage"
      }
    }

    stage('CodeCoverage') {
      steps {
        echo "CodeCoverage Stage"
      }
    }

    stage('CodeQuality') {
      steps {
        echo "CodeQuality Stage"
      }
    }

    stage('Release') {
      steps {
        echo "Release Stage"
      }
    }

  }
}