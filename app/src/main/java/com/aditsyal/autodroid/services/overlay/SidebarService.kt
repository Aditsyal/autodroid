package com.aditsyal.autodroid.services.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aditsyal.autodroid.R
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.domain.usecase.ExecuteMacroUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class SidebarService : Service() {

    @Inject
    lateinit var macroRepository: MacroRepository

    @Inject
    lateinit var executeMacroUseCase: ExecuteMacroUseCase

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var params: WindowManager.LayoutParams

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var isExpanded = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    private fun showOverlay() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        // Create the floating bubble
        overlayView = createFloatingBubble()
        windowManager.addView(overlayView, params)
    }

    private fun createFloatingBubble(): View {
        val bubble = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_play)
            setBackgroundResource(R.drawable.floating_bubble_background)
            setColorFilter(ContextCompat.getColor(context, android.R.color.white))

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY

                        // Only move if it's a significant drag
                        if (abs(dx) > 10 || abs(dy) > 10) {
                            params.x = initialX + dx.toInt()
                            params.y = initialY + dy.toInt()
                            windowManager.updateViewLayout(overlayView, params)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY

                        // If it was just a tap (not a drag), expand the menu
                        if (abs(dx) < 10 && abs(dy) < 10) {
                            toggleExpandedMenu()
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        return bubble
    }

    private fun toggleExpandedMenu() {
        if (isExpanded) {
            collapseMenu()
        } else {
            expandMenu()
        }
    }

    private fun expandMenu() {
        if (isExpanded) return

        isExpanded = true

        // Remove the bubble
        windowManager.removeView(overlayView)

        // Create expanded menu
        val expandedLayout = createExpandedMenu()
        overlayView = expandedLayout

        // Adjust params for larger size
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT

        windowManager.addView(overlayView, params)
    }

    private fun collapseMenu() {
        if (!isExpanded) return

        isExpanded = false

        // Remove expanded menu
        windowManager.removeView(overlayView)

        // Create bubble again
        val bubble = createFloatingBubble()
        overlayView = bubble

        // Reset params
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT

        windowManager.addView(overlayView, params)
    }

    private fun createExpandedMenu(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.expanded_menu_background)
        }

        // Header with close button
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 8)
        }

        val title = TextView(this).apply {
            text = "Quick Actions"
            textSize = 18f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val closeButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundResource(android.R.color.transparent)
            setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            setOnClickListener { collapseMenu() }
        }

        header.addView(title)
        header.addView(closeButton)
        layout.addView(header)

        // Macros list
        serviceScope.launch {
            val macros = macroRepository.getAllMacros().collect { macroList ->
                runOnUiThread {
                    if (macroList.isNotEmpty()) {
                        val recyclerView = RecyclerView(this@SidebarService).apply {
                            layoutManager = LinearLayoutManager(this@SidebarService)
                            adapter = MacroAdapter(macroList) { macro ->
                                serviceScope.launch {
                                    executeMacroUseCase(macro.id)
                                    collapseMenu()
                                }
                            }
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                300
                            ).apply {
                                setMargins(16, 8, 16, 16)
                            }
                        }
                        layout.addView(recyclerView)
                    }
                }
            }
        }

        return layout
    }

    private fun runOnUiThread(action: () -> Unit) {
        android.os.Handler(mainLooper).post(action)
    }

    inner class MacroAdapter(
        private val macros: List<MacroDTO>,
        private val onMacroClick: (MacroDTO) -> Unit
    ) : RecyclerView.Adapter<MacroAdapter.MacroViewHolder>() {

        inner class MacroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardView: CardView = itemView as CardView
            val textView: TextView = cardView.getChildAt(0) as TextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MacroViewHolder {
            val cardView = CardView(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 4)
                }
                radius = 8f
                setCardBackgroundColor(ContextCompat.getColor(parent.context, android.R.color.white))
                setContentPadding(16, 16, 16, 16)
            }

            val textView = TextView(parent.context).apply {
                setTextColor(ContextCompat.getColor(parent.context, android.R.color.black))
                textSize = 16f
            }

            cardView.addView(textView)

            return MacroViewHolder(cardView)
        }

        override fun onBindViewHolder(holder: MacroViewHolder, position: Int) {
            val macro = macros[position]
            holder.textView.text = macro.name
            holder.cardView.setOnClickListener { onMacroClick(macro) }
        }

        override fun getItemCount(): Int = macros.size
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            windowManager.removeView(overlayView)
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
