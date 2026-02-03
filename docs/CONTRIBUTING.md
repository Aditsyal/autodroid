# Contributing to AutoDroid

Thank you for your interest in contributing to AutoDroid! 🎉

We welcome contributions from the community, whether it's bug reports, feature requests, documentation improvements, or code contributions. This guide will help you get started.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Development Workflow](#development-workflow)
- [Pull Request Process](#pull-request-process)
- [Code Style Guidelines](#code-style-guidelines)
- [Testing Guidelines](#testing-guidelines)
- [Documentation Guidelines](#documentation-guidelines)
- [Issue Reporting](#issue-reporting)
- [Feature Requests](#feature-requests)
- [Community Support](#community-support)
- [Recognition](#recognition)

## Code of Conduct

This project follows a code of conduct to ensure a welcoming environment for all contributors. By participating, you agree to:

- **Be Respectful**: Treat all people with respect, regardless of background or experience level
- **Be Inclusive**: Welcome contributions from everyone
- **Be Collaborative**: Work together to solve problems
- **Be Professional**: Keep discussions constructive and on-topic
- **Follow Guidelines**: Adhere to the contribution guidelines

If you encounter any violations, please report them to the maintainers.

## How to Contribute

### Types of Contributions

1. **🐛 Bug Reports**: Report bugs and help us improve stability
2. **✨ Feature Requests**: Suggest new features and improvements
3. **📝 Documentation**: Improve documentation, tutorials, or examples
4. **💻 Code Contributions**: Submit fixes, features, or refactoring
5. **🧪 Testing**: Add or improve tests
6. **🎨 Design**: Improve UI/UX or provide design assets
7. **🌍 Translation**: Help translate the app or documentation

### Getting Started

1. **Fork the Repository**: Click the "Fork" button on GitHub
2. **Clone Your Fork**: `git clone https://github.com/yourusername/autodroid.git`
3. **Set Up Development Environment**: Follow the [Getting Started Guide](GETTING_STARTED.md)
4. **Create a Branch**: `git checkout -b feature/your-feature-name`
5. **Make Changes**: Implement your contribution
6. **Test Your Changes**: Run tests and ensure everything works
7. **Submit a Pull Request**: Follow the PR process below

## Development Setup

### Prerequisites

- **Android Studio**: Koala (2024.1) or newer
- **JDK**: 17 or higher
- **Git**: Latest version
- **Android SDK**: API 35 with build tools

### Quick Setup

```bash
# Clone the repository
git clone https://github.com/yourusername/autodroid.git
cd autodroid

# Open in Android Studio
# Android Studio will handle Gradle sync and dependency download

# Run the app
./gradlew installDebug
```

For detailed setup instructions, see [Getting Started](GETTING_STARTED.md).

## Development Workflow

### 1. Choose an Issue

- Check the [GitHub Issues](https://github.com/yourusername/autodroid/issues) page
- Look for issues labeled `good first issue` or `help wanted`
- Comment on the issue to indicate you're working on it

### 2. Create a Branch

```bash
# Create and switch to a new branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-number-description

# Or for documentation
git checkout -b docs/improve-contributing-guide
```

### 3. Implement Your Changes

- Follow the [Code Style Guidelines](#code-style-guidelines)
- Write tests for new functionality
- Update documentation if needed
- Ensure your changes don't break existing functionality

### 4. Test Your Changes

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run integration tests
./gradlew connectedDebugAndroidTest

# Run lint checks
./gradlew lintDebug

# Run security scan
./gradlew dependencyCheckAnalyze
```

### 5. Commit Your Changes

```bash
# Add your changes
git add .

# Commit with a descriptive message
git commit -m "Add feature: brief description of what you implemented"

# Follow conventional commit format:
# feat: add new feature
# fix: bug fix
# docs: documentation changes
# style: formatting changes
# refactor: code restructuring
# test: adding tests
# chore: maintenance tasks
```

### 6. Push and Create Pull Request

```bash
# Push your branch
git push origin feature/your-feature-name

# Create a Pull Request on GitHub
# Fill out the PR template with details about your changes
```

## Pull Request Process

### PR Requirements

Before submitting a PR, ensure:

- [ ] **Tests Pass**: All tests pass locally
- [ ] **Lint Clean**: No lint errors or warnings
- [ ] **Documentation Updated**: Docs updated for new features
- [ ] **Code Style**: Follows Kotlin and Android conventions
- [ ] **Commits Squashed**: Related commits combined
- [ ] **Branch Up-to-Date**: Merged latest main branch

### PR Template

Use this template when creating a PR:

```markdown
## Description

Brief description of the changes made.

## Type of Change

- [ ] Bug fix (non-breaking change)
- [ ] New feature (non-breaking change)
- [ ] Breaking change
- [ ] Documentation update
- [ ] Refactoring

## How Has This Been Tested?

- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] All existing tests pass

## Checklist

- [ ] My code follows the project's style guidelines
- [ ] I have performed a self-review of my own code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works

## Screenshots (if applicable)

Add screenshots of UI changes.

## Additional Notes

Any additional information or context.
```

### PR Review Process

1. **Automated Checks**: GitHub Actions will run tests and linting
2. **Manual Review**: Maintainers will review your code
3. **Feedback**: Address any requested changes
4. **Approval**: PR approved and merged
5. **Release**: Changes included in next release

### PR Size Guidelines

- **Small PRs**: < 200 lines of code (preferred)
- **Medium PRs**: 200-500 lines of code
- **Large PRs**: > 500 lines (should be split into multiple PRs)

Large PRs are harder to review and more likely to introduce bugs.

## Code Style Guidelines

### Kotlin Style

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Good: Descriptive names, proper formatting
class MacroRepositoryImpl(
    private val macroDao: MacroDao,
    private val triggerDao: TriggerDao
) : MacroRepository {

    override suspend fun getMacroById(id: Long): MacroDTO? {
        return try {
            val entity = macroDao.getMacroById(id)
            entity?.toDTO()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get macro $id")
            null
        }
    }
}

// Bad: Poor naming, no error handling
class Repo(
    val dao: MacroDao
) : MacroRepository {

    override suspend fun get(id: Long): MacroDTO? {
        return dao.getMacroById(id)?.toDTO()
    }
}
```

### Android-Specific Guidelines

- **Use ViewModels**: For UI state management
- **Dependency Injection**: Use Hilt for DI
- **Coroutines**: For asynchronous operations
- **Flow**: For reactive data streams
- **Room**: For database operations
- **WorkManager**: For background tasks

### Code Organization

```
app/src/main/java/com/aditsyal/autodroid/
├── domain/              # Business logic
│   ├── model/          # Data models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
├── data/               # Data layer
│   ├── local/         # Room database
│   ├── remote/        # Network (if needed)
│   └── repository/    # Repository implementations
├── presentation/       # UI layer
│   ├── ui/            # Compose screens
│   ├── components/    # Reusable components
│   ├── theme/         # Theming
│   └── viewmodels/    # ViewModels
├── di/                # Dependency injection
└── utils/             # Utilities
```

### Naming Conventions

- **Classes**: PascalCase (`MacroRepository`, `ExecuteActionUseCase`)
- **Functions**: camelCase (`getMacroById()`, `executeAction()`)
- **Variables**: camelCase (`macroList`, `isEnabled`)
- **Constants**: SCREAMING_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Packages**: lowercase (`com.aditsyal.autodroid.domain`)

### Documentation

```kotlin
/**
 * Executes a macro with all its triggers, constraints, and actions.
 *
 * This use case handles the complete macro execution flow:
 * 1. Load macro from database
 * 2. Evaluate constraints
 * 3. Execute actions in order
 * 4. Log execution results
 *
 * @param macroId The ID of the macro to execute
 * @return Result indicating success or failure
 */
class ExecuteMacroUseCase @Inject constructor(
    private val repository: MacroRepository,
    // ... other dependencies
) {
    suspend operator fun invoke(macroId: Long): Result<Unit> {
        // Implementation
    }
}
```

## Testing Guidelines

### Test Structure

```
app/src/test/java/com/aditsyal/autodroid/
├── domain/
│   └── usecase/
│       └── ExecuteMacroUseCaseTest.kt
├── presentation/
│   └── viewmodels/
│       └── MacroListViewModelTest.kt
└── utils/
    └── DateTimeHelperTest.kt
```

### Unit Test Example

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ExecuteMacroUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var useCase: ExecuteMacroUseCase

    private val mockRepository = mockk<MacroRepository>()
    private val mockExecuteActionUseCase = mockk<ExecuteActionUseCase>()

    @Before
    fun setup() {
        useCase = ExecuteMacroUseCase(
            mockRepository,
            mockExecuteActionUseCase,
            // ... other mocks
        )
    }

    @Test
    fun `execute macro returns success when all actions succeed`() = runTest {
        // Given
        val macro = MacroDTO(id = 1L, name = "Test", enabled = true)
        coEvery { mockRepository.getMacroById(1L) } returns macro
        coEvery { mockExecuteActionUseCase(any(), any()) } returns Result.success(Unit)

        // When
        val result = useCase(1L)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockRepository.getMacroById(1L) }
    }
}
```

### Test Coverage Requirements

- **Domain Layer**: > 90% coverage
- **Data Layer**: > 85% coverage
- **Presentation Layer**: > 80% coverage
- **Overall**: > 85% coverage

### Testing Best Practices

1. **Arrange-Act-Assert**: Structure tests clearly
2. **One Concept Per Test**: Test one behavior at a time
3. **Descriptive Names**: Tests should read like specifications
4. **Mock Dependencies**: Use mocks for external dependencies
5. **Test Edge Cases**: Cover error conditions and boundaries
6. **Integration Tests**: Test component interactions
7. **UI Tests**: Test user-facing functionality

## Documentation Guidelines

### Documentation Types

1. **Code Documentation**: KDoc comments for public APIs
2. **README Files**: Project and feature documentation
3. **Guides**: User and developer guides
4. **API Reference**: Generated API documentation
5. **Changelogs**: Version history and changes

### Documentation Standards

- **Clear and Concise**: Get to the point quickly
- **Examples**: Include code examples where helpful
- **Cross-References**: Link related documentation
- **Up-to-Date**: Keep documentation current with code changes
- **Accessible**: Use simple language, avoid jargon

### Documentation Structure

```
docs/
├── README.md              # Project overview
├── GETTING_STARTED.md     # Setup guide
├── USER_GUIDE.md          # User manual
├── ARCHITECTURE.md        # System design
├── ACTIONS.md            # Action development
├── TRIGGERS.md           # Trigger development
├── CONSTRAINTS.md        # Constraint system
├── TESTING.md            # Testing guide
├── DATABASE.md           # Database docs
├── SERVICES.md           # Android services
├── VARIABLES.md          # Variable system
├── PERFORMANCE.md        # Optimization guide
├── SECURITY.md           # Security considerations
├── DEPLOYMENT.md         # Build and release
├── UI_COMPONENTS.md      # UI component library
├── API_REFERENCE.md      # API documentation
├── TROUBLESHOOTING.md    # Common issues
├── CONTRIBUTING.md       # This file
├── CHANGELOG.md          # Version history
├── DIAGRAMS/             # Visual diagrams
└── SCREENSHOTS/          # Screenshots
```

## Issue Reporting

### Bug Reports

Use the bug report template:

```markdown
**Describe the Bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:

1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected Behavior**
A clear description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment**

- Device: [e.g., Pixel 6]
- OS: [e.g., Android 13]
- Version: [e.g., 1.0.0]
- Build: [e.g., Debug/Release]

**Additional Context**
Add any other context about the problem here.
```

### Security Issues

For security vulnerabilities:

1. **Don't** create a public GitHub issue
2. **Do** email security@yourdomain.com (if applicable)
3. **Or** use GitHub's security advisory feature
4. Provide detailed reproduction steps
5. Allow time for investigation before public disclosure

## Feature Requests

### Feature Request Template

```markdown
**Is your feature request related to a problem? Please describe.**
A clear and concise description of what the problem is.

**Describe the solution you'd like**
A clear and concise description of what you want to happen.

**Describe alternatives you've considered**
A clear and concise description of any alternative solutions or features you've considered.

**Additional context**
Add any other context or screenshots about the feature request here.

**Implementation Notes**
If you have thoughts on implementation, add them here.
```

### Feature Evaluation Criteria

Features are evaluated based on:

- **User Value**: How many users would benefit
- **Technical Feasibility**: Can it be implemented with current architecture
- **Maintenance Cost**: Ongoing maintenance burden
- **Scope**: Size and complexity of implementation
- **Compatibility**: Android version support requirements

## Community Support

### Getting Help

- **GitHub Issues**: For bugs and feature requests
- **GitHub Discussions**: For general questions and discussions
- **Documentation**: Check existing docs first
- **Search**: Search existing issues before creating new ones

### Communication Guidelines

- **Be Specific**: Provide detailed information
- **Be Patient**: Allow time for responses
- **Be Helpful**: Share solutions you've found
- **Stay On Topic**: Keep discussions relevant
- **Respect Time**: Maintainers volunteer their time

### Community Resources

- **GitHub Repository**: Source code and issues
- **Documentation**: Comprehensive guides
- **Wiki**: Additional resources (if available)
- **Contributing Guide**: This document
- **Code of Conduct**: Community guidelines

## Recognition

### Contributor Recognition

We recognize contributions in several ways:

- **GitHub Contributors**: Listed in repository contributors
- **Changelog**: Contributors mentioned in release notes
- **Hall of Fame**: Special recognition for major contributors
- **Social Media**: Shoutouts for significant contributions

### Types of Recognition

- **Code Contributors**: Authors of merged PRs
- **Issue Reporters**: Users who report and help debug issues
- **Documentation Contributors**: Authors of documentation improvements
- **Reviewers**: Contributors who help review PRs
- **Mentors**: Contributors who help onboard new contributors

### Contribution Levels

- **First-time Contributor**: Welcome aboard! 🎉
- **Regular Contributor**: Consistent valuable contributions
- **Core Contributor**: Major features or significant impact
- **Maintainer**: Ongoing maintenance and project direction

## Frequently Asked Questions

### Q: How do I get started with contributing?

A: Start by reading the [Getting Started Guide](GETTING_STARTED.md), then look for issues labeled `good first issue`. Fork the repo, make your changes, and submit a PR.

### Q: What if I find a bug but don't know how to fix it?

A: Report it as a GitHub issue with detailed reproduction steps. Even if you can't fix it, your report helps others.

### Q: How long does it take for PRs to be reviewed?

A: We aim to review PRs within 1-2 weeks, but it can vary based on complexity and maintainer availability.

### Q: Can I contribute translations?

A: Yes! We welcome translations. Check the [Translation Guide](docs/TRANSLATION.md) for details.

### Q: What if my PR has conflicts?

A: Merge the latest main branch into your feature branch and resolve conflicts.

### Q: Can I work on multiple issues at once?

A: Focus on one issue at a time to avoid confusion and ensure quality.

### Q: What if I need help with my contribution?

A: Ask questions in the PR comments or GitHub Discussions. We're here to help!

---

**Thank you for contributing to AutoDroid! Your help makes the project better for everyone.** 🙏

For more information, check out our [Getting Started Guide](GETTING_STARTED.md) or join the discussion on [GitHub](https://github.com/yourusername/autodroid).
