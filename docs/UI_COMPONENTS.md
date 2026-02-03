# UI Components and Theming Guide

## Table of Contents

- [Overview](#overview)
- [Material Design 3](#material-design-3)
- [Theming System](#theming-system)
- [Core Components](#core-components)
- [Layout Components](#layout-components)
- [Form Components](#form-components)
- [Feedback Components](#feedback-components)
- [Navigation Components](#navigation-components)
- [Custom Components](#custom-components)
- [Animation and Transitions](#animation-and-transitions)
- [Accessibility](#accessibility)
- [Performance Optimization](#performance-optimization)
- [Testing UI Components](#testing-ui-components)
- [Best Practices](#best-practices)

## Overview

AutoDroid uses Jetpack Compose for its UI, following Material Design 3 principles with a custom theming system. The app provides a consistent, accessible, and performant user experience across different screen sizes and orientations.

### UI Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Architecture                         │
│                                                           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │ Material     │    │ Custom       │    │ Component   │ │
│  │ Design 3     │    │ Theming      │    │ Library     │ │
│  └─────────────┘    └─────────────┘    └─────────────┘ │
│                         ↓                    │
│  ┌─────────────────────────────────────────────────┐ │
│  │       Screens & ViewModels                     │ │
│  └─────────────────────────────────────────────────┘ │
│                         ↓                    │
│  ┌─────────────────────────────────────────────────┐ │
│  │       User Experience                         │ │
│  └─────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Material Design 3

### Design System Implementation

```kotlin
// Material Design 3 theme
@Composable
fun AutoDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AutoDroidTypography,
        shapes = AutoDroidShapes,
        content = content
    )
}

// Color schemes
private val darkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF4CAF50),
    onSecondaryContainer = Color(0xFFE8F5E8),
    tertiary = Color(0xFFFFAB91),
    onTertiary = Color(0xFFBF360C),
    error = Color(0xFFEF5350),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF757575)
)

private val lightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF388E3C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8F5E8),
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiary = Color(0xFFFF7043),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
    outline = Color(0xFFBDBDBD)
)
```

### Typography System

```kotlin
// Custom typography
val AutoDroidTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Shape System

```kotlin
// Custom shapes
val AutoDroidShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
```

## Theming System

### Dynamic Theming

```kotlin
// Dynamic color theming
@Composable
fun DynamicAutoDroidTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = if (isDynamicColorAvailable) {
        val dynamicColorScheme = dynamicDarkColorScheme(context)
        // Customize with app-specific colors
        dynamicColorScheme.copy(
            primary = Color(0xFF90CAF9), // Keep app brand colors
            secondary = Color(0xFF81C784)
        )
    } else {
        if (isSystemInDarkTheme()) darkColorScheme else lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AutoDroidTypography,
        shapes = AutoDroidShapes,
        content = content
    )
}
```

### Theme Extensions

```kotlin
// Extended theme properties
data class AutoDroidColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val automationEnabled: Color,
    val automationDisabled: Color
)

val LocalAutoDroidColors = staticCompositionLocalOf {
    AutoDroidColors(
        success = Color(0xFF4CAF50),
        warning = Color(0xFFFF9800),
        info = Color(0xFF2196F3),
        automationEnabled = Color(0xFF4CAF50),
        automationDisabled = Color(0xFF757575)
    )
}

val MaterialTheme.autoDroidColors: AutoDroidColors
    @Composable
    get() = LocalAutoDroidColors.current
```

### Theme Usage

```kotlin
@Composable
fun ThemedComponent() {
    val colors = MaterialTheme.autoDroidColors

    Column {
        Text(
            text = "Success message",
            color = colors.success
        )
        Text(
            text = "Warning message",
            color = colors.warning
        )
        Text(
            text = "Automation enabled",
            color = colors.automationEnabled
        )
    }
}
```

## Core Components

### App Scaffold

```kotlin
// Main app scaffold
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoDroidScaffold(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = onNavigationClick?.let {
                    {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    }
                },
                actions = actions
            )
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}
```

### Loading States

```kotlin
// Loading component
@Composable
fun AutoDroidLoading(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
```

### Error States

```kotlin
// Error component
@Composable
fun AutoDroidError(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        onRetry?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = it) {
                Text("Retry")
            }
        }
    }
}
```

### Empty States

```kotlin
// Empty state component
@Composable
fun AutoDroidEmpty(
    title: String,
    message: String,
    icon: ImageVector,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        action?.let {
            Spacer(modifier = Modifier.height(16.dp))
            it()
        }
    }
}
```

## Layout Components

### Responsive Layout

```kotlin
// Responsive layout that adapts to screen size
@Composable
fun ResponsiveLayout(
    content: @Composable (PaddingValues, Dp) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val padding = when {
        screenWidth < 600.dp -> 16.dp // Mobile
        screenWidth < 840.dp -> 24.dp // Tablet
        else -> 32.dp // Desktop
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        content(PaddingValues(padding), screenWidth)
    }
}
```

### Grid Layout

```kotlin
// Adaptive grid layout
@Composable
fun AdaptiveGrid(
    items: List<Any>,
    minItemWidth: Dp = 160.dp,
    content: @Composable (Any, Modifier) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val columns = maxOf(1, (screenWidth / minItemWidth).toInt())

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            content(item, Modifier.fillMaxWidth())
        }
    }
}
```

### Card Layouts

```kotlin
// Macro card component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroCard(
    macro: MacroDTO,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = macro.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = macro.description ?: "No description",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = macro.enabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete macro",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Trigger and action counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${macro.triggers.size} triggers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${macro.actions.size} actions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
```

## Form Components

### Text Input Fields

```kotlin
// Enhanced text field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoDroidTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { /* Handle next */ },
                onDone = { /* Handle done */ }
            ),
            maxLines = maxLines,
            isError = error != null,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
```

### Dropdowns and Pickers

```kotlin
// Trigger picker dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerPickerDialog(
    onDismiss: () -> Unit,
    onTriggerSelected: (TriggerOption) -> Unit
) {
    val triggerOptions = remember {
        listOf(
            TriggerOption("Time", "TIME", mapOf()),
            TriggerOption("Location", "LOCATION", mapOf()),
            TriggerOption("Shake", "SHAKE", mapOf()),
            // ... more options
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Trigger Type") },
        text = {
            LazyColumn {
                items(triggerOptions) { option ->
                    ListItem(
                        headlineContent = { Text(option.name) },
                        modifier = Modifier.clickable {
                            onTriggerSelected(option)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

### Switches and Toggles

```kotlin
// Enhanced switch with label
@Composable
fun LabeledSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = null, // Handled by parent
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
```

### Time and Date Pickers

```kotlin
// Time picker dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedTime = LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(selectedTime)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

## Feedback Components

### Snackbars

```kotlin
// Snackbar host and management
@Composable
fun AutoDroidSnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
    ) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            actionColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Usage
@Composable
fun ScreenWithSnackbar() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { AutoDroidSnackbarHost(snackbarHostState) },
        content = { padding ->
            // Screen content
            Button(onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar("Action completed!")
                }
            }) {
                Text("Show Snackbar")
            }
        }
    )
}
```

### Progress Indicators

```kotlin
// Linear progress for operations
@Composable
fun OperationProgress(
    progress: Float, // 0.0 to 1.0
    message: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// Circular progress for loading
@Composable
fun LoadingSpinner(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
```

### Confirmation Dialogs

```kotlin
// Confirmation dialog for destructive actions
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    )
}
```

## Navigation Components

### Bottom Navigation

```kotlin
// Bottom navigation bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoDroidBottomNavigation(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItem("Macros", "macros", Icons.Filled.Home),
        NavigationItem("Templates", "templates", Icons.Filled.List),
        NavigationItem("History", "history", Icons.Filled.History),
        NavigationItem("Settings", "settings", Icons.Filled.Settings)
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigateToRoute(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

data class NavigationItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
```

### Drawer Navigation

```kotlin
// Navigation drawer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoDroidNavigationDrawer(
    drawerState: DrawerState,
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    onSignOut: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // Header
                    Text(
                        text = "AutoDroid",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Navigation items
                    val items = listOf(
                        DrawerItem("Home", "home", Icons.Filled.Home),
                        DrawerItem("Profile", "profile", Icons.Filled.Person),
                        DrawerItem("Settings", "settings", Icons.Filled.Settings)
                    )

                    items.forEach { item ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(item.icon, contentDescription = null)
                            },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                onNavigateToRoute(item.route)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Sign out
                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Filled.ExitToApp, contentDescription = null)
                        },
                        label = { Text("Sign Out") },
                        selected = false,
                        onClick = onSignOut
                    )
                }
            }
        }
    ) {
        content()
    }
}
```

### Tab Navigation

```kotlin
// Tab layout for macro editor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroEditorTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Triggers", "Actions", "Constraints", "Variables")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(title) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
```

## Custom Components

### Macro Status Indicator

```kotlin
// Visual indicator for macro status
@Composable
fun MacroStatusIndicator(
    isEnabled: Boolean,
    lastExecutionStatus: String?,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when {
        !isEnabled -> Color.Gray to Icons.Filled.Pause
        lastExecutionStatus == "SUCCESS" -> Color.Green to Icons.Filled.CheckCircle
        lastExecutionStatus == "FAILURE" -> Color.Red to Icons.Filled.Error
        lastExecutionStatus == "SKIPPED" -> Color.Yellow to Icons.Filled.Warning
        else -> Color.Gray to Icons.Filled.Help
    }

    Box(
        modifier = modifier
            .size(16.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = Color.White
        )
    }
}
```

### Trigger/Action Chips

```kotlin
// Chip component for displaying triggers/actions
@Composable
fun TriggerChip(
    trigger: TriggerDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = getTriggerDisplayName(trigger.type),
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = getTriggerIcon(trigger.type),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier
    )
}

