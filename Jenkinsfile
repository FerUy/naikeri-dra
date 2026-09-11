pipeline {
    agent any

    tools {
        jdk 'jdk-11'
        maven 'maven-3.9.12'
    }

    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '300', numToKeepStr: '10'))
    }

    parameters {
        string(name: 'NAIKERI_DRA_MAJOR_VERSION', defaultValue: '2.2.0', description: 'The major version for Naikeri DRA')
    }

    stages {
        stage('Set Version') {
            steps {
                echo "Setting version to ${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}"
                sh "mvn versions:set -DnewVersion=${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER} -DgenerateBackupPoms=false"
            }
        }

        stage('Build') {
            steps {
                script {
                    currentBuild.displayName = "#${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}"
                    currentBuild.description = "Naikeri DRA"
                }
                sh "mvn clean install -Passembly"
            }
        }

        stage('Release') {
            steps {
                withAnt(installation: 'Ant_1.10.15') {
                    dir('release') {
                        sh "ant -f build.xml -Dskip.maven.build=true -Dnaikeri.dra.release.version=${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}"
                    }
                }
            }
        }

        stage('Save Artifacts') {
            steps {
                archiveArtifacts artifacts: "release/Naikeri-DRA-${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}.zip", followSymlinks: false, onlyIfSuccessful: true
            }
        }
    }

    post {
        success { echo "Successfully built Naikeri DRA ${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}" }
        failure { echo "Building Naikeri DRA failed." }
        always  { sh 'rm -rf release/target release/Naikeri-DRA-*.zip' }
    }
}
