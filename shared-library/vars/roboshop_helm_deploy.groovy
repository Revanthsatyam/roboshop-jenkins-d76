def call(Map args = [:]) {
  // You can define environment and parameters here if necessary
  def env = args.get('env', null)
  def component = args.get('component', null)
  def tag = args.get('tag', null)

  if (env == null || component == null || tag == null) {
    error "Missing required arguments: env, component, or tag."
  }

  // Update KubeConfig
  echo "Updating KubeConfig for environment: ${env}"
  sh "aws eks update-kubeconfig --name ${env}-eks-cluster --region us-east-1"

  // Get APP Code
  echo "Cloning APP and CHART repositories"
  dir('APP') {
    if (env == 'prod') {
      git branch: 'main', url: "https://github.com/Revanthsatyam/${component}"
    }
    if (env == 'stage') {
      git branch: 'stage', url: "https://github.com/Revanthsatyam/${component}"
    }
  }
  dir('CHART') {
    git branch: 'main', url: 'https://github.com/Revanthsatyam/roboshop-helm-d76'
  }

  // Deployment
  echo "Deploying ${component} with image tag ${tag}"
  sh "helm upgrade --install ${component} ./CHART -f APP/helm/${env}.yaml --set image_tag=${tag}"

  // Clean up workspace after the run
  cleanWs()
}