private fun getTriggerDisplayName(type: String): String {
    return when (type) {
        "TIME" -> "Time"
        "LOCATION" -> "Location"
        "SHAKE" -> "Shake"
        else -> type
    }
}

private fun getTriggerIcon(type: String): ImageVector {
    return when (type) {
        "TIME" -> Icons.Filled.Schedule
        "LOCATION" -> Icons.Filled.LocationOn
        "SHAKE" -> Icons.Filled.Vibration
        else -> Icons.Filled.Settings
    }
}
```

### Execution Timeline

```kotlin
// Timeline view for execution history with action-by-action breakdown
@Composable
fun ExecutionTimeline(
    executions: List<ExecutionLogDTO>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(executions) { execution ->
            TimelineItem(execution = execution)
        }
    }
}
```

### Sidebar Launcher Overlay

The `SidebarView` is a custom Android View (rather than a Composable) to ensure it stays visible across all apps as a system overlay.

- **Interaction**: Drag to reposition vertically, tap to expand.
- **Menu**: RecyclerView-based list of favorite macros for quick execution.
- **Theming**: Adapts to app colors using dynamic theming even when the main app is closed.

### Dry-Run Simulation View

The `DryRunPreviewScreen` provides a step-by-step simulation of macro execution.

- **Constraint Evaluation**: Visual indicators (Green/Red) for each constraint's current state.
- **Action Sequence**: List of actions that will execute, with variable resolution shown in real-time.
- **Impact Analysis**: Estimated battery drain and execution time based on action types and history.
- **Natural Motion**: Uses spring physics to animate the simulation progress.

## Animation and Transitions

### M3 Expressive Motion System

AutoDroid implements Material Design 3's Expressive motion system using physics-based spring animations for natural, adaptive interactions. The `MotionTokens.kt` file provides centralized motion specifications that follow Google's guidelines for responsive, delightful animations.

```kotlin
// MotionTokens provides standardized animation specs
object MotionTokens {

