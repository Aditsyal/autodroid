package com.aditsyal.autodroid.presentation.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * M3 Expressive Motion System
 * Based on Material Design 3 Expressive guidelines for natural, adaptive animations
 */
object MotionTokens {

    // Spring specifications for natural motion
    val StandardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val SmoothSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Duration tokens for adaptive timing
    object Duration {
        const val ExtraShort = 100 // For small, responsive interactions
        const val Short = 200      // For quick transitions
        const val Medium = 300     // For most component animations
        const val Long = 500       // For large layout changes
        const val ExtraLong = 700  // For full screen transitions
    }

    // Easing curves for natural motion
    val EmphasizedDecelerate: Easing = FastOutSlowInEasing
    val EmphasizedAccelerate: Easing = LinearOutSlowInEasing
    val StandardDecelerate: Easing = FastOutSlowInEasing
    val Linear: Easing = LinearEasing

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

        // FAB expansion for IntOffset (slide animations)
        val FabExpandOffset = spring<IntOffset>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

        // FAB expansion for IntSize (expand animations)
        val FabExpandSize = spring<IntSize>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

        // Screen transitions
        val ScreenEnter = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

        val ScreenExit = tween<Float>(
            durationMillis = Duration.Medium,
            easing = MotionTokens.StandardDecelerate
        )

        // Content expansion
        val ContentExpand = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )

        // Content expansion for IntSize
        val ContentExpandSize = spring<IntSize>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )

        // State changes (loading, empty states)
        val StateChange = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

        // Scale animations
        val ScaleIn = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )

        val ScaleOut = tween<Float>(
            durationMillis = Duration.Short,
            easing = MotionTokens.StandardDecelerate
        )
    }

    // Animation scale factors for different contexts
    object Scale {
        const val Press = 0.92f      // Subtle press feedback
        const val Hover = 1.02f      // Gentle hover lift
        const val Focus = 1.05f      // Clear focus indication
        const val Active = 0.95f     // Active state feedback
    }

    // Stagger delays for sequential animations
    object Stagger {
        const val ItemDelay = 50    // Delay between list items
        const val FabButtonDelay = 75 // Delay between FAB menu items
    }
}