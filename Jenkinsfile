// required plugins:
// - OAuth Credentials plugin, org.jenkins-ci.plugins:oauth-credentials:0.4
// - Google Container Registry Auth0, google-container-registry-auth:0.3

def projectVersion
def imageForGradleStages = 'openjdk:11-jdk'
def dockerArgsForGradleStages = '-e GRADLE_USER_HOME=$WORKSPACE/gradle-home -v $HOME/.gradle/caches:/gradle-cache:ro -e GRADLE_RO_DEP_CACHE=/gradle-cache'

def withDockerNetwork(Closure inner) {
  try {
    networkId = UUID.randomUUID().toString()
    sh "docker network create ${networkId}"
    inner.call(networkId)
  } finally {
    sh "docker network rm ${networkId}"
  }
}

pipeline {
    agent none

    options {
        buildDiscarder(logRotator(numToKeepStr: '50', artifactNumToKeepStr: '5'))
    }

    environment {
        // In case the build server exports a custom JAVA_HOME, we fix the JAVA_HOME
        // to the one used by the docker image.
        JAVA_HOME='/usr/local/openjdk-11'
        GRADLE_OPTS='-Dhttp.proxyHost=cache.sernet.private -Dhttp.proxyPort=3128 -Dhttps.proxyHost=cache.sernet.private -Dhttps.proxyPort=3128'
    }

    stages {
        stage('Setup') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                sh 'env'
                buildDescription "${env.GIT_BRANCH} ${env.GIT_COMMIT[0..8]}"
                script {
                    projectVersion = sh(returnStdout: true, script: '''./gradlew properties -q | awk '/^version:/ {print $2}' ''').trim()
                }
            }
        }
        stage('Build') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                sh './gradlew --no-daemon classes'
            }
        }
        stage('Gradle test') {
                    agent any
                    environment {
                        JAVA_HOME='/opt/java/openjdk'
                    }
                    steps {
                        script {
                            def veoHistoryTests = docker.build("veo_history_tests", "-f Test-Dockerfile .")
                            withDockerNetwork{ n ->
                                docker.image('postgres').withRun("--network ${n} --name veo-history -e POSTGRES_PASSWORD=postgres") { db ->
                                    sh 'until pg_isready; do sleep 1; done'
                                    veoHistoryTests.inside("--network ${n} --name veo-history-${n} --entrypoint=''") {
                                        sh "gradle test --no-daemon -PdataSourceUrl=jdbc:postgresql://veo-history:5432/postgres -PdataSourceUsername=postgres -PdataSourcePassword=postgres"
                                        junit testResults: 'build/test-results/test/**/*.xml'
                                        jacoco classPattern: 'build/classes/*/main', sourcePattern: 'src/main'
                                    }
                                }
                            }
                        }
                    }
                }
        stage('Artifacts') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                sh './gradlew --no-daemon build -x test'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
        stage('Dockerimage') {
            agent {
                label 'docker-image-builder'
            }
            steps {
                script {
                    def dockerImage = docker.build("eu.gcr.io/veo-projekt/veo-history:git-${env.GIT_COMMIT}", "--label org.opencontainers.image.version='$projectVersion' --label org.opencontainers.image.revision='$env.GIT_COMMIT' .")
                    // Finally, we'll push the image with several tags:
                    // Pushing multiple tags is cheap, as all the layers are reused.
                    withDockerRegistry(credentialsId: 'gcr:verinice-projekt@gcr', url: 'https://eu.gcr.io') {
                        dockerImage.push("git-${env.GIT_COMMIT}")
                        if (env.GIT_BRANCH == 'master') {
                            dockerImage.push("latest")
                            dockerImage.push("master-build-${env.BUILD_NUMBER}")
                        } else if (env.GIT_BRANCH == 'develop') {
                            dockerImage.push("develop")
                            dockerImage.push("develop-build-${env.BUILD_NUMBER}")
                        }
                    }
                }
            }
        }
        stage('Trigger Deployment') {
            agent any
            when {
                branch 'master'
            }
            steps {
                build job: 'verinice-veo-deployment/master'
            }
        }
    }
    post {
        always {
           node('') {
                recordIssues(enabledForFailure: true, tools: [java()])
                recordIssues(
                  enabledForFailure: true,
                  tools: [
                    taskScanner(
                      highTags: 'FIXME',
                      ignoreCase: true,
                      normalTags: 'TODO',
                      excludePattern: 'Jenkinsfile, gradle-home/**, .gradle/**, buildSrc/.gradle/**, */build/**, **/*.pdf, **/*.png, **/*.jpg, **/*.vna'
                    )
                  ]
                )
            }
        }
    }
}
