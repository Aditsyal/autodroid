# Getting Started Guide

Welcome to AutoDroid development! This guide will help you set up your development environment and get started with building AutoDroid.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Building the Project](#building-the-project)
- [Running the App](#running-the-app)
- [Development Workflow](#development-workflow)
- [Testing](#testing)
- [Common Development Tasks](#common-development-tasks)
- [Troubleshooting](#troubleshooting)
- [Next Steps](#next-steps)

## Prerequisites

### Required Software

- **Android Studio**: Koala (2024.1) or newer
  - Download from [developer.android.com/studio](https://developer.android.com/studio)
  - Install the latest Android SDK Build Tools
  - Install Android 15 (API 35) SDK platform

- **JDK**: 17 or higher
  - Android Studio usually includes a bundled JDK
  - Or install from [oracle.com/java](https://www.oracle.com/java/technologies/downloads/)

- **Git**: Version control system
  - Download from [git-scm.com](https://git-scm.com/downloads)

- **Android SDK Components** (via Android Studio SDK Manager):
  - Android SDK Build-Tools 35.0.0
  - Android SDK Platform-Tools
  - Android SDK Platform 35
  - Intel x86 Emulator Accelerator (HAXM installer) for Mac/Windows
  - Google Play services

### Optional but Recommended

- **A Physical Android Device**: For testing automation features
  - API 24 (Android 7.0) or higher
  - Developer options enabled
  - USB debugging enabled

- **Android Emulator**: For quick testing
  - Create with Android 15 (API 35) image
  - Include Google APIs

- **Genymotion**: Alternative fast emulator (optional)

## Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/autodroid.git
cd autodroid
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Click **"Open an Existing Project"**
3. Navigate to the `autodroid` directory
4. Click **OK**

### 3. Gradle Sync

Wait for Android Studio to:
- Download Gradle wrapper
- Download dependencies (this may take several minutes on first run)
- Index the project

If sync fails:
- Try **File > Invalidate Caches > Invalidate and Restart**
- Check your internet connection
- Verify JDK version is 17 or higher

### 4. Verify Setup

Open the **Terminal** tab in Android Studio and run:

```bash
./gradlew --version
```

You should see Gradle version 8.13.1 or higher.

## Building the Project

### Debug Build

```bash
./gradlew assembleDebug
```

This creates `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

This creates `app/build/outputs/apk/release/app-release.apk`

Note: Release builds require signing configuration.

### Clean Build

If you encounter build issues:

```bash
./gradlew clean
./gradlew assembleDebug
```

## Running the App

### On Emulator

1. Open **Device Manager** in Android Studio
2. Create or start an emulator
3. Click the **Run** button (▶️) or press `Shift + F10`

### On Physical Device

1. Enable **Developer Options** on your device:
   - Go to **Settings > About phone**
   - Tap **Build number** 7 times
2. Enable **USB Debugging**:
   - Go to **Settings > Developer Options**
   - Enable **USB Debugging**
3. Connect device via USB
4. Click **Run** button in Android Studio
5. Accept the USB debugging prompt on your device

### First-Time Setup

After launching the app:

1. **Grant Permissions**: Grant initial permissions when prompted
2. **Accessibility Service**: Go to **Settings > Accessibility > AutoDroid** and enable
3. **Battery Optimization**: Go to **Settings > Apps > AutoDroid > Battery** and set to "Don't optimize"
4. **Location**: Enable location services for location-based triggers
5. **Notifications**: Allow notifications for automation feedback

See [User Guide](USER_GUIDE.md) for detailed setup instructions.

## Development Workflow

### Typical Development Cycle

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make changes** to source files

3. **Build and test**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

5. **Run lint**:
   ```bash
   ./gradlew lintDebug
   ```

6. **Commit changes**:
   ```bash
   git add .
   git commit -m "Add feature: description"
   ```

7. **Push to remote**:
   ```bash
   git push origin feature/your-feature-name
   ```

8. **Create Pull Request** on GitHub

### Hot Reload with Compose

When developing UI with Jetpack Compose, changes are often visible immediately:

- Enable **Live Edit** in Android Studio (Experimental)
- Or use **Compose Preview** for UI components
- Press **R** key in Run window for recomposition

### Debugging

- **Set breakpoints** by clicking the gutter next to line numbers
- **Debug app** by clicking the **Debug** button (🐛) or press `Shift + F9`
- **View logs** in Logcat (filter by "AutoDroid")
- **View network calls** in App Inspection
- **View database** in App Inspection

## Testing

### Run Unit Tests

```bash
# All unit tests
./gradlew testDebugUnitTest

# Specific test class
./gradlew testDebugUnitTest --tests "com.aditsyal.autodroid.presentation.viewmodels.MacroListViewModelTest"
```

### Run Integration Tests

```bash
# Connect device/emulator first
./gradlew connectedDebugAndroidTest
```

### Test Coverage

```bash
# Generate coverage report
./gradlew testDebugUnitTestCoverage
# View report at: app/build/reports/coverage/test/debug/index.html
```

See [Testing Guide](TESTING.md) for comprehensive testing documentation.

## Common Development Tasks

### Adding a New Trigger

1. Create trigger provider in `app/src/main/java/com/aditsyal/autodroid/automation/trigger/providers/`
2. Register in `di/TriggerModule.kt`
3. Add to `presentation/components/TriggerPickerDialog.kt`
4. Write unit tests
5. Update documentation

See [Trigger Development Guide](TRIGGERS.md) for details.

### Adding a New Action

1. Add action executor in `domain/usecase/executors/`
2. Register in `domain/usecase/ExecuteActionUseCase.kt`
3. Add to `presentation/components/ActionPickerDialog.kt`
4. Write unit tests
5. Update documentation

See [Action Development Guide](ACTIONS.md) for details.

### Adding a New Constraint

1. Add constraint type to domain models
2. Implement evaluation logic in `domain/usecase/EvaluateConstraintsUseCase.kt`
3. Add to `presentation/components/ConstraintPickerDialog.kt`
4. Write unit tests
5. Update documentation

See [Constraint Guide](CONSTRAINTS.md) for details.

### Database Schema Changes

1. Update entity in `data/local/entities/`
2. Update DAO if needed
3. Increment database version in `AutomationDatabase.kt`
4. Create migration in `AutomationDatabase.kt`
5. Test migration

See [Database Guide](DATABASE.md) for details.

### Adding a New Screen

1. Create Composable in `presentation/screens/`
2. Create ViewModel in `presentation/viewmodels/`
3. Add route in `presentation/navigation/NavGraph.kt`
4. Write unit tests
5. Update documentation

See [UI Components Guide](UI_COMPONENTS.md) for details.

## Troubleshooting

### Build Issues

**Problem**: Gradle sync fails
- **Solution**: File > Invalidate Caches > Invalidate and Restart

**Problem**: Out of memory during build
- **Solution**: Increase Gradle heap size in `gradle.properties`:
  ```properties
  org.gradle.jvmargs=-Xmx4096m
  ```

**Problem**: "SDK not found" error
- **Solution**: Install missing SDK via Tools > SDK Manager

### Runtime Issues

**Problem**: App crashes on launch
- **Solution**: Check Logcat for crash logs, verify permissions granted

**Problem**: Triggers not firing
- **Solution**: Enable accessibility service, disable battery optimization

**Problem**: Actions not executing
- **Solution**: Check execution logs, verify permissions, check constraints

### Development Issues

**Problem**: Tests fail
- **Solution**: Check test logs, verify test dependencies, update test data

**Problem**: Lint errors
- **Solution**: Fix lint issues or add `@SuppressLint` with justification

**Problem**: Code style violations
- **Solution**: Run `./gradlew ktlintFormat` to auto-format

See [Troubleshooting Guide](TROUBLESHOOTING.md) for more solutions.

## Project Structure Overview

```
app/src/main/java/com/aditsyal/autodroid/
├── automation/              # Automation system
│   └── trigger/             # Trigger providers
├── data/                    # Data layer
│   ├── local/               # Room database
│   ├── models/              # DTOs
│   └── repository/          # Repository implementations
├── di/                      # Dependency injection
├── domain/                  # Domain layer
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Business logic
├── presentation/            # Presentation layer
│   ├── components/          # UI components
│   ├── screens/             # Composable screens
│   ├── ui/                  # Additional UI
│   ├── theme/               # Theming
│   └── viewmodels/          # ViewModels
├── receivers/               # Broadcast receivers
├── services/                # Android services
├── utils/                   # Utilities
└── workers/                 # WorkManager workers
```

See [Architecture Documentation](ARCHITECTURE.md) for detailed architecture information.

## Key Files to Know

- `app/build.gradle.kts`: App-level Gradle configuration
- `gradle/libs.versions.toml`: Dependency versions
- `AndroidManifest.xml`: App configuration and permissions
- `AutodroidApplication.kt`: Application class
- `MainActivity.kt`: Main activity entry point
- `AutomationDatabase.kt`: Room database configuration

## Development Tips

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Keep functions short and focused
- Add comments for complex logic
- Write tests for new features

### Performance

- Use Flow for reactive data
- Optimize database queries with indices
- Use coroutines for async operations
- Avoid main thread blocking
- Profile with Android Profiler

### Best Practices

- Use dependency injection (Hilt)
- Follow clean architecture principles
- Keep UI state in ViewModels
- Use sealed classes for state
- Handle errors gracefully
- Log with Timber

### Git Workflow

- Use feature branches
- Write descriptive commit messages
- Keep PRs focused
- Update documentation with changes
- Review your own code before submitting

## Next Steps

Now that you're set up, explore the documentation:

- **[User Guide](USER_GUIDE.md)**: Learn how to use AutoDroid features
- **[Architecture Documentation](ARCHITECTURE.md)**: Understand the system architecture
- **[Trigger Development Guide](TRIGGERS.md)**: Add new triggers
- **[Action Development Guide](ACTIONS.md)**: Add new actions
- **[Testing Guide](TESTING.md)**: Write and run tests
- **[Contributing Guide](CONTRIBUTING.md)**: Contribution guidelines

## Getting Help

- **Documentation**: Check the `/docs` directory
- **Issues**: Search existing GitHub issues
- **Logs**: Check Logcat (filter by "AutoDroid")
- **Community**: Join discussions in GitHub Discussions

Happy coding! 🚀
