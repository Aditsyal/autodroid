# Build and Deployment Guide

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Build Configuration](#build-configuration)
- [Building the Application](#building-the-application)
- [Signing Configuration](#signing-configuration)
- [Release Process](#release-process)
- [Publishing to Play Store](#publishing-to-play-store)
- [Continuous Integration](#continuous-integration)
- [Version Management](#version-management)
- [Distribution Channels](#distribution-channels)
- [Rollback Procedures](#rollback-procedures)
- [Post-Release Tasks](#post-release-tasks)

## Overview

AutoDroid uses Gradle as its build system with Android Gradle Plugin (AGP) for Android-specific build tasks. The build process includes compilation, testing, linting, and packaging for both debug and release variants.

### Build System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Build System                            │
│                                                           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │ Gradle       │    │ Android      │    │ Signing     │ │
│  │ Wrapper      │    │ Gradle       │    │ Config      │ │
│  │             │    │ Plugin       │    │             │ │
│  └─────────────┘    └─────────────┘    └─────────────┘ │
│                         ↓                    │
│  ┌─────────────────────────────────────────────────┐ │
│  │      Build Variants (Debug, Release)           │ │
│  └─────────────────────────────────────────────────┘ │
│                         ↓                    │
│  ┌─────────────────────────────────────────────────┐ │
│  │      Output (APK, AAB, Mapping Files)         │ │
│  └─────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Prerequisites

### Development Environment

- **Android Studio**: Koala (2024.1) or newer
- **JDK**: 17 or higher (included with Android Studio)
- **Android SDK**: API 35 (Android 15) with build tools
- **Gradle**: 8.13.1 (managed by wrapper)

### System Requirements

- **RAM**: 8GB minimum, 16GB recommended
- **Disk Space**: 10GB for Android SDK and project
- **OS**: Windows 10+, macOS 10.14+, Linux Ubuntu 18.04+

### Environment Setup

```bash
# Verify Java version
java -version
# Should show Java 17 or higher

# Verify Gradle wrapper
./gradlew --version
# Should show Gradle 8.13.1

# Accept Android SDK licenses
yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses

# Verify Android SDK components
$ANDROID_HOME/tools/bin/sdkmanager --list_installed
```

## Build Configuration

### Project Structure

```
app/
├── build.gradle.kts          # App-level build configuration
├── proguard-rules.pro        # ProGuard/R8 optimization rules
├── libs.versions.toml        # Dependency versions
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   └── java/...          # Source code
    └── androidTest/          # Instrumentation tests

gradle/
├── wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties

build.gradle.kts              # Project-level build configuration
gradle.properties            # Gradle properties
settings.gradle.kts          # Project settings
```

### Gradle Configuration Files

#### Project-level build.gradle.kts

```kotlin
plugins {
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("org.owasp.dependencycheck") version "10.0.4" apply false
}
```

#### App-level build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("org.owasp.dependencycheck")
}

android {
    namespace = "com.aditsyal.autodroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aditsyal.autodroid"
        minSdk = 24
        targetSdk = 35
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        release {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = getVersions().getString("composeCompiler")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core dependencies defined in libs.versions.toml
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // ... other dependencies
}

fun getVersionCode(): Int {
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD").start()
        val count = process.inputStream.bufferedReader().readText().trim().toInt()
        count + 1000 // Add offset to avoid conflicts
    } catch (e: Exception) {
        1
    }
}

fun getVersionName(): String {
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start()
        process.inputStream.bufferedReader().readText().trim().removePrefix("v")
    } catch (e: Exception) {
        "1.0.0"
    }
}
```

### Dependency Management

#### libs.versions.toml

```toml
[versions]
agp = "8.13.1"
kotlin = "2.1.0"
core-ktx = "1.13.1"
compose-bom = "2024.04.01"
hilt = "2.57.2"
room = "2.8.4"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### Gradle Properties

```properties
# Gradle properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.daemon=true

# Android specific
android.useAndroidX=true
android.enableJetifier=false
android.enableR8.fullMode=true

# Kotlin
kotlin.code.style=official
kotlin.incremental=true

# Dependency analysis
systemProp.dependency.analysis=true
```

## Building the Application

### Debug Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run on device
./gradlew runDebug
```

### Release Build

```bash
# Build release APK
./gradlew assembleRelease

# Build release AAB (Android App Bundle)
./gradlew bundleRelease
```

### Custom Build Variants

```bash
# Build specific variant
./gradlew assembleFlavorDebug
./gradlew assembleFlavorRelease

# Clean and build
./gradlew clean assembleDebug

# Build all variants
./gradlew assemble
```

### Build Analysis

```bash
# Analyze build performance
./gradlew build --profile

# View build scan
./gradlew build --scan

# Show build dependencies
./gradlew dependencies

# Show build tasks
./gradlew tasks
```

### Incremental Builds

```bash
# Enable incremental builds
echo "kotlin.incremental=true" >> gradle.properties
echo "org.gradle.caching=true" >> gradle.properties

# Use build cache
./gradlew --build-cache assembleDebug
```

## Signing Configuration

### Debug Signing

```kotlin
android {
    signingConfigs {
        create("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
}
```

### Release Signing

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

### Environment Variables

```bash
# Set signing environment variables
export STORE_PASSWORD="your_store_password"
export KEY_ALIAS="your_key_alias"
export KEY_PASSWORD="your_key_password"

# Or use .env file
echo "STORE_PASSWORD=your_store_password" > .env
echo "KEY_ALIAS=your_key_alias" >> .env
echo "KEY_PASSWORD=your_key_password" >> .env
```

### Key Generation

```bash
# Generate release keystore
keytool -genkey -v -keystore release.keystore \
  -alias release -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Your Name, OU=Your Unit, O=Your Organization, L=Your City, ST=Your State, C=US"
```

### Secure Signing

```kotlin
// Secure signing with encrypted properties
android {
    signingConfigs {
        create("release") {
            val keystoreProperties = loadKeystoreProperties()
            storeFile = file(keystoreProperties["storeFile"]!!)
            storePassword = keystoreProperties["storePassword"]!!
            keyAlias = keystoreProperties["keyAlias"]!!
            keyPassword = keystoreProperties["keyPassword"]!!
        }
    }
}

fun loadKeystoreProperties(): Map<String, String> {
    val properties = Properties()
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { properties.load(it) }
    }
    return properties.map { it.key.toString() to it.value.toString() }.toMap()
}
```

## Release Process

### Pre-Release Checklist

- [ ] **Code Quality**
  - [ ] All tests passing (`./gradlew test`)
  - [ ] Lint clean (`./gradlew lint`)
  - [ ] Security scan passed (`./gradlew dependencyCheckAnalyze`)
  - [ ] Code coverage > 70%

- [ ] **Versioning**
  - [ ] Version code incremented
  - [ ] Version name updated
  - [ ] Changelog updated
  - [ ] Release notes prepared

- [ ] **Assets**
  - [ ] Screenshots updated
  - [ ] App icons optimized
  - [ ] Feature graphic ready
  - [ ] Privacy policy updated

- [ ] **Testing**
  - [ ] Manual testing completed
  - [ ] Beta testing feedback addressed
  - [ ] Crash reports reviewed
  - [ ] Performance benchmarks met

- [ ] **Legal**
  - [ ] Privacy policy reviewed
  - [ ] Terms of service updated
  - [ ] Export compliance checked
  - [ ] Third-party licenses documented

### Release Build Process

```bash
# 1. Update version
echo "Current version: $(./gradlew -q printVersionName)"
# Update version in build.gradle.kts or use git tags

# 2. Clean build
./gradlew clean

# 3. Run full test suite
./gradlew test lintDebug dependencyCheckAnalyze

# 4. Build release APK and AAB
./gradlew assembleRelease bundleRelease

# 5. Verify build artifacts
ls -la app/build/outputs/apk/release/
ls -la app/build/outputs/bundle/release/

# 6. Generate release notes
git log --oneline --since="last_release" > release_notes.txt
```

### Automated Release Script

```bash
#!/bin/bash
# release.sh

set -e  # Exit on any error

echo "Starting release process..."

# Validate environment
if [ -z "$STORE_PASSWORD" ]; then
    echo "Error: STORE_PASSWORD not set"
    exit 1
fi

# Run tests
echo "Running tests..."
./gradlew testDebugUnitTest

# Build release
echo "Building release..."
./gradlew clean assembleRelease bundleRelease

# Verify signing
echo "Verifying signing..."
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk

# Create release archive
echo "Creating release archive..."
RELEASE_DIR="release-$(date +%Y%m%d-%H%M%S)"
mkdir "$RELEASE_DIR"
cp app/build/outputs/apk/release/app-release.apk "$RELEASE_DIR/"
cp app/build/outputs/bundle/release/app-release.aab "$RELEASE_DIR/"
cp app/build/outputs/mapping/release/mapping.txt "$RELEASE_DIR/"

# Create checksums
cd "$RELEASE_DIR"
sha256sum * > checksums.sha256
cd ..

echo "Release ready in $RELEASE_DIR/"
```

## Publishing to Play Store

### Google Play Console Setup

1. **Create App**
   - Go to Google Play Console
   - Create new app
   - Fill app details (title, description, screenshots)

2. **Upload Artifacts**
   - Upload AAB (preferred) or APK
   - Upload mapping file for crash reports
   - Fill release notes

3. **App Content**
   - Privacy policy URL
   - Content rating
   - Target audience

4. **Store Listing**
   - App title and description
   - Feature graphic (1024x500)
   - Screenshots (2-8 per type)
   - Icon (512x512)

### Play Store Publishing

```bash
# Using fastlane (recommended)
# Install fastlane: gem install fastlane

# Create fastlane directory
mkdir fastlane

# Create Fastfile
cat > fastlane/Fastfile << 'EOF'
lane :deploy_internal do
  upload_to_play_store(
    track: 'internal',
    aab: 'app/build/outputs/bundle/release/app-release.aab',
    mapping: 'app/build/outputs/mapping/release/mapping.txt',
    release_status: 'draft'
  )
end

lane :deploy_beta do
  upload_to_play_store(
    track: 'beta',
    aab: 'app/build/outputs/bundle/release/app-release.aab',
    mapping: 'app/build/outputs/mapping/release/mapping.txt'
  )
end

lane :deploy_production do
  upload_to_play_store(
    track: 'production',
    aab: 'app/build/outputs/bundle/release/app-release.aab',
    mapping: 'app/build/outputs/mapping/release/mapping.txt'
  )
end
EOF

# Deploy to internal testing
fastlane deploy_internal

# Deploy to beta
fastlane deploy_beta

# Deploy to production
fastlane deploy_production
```

### Manual Publishing Steps

1. **Upload Bundle/APK**

   ```bash
   # Upload via web interface or API
   # Include mapping file for crash symbolication
   ```

2. **Set Release Tracks**
   - **Internal**: For internal testing (up to 100 testers)
   - **Alpha**: For early adopters (open testing)
   - **Beta**: For beta testing (open testing)
   - **Production**: For public release

3. **Staged Rollout**
   - Start with 5-10% of users
   - Monitor crash reports and ratings
   - Gradually increase rollout percentage
   - Full rollout when stable

4. **Release Management**
   - Set release notes
   - Choose countries for release
   - Set pricing (if paid)
   - Enable in-app purchases if needed

## Continuous Integration

### GitHub Actions Setup

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew assembleDebug --no-daemon

      - name: Run tests
        run: ./gradlew testDebugUnitTest --no-daemon

      - name: Run lint
        run: ./gradlew lintDebug --no-daemon

      - name: Run dependency check
        run: ./gradlew dependencyCheckAnalyze --no-daemon

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: app/build/test-results/

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: coverage-report
          path: app/build/reports/coverage/
```

### Jenkins Pipeline

```groovy
// Jenkinsfile
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/yourusername/autodroid.git'
            }
        }

        stage('Setup') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew testDebugUnitTest'
            }
            post {
                always {
                    junit 'app/build/test-results/**/*.xml'
                    publishCoverage adapters: [jacocoAdapter('app/build/reports/coverage/')]
                }
            }
        }

        stage('Lint') {
            steps {
                sh './gradlew lintDebug'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew assembleRelease'
            }
        }

        stage('Security Scan') {
            steps {
                sh './gradlew dependencyCheckAnalyze'
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'app/build/outputs/**/*.apk', fingerprint: true
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
```

### Build Optimization

```yaml
# GitHub Actions with build optimization
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 1 # Shallow clone for faster checkout

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}

      - name: Build and test
        run: ./gradlew build --parallel --build-cache --configuration-cache
```

## Version Management

### Semantic Versioning

AutoDroid follows [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes (1.0.0, 2.0.0)
- **MINOR**: New features (1.1.0, 1.2.0)
- **PATCH**: Bug fixes (1.0.1, 1.0.2)

### Git Tags

```bash
# Create version tag
git tag -a v1.2.3 -m "Release version 1.2.3"

# Push tags
git push origin v1.2.3

# List tags
git tag -l "v*"
```

### Version Calculation

```kotlin
// Automatic version calculation from git
fun getVersionName(): String {
    return try {
        // Get latest tag
        val tagProcess = Runtime.getRuntime().exec("git describe --tags --abbrev=0")
        val tag = tagProcess.inputStream.bufferedReader().readText().trim()

        // Remove 'v' prefix
        tag.removePrefix("v")
    } catch (e: Exception) {
        // Fallback version
        "1.0.0-SNAPSHOT"
    }
}

fun getVersionCode(): Int {
    return try {
        // Count commits
        val countProcess = Runtime.getRuntime().exec("git rev-list --count HEAD")
        val count = countProcess.inputStream.bufferedReader().readText().trim().toInt()

        // Add build number offset
        count + 1000
    } catch (e: Exception) {
        1
    }
}
```

### Pre-release Versions

```kotlin
fun getVersionName(): String {
    return when {
        isSnapshot() -> "${getBaseVersion()}-SNAPSHOT"
        isBeta() -> "${getBaseVersion()}-beta"
        isRc() -> "${getBaseVersion()}-rc"
        else -> getBaseVersion()
    }
}

fun isSnapshot(): Boolean {
    return System.getenv("BUILD_TYPE") == "SNAPSHOT"
}
```

## Distribution Channels

### Google Play Store

**Primary Distribution Channel**

```bash
# Upload to Play Store
./gradlew publishReleaseBundleToPlay
```

**Play Store Features:**

- Automatic updates
- In-app purchases
- Crash reporting
- User reviews and ratings

### Alternative Distribution

#### F-Droid

```bash
# Build for F-Droid
./gradlew assembleRelease

# Sign with F-Droid key
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore fdroid.keystore \
  app/build/outputs/apk/release/app-release.apk fdroid

# Submit to F-Droid repository
```

#### Direct APK Distribution

```bash
# Build universal APK
./gradlew assembleUniversalRelease

# Host on website or distribute directly
# Include instructions for sideloading
```

### Beta Testing

#### Google Play Beta

```bash
# Upload to beta track
fastlane beta
```

## Rollback Procedures

### Play Store Rollback

1. **Stop Release**

   ```bash
   # Stop current release
   fastlane stop_release
   ```

2. **Upload Previous Version**

   ```bash
   # Upload previous AAB/APK
   fastlane deploy_production version:previous_version
   ```

3. **Gradual Rollback**
   - Start with small percentage
   - Monitor crash reports
   - Increase percentage if stable

### Emergency Rollback

```bash
#!/bin/bash
# emergency_rollback.sh

echo "Starting emergency rollback..."

# Get previous version
PREVIOUS_VERSION=$(git describe --tags --abbrev=0 HEAD~1)
echo "Rolling back to $PREVIOUS_VERSION"

# Checkout previous version
git checkout $PREVIOUS_VERSION

# Build and deploy
./gradlew clean assembleRelease bundleRelease
fastlane deploy_production

echo "Rollback complete"
```

### Database Migration Rollback

```kotlin
// Handle database rollback
class DatabaseRollbackManager @Inject constructor(
    private val database: AutomationDatabase
) {

    suspend fun rollbackToVersion(targetVersion: Int) {
        when (targetVersion) {
            4 -> rollbackToV4()
            3 -> rollbackToV3()
            else -> throw IllegalArgumentException("Cannot rollback to version $targetVersion")
        }
    }

    private suspend fun rollbackToV4() {
        // Custom rollback logic
        database.macroDao().deleteAllMacros()
        // Restore from backup if available
    }
}
```

## Post-Release Tasks

### Release Announcement

```markdown
# AutoDroid v1.2.0 Released! 🚀

## What's New

- ✨ New trigger types: Calendar events, NFC tags
- 🔧 Improved performance and battery usage
- 🐛 Bug fixes and stability improvements

## Download

- [Google Play Store](https://play.google.com/store/apps/details?id=com.aditsyal.autodroid)
- [F-Droid](https://f-droid.org/packages/com.aditsyal.autodroid/)

## Changelog

- Full changelog: [CHANGELOG.md](docs/CHANGELOG.md)
```

### Social Media Updates

- Post on Twitter/X
- Update LinkedIn
- Share on Reddit (r/androidapps)
- Update Discord/Telegram channels

### Documentation Updates

```bash
# Update documentation
git checkout -b update-docs
# Update version numbers in docs
# Update screenshots if UI changed
# Update API docs if needed
git commit -m "Update documentation for v1.2.0"
git push origin update-docs
```

### User Feedback Collection

```kotlin
// Collect post-release feedback
class FeedbackManager @Inject constructor() {

    fun showPostReleaseSurvey() {
        // Show in-app survey 7 days after update
        if (isFirstRunAfterUpdate() && daysSinceInstall() > 7) {
            showSurveyDialog()
        }
    }

    fun collectCrashReports() {
        // Crash reporting via Timber logging
    }

    fun monitorAppRating() {
        // Check Play Store rating periodically
        // Prompt for rating if not rated
    }
}
```

### Maintenance Tasks

- **Monitor crash reports** in Play Console
- **Review user reviews** and ratings
- **Monitor performance metrics**
- **Plan next release** features
- **Update dependencies** for security patches
- **Address support tickets**

---

**A successful release requires careful planning, thorough testing, and diligent post-release monitoring.** 📦