    // Spring specifications for natural motion
    val StandardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Motion specs for common interactions
    object MotionSpec {
        // Press interactions (buttons, cards)
        val Press = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )

        // FAB expansion/collapse
        val FabExpand = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

        // Screen transitions
        val ScreenEnter = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

        // Content expansion
        val ContentExpand = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    // Scale factors for different interaction types
    object Scale {
        const val Press = 0.92f      // Subtle press feedback
        const val Hover = 1.02f      // Gentle hover lift
        const val Focus = 1.05f      // Clear focus indication
    }
}
```

#### Usage in Components

```kotlin
@Composable
fun AnimatedCard(
    isPressed: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.Scale.Press else 1f,
        animationSpec = MotionTokens.MotionSpec.Press,
        label = "card_scale"
    )

    Card(
        modifier = modifier.scale(scale),
        // ... other properties
    ) {
        // Card content
    }
}
```

#### Benefits

- **Natural Physics**: Spring animations feel responsive and natural
- **Adaptive**: Motion adapts to content and user preferences
- **Consistent**: Standardized specs ensure uniform motion across the app
- **Performance**: Spring animations are more efficient than tween-based animations
- **Accessibility**: Motion respects user preferences for reduced motion

### Screen Transitions

```kotlin
// Navigation transitions
@Composable
fun AutoDroidNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Navigation destinations
    }
}
```

### List Item Animations

```kotlin
// Animated list items
@Composable
fun AnimatedMacroList(
    macros: List<MacroDTO>,
    onMacroClick: (MacroDTO) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = macros,
            key = { macro -> macro.id }
        ) { macro ->
            MacroCard(
                macro = macro,
                onClick = { onMacroClick(macro) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            )
        }
    }
}
```

### State Change Animations

```kotlin
// Animated state changes
@Composable
fun AnimatedMacroStatus(
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isEnabled) Color.Green else Color.Gray,
        animationSpec = tween(300)
    )

    val scale by animateFloatAsState(
        targetValue = if (isEnabled) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Icon(
        imageVector = if (isEnabled) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
        contentDescription = null,
        tint = color,
        modifier = modifier
            .scale(scale)
            .size(24.dp)
    )
}
```

### Loading Animations

```kotlin
// Pulsing loading animation
@Composable
fun PulsingLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
    }
}
```

## Accessibility

### Screen Reader Support

```kotlin
// Accessible macro card
@Composable
fun AccessibleMacroCard(
    macro: MacroDTO,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        onClickLabel = "Open macro ${macro.name}",
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                // Custom accessibility properties
                stateDescription = if (macro.enabled) "Enabled" else "Disabled"
                collectionInfo = CollectionInfo(
                    rowCount = 1,
                    columnCount = 3,
                    selectionMode = SelectionMode.Single
                )
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = macro.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics {
                    heading()
                }
            )

            Text(
                text = macro.description ?: "No description",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.semantics {
                    contentDescription = "Description: ${macro.description ?: "No description"}"
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Switch(
                    checked = macro.enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.semantics {
                        // Custom accessibility label
                        contentDescription = if (macro.enabled) {
                            "Macro is enabled. Double tap to disable"
                        } else {
                            "Macro is disabled. Double tap to enable"
                        }
                    }
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics {
                        contentDescription = "Delete macro ${macro.name}"
                        role = Role.Button
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null, // Handled by parent
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

### Focus Management

```kotlin
// Focus management for forms
@Composable
fun AccessibleForm(
    onSubmit: () -> Unit,
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics {
                // Form semantics
                contentDescription = "Macro creation form"
                isTraversalGroup = true
            }
    ) {
        content()

        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Create macro"
                    role = Role.Button
                }
        ) {
            Text("Create Macro")
        }
    }
}
```

### Color Contrast

```kotlin
// High contrast colors for accessibility
val HighContrastColors = lightColorScheme(
    primary = Color(0xFF0000FF), // Pure blue for high contrast
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEEEFF),
    onPrimaryContainer = Color(0xFF000080),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    // ... other colors with high contrast ratios
)
```

## Performance Optimization

### Compose Recomposition Optimization

```kotlin
// Stable data classes
@Stable
data class MacroUiState(
    val id: Long,
    val name: String,
    val enabled: Boolean,
    val description: String?,
    val triggerCount: Int,
    val actionCount: Int
)

// Stable lambdas
@Composable
fun OptimizedMacroList(
    macros: List<MacroUiState>,
    onMacroClick: (Long) -> Unit, // Stable lambda
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = macros,
            key = { macro -> macro.id }
        ) { macro ->
            MacroCard(
                macro = macro,
                onClick = { onMacroClick(macro.id) }
            )
        }
    }
}
```

### Memory Optimization

```kotlin
// Efficient image loading
@Composable
fun OptimizedAsyncImage(
    uri: Uri,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true)
            .size(Size.ORIGINAL)
            .precision(Precision.INEXACT)
            .memoryCacheKey(uri.toString())
            .diskCacheKey(uri.toString())
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.placeholder),
        error = painterResource(R.drawable.error)
    )
}
```

### State Hoisting

```kotlin
// Proper state hoisting for performance
@Composable
fun MacroEditorScreen(
    viewModel: MacroEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onNameChange: (String) -> Unit = remember(viewModel) {
        { viewModel.updateName(it) }
    }
    val onDescriptionChange: (String) -> Unit = remember(viewModel) {
        { viewModel.updateDescription(it) }
    }

    MacroEditorContent(
        uiState = uiState,
        onNameChange = onNameChange,
        onDescriptionChange = onDescriptionChange
    )
}
```

## Testing UI Components

### Component Testing

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
class MacroCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `macro card displays correct information`() {
        // Given
        val macro = MacroDTO(
            id = 1L,
            name = "Test Macro",
            enabled = true,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )

        // When
        composeTestRule.setContent {
            AutoDroidTheme {
                MacroCard(
                    macro = macro,
                    onClick = {},
                    onToggle = {},
                    onDelete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Macro").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 triggers").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 actions").assertIsDisplayed()
    }

    @Test
    fun `macro card toggle calls on toggle callback`() {
        // Given
        var toggleCalled = false
        var toggleValue = false
        val macro = MacroDTO(
            id = 1L,
            name = "Test Macro",
            enabled = false,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )

        // When
        composeTestRule.setContent {
            AutoDroidTheme {
                MacroCard(
                    macro = macro,
                    onClick = {},
                    onToggle = { value ->
                        toggleCalled = true
                        toggleValue = value
                    },
                    onDelete = {}
                )
            }
        }

        // Toggle the switch
        composeTestRule.onNodeWithTag("macro_toggle").performClick()

        // Then
        assertTrue(toggleCalled)
        assertTrue(toggleValue)
    }
}
```

### Screen Testing

```kotlin
class MacroListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `screen displays macros when loaded`() {
        // Given
        val macros = listOf(
            MacroDTO(id = 1L, name = "Macro 1", enabled = true),
            MacroDTO(id = 2L, name = "Macro 2", enabled = false)
        )

        // When
        composeTestRule.setContent {
            AutoDroidTheme {
                MacroListScreen(
                    macros = macros,
                    isLoading = false,
                    error = null,
                    onMacroClick = {},
                    onCreateMacro = {},
                    onToggleMacro = {},
                    onDeleteMacro = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Macro 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Macro 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loading...").assertDoesNotExist()
    }

    @Test
    fun `screen shows loading state`() {
        // When
        composeTestRule.setContent {
            AutoDroidTheme {
                MacroListScreen(
                    macros = emptyList(),
                    isLoading = true,
                    error = null,
                    onMacroClick = {},
                    onCreateMacro = {},
                    onToggleMacro = {},
                    onDeleteMacro = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun `screen shows error state`() {
        // When
        composeTestRule.setContent {
            AutoDroidTheme {
                MacroListScreen(
                    macros = emptyList(),
                    isLoading = false,
                    error = "Network error",
                    onMacroClick = {},
                    onCreateMacro = {},
                    onToggleMacro = {},
                    onDeleteMacro = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }
}
```

### Accessibility Testing

```kotlin
@Test
fun `macro card has proper accessibility labels`() {
    val macro = MacroDTO(id = 1L, name = "Test Macro", enabled = true)

    composeTestRule.setContent {
        AutoDroidTheme {
            AccessibleMacroCard(
                macro = macro,
                onClick = {},
                onToggle = {},
                onDelete = {}
            )
        }
    }

    // Check accessibility properties
    composeTestRule.onNodeWithText("Test Macro")
        .assert(hasContentDescription("Open macro Test Macro"))

    composeTestRule.onNodeWithTag("macro_toggle")
        .assert(hasContentDescription("Macro is enabled. Double tap to disable"))
}
```

## Best Practices

### Component Design

1. **Single Responsibility**: Each component should have one clear purpose
2. **Consistent API**: Use similar parameter patterns across components
3. **Composable**: Components should work well together
4. **Testable**: Components should be easy to test in isolation

### State Management

1. **State Hoisting**: Lift state up when shared between components
2. **Stable Types**: Use @Stable and @Immutable for performance
3. **Remember**: Use remember for expensive computations
4. **Side Effects**: Handle side effects properly with LaunchedEffect

### Theming

1. **Consistent Colors**: Use theme colors instead of hard-coded values
2. **Semantic Colors**: Use colors that convey meaning (success, error, etc.)
3. **Dynamic Themes**: Support light/dark and dynamic color schemes
4. **Accessibility**: Ensure sufficient color contrast

### Performance

1. **Lazy Loading**: Use LazyColumn for large lists
2. **Image Optimization**: Load appropriate image sizes
3. **Recomposition**: Minimize unnecessary recompositions
4. **Memory**: Clean up resources and avoid leaks

### Accessibility

1. **Screen Readers**: Provide content descriptions
2. **Keyboard Navigation**: Support keyboard interaction
3. **Focus Management**: Handle focus appropriately
4. **Color Contrast**: Use colors with sufficient contrast
5. **Touch Targets**: Ensure adequate touch target sizes

### Testing

1. **Unit Tests**: Test component logic in isolation
2. **Integration Tests**: Test component interactions
3. **UI Tests**: Test visual behavior and user interactions
4. **Accessibility Tests**: Ensure accessibility compliance
5. **Performance Tests**: Monitor rendering performance

---

**Well-designed UI components create a polished, accessible, and performant user experience.** 🎨
