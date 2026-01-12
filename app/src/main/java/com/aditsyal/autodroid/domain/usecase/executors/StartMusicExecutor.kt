package com.aditsyal.autodroid.domain.usecase.executors

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class StartMusicExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val appPackage = config["package"]?.toString()
            val action = config["action"]?.toString()?.lowercase() ?: "launch"

            when (action) {
                "launch", "open" -> launchMusicPlayer(appPackage)
                "play" -> playMusic(appPackage)
                "search" -> searchMusic(config)
                else -> throw IllegalArgumentException("Unknown music action: $action")
            }

            Timber.i("Music player action executed: $action")
        }.onFailure { e ->
            Timber.e(e, "Music player execution failed")
        }
    }

    private fun launchMusicPlayer(packageName: String?) {
        try {
            val intent = if (packageName != null) {
                // Launch specific music app
                context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                } ?: throw IllegalArgumentException("Music app not found: $packageName")
            } else {
                // Launch default music player
                Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            context.startActivity(intent)
            Timber.d("Launched music player: ${packageName ?: "default"}")

        } catch (e: Exception) {
            // Fallback: Try alternative music player intents
            tryFallbackMusicIntents()
        }
    }

    private fun tryFallbackMusicIntents() {
        val fallbackIntents = listOf(
            Intent("android.intent.action.MUSIC_PLAYER").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            // Try to launch known music apps
            getLaunchIntentForPackage("com.google.android.music"),
            getLaunchIntentForPackage("com.spotify.music"),
            getLaunchIntentForPackage("com.apple.android.music")
        )

        for (intent in fallbackIntents) {
            try {
                context.startActivity(intent)
                Timber.d("Successfully launched music player with fallback intent")
                return
            } catch (e: Exception) {
                Timber.w("Fallback music intent failed: ${e.message}")
            }
        }

        throw RuntimeException("Unable to launch any music player application")
    }

    private fun getLaunchIntentForPackage(packageName: String): Intent? {
        return try {
            context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun playMusic(packageName: String?) {
        // This is similar to launch but with play intent
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE)
            putExtra(MediaStore.EXTRA_MEDIA_GENRE, "")
            putExtra(SearchManager.QUERY, "") // Empty query to just start playback
        }

        if (packageName != null) {
            intent.setPackage(packageName)
        }

        try {
            context.startActivity(intent)
            Timber.d("Started music playback")
        } catch (e: Exception) {
            Timber.w("Play music intent failed, trying launch instead")
            launchMusicPlayer(packageName)
        }
    }

    private fun searchMusic(config: Map<String, Any>) {
        val query = config["query"]?.toString() ?: ""
        val artist = config["artist"]?.toString()
        val album = config["album"]?.toString()
        val song = config["song"]?.toString()

        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        when {
            !query.isBlank() -> {
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
                intent.putExtra(SearchManager.QUERY, query)
            }
            artist != null && album != null -> {
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)
                intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist)
                intent.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album)
            }
            artist != null -> {
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
                intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist)
            }
            album != null -> {
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)
                intent.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album)
            }
            song != null -> {
                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)
                intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, song)
            }
            else -> {
                throw IllegalArgumentException("Search requires query, artist, album, or song parameter")
            }
        }

        try {
            context.startActivity(intent)
            Timber.d("Searched for music with query: $query")
        } catch (e: Exception) {
            Timber.w("Music search failed, trying general launch")
            launchMusicPlayer(null)
        }
    }

    // Utility methods
    fun getInstalledMusicApps(): List<String> {
        return try {
            val packageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val apps = packageManager.queryIntentActivities(intent, 0)
                .filter { resolveInfo ->
                    val packageName = resolveInfo.activityInfo.packageName
                    // Check if app declares music-related intents
                    hasMusicIntent(packageName)
                }
                .map { it.activityInfo.packageName }
                .distinct()

            Timber.d("Found ${apps.size} music apps: $apps")
            apps
        } catch (e: Exception) {
            Timber.e(e, "Failed to get installed music apps")
            emptyList()
        }
    }

    private fun hasMusicIntent(packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            val intents = listOf(
                MediaStore.INTENT_ACTION_MUSIC_PLAYER,
                MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH,
                "android.intent.action.MUSIC_PLAYER"
            )

            intents.any { action ->
                val intent = Intent(action)
                intent.setPackage(packageName)
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
            }
        } catch (e: Exception) {
            false
        }
    }
}