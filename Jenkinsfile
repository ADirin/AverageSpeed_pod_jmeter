pipeline {
    agent any
    tools {
        maven 'Maven3'
    }

    environment {
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        JAVA_HOME = 'C:\\Program Files\\Java\\jdk-21'  // Adjust to your actual JDK pat
        SONARQUBE_SERVER = 'SonarQubeServer'  // The name of the SonarQube server configured in Jenkins
        SONAR_TOKEN = 'squ_36ddcb7129eea21de9e6ef95dd0081b14220ecce' // Store the token securely
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'

        JMETER_HOME = "/usr/bin/jmeter"   // adjust if needed
        RESULT_DIR  = "results"


        DOCKERHUB_REPO = 'amirdirin/lectdemo3010_pod_jmeter_2026'
        DOCKER_IMAGE_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/ADirin/AverageSpeed_pod_jmeter.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    bat """
                                ${tool 'SonarScanner'}\\bin\\sonar-scanner ^
                                -Dsonar.projectKey=avg_consol ^
                                -Dsonar.sources=src ^
                                -Dsonar.projectName=avg_consol ^
                                -Dsonar.host.url=http://localhost:9000 ^
                                -Dsonar.login=${env.SONAR_TOKEN} ^
                                -Dsonar.java.binaries=target/classes
                            """
                }
            }
        }



        stage('Run JMeter Performance Test') {
            steps {
                sh """
                mkdir -p ${RESULT_DIR}
                jmeter \
                  -n \
                  -t jmeter/avg_speed_test.jmx \
                  -l ${RESULT_DIR}/results.jtl \
                  -e \
                  -o ${RESULT_DIR}/report
                """
            }
        }
    }

    post {
        always {

            // ✅ Publish JMeter raw results
            perfReport(
                    sourceDataFiles: "${RESULT_DIR}/results.jtl",
                    failBuildIfNoResultFile: true
            )

            // ✅ Publish HTML report
            publishHTML(
                    target: [
                            reportDir: "${RESULT_DIR}/report",
                            reportFiles: "index.html",
                            reportName: "JMeter Performance Report",
                            keepAll: true,
                            alwaysLinkToLastBuild: true
                    ]
            )
        }
    }




    stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                    // Or specify Dockerfile path explicitly if needed
                    // docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}", "-f ./Dockerfile .")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                        docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                    }
                }
            }
        }
    }
}
