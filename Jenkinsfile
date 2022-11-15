pipeline {
    agent any

    tools {
        jdk 'JDK 11'
		maven 'Maven_3.8.5'
	}
	
    options {
    	buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '300', numToKeepStr: '10'))
    }

    parameters {
        string(name: 'NAIKERI_DRA_MAJOR_VERSION', defaultValue: '2.1.0', description: 'The major version for Naikeri DRA')
        string(name: 'SGW_VERSION', defaultValue: '2.1.1', description: 'The version to use for Naikeri Signaling Gateway')
        string(name: 'SGW_BUILD', defaultValue: '12', description: 'The build number for Naikeri Signaling Gateway')
        string(name: 'SCTP_VERSION', defaultValue: '2.1.0', description: 'The version number for Naikeri SCTP')
        string(name: 'SCTP_BUILD', defaultValue: '17', description: 'The build number for Naikeri SCTP')
        string(name: 'JDIAMETER_VERSION', defaultValue: '2.0.0', description: 'The major version for the Naikeri jDIAMETER')
        string(name: 'JDIAMETER_BUILD', defaultValue: '241', description: 'The build number for the Naikeri jDIAMETER')
        string(name: 'JSS7_VERSION', defaultValue: '8.5.0', description: 'The version to use for Naikeri jSS7')
        string(name: 'JSS7_BUILD', defaultValue: '334', description: 'The build number for Naikeri jSS7')
    }

    environment {
        NAIKERI_SG_VERSION = "${params.SGW_VERSION}-${params.SGW_BUILD}"
        NAIKERI_SCTP_VERSION = "${params.SCTP_VERSION}-${params.SCTP_BUILD}"
        NAIKERI_JDIAMETER_VERSION = "${params.JDIAMETER_VERSION}-${params.JDIAMETER_BUILD}"
        NAIKERI_JSS7_VERSION = "${params.JSS7_VERSION}-${params.JSS7_BUILD}"
    }

	stages {
        stage("Set Version") {
          steps {
            sh "mvn versions:set -DnewVersion=${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}"
            echo "Setting version to ${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER} completed"
          }
        }

		stage("Build") {
			steps {
				echo "Building application..."
				script {
           			currentBuild.displayName = "#${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}"
           			currentBuild.description = "Naikeri DRA"
       	        }
		  	    sh "mvn -Dnaikeri-signaling-gateway-core.version=${NAIKERI_SG_VERSION} -Ddiameter.release.version=${NAIKERI_JDIAMETER_VERSION} -Dsctp.version=${NAIKERI_SCTP_VERSION} -Djss7.version=${NAIKERI_JSS7_VERSION} clean install"

			    echo "Maven build completed."
			}
		}

        stage("Release") {
		    steps {
				withAnt(installation: 'Ant_1.10.12') {
					echo "Building a released version"
                    dir('release') {
                        sh """
                            ant -f build.xml \
                            -Dnaikeri.dra.release.version=${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER} \
                        """
 				   }
				}
			}
		}

        stage('Save Artifacts') {
            steps {
                echo "Archiving NAIKERI_DRA-${params.NAIKERI_DRA_MAJOR_VERSION}-${BUILD_NUMBER}"
                archiveArtifacts artifacts: "release/Naikeri-DRA-*.zip", followSymlinks: false, onlyIfSuccessful: true
            }
        }
    }

	post {
		success {
			echo "Successfully build"
			// slackSend channel: 'naikeri-dra', message: 'Build was successful'
		}
		failure {
			echo "Building Naikeri DRA failed."
		}
		always {
		    sh "rm -rf release/Naikeri-DRA-*.zip"
            sh 'rm -rf release/checkout'
            sh 'rm -rf release/target'
        }
	}
}

