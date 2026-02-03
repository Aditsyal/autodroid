# AutoDroid - Android Automation App

**Personal Android Automation App** - Create powerful automations to streamline your Android experience. Schedule tasks, respond to system events, and automate UI interactions with ease.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)](https://developer.android.com)
[![CI](https://img.shields.io/badge/CI-Passing-brightgreen.svg)](https://github.com/yourusername/autodroid/actions)

## 📖 Documentation Index

- **[Getting Started](docs/GETTING_STARTED.md)**: Development setup and first steps
- **[User Guide](docs/USER_GUIDE.md)**: How to use AutoDroid features
- **[Architecture](docs/ARCHITECTURE.md)**: System architecture and design
- **[Triggers](docs/TRIGGERS.md)**: Trigger development guide
- **[Actions](docs/ACTIONS.md)**: Action development guide
- **[Constraints](docs/CONSTRAINTS.md)**: Constraint system guide
- **[Variables](docs/VARIABLES.md)**: Variable system and operations
- **[Testing](docs/TESTING.md)**: Testing strategies and guidelines
- **[Database](docs/DATABASE.md)**: Database schema and queries
- **[Services](docs/SERVICES.md)**: Android services documentation
- **[UI Components](docs/UI_COMPONENTS.md)**: UI components and theming
- **[Troubleshooting](docs/TROUBLESHOOTING.md)**: Common issues and solutions
- **[FAQ](docs/FAQ.md)**: Frequently asked questions
- **[Performance](docs/PERFORMANCE.md)**: Performance optimization
- **[Security](docs/SECURITY.md)**: Security considerations
- **[Privacy Policy](docs/PRIVACY_POLICY.md)**: Data privacy details
- **[Terms of Service](docs/TERMS_OF_SERVICE.md)**: Usage terms and conditions
- **[Contributing](docs/CONTRIBUTING.md)**: Contribution guidelines
- **[Changelog](docs/CHANGELOG.md)**: Version history and changes

## 📱 Features

### 33+ Trigger Types

- **Time-based**: Specific time, intervals, day of week, date ranges
- **Location**: Geofencing (enter/exit)
- **Device State**: Screen on/off, device locked/unlocked, charging status, battery level
- **Connectivity**: WiFi connected/disconnected, specific SSID, Bluetooth connected/disconnected, specific device, mobile data
- **App Events**: App launched/closed, installed/uninstalled, notification received
- **Communication**: Call received/ended, SMS received, missed call
- **Sensors**: Shake detection, proximity, light level, orientation change
- **System Events**: Device boot, headphone connected, USB connected, NFC tag detected
- **Calendar**: Event started/ended, specific event search
- **Audio Profile**: Ringtone mode changed, volume level thresholds

### 49+ Action Types

- **System Settings**: WiFi toggle, Bluetooth toggle, brightness control, screen timeout, airplane mode, GPS toggle, volume control, DND mode
- **Device Control**: Lock screen, unlock screen, sleep device, vibrate, haptic feedback
- **Communication**: Send SMS, delete SMS, send email, make call, speak text (TTS)
- **App Control**: Launch app, close app, clear app cache, open URL
- **Media**: Play sound, stop sound, change wallpaper, media control (play/pause/skip), start music player
- **Notifications**: Show notification, close notification, show toast
- **Automation**: Delay, set variable, HTTP request (Webhook), log event
- **Logic Control**: If/else conditions, while loops, for loops, break/continue

### 15+ Constraint Types

- **Time**: Time range, day of week, exclude weekends, specific date
- **Device State**: Battery level range, charging status, screen state, device locked
- **Connectivity**: WiFi connected/disconnected (any/specific SSID), mobile data active, Bluetooth connected (any/specific device)
- **Location**: Inside/outside geofence
- **Context**: App running, headphones connected, Do Not Disturb enabled

### Advanced Features

- **Variables**: Local and global variables with operations (add, subtract, multiply, divide, append, substring)
- **Logic Control**: If/else branching, while loops, for loops with break/continue support
- **Templates**: Intelligent recommendation engine with search and filter capabilities
- **Execution History**: Detailed logs with expandable action-by-action breakdown
- **Conflict Detection**: Real-time detection of resource contention and overlapping triggers
- **Dry-Run Preview**: Physics-based simulation system to preview macro execution results
- **Sidebar Launcher**: Overlay service for quick access and manual automation trigger
- **Material You**: Dynamic color support with AMOLED dark mode and expressive motion
- **Background Execution**: Reliable automation with foreground service and WorkManager
- **Rule Management**: Create, edit, enable/disable, import/export automation rules

## 🏗️ Architecture

This app follows **Clean Architecture** principles with clear separation of concerns:

```
📁 app/src/main/java/com/aditsyal/autodroid/
├── 🏛️ data/                    # Data layer (Room, Repositories)
│   ├── local/                  # Local storage (Room DB)
│   ├── models/                 # DTOs and data models
│   └── repository/             # Repository implementations
├── 🧠 domain/                  # Business logic layer
│   ├── repository/             # Repository interfaces
│   └── usecase/                # Use cases (business logic)
├── 🎨 presentation/            # UI layer (Compose)
│   ├── components/             # Reusable UI components
│   ├── screens/                # Screen composables
│   ├── ui/                     # Additional UI screens
│   ├── theme/                  # App theming
│   └── viewmodels/             # ViewModels with Hilt injection
├── 🔧 di/                      # Dependency injection modules
├── ⚙️ automation/              # Automation system
│   └── trigger/                # Trigger providers and management
├── 📡 services/                # Android services (Accessibility, Foreground)
├── 📬 receivers/               # Broadcast receivers
├── 🛠️ utils/                   # Utility classes (Cache, Permission, Sound)
└── 🔨 workers/                 # WorkManager workers
```

### Key Technologies

- **Kotlin 2.1.0**: Modern Android development with Coroutines
- **Jetpack Compose**: Declarative UI toolkit
- **Hilt 2.57.2**: Dependency injection framework
- **Room 2.8.4**: Local database with proper indexing
- **WorkManager 2.11.0**: Reliable background task scheduling
- **Accessibility Service**: UI automation capabilities
- **Timber 5.0.1**: Structured logging
- **Flow**: Reactive data streams

### Documentation

- **[Architecture Documentation](docs/ARCHITECTURE.md)**: Detailed architecture overview
- **[Getting Started](docs/GETTING_STARTED.md)**: Development setup guide
- **[User Guide](docs/USER_GUIDE.md)**: How to use the app
- **[Trigger Development](docs/TRIGGERS.md)**: Creating new triggers
- **[Action Development](docs/ACTIONS.md)**: Creating new actions
- **[Constraint Guide](docs/CONSTRAINTS.md)**: Working with constraints
- **[Troubleshooting](docs/TROUBLESHOOTING.md)**: Common issues and solutions

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Koala or newer (2024.1+)
- **JDK**: 17 or higher
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 35 (Android 15)
- **Kotlin**: 2.1.0
- **Gradle**: 8.13.1

### Quick Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/autodroid.git
   cd autodroid
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the autodroid directory
   - Wait for Gradle sync to complete

3. **Build the project**

   ```bash
   # Using command line
   ./gradlew assembleDebug

   # Or use Android Studio: Build > Make Project
   ```

4. **Run on device/emulator**

   ```bash
   # Connect device or start emulator
   ./gradlew installDebug

   # Or use Android Studio: Run > Run 'app'
   ```

### Required Setup for Automation Features

After installing the app, enable the following for full functionality:

1. **Accessibility Service** (required for UI automation)
   - Go to **Settings > Accessibility > AutoDroid**
   - Enable the **AutoDroid** accessibility service
   - Grant all requested permissions

2. **Battery Optimization** (for reliable background execution)
   - Go to **Settings > Apps > AutoDroid > Battery**
   - Set to **Don't optimize** (or **Unrestricted**)

3. **Location Services** (for location-based triggers)
   - Go to **Settings > Location**
   - Enable location services
   - Set location accuracy to "High accuracy"

4. **Exact Alarms** (for precise time-based triggers)
   - Go to **Settings > Apps > AutoDroid > Alarms & reminders**
   - Enable "Allow alarms and reminders"

### Additional Resources

- **[Detailed Setup Guide](docs/GETTING_STARTED.md)**: Comprehensive development setup
- **[User Guide](docs/USER_GUIDE.md)**: How to create and use automations
- **[Troubleshooting](docs/TROUBLESHOOTING.md)**: Common issues and solutions

### Required Permissions

The app requests the following permissions for automation features:

- **Accessibility Service**: For UI automation across apps
- **Location**: For location-based triggers (fine location, background location)
- **Foreground Service**: For reliable background execution
- **Notifications**: For automation feedback and status updates
- **SMS**: For SMS-based automations (SEND_SMS)
- **Phone**: For call-related automations (READ_PHONE_STATE, CALL_PHONE)
- **Bluetooth**: For Bluetooth automations (BLUETOOTH_CONNECT, BLUETOOTH_SCAN)
- **Exact Alarms**: For precise time-based triggers (SCHEDULE_EXACT_ALARM)
- **Battery Optimization**: For reliable background execution

## 🔧 Configuration

### Accessibility Service Setup

1. Go to **Settings > Accessibility > AutoDroid**
2. Enable the **AutoDroid** accessibility service
3. Grant all requested permissions

### Battery Optimization

For reliable background execution:

1. Go to **Settings > Apps > AutoDroid > Battery**
2. Set to **Don't optimize** (or **Unrestricted** on some devices)

## 🧪 Testing

### Run Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Integration tests
./gradlew connectedDebugAndroidTest

# Run all tests
./gradlew test
```

### Code Quality

```bash
# Run lint checks
./gradlew lintDebug

# Run OWASP dependency check
./gradlew dependencyCheckAnalyze
```

### Testing Documentation

- **[Testing Guide](docs/TESTING.md)**: Comprehensive testing strategies
- See `docs/TESTING.md` for detailed testing procedures

### Manual Testing Checklist

Before release, verify:

- [ ] Automation rules execute reliably
- [ ] Background execution works after reboot
- [ ] Accessibility service processes events without blocking UI
- [ ] Battery impact is minimal (< 5% per hour)
- [ ] No crashes on various Android versions/devices
- [ ] Permission denials are handled gracefully
- [ ] All trigger types work correctly
- [ ] All action types execute without errors

## 📊 Performance & Battery

This app is optimized for minimal battery impact:

- **Accessibility Events**: Debounced (300ms) to prevent excessive processing
- **Background Execution**: Uses WorkManager with proper constraints
- **Database Queries**: Indexed for efficient rule matching
- **Memory Management**: Proper coroutine cancellation and lifecycle handling

Expected battery impact: **< 5% per hour** under typical usage.

## 🛠️ Development

### Code Quality

- **Kotlin Linter**: Enabled with strict rules
- **Android Lint**: All warnings resolved
- **ProGuard/R8**: Enabled for release builds with custom rules
- **Testing**: Unit test coverage > 70% (target)

### Adding New Triggers/Actions

See detailed guides:

- **[Trigger Development Guide](docs/TRIGGERS.md)**: How to create new trigger providers
- **[Action Development Guide](docs/ACTIONS.md)**: How to add new action types
- **[Architecture Documentation](docs/ARCHITECTURE.md)**: System architecture overview
- **[Troubleshooting Guide](docs/TROUBLESHOOTING.md)**: Common issues and solutions

Quick steps:

1. **Define the trigger/action model** in `domain/models/`
2. **Create repository methods** in `data/repository/`
3. **Implement use case** in `domain/usecase/`
4. **Add UI components** in `presentation/components/`
5. **Update ViewModel** to handle the new functionality
6. **Add unit tests** for the new logic

## 🚨 Known Issues & Limitations

- **Android 12+ Restrictions**: Some automation features may require additional setup
- **Device Compatibility**: Features may vary across OEMs (Samsung, Google Pixel, etc.)
- **Background Execution**: Doze mode may delay some automations (uses proper exemptions)
- **Accessibility Limitations**: Some apps may not be fully accessible due to implementation
- **See [Troubleshooting Guide](docs/TROUBLESHOOTING.md)** for common issues and solutions

## 🗺️ Roadmap

### Version 1.2 (Planned)

- [ ] NFC tag triggers enhancement
- [ ] Macro sharing via QR codes
- [ ] Cloud backup and sync
- [ ] Advanced scheduling (cron-like expressions)
- [ ] Performance monitoring dashboard

### Version 1.3 (Future)

- [ ] Machine learning-based automation suggestions
- [ ] Integration with smart home devices
- [ ] Voice commands support
- [ ] Macro marketplace for sharing templates

### Version 2.0 (Long-term)

- [ ] Multi-language support (i18n)
- [ ] Plugin system for third-party extensions
- [ ] Web dashboard for remote management

**Want to contribute?** See [Contributing Guidelines](#-contributing) below.


## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Quick Start

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes following our [coding conventions](#code-style)
4. Add tests for new functionality
5. Run tests and lint: `./gradlew test lintDebug`
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

### For More Details

See **[CONTRIBUTING.md](docs/CONTRIBUTING.md)** for:

- Detailed contribution guidelines
- Code of conduct
- Pull request process
- Code review checklist
- Issue reporting guidelines

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable/function names
- Add documentation for public APIs
- Write tests for new functionality
- Keep PRs focused and small
- Update documentation for new features
- Run tests and lint before submitting

## 📄 License

This project is licensed under the GNU License 3.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by Tasker, MacroDroid, and Automate.

---
