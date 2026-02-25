pipeline {
    agent any
    
    parameters {
        choice(
            name: 'PLATFORM',
            choices: ['android', 'ios'],
            description: 'Select platform to run tests'
        )
        choice(
            name: 'EXECUTION_TYPE',
            choices: ['local', 'browserstack'],
            description: 'Select execution type'
        )
        choice(
            name: 'DEVICE_TYPE',
            choices: ['auto', 'emulator', 'real'],
            description: 'Select device type (for local execution)'
        )
        choice(
            name: 'TEST_TAGS',
            choices: ['@smoke', '@regression', '@login', '@logout', '@smoke or @regression', '@smoke and @positive'],
            description: 'Select test tags to run'
        )
        string(
            name: 'CUSTOM_TAGS',
            defaultValue: '',
            description: 'Custom Cucumber tags (leave empty to use TEST_TAGS)'
        )
        booleanParam(
            name: 'PARALLEL_EXECUTION',
            defaultValue: false,
            description: 'Run tests in parallel'
        )
        booleanParam(
            name: 'GENERATE_ALLURE_REPORT',
            defaultValue: true,
            description: 'Generate Allure report'
        )
        booleanParam(
            name: 'SEND_EMAIL_NOTIFICATION',
            defaultValue: true,
            description: 'Send email notification after test execution'
        )
        string(
            name: 'EMAIL_RECIPIENTS',
            defaultValue: 'team@example.com',
            description: 'Email recipients (comma-separated)'
        )
    }
    
    environment {
        MAVEN_HOME = tool 'Maven'
        JAVA_HOME = tool 'JDK11'
        PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
        
        // BrowserStack credentials from Jenkins credentials store
        BROWSERSTACK_USERNAME = credentials('browserstack-username')
        BROWSERSTACK_ACCESS_KEY = credentials('browserstack-accesskey')
        
        // Build information
        BUILD_TIMESTAMP = "${new Date().format('yyyy-MM-dd_HH-mm-ss')}"
        REPORT_DIR = "target/reports"
        SCREENSHOT_DIR = "target/screenshots"
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }
        
        stage('Environment Check') {
            steps {
                echo '========================================'
                echo 'Checking Environment Configuration'
                echo '========================================'
                script {
                    if (isUnix()) {
                        sh '''
                            echo "Java Version:"
                            java -version
                            echo ""
                            echo "Maven Version:"
                            mvn -version
                            echo ""
                            echo "Environment Variables:"
                            echo "Platform: ${PLATFORM}"
                            echo "Execution Type: ${EXECUTION_TYPE}"
                            echo "Test Tags: ${TEST_TAGS}"
                        '''
                        
                        // Check ADB for Android local execution
                        if (params.PLATFORM == 'android' && params.EXECUTION_TYPE == 'local') {
                            sh '''
                                echo "Checking ADB:"
                                adb version || echo "ADB not found"
                                echo ""
                                echo "Connected Devices:"
                                adb devices
                            '''
                        }
                    } else {
                        bat '''
                            echo "Java Version:"
                            java -version
                            echo.
                            echo "Maven Version:"
                            mvn -version
                            echo.
                            echo "Environment Variables:"
                            echo Platform: %PLATFORM%
                            echo Execution Type: %EXECUTION_TYPE%
                            echo Test Tags: %TEST_TAGS%
                        '''
                        
                        // Check ADB for Android local execution
                        if (params.PLATFORM == 'android' && params.EXECUTION_TYPE == 'local') {
                            bat '''
                                echo "Checking ADB:"
                                adb version || echo "ADB not found"
                                echo.
                                echo "Connected Devices:"
                                adb devices
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Clean') {
            steps {
                echo 'Cleaning previous builds...'
                script {
                    if (isUnix()) {
                        sh 'mvn clean'
                    } else {
                        bat 'mvn clean'
                    }
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                echo '========================================'
                echo "Running Tests: ${params.PLATFORM} - ${params.EXECUTION_TYPE}"
                echo '========================================'
                script {
                    // Determine Maven profile
                    def profile = params.PLATFORM
                    if (params.EXECUTION_TYPE == 'browserstack') {
                        profile = "bs-${params.PLATFORM}"
                    } else if (params.EXECUTION_TYPE == 'local' && params.DEVICE_TYPE != 'auto') {
                        profile = "local-${params.PLATFORM}-${params.DEVICE_TYPE}"
                    }
                    
                    // Determine test tags to use
                    def testTags = params.CUSTOM_TAGS ?: params.TEST_TAGS
                    
                    // Build Maven command
                    def mvnCmd = "mvn clean test -P${profile}"
                    mvnCmd += " -Dcucumber.filter.tags=\"${testTags}\""
                    
                    // Add parallel execution if enabled
                    if (params.PARALLEL_EXECUTION) {
                        mvnCmd += " -Ddataproviderthreadcount=2"
                    }
                    
                    echo "Executing: ${mvnCmd}"
                    
                    // Execute tests
                    try {
                        if (isUnix()) {
                            sh mvnCmd
                        } else {
                            bat mvnCmd
                        }
                    } catch (Exception e) {
                        echo "Tests failed with error: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Generate Allure Report') {
            when {
                expression { params.GENERATE_ALLURE_REPORT == true }
            }
            steps {
                echo '========================================'
                echo 'Generating Allure Report'
                echo '========================================'
                script {
                    try {
                        allure includeProperties: false,
                               jdk: '',
                               results: [[path: 'target/reports/allure/*/allure-results']]
                    } catch (Exception e) {
                        echo "Failed to generate Allure report: ${e.getMessage()}"
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '========================================'
            echo 'Publishing Test Reports and Artifacts'
            echo '========================================'
            
            script {
                // Archive test reports with fingerprinting
                archiveArtifacts artifacts: '**/target/reports/**/*.html', 
                                allowEmptyArchive: true,
                                fingerprint: true
                                
                archiveArtifacts artifacts: '**/target/screenshots/**/*.png', 
                                allowEmptyArchive: true,
                                fingerprint: true
                                
                archiveArtifacts artifacts: '**/target/cucumber-reports/**/*', 
                                allowEmptyArchive: true,
                                fingerprint: true
                
                // Archive logs
                archiveArtifacts artifacts: '**/target/logs/**/*.log', 
                                allowEmptyArchive: true
                
                // Publish Cucumber reports with trends
                try {
                    cucumber fileIncludePattern: '**/target/cucumber-reports/cucumber.json',
                             sortingMethod: 'ALPHABETICAL',
                             trendsLimit: 100
                } catch (Exception e) {
                    echo "Failed to publish Cucumber report: ${e.getMessage()}"
                }
                
                // Publish Extent HTML reports
                try {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/reports/extent',
                        reportFiles: '**/ExtentReport*.html',
                        reportName: 'Extent Report',
                        reportTitles: 'Mobile Automation Test Report'
                    ])
                } catch (Exception e) {
                    echo "Failed to publish Extent report: ${e.getMessage()}"
                }
                
                // Publish JUnit test results
                try {
                    junit testResults: '**/target/surefire-reports/*.xml',
                          allowEmptyResults: true,
                          healthScaleFactor: 1.0
                } catch (Exception e) {
                    echo "Failed to publish JUnit results: ${e.getMessage()}"
                }
            }
        }
        
        success {
            echo '========================================'
            echo '✓ Tests Completed Successfully!'
            echo '========================================'
            script {
                if (params.SEND_EMAIL_NOTIFICATION) {
                    def emailBody = """
                        <html>
                        <body style="font-family: Arial, sans-serif;">
                            <h2 style="color: #28a745;">✓ Test Execution Successful</h2>
                            <table style="border-collapse: collapse; width: 100%;">
                                <tr style="background-color: #f2f2f2;">
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Job Name:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${env.JOB_NAME}</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Build Number:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">#${env.BUILD_NUMBER}</td>
                                </tr>
                                <tr style="background-color: #f2f2f2;">
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Platform:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${params.PLATFORM}</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Execution Type:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${params.EXECUTION_TYPE}</td>
                                </tr>
                                <tr style="background-color: #f2f2f2;">
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Test Tags:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${params.CUSTOM_TAGS ?: params.TEST_TAGS}</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Duration:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${currentBuild.durationString}</td>
                                </tr>
                            </table>
                            <br/>
                            <p><a href="${env.BUILD_URL}" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">View Console Output</a></p>
                            <p><a href="${env.BUILD_URL}Extent_20Report/" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">View Test Report</a></p>
                        </body>
                        </html>
                    """
                    
                    emailext(
                        subject: "✓ SUCCESS: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
                        body: emailBody,
                        to: params.EMAIL_RECIPIENTS,
                        mimeType: 'text/html',
                        attachLog: false
                    )
                }
            }
        }
        
        failure {
            echo '========================================'
            echo '✗ Tests Failed!'
            echo '========================================'
            script {
                if (params.SEND_EMAIL_NOTIFICATION) {
                    def emailBody = """
                        <html>
                        <body style="font-family: Arial, sans-serif;">
                            <h2 style="color: #dc3545;">✗ Test Execution Failed</h2>
                            <table style="border-collapse: collapse; width: 100%;">
                                <tr style="background-color: #f2f2f2;">
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Job Name:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${env.JOB_NAME}</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Build Number:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">#${env.BUILD_NUMBER}</td>
                                </tr>
                                <tr style="background-color: #f2f2f2;">
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Platform:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${params.PLATFORM}</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Execution Type:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${params.EXECUTION_TYPE}</td>
                                </tr>
                                <tr style="background-color: #f2f2f2;">
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Test Tags:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${params.CUSTOM_TAGS ?: params.TEST_TAGS}</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd;"><strong>Duration:</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">${currentBuild.durationString}</td>
                                </tr>
                            </table>
                            <br/>
                            <p style="color: #dc3545;"><strong>Please check console output and screenshots for failure details</strong></p>
                            <p><a href="${env.BUILD_URL}console" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">View Console Output</a></p>
                            <p><a href="${env.BUILD_URL}Extent_20Report/" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">View Test Report</a></p>
                        </body>
                        </html>
                    """
                    
                    emailext(
                        subject: "✗ FAILURE: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
                        body: emailBody,
                        to: params.EMAIL_RECIPIENTS,
                        mimeType: 'text/html',
                        attachLog: true
                    )
                }
            }
        }
        
        unstable {
            echo '========================================'
            echo '⚠ Tests Completed with Some Failures'
            echo '========================================'
        }
        
        cleanup {
            echo '========================================'
            echo 'Build Cleanup Complete'
            echo '========================================'
            // Optional: Uncomment to clean workspace after build
            // cleanWs()
        }
    }
}
