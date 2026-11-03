package com.nova.gamingsuite

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class NovaSidebarService : Service() {
    private lateinit var wm: WindowManager
    private var handle: TextView? = null
    private var panel: LinearLayout? = null
    private lateinit var params: WindowManager.LayoutParams

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    private fun overlayType() =
        if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createHandle()
    }

    private fun createHandle() {
        val prefs = getSharedPreferences("nova_sidebar", MODE_PRIVATE)

        params = WindowManager.LayoutParams(
            dp(44), dp(72), overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("x", 0)
            y = prefs.getInt("y", dp(220))
        }

        val v = TextView(this).apply {
            text = "N\nV"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.BLACK)
            setBackgroundColor(android.graphics.Color.rgb(54, 230, 242))
            elevation = dp(8).toFloat()
        }

        v.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f
            var downY = 0f
            var startX = 0
            var startY = 0
            var moved = false

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        downY = event.rawY
                        startX = params.x
                        startY = params.y
                        moved = false
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downX).toInt()
                        val dy = (event.rawY - downY).toInt()

                        if (kotlin.math.abs(dx) > dp(5) ||
                            kotlin.math.abs(dy) > dp(5)) moved = true

                        params.x = (startX + dx).coerceAtLeast(0)
                        params.y = (startY + dy).coerceAtLeast(0)

                        try {
                            wm.updateViewLayout(view, params)
                        } catch (_: Exception) {}
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (moved) {
                            prefs.edit()
                                .putInt("x", params.x)
                                .putInt("y", params.y)
                                .apply()
                        } else {
                            if (panel == null) showPanel() else removePanel()
                        }
                        return true
                    }
                }
                return true
            }
        })

        handle = v
        try {
            wm.addView(v, params)
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun showPanel() {
        if (panel != null) return

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(android.graphics.Color.rgb(17, 24, 39))
        }

        fun button(label: String, action: () -> Unit) {
            box.addView(Button(this).apply {
                text = label
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }

        val stats = TextView(this).apply {
            textSize = 13f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, dp(10), 0, dp(10))
        }

        fun refresh() {
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val mem = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mem)

            val battery = registerReceiver(
                null,
                android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val temp = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1

            val batteryText =
                if (level >= 0 && scale > 0) "${level * 100 / scale}%" else "N/A"
            val tempText =
                if (temp >= 0) String.format("%.1f °C", temp / 10f) else "N/A"

            stats.text =
                "RAM tersedia: ${mem.availMem / (1024 * 1024)} MB\n" +
                "Baterai: $batteryText\n" +
                "Suhu baterai: $tempText"
        }

        box.addView(TextView(this).apply {
            text = "N O V A  •  QUICK PANEL"
            textSize = 14f
            setTextColor(android.graphics.Color.rgb(54, 230, 242))
        })

        box.addView(stats)
        refresh()

        button("REFRESH DEVICE DATA", ::refresh)

        fun navigate(tab: Int) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra("nova_tab", tab)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            startActivity(intent)
        }

        button("HOME") { navigate(0) }
        button("GAME SPACE") { navigate(1) }
        button("PROFILES") { navigate(2) }
        button("MONITOR") { navigate(3) }
        button("GAME HISTORY") { navigate(4) }
        button("TOOLS") { navigate(5) }
        button("CLOSE PANEL", ::removePanel)

        val p = WindowManager.LayoutParams(
            dp(250),
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (params.x + dp(48))
                .coerceAtMost(
                    (resources.displayMetrics.widthPixels - dp(255))
                        .coerceAtLeast(0)
                )
            y = params.y
        }

        panel = box
        try {
            wm.addView(box, p)
        } catch (_: Exception) {
            panel = null
        }
    }

    private fun removePanel() {
        panel?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        panel = null
    }

    override fun onStartCommand(
        intent: Intent?, flags: Int, startId: Int
    ): Int = START_NOT_STICKY

    override fun onDestroy() {
        removePanel()
        handle?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        handle = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

@Composable
fun ToolsPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var active by remember {
        mutableStateOf(
            context.getSharedPreferences("nova_sidebar", Context.MODE_PRIVATE)
                .getBoolean("sidebar_enabled", false)
        )
    }

    val permissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts
                .StartActivityForResult()
        ) { }

    Column(
        modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "NOVA TOOLS",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Card {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("GAMING SIDEBAR", color = Color(0xFF36E6F2))

                Text(
                    if (active) "SIDEBAR ACTIVE" else "SIDEBAR DISABLED",
                    color = if (active) Color(0xFF65E6A5) else Color.Gray
                )

                Button(
                    onClick = {
                        if (!Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            permissionLauncher.launch(intent)
                        } else {
                            try {
                                context.startService(
                                    Intent(context, NovaSidebarService::class.java)
                                )
                                active = true
                                context.getSharedPreferences(
                                    "nova_sidebar", Context.MODE_PRIVATE
                                ).edit().putBoolean("sidebar_enabled", true).apply()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context, "Gagal: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ENABLE / START SIDEBAR")
                }

                OutlinedButton(
                    onClick = {
                        context.stopService(
                            Intent(context, NovaSidebarService::class.java)
                        )
                        active = false
                        context.getSharedPreferences(
                            "nova_sidebar", Context.MODE_PRIVATE
                        ).edit().putBoolean("sidebar_enabled", false).apply()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("STOP SIDEBAR")
                }

                Text(
                    "Tombol NOVA dapat digeser di sisi layar. " +
                    "Ketuk untuk membuka panel. Izin overlay dikelola Android.",
                    color = Color(0xFF8995A9),
                    fontSize = 11.sp
                )
            }
        }
    }
}
