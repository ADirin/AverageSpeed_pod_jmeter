pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        JAVA_HOME = "C:\\Program Files\\Java\\jdk-21"
        JMETER_HOME = "C:\\apache-jmeter-5.6.3\\bin"
        RESULT_DIR = "results"
        SONAR_TOKEN = 'squ_36ddcb7129eea21de9e6ef95dd0081b14220ecce'
        DOCKERHUB_REPO = "amirdirin/lectdemo3010_pod_jmeter_2026"
        DOCKER_IMAGE_TAG = "latest"
        DOCKERHUB_CREDENTIALS_ID = "Docker_Hub"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master',
                        url: 'https://github.com/ADirin/AverageSpeed_pod_jmeter.git'
            }
        }

        stage('Build Maven') {
            steps {
                bat 'mvn clean install'
            }
        }
/*
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    bat """
                     ${tool 'SonarScanner'}\\\\bin\\\\sonar-scanner ^
                      -Dsonar.projectKey=AverageSpeed ^
                      -Dsonar.sources=src ^
                      -Dsonar.projectName=avg_consol ^
                      -Dsonar.host.url=http://localhost:9000 ^
                      -Dsonar.login=%SONAR_TOKEN% ^
                      -Dsonar.java.binaries=target/classes
                    """
                }
            }
        }
*/

        stage('Run JMeter Performance Test') {
            steps {
                bat """
        if not exist results mkdir results
        "C:\\apache-jmeter-5.6.3\\bin\\jmeter.bat" -n -t jmeter\\avg_speed_test.jmx -l results\\results.jtl -e -o results\\report
        """
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry(
                            'https://index.docker.io/v1/',
                            DOCKERHUB_CREDENTIALS_ID
                    ) {
                        docker.image(
                                "${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}"
                        ).push()
                    }
                }
            }
        }
    }

    post {
        always {

            perfReport(
                    sourceDataFiles: "${RESULT_DIR}/results.jtl",
                    failBuildIfNoResultFile: true
            )

            publishHTML(
                    target: [
                            reportDir: "${RESULT_DIR}/report",
                            reportFiles: 'index.html',
                            reportName: 'JMeter Performance Report',
                            keepAll: true,
                            alwaysLinkToLastBuild: true
                    ]
            )
        }
    }
}




