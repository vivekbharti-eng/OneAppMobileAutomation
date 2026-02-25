# Jenkins CI/CD Integration Guide

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Jenkins Installation](#jenkins-installation)
- [Jenkins Configuration](#jenkins-configuration)
- [Plugin Installation](#plugin-installation)
- [Pipeline Setup](#pipeline-setup)
- [BrowserStack Integration](#browserstack-integration)
- [Email Notifications](#email-notifications)
- [Running Tests](#running-tests)
- [Troubleshooting](#troubleshooting)

---

## Overview

This guide helps you integrate the OneAppAutomation framework with Jenkins for continuous integration and continuous testing.

### Benefits of Jenkins Integration:
- ✅ Automated test execution on code commits
- ✅ Scheduled test runs (nightly, hourly, etc.)
- ✅ Parallel test execution across multiple devices
- ✅ Comprehensive test reports and trends
- ✅ Email notifications for test results
- ✅ Integration with BrowserStack/SauceLabs
- ✅ Test history and trend analysis

---

## Prerequisites

### Required Software
1. **Jenkins** (Latest LTS version)
   - Download from: https://www.jenkins.io/download/
   
2. **Java JDK 11 or higher**
   ```bash
   java -version
   ```

3. **Apache Maven 3.6+**
   ```bash
   mvn -version
   ```

4. **Git** (for source code management)
   ```bash
   git --version
   ```

5. **Android SDK** (for Android testing)
   - Set `ANDROID_HOME` environment variable

6. **Appium** (for mobile automation)
   ```bash
   npm install -g appium
   ```

---

## Jenkins Installation

### Windows Installation

1. **Download Jenkins**
   ```powershell
   # Download Jenkins WAR file
   Invoke-WebRequest -Uri "https://get.jenkins.io/war-stable/latest/jenkins.war" -OutFile "jenkins.war"
   ```

2. **Start Jenkins**
   ```powershell
   java -jar jenkins.war --httpPort=8080
   ```

3. **Access Jenkins**
   - Open browser: `http://localhost:8080`
   - Get initial admin password:
     ```powershell
     Get-Content "$env:USERPROFILE\.jenkins\secrets\initialAdminPassword"
     ```

### Linux Installation

```bash
# Ubuntu/Debian
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt-get update
sudo apt-get install jenkins

# Start Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins
```

### Docker Installation

```bash
docker run -d -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home --name jenkins jenkins/jenkins:lts
```

---

## Jenkins Configuration

### 1. Install Suggested Plugins
After initial setup, install the suggested plugins.

### 2. Configure Global Tools

**Go to:** Manage Jenkins → Global Tool Configuration

#### Java Configuration
- Name: `JDK11`
- JAVA_HOME: `/path/to/jdk11` or `C:\Program Files\Java\jdk-11`
- ☑ Install automatically (optional)

#### Maven Configuration
- Name: `Maven`
- MAVEN_HOME: `/path/to/maven` or `C:\Program Files\Apache\Maven`
- Version: `3.9.x`
- ☑ Install automatically (optional)

#### Git Configuration
- Name: `Default`
- Path to Git executable: `git` or `C:\Program Files\Git\bin\git.exe`

---

## Plugin Installation

### Required Plugins

**Go to:** Manage Jenkins → Manage Plugins → Available

Install the following plugins:

1. **Pipeline Plugins**
   - Pipeline
   - Pipeline: Stage View
   - Pipeline: GitHub
   - Pipeline: Build Step

2. **Test Reporting Plugins**
   - Cucumber Reports
   - HTML Publisher
   - JUnit
   - Allure Jenkins Plugin

3. **Notification Plugins**
   - Email Extension Plugin
   - Slack Notification Plugin (optional)

4. **Version Control**
   - Git plugin
   - GitHub plugin

5. **Utility Plugins**
   - Workspace Cleanup Plugin
   - Timestamper
   - Build Timeout

### Install via Jenkins CLI

```bash
java -jar jenkins-cli.jar -s http://localhost:8080/ install-plugin \
  pipeline-model-definition \
  git \
  cucumber-reports \
  htmlpublisher \
  junit \
  allure-jenkins-plugin \
  email-ext \
  timestamper \
  ws-cleanup
```

---

## Pipeline Setup

### Method 1: Pipeline from SCM

1. **Create New Item**
   - Go to Jenkins Dashboard
   - Click "New Item"
   - Enter job name: `OneAppAutomation-Pipeline`
   - Select "Pipeline"
   - Click "OK"

2. **Configure Pipeline**
   - **General Section:**
     - ☑ Discard old builds (keep last 10)
     - ☑ This project is parameterized
   
   - **Pipeline Section:**
     - Definition: `Pipeline script from SCM`
     - SCM: `Git`
     - Repository URL: `https://github.com/your-repo/OneAppAutomation.git`
     - Credentials: Add your Git credentials
     - Branch: `*/main` or `*/master`
     - Script Path: `Jenkinsfile`

3. **Save Configuration**

### Method 2: Inline Pipeline Script

1. **Create New Item** (same as above)

2. **Configure Pipeline**
   - Definition: `Pipeline script`
   - Copy entire content from `Jenkinsfile` into the script box

3. **Save Configuration**

---

## BrowserStack Integration

### 1. Get BrowserStack Credentials
- Login to BrowserStack
- Go to Account → Settings → Access Key
- Copy `Username` and `Access Key`

### 2. Add Credentials to Jenkins

**Go to:** Manage Jenkins → Manage Credentials → Global → Add Credentials

#### BrowserStack Username
- Kind: `Secret text`
- Secret: `<your-browserstack-username>`
- ID: `browserstack-username`
- Description: `BrowserStack Username`

#### BrowserStack Access Key
- Kind: `Secret text`
- Secret: `<your-browserstack-access-key>`
- ID: `browserstack-accesskey`
- Description: `BrowserStack Access Key`

### 3. Update Jenkinsfile (Already configured)
The Jenkinsfile already includes BrowserStack credentials:
```groovy
environment {
    BROWSERSTACK_USERNAME = credentials('browserstack-username')
    BROWSERSTACK_ACCESS_KEY = credentials('browserstack-accesskey')
}
```

---

## Email Notifications

### 1. Configure SMTP Server

**Go to:** Manage Jenkins → Configure System → Extended E-mail Notification

#### Gmail Configuration
- **SMTP Server:** `smtp.gmail.com`
- **SMTP Port:** `465`
- **Use SSL:** ☑
- **User Name:** `your-email@gmail.com`
- **Password:** App-specific password (not your Gmail password)
- **Default Content Type:** `HTML (text/html)`

#### Create Gmail App Password
1. Go to Google Account Security
2. 2-Step Verification → App passwords
3. Generate password for Jenkins
4. Use this password in Jenkins

#### Outlook/Office 365 Configuration
- **SMTP Server:** `smtp.office365.com`
- **SMTP Port:** `587`
- **Use TLS:** ☑
- **User Name:** `your-email@domain.com`
- **Password:** Your password

### 2. Test Email Configuration
Click "Test configuration by sending test e-mail"

### 3. Update Email Recipients
In Jenkins job parameters, update `EMAIL_RECIPIENTS` with your team emails:
```
team@company.com,qa@company.com,dev@company.com
```

---

## Running Tests

### Manual Build with Parameters

1. **Navigate to Job**
   - Go to Jenkins Dashboard
   - Click on `OneAppAutomation-Pipeline`

2. **Build with Parameters**
   - Click "Build with Parameters"
   - Select options:
     - **Platform:** `android` or `ios`
     - **Execution Type:** `local` or `browserstack`
     - **Device Type:** `auto`, `emulator`, or `real`
     - **Test Tags:** `@smoke`, `@regression`, etc.
     - **Custom Tags:** (optional)
     - **Parallel Execution:** ☑ for parallel tests
     - **Generate Allure Report:** ☑
     - **Send Email Notification:** ☑
     - **Email Recipients:** `team@example.com`

3. **Click "Build"**

### Scheduled Builds

Add build triggers in job configuration:

#### Nightly Smoke Tests
```groovy
triggers {
    cron('H 2 * * *')  // Run at 2 AM daily
}
```

#### Hourly Regression Tests
```groovy
triggers {
    cron('H * * * *')  // Run every hour
}
```

#### On SCM Changes
```groovy
triggers {
    pollSCM('H/15 * * * *')  // Poll every 15 minutes
}
```

### Webhook Triggers (GitHub/GitLab)

#### GitHub Webhook
1. Go to GitHub Repository → Settings → Webhooks
2. Add webhook:
   - **Payload URL:** `http://jenkins-server:8080/github-webhook/`
   - **Content type:** `application/json`
   - **Events:** Push events
3. Save webhook

#### Jenkins Configuration
- In job configuration, enable:
  - ☑ GitHub hook trigger for GITScm polling

---

## Build Pipeline Parameters

### Available Parameters

| Parameter | Options | Description |
|-----------|---------|-------------|
| **PLATFORM** | `android`, `ios` | Target platform |
| **EXECUTION_TYPE** | `local`, `browserstack` | Where to run tests |
| **DEVICE_TYPE** | `auto`, `emulator`, `real` | Device type for local |
| **TEST_TAGS** | `@smoke`, `@regression`, `@login`, `@logout` | Cucumber tags |
| **CUSTOM_TAGS** | String | Custom tag expression |
| **PARALLEL_EXECUTION** | Boolean | Enable parallel execution |
| **GENERATE_ALLURE_REPORT** | Boolean | Generate Allure reports |
| **SEND_EMAIL_NOTIFICATION** | Boolean | Send email after build |
| **EMAIL_RECIPIENTS** | String | Comma-separated emails |

### Example Tag Combinations
```
@smoke                          # Smoke tests only
@regression                     # Regression tests only
@smoke or @regression           # All smoke + regression
@smoke and @positive            # Smoke tests that are positive
@login and not @negative        # Login tests excluding negative
@logout and @profile            # Logout tests for profile
```

---

## Viewing Reports

### 1. Extent Reports
- After build completes, click "Extent Report" in build navigation
- View detailed HTML test report with:
  - Test execution summary
  - Pass/Fail statistics
  - Screenshots for failures
  - Detailed logs

### 2. Cucumber Reports
- Click "Cucumber Reports" in build navigation
- View feature-wise test results
- See step-by-step execution logs

### 3. Allure Reports
- Click "Allure Report" in build navigation
- Interactive report with:
  - Test execution trends
  - Categories and suites
  - Time-based statistics
  - Historical trends

### 4. JUnit Results
- View test results in "Test Results" link
- See test pass/fail trends over builds

### 5. Console Output
- Click "Console Output" to view execution logs
- Debug test failures and environment issues

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Jenkins Can't Find Maven
**Error:** `mvn: command not found`

**Solution:**
```bash
# Check Maven installation
mvn -version

# Add Maven to PATH
export PATH=$PATH:/path/to/maven/bin

# Or configure in Jenkins Global Tool Configuration
```

#### 2. ADB Not Found
**Error:** `adb: command not found`

**Solution:**
```bash
# Set ANDROID_HOME
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Or add to Jenkins environment variables
```

#### 3. Tests Fail - No Devices Connected
**Error:** `No devices connected`

**Solution:**
- Check device connection:
  ```bash
  adb devices
  ```
- Start Appium server before tests
- For Jenkins slave, ensure device is connected to slave machine

#### 4. BrowserStack Tests Fail
**Error:** `Invalid BrowserStack credentials`

**Solution:**
- Verify credentials are correctly set in Jenkins
- Check credential IDs match: `browserstack-username` and `browserstack-accesskey`
- Test credentials manually

#### 5. Email Not Sent
**Error:** `Failed to send email`

**Solution:**
- Verify SMTP configuration
- Check firewall rules
- Enable "Less secure app access" for Gmail (or use App Password)
- Test email from Jenkins System Configuration

#### 6. Reports Not Publishing
**Error:** `No reports found`

**Solution:**
- Check report paths in `archiveArtifacts`
- Verify tests generated reports in `target/reports/`
- Ensure files are not excluded by `.gitignore`

#### 7. Build Timeout
**Error:** `Build timed out`

**Solution:**
- Increase timeout in Jenkinsfile or job configuration
- Optimize test execution
- Run tests in parallel

---

## Advanced Configuration

### Multi-branch Pipeline

Create multi-branch pipeline for feature branch testing:

1. **New Item** → **Multibranch Pipeline**
2. Configure branch sources (Git/GitHub)
3. Add branch scanning triggers
4. Jenkinsfile auto-discovered in each branch

### Docker Agents

Run tests in Docker containers:

```groovy
pipeline {
    agent {
        docker {
            image 'maven:3.8-openjdk-11'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    // ... rest of pipeline
}
```

### Parallel Execution

Run tests on multiple platforms simultaneously:

```groovy
stage('Parallel Tests') {
    parallel {
        stage('Android') {
            steps {
                sh 'mvn test -Pandroid -Dcucumber.filter.tags="@smoke"'
            }
        }
        stage('iOS') {
            steps {
                sh 'mvn test -Pios -Dcucumber.filter.tags="@smoke"'
            }
        }
    }
}
```

### Slack Integration

Add Slack notifications:

```groovy
post {
    success {
        slackSend color: 'good', 
                  message: "Build #${env.BUILD_NUMBER} succeeded!"
    }
    failure {
        slackSend color: 'danger', 
                  message: "Build #${env.BUILD_NUMBER} failed!"
    }
}
```

---

## Best Practices

### 1. Security
- ✅ Use credentials binding for sensitive data
- ✅ Never commit passwords/API keys to source code
- ✅ Use Jenkins credential store
- ✅ Enable HTTPS for Jenkins

### 2. Performance
- ✅ Run smoke tests frequently
- ✅ Run regression tests nightly
- ✅ Use parallel execution for faster results
- ✅ Archive only necessary artifacts

### 3. Maintenance
- ✅ Regularly update Jenkins and plugins
- ✅ Clean old builds and artifacts
- ✅ Monitor Jenkins performance
- ✅ Backup Jenkins home directory

### 4. Reporting
- ✅ Keep comprehensive test reports
- ✅ Track test trends over time
- ✅ Set up failure notifications
- ✅ Review reports after each build

---

## Support and Resources

### Documentation Links
- [Jenkins Official Docs](https://www.jenkins.io/doc/)
- [Pipeline Syntax Reference](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Cucumber Plugin](https://plugins.jenkins.io/cucumber-reports/)
- [Allure Plugin](https://plugins.jenkins.io/allure-jenkins-plugin/)

### Framework Documentation
- [QUICKSTART.md](QUICKSTART.md)
- [IDE_EXECUTION_GUIDE.md](IDE_EXECUTION_GUIDE.md)
- [REPORTS_GUIDE.md](REPORTS_GUIDE.md)
- [REAL_DEVICE_GUIDE.md](REAL_DEVICE_GUIDE.md)

---

## Conclusion

You now have a complete Jenkins CI/CD setup for the OneAppAutomation framework with:
- ✅ Automated test execution
- ✅ Comprehensive reporting
- ✅ Email notifications
- ✅ BrowserStack integration
- ✅ Parallel test execution
- ✅ Scheduled builds

For additional help or customization, refer to the framework documentation or contact the automation team.
