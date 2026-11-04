package com.nova.gamingsuite

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.provider.Settings
import android.os.Process
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.io.File
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow

private val Background = Color(0xFF080B14)
private val SurfaceDark = Color(0xFF111827)
private val Cyan = Color(0xFF36E6F2)
private val Purple = Color(0xFF9B72FF)
private val Muted = Color(0xFF8995A9)


private data class InstalledApp(
    val name: String,
    val packageName: String,
    val icon: Bitmap
)

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    val width = drawable.intrinsicWidth.coerceAtLeast(1).coerceAtMost(192)
    val height = drawable.intrinsicHeight.coerceAtLeast(1).coerceAtMost(192)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return bitmap
}

private fun scanLaunchableApps(context: Context): List<InstalledApp> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val results = context.packageManager.queryIntentActivities(intent, 0)

    return results.mapNotNull { info ->
        try {
            InstalledApp(
                name = info.loadLabel(context.packageManager).toString(),
                packageName = info.activityInfo.packageName,
                icon = drawableToBitmap(info.loadIcon(context.packageManager))
            )
        } catch (_: Exception) {
            null
        }
    }.distinctBy { it.packageName }.sortedBy { it.name.lowercase() }
}

private data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val android: String,
    val sdk: String,
    val totalRam: String,
    val availableRam: String,
    val battery: String,
    val batteryTemp: String,
    val cpu: String
)

private fun readDeviceInfo(context: Context): DeviceInfo {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo()
    manager.getMemoryInfo(memory)

    fun mb(bytes: Long) = "${bytes / (1024L * 1024L)} MB"

    val batteryIntent = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1

    val battery = if (level >= 0 && scale > 0) {
        "${level * 100 / scale}%"
    } else "N/A"

    val batteryTemp = if (temp >= 0) {
        String.format("%.1f °C", temp / 10f)
    } else "N/A"

    return DeviceInfo(
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        android = Build.VERSION.RELEASE ?: "N/A",
        sdk = Build.VERSION.SDK_INT.toString(),
        totalRam = mb(memory.totalMem),
        availableRam = mb(memory.availMem),
        battery = battery,
        batteryTemp = batteryTemp,
        cpu = Build.SUPPORTED_ABIS.firstOrNull() ?: "N/A"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 11, 20)
        window.navigationBarColor = android.graphics.Color.rgb(8, 11, 20)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Background,
                    surface = SurfaceDark,
                    primary = Cyan,
                    secondary = Purple
                )
            ) {
                NovaApp()
            }
        }
    }
}

@Composable
private fun NovaApp() {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = listOf("Home", "Games", "Profiles", "Monitor", "History", "Tools")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.SportsEsports,
        Icons.Default.Bolt,
        Icons.Default.MonitorHeart,
        Icons.Default.History,
        Icons.Default.Tune
    )

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0D1320)) {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(icons[index], contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan,
                            selectedTextColor = Cyan,
                            indicatorColor = Color(0xFF19313C),
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (selected) {
            0 -> Dashboard(Modifier.padding(padding))
            1 -> GameSpace(Modifier.padding(padding))
            2 -> GamingProfiles(Modifier.padding(padding))
            3 -> GamingMonitor(Modifier.padding(padding))
            4 -> GameHistoryPage(Modifier.padding(padding))
            else -> PlaceholderPage(tabs[selected], Modifier.padding(padding))
        }
    }
}




@Composable
private fun GamingProfiles(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("nova_game_preferences", Context.MODE_PRIVATE)
    }

    var activeProfile by remember {
        mutableStateOf(prefs.getString("active_profile", "BALANCED") ?: "BALANCED")
    }

    val profileInfo = listOf(
        Triple("ECO", "Hemat daya", "Cocok untuk penggunaan ringan dan sesi bermain santai."),
        Triple("BALANCED", "Seimbang", "Pilihan harian untuk menyeimbangkan performa dan konsumsi daya."),
        Triple("PERFORMANCE", "Prioritas performa", "Rekomendasi untuk sesi gaming yang membutuhkan respons lebih tinggi.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "GAMING PROFILES",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Pilih karakter penggunaan perangkat saat bermain.",
            color = Color.LightGray,
            fontSize = 13.sp
        )

        profileInfo.forEach { (profile, subtitle, description) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeProfile == profile)
                        Color(0xFF183044) else Color(0xFF111B2B)
                ),
                border = BorderStroke(
                    1.dp,
                    if (activeProfile == profile) Color(0xFF36D6E7)
                    else Color(0xFF263449)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = profile,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF36D6E7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    if (activeProfile == profile) {
                        Text(
                            text = "✓ PROFILE ACTIVE",
                            color = Color(0xFF65E6A5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        OutlinedButton(
                            onClick = {
                                activeProfile = profile
                                prefs.edit()
                                    .putString("active_profile", profile)
                                    .apply()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ACTIVATE PROFILE")
                        }
                    }
                }
            }
        }

        Text(
            text = "Catatan: profil ini menyimpan pilihan dan rekomendasi penggunaan. Profil belum mengubah frekuensi CPU/GPU atau melewati batas thermal Android.",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun GameSpace(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("nova_game_preferences", Context.MODE_PRIVATE)
    }

    var apps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    var search by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var favorites by remember {
        mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet())
    }
    var favoriteOnly by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<InstalledApp?>(null) }

    fun refreshApps() {
        loading = true
        scanError = null
        try {
            apps = scanLaunchableApps(context)
        } catch (e: Exception) {
            scanError = e.message ?: "Tidak dapat memindai aplikasi."
        }
        loading = false
    }

    fun toggleFavorite(packageName: String) {
        val updated = favorites.toMutableSet()
        if (!updated.add(packageName)) updated.remove(packageName)
        favorites = updated
        prefs.edit().putStringSet("favorites", updated).apply()
    }

    LaunchedEffect(Unit) { refreshApps() }

    val filtered = apps.filter {
        (!favoriteOnly || it.packageName in favorites) &&
        (it.name.contains(search, true) || it.packageName.contains(search, true))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("N O V A", color = Cyan, fontSize = 13.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("GAME MANAGEMENT", color = Color.White,
                fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text("MANAGE YOUR INSTALLED GAMES", color = Muted,
                fontSize = 10.sp, letterSpacing = 1.3.sp)
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${apps.size} aplikasi terdeteksi", color = Cyan,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${favorites.size} aplikasi favorit",
                        color = Muted, fontSize = 10.sp)
                }
                TextButton(onClick = { refreshApps() }) {
                    Icon(Icons.Default.Refresh, null, tint = Cyan)
                    Text("Scan", color = Cyan)
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Cari aplikasi atau game") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Muted,
                    focusedLabelColor = Cyan,
                    unfocusedLabelColor = Muted,
                    cursorColor = Cyan
                )
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !favoriteOnly,
                    onClick = { favoriteOnly = false },
                    label = { Text("Semua") }
                )
                FilterChip(
                    selected = favoriteOnly,
                    onClick = { favoriteOnly = true },
                    label = { Text("★ Favorit") }
                )
            }
        }

        if (loading) item { Text("Memindai aplikasi...", color = Muted) }

        if (scanError != null) {
            item { Text("Scan gagal: $scanError", color = Color(0xFFFF7777)) }
        }

        if (!loading && filtered.isEmpty()) {
            item {
                Text(
                    if (favoriteOnly) "Belum ada aplikasi favorit."
                    else if (apps.isEmpty()) "Tidak ada aplikasi launcher ditemukan."
                    else "Aplikasi tidak ditemukan.",
                    color = Muted
                )
            }
        }

        items(filtered.size) { index ->
            val app = filtered[index]
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF202C3E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = app.name,
                        modifier = Modifier.size(44.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.name, color = Color.White,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(app.packageName, color = Muted,
                            fontSize = 9.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        TextButton(
                            onClick = { selectedApp = app },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("DETAILS", color = Cyan, fontSize = 9.sp)
                        }
                    }
                    IconButton(onClick = { toggleFavorite(app.packageName) }) {
                        Icon(
                            if (app.packageName in favorites) Icons.Default.Star
                            else Icons.Default.StarBorder,
                            contentDescription = "Favorit",
                            tint = if (app.packageName in favorites) Cyan else Muted
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            context.packageManager
                                .getLaunchIntentForPackage(app.packageName)
                                ?.let { context.startActivity(it) }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF19313C),
                            contentColor = Cyan
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("OPEN", fontSize = 9.sp)
                    }
                }
            }
        }
    }

    selectedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            containerColor = SurfaceDark,
            title = { Text(app.name, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PACKAGE ID", color = Cyan, fontSize = 10.sp)
                    Text(app.packageName, color = Color.White, fontSize = 12.sp)
                    Text("Aplikasi terdeteksi dari launcher Android.",
                        color = Muted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${app.packageName}")
                    )
                    context.startActivity(intent)
                    selectedApp = null
                }) { Text("APP INFO", color = Cyan) }
            },
            dismissButton = {
                TextButton(onClick = { selectedApp = null }) {
                    Text("TUTUP", color = Muted)
                }
            }
        )
    }
}

@Composable
private fun Dashboard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var device by remember { mutableStateOf(readDeviceInfo(context)) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("N O V A", color = Cyan, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Text("GAMING SUITE", color = Color.White, fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold)
                    Text("YOUR GAMING CONTROL CENTER", color = Muted,
                        fontSize = 9.sp, letterSpacing = 1.5.sp)
                }
                Surface(color = Color(0xFF12313A),
                    shape = RoundedCornerShape(12.dp)) {
                    Text("CORE 1.1", color = Cyan,
                        modifier = Modifier.padding(12.dp), fontSize = 11.sp)
                }
            }
        }

        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, Color(0xFF24414D))
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("SYSTEM STATUS", color = Cyan, fontSize = 10.sp,
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Device detected.", color = Color.White,
                        fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${device.manufacturer} ${device.model}",
                        color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(14.dp))
                    Text("● DEVICE ONLINE", color = Color(0xFF61E6AD),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("DEVICE OVERVIEW", color = Muted, fontSize = 11.sp,
                    letterSpacing = 2.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = { device = readDeviceInfo(context) }) {
                    Icon(Icons.Default.Refresh, null, tint = Cyan)
                    Text("Refresh", color = Cyan)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("RAM AVAILABLE", device.availableRam,
                    "Total ${device.totalRam}", Purple, Modifier.weight(1f))
                MetricCard("BATTERY", device.battery,
                    device.batteryTemp, Cyan, Modifier.weight(1f))
            }
        }

        item {
            Text("DEVICE INFORMATION", color = Muted, fontSize = 11.sp,
                letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        }

        item { InfoCard("MANUFACTURER", device.manufacturer) }
        item { InfoCard("MODEL", device.model) }
        item { InfoCard("ANDROID VERSION", device.android) }
        item { InfoCard("ANDROID SDK", device.sdk) }
        item { InfoCard("CPU ARCHITECTURE", device.cpu) }

        item {
            Surface(color = SurfaceDark, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("SENSOR AVAILABILITY", color = Cyan,
                        fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("CPU temperature: N/A", color = Muted, fontSize = 12.sp)
                    Text("GPU temperature: N/A", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Sensor readings depend on system access.",
                        color = Muted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Muted, fontSize = 10.sp,
                modifier = Modifier.weight(1f))
            Text(value, color = Color.White, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetricCard(
    name: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF202C3E))
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(name, color = accent, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(value, color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold)
            Text(detail, color = Muted, fontSize = 10.sp)
        }
    }
}


private fun readCpuFrequency(): String {
    return try {
        val file = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
        if (!file.canRead()) return "N/A"

        val khz = file.readText().trim().toLongOrNull()
            ?: return "N/A"

        if (khz <= 0) "N/A"
        else String.format("%.2f GHz", khz / 1_000_000.0)
    } catch (_: Exception) {
        "N/A"
    }
}


private data class MonitorSample(
    val time: Long,
    val ramMb: Int,
    val batteryTemp: Float
)

private fun loadMonitorHistory(context: Context): List<MonitorSample> {
    val prefs = context.getSharedPreferences("nova_monitor_history", Context.MODE_PRIVATE)
    return prefs.getString("samples", "")!!
        .lines()
        .mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size != 3) return@mapNotNull null
            val time = parts[0].toLongOrNull() ?: return@mapNotNull null
            val ram = parts[1].toIntOrNull() ?: return@mapNotNull null
            val temp = parts[2].toFloatOrNull() ?: return@mapNotNull null
            MonitorSample(time, ram, temp)
        }.takeLast(300)
}

private fun saveMonitorHistory(context: Context, samples: List<MonitorSample>) {
    val content = samples.takeLast(300).joinToString("\n") {
        "${it.time},${it.ramMb},${it.batteryTemp}"
    }
    context.getSharedPreferences("nova_monitor_history", Context.MODE_PRIVATE)
        .edit().putString("samples", content).apply()
}



private data class GameSession(
    val packageName: String,
    val startTime: Long,
    val endTime: Long
)

private const val GAME_HISTORY_PREFS = "nova_game_history"
private const val GAME_HISTORY_KEY = "sessions"
private const val GAME_TRACKER_CURSOR = "tracker_cursor"
private const val GAME_ACTIVE_PACKAGE = "active_package"
private const val GAME_ACTIVE_START = "active_start"

private fun loadGameSessions(context: Context): List<GameSession> {
    val raw = context.getSharedPreferences(
        GAME_HISTORY_PREFS, Context.MODE_PRIVATE
    ).getString(GAME_HISTORY_KEY, "") ?: ""

    return raw.lines().mapNotNull { line ->
        val v = line.split(",")
        if (v.size != 3) return@mapNotNull null
        val start = v[1].toLongOrNull() ?: return@mapNotNull null
        val end = v[2].toLongOrNull() ?: return@mapNotNull null
        if (end <= start) return@mapNotNull null
        GameSession(v[0], start, end)
    }.takeLast(500)
}

private fun saveGameSessions(context: Context, sessions: List<GameSession>) {
    val raw = sessions.takeLast(500).joinToString("\n") {
        "${it.packageName},${it.startTime},${it.endTime}"
    }
    context.getSharedPreferences(GAME_HISTORY_PREFS, Context.MODE_PRIVATE)
        .edit().putString(GAME_HISTORY_KEY, raw).apply()
}

private fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    return try {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    } catch (_: Exception) {
        false
    }
}

private fun closeActiveGameSession(context: Context, end: Long) {
    val prefs = context.getSharedPreferences(
        GAME_HISTORY_PREFS, Context.MODE_PRIVATE
    )
    val pkg = prefs.getString(GAME_ACTIVE_PACKAGE, null)
    val start = prefs.getLong(GAME_ACTIVE_START, 0L)

    if (pkg != null && start > 0L && end > start) {
        val sessions = loadGameSessions(context).toMutableList()
        sessions.add(GameSession(pkg, start, end))
        saveGameSessions(context, sessions)
    }

    prefs.edit()
        .remove(GAME_ACTIVE_PACKAGE)
        .remove(GAME_ACTIVE_START)
        .apply()
}

private fun updateGameTracking(context: Context) {
    if (!hasUsageAccess(context)) return

    val historyPrefs = context.getSharedPreferences(
        GAME_HISTORY_PREFS, Context.MODE_PRIVATE
    )
    val now = System.currentTimeMillis()
    var cursor = historyPrefs.getLong(GAME_TRACKER_CURSOR, 0L)

    if (cursor <= 0L) {
        cursor = now - 15000L
        historyPrefs.edit().putLong(GAME_TRACKER_CURSOR, cursor).apply()
    }

    val favorites = context.getSharedPreferences(
        "nova_game_preferences", Context.MODE_PRIVATE
    ).getStringSet("favorites", emptySet())?.toSet() ?: emptySet()

    if (favorites.isEmpty()) {
        closeActiveGameSession(context, now)
        historyPrefs.edit().putLong(GAME_TRACKER_CURSOR, now).apply()
        return
    }

    val manager = context.getSystemService(Context.USAGE_STATS_SERVICE)
        as UsageStatsManager
    val events = manager.queryEvents(cursor, now)
    val event = UsageEvents.Event()
    var newest = cursor

    while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.timeStamp > newest) newest = event.timeStamp

        val pkg = event.packageName ?: continue
        val resumed = if (Build.VERSION.SDK_INT >= 29) {
            event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
        } else {
            event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
        }
        val paused = if (Build.VERSION.SDK_INT >= 29) {
            event.eventType == UsageEvents.Event.ACTIVITY_PAUSED ||
                event.eventType == UsageEvents.Event.ACTIVITY_STOPPED
        } else {
            event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND
        }

        val active = historyPrefs.getString(GAME_ACTIVE_PACKAGE, null)

        if (resumed) {
            if (active != null && active != pkg) {
                closeActiveGameSession(context, event.timeStamp)
            }
            if (pkg in favorites &&
                historyPrefs.getString(GAME_ACTIVE_PACKAGE, null) == null
            ) {
                historyPrefs.edit()
                    .putString(GAME_ACTIVE_PACKAGE, pkg)
                    .putLong(GAME_ACTIVE_START, event.timeStamp)
                    .apply()
            }
        } else if (paused && active == pkg) {
            closeActiveGameSession(context, event.timeStamp)
        }
    }

    historyPrefs.edit().putLong(
        GAME_TRACKER_CURSOR, maxOf(newest, now)
    ).apply()
}

private fun formatGameDuration(ms: Long): String {
    val total = (ms.coerceAtLeast(0L) / 1000L)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    return if (hours > 0) "${hours}j ${minutes}m" else "${minutes}m"
}

private const val MONITOR_CHANNEL = "nova_monitor_channel"
private const val MONITOR_NOTIFICATION_ID = 5101
private const val ACTION_STOP_MONITOR = "com.nova.gamingsuite.STOP_MONITOR"

class NovaMonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())

    private val sampler = object : Runnable {
        override fun run() {
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memory = ActivityManager.MemoryInfo()
            manager.getMemoryInfo(memory)

            val batteryIntent = registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val rawTemp = batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE, -1
            ) ?: -1

            val sample = MonitorSample(
                time = System.currentTimeMillis(),
                ramMb = (memory.availMem / (1024L * 1024L)).toInt(),
                batteryTemp = if (rawTemp >= 0) rawTemp / 10f else -1f
            )

            val history = loadMonitorHistory(this@NovaMonitorService).toMutableList()
            history.add(sample)
            saveMonitorHistory(this@NovaMonitorService, history)
            updateGameTracking(this@NovaMonitorService)

            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_MONITOR) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildMonitorNotification()

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    MONITOR_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(MONITOR_NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        getSharedPreferences("nova_monitor_history", Context.MODE_PRIVATE)
            .edit().putBoolean("service_running", true).apply()

        handler.removeCallbacks(sampler)
        handler.post(sampler)

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                MONITOR_CHANNEL,
                "NOVA Gaming Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when NOVA device monitoring is active"
                setShowBadge(false)
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildMonitorNotification(): Notification {
        val stopIntent = Intent(this, NovaMonitorService::class.java).apply {
            action = ACTION_STOP_MONITOR
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            5102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, MONITOR_CHANNEL)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("NOVA Gaming Monitor")
            .setContentText("Monitoring RAM and battery temperature every 5 seconds")
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "STOP",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(sampler)
        closeActiveGameSession(this, System.currentTimeMillis())
        getSharedPreferences("nova_monitor_history", Context.MODE_PRIVATE)
            .edit().putBoolean("service_running", false).apply()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}


@Composable
private fun GamingMonitor(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var device by remember { mutableStateOf(readDeviceInfo(context)) }
    var cpuFrequency by remember { mutableStateOf(readCpuFrequency()) }

    val history = remember {
        mutableStateListOf<MonitorSample>().apply {
            addAll(loadMonitorHistory(context))
        }
    }

    var serviceRunning by remember {
        mutableStateOf(
            context.getSharedPreferences("nova_monitor_history", Context.MODE_PRIVATE)
                .getBoolean("service_running", false)
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csv = buildString {
                    append("timestamp,available_ram_mb,battery_temperature_celsius\n")
                    history.forEach {
                        append("${it.time},${it.ramMb},${it.batteryTemp}\n")
                    }
                }
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(csv.toByteArray(Charsets.UTF_8))
                }
            } catch (_: Exception) {
                android.widget.Toast.makeText(
                    context, "CSV export failed", android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            device = readDeviceInfo(context)
            cpuFrequency = readCpuFrequency()
            serviceRunning = context.getSharedPreferences(
                "nova_monitor_history", Context.MODE_PRIVATE
            ).getBoolean("service_running", false)

            val latest = loadMonitorHistory(context)
            history.clear()
            history.addAll(latest)
            delay(5000)
        }
    }

    val maxRam = (history.maxOfOrNull { it.ramMb } ?: 1).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "GAMING MONITOR",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Background monitoring • 5-second interval", color = Muted, fontSize = 11.sp)
            }
            IconButton(onClick = {
                device = readDeviceInfo(context)
                cpuFrequency = readCpuFrequency()
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Cyan)
            }
        }

        MonitorMetric(
            "AVAILABLE RAM", device.availableRam,
            "Total memory: ${device.totalRam}", Icons.Default.Memory
        )
        MonitorMetric(
            "BATTERY", device.battery,
            "Battery temperature: ${device.batteryTemp}",
            Icons.Default.BatteryChargingFull
        )
        MonitorMetric(
            "CPU FREQUENCY", cpuFrequency,
            "CPU ABI: ${device.cpu}", Icons.Default.Speed
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF263449))
        ) {
            Column(
                Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("BACKGROUND SERVICE", color = Cyan, fontWeight = FontWeight.Bold)
                Text(
                    if (serviceRunning) "● MONITORING ACTIVE"
                    else "○ MONITORING STOPPED",
                    color = if (serviceRunning) Color(0xFF65E6A5) else Muted
                )

                Button(
                    onClick = {
                        if (!serviceRunning) {
                            if (Build.VERSION.SDK_INT >= 33 &&
                                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                (context as? ComponentActivity)?.requestPermissions(
                                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 5103
                                )
                                android.widget.Toast.makeText(
                                    context,
                                    "Izinkan notifikasi, lalu tekan START lagi.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } else {
                                try {
                                    val intent = Intent(context, NovaMonitorService::class.java)
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                    serviceRunning = true
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context, "Service gagal dimulai: ${e.message}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            context.stopService(
                                Intent(context, NovaMonitorService::class.java)
                            )
                            serviceRunning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (serviceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (serviceRunning) "STOP MONITORING" else "START BACKGROUND MONITOR")
                }

                Text(
                    "Service memakai notifikasi permanen. Android tetap dapat menghentikannya karena pembatasan sistem atau baterai.",
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF263449))
        ) {
            Column(
                Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("RAM HISTORY", color = Cyan, fontWeight = FontWeight.Bold)
                Text("${history.size} samples saved", color = Muted, fontSize = 11.sp)

                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    history.takeLast(40).forEach { sample ->
                        val fraction = (sample.ramMb.toFloat() / maxRam).coerceIn(0.05f, 1f)
                        Box(
                            Modifier.weight(1f)
                                .fillMaxHeight(fraction)
                                .background(Cyan, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF263449))
        ) {
            Column(
                Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("BATTERY TEMPERATURE HISTORY", color = Cyan, fontWeight = FontWeight.Bold)
                history.takeLast(8).reversed().forEach { sample ->
                    Text(
                        "${sample.time} • ${
                            if (sample.batteryTemp >= 0) "${sample.batteryTemp} °C" else "N/A"
                        }",
                        color = Muted,
                        fontSize = 11.sp
                    )
                }
                if (history.isEmpty()) Text("No samples yet", color = Muted)
            }
        }

        OutlinedButton(
            onClick = { exportLauncher.launch("NOVA_Monitor_History.csv") },
            modifier = Modifier.fillMaxWidth(),
            enabled = history.isNotEmpty()
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("EXPORT CSV")
        }

        OutlinedButton(
            onClick = {
                if (serviceRunning) {
                    context.stopService(Intent(context, NovaMonitorService::class.java))
                    serviceRunning = false
                }
                history.clear()
                saveMonitorHistory(context, history)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = history.isNotEmpty()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("CLEAR HISTORY")
        }
    }
}


@Composable
private fun GameHistoryPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var sessions by remember { mutableStateOf(loadGameSessions(context)) }
    var access by remember { mutableStateOf(hasUsageAccess(context)) }

    fun refresh() {
        sessions = loadGameSessions(context)
        access = hasUsageAccess(context)
    }

    val grouped = sessions.groupBy { it.packageName }
    val totalMs = sessions.sumOf { it.endTime - it.startTime }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("N O V A", color = Cyan, fontSize = 13.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("GAME HISTORY", color = Color.White,
                fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text("AUTOMATIC PLAYTIME TRACKER", color = Muted,
                fontSize = 10.sp, letterSpacing = 1.2.sp)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.padding(16.dp)) {
                    Text("TOTAL PLAYTIME", color = Muted, fontSize = 11.sp)
                    Text(formatGameDuration(totalMs), color = Cyan,
                        fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("${sessions.size} sesi tercatat",
                        color = Muted, fontSize = 12.sp)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.padding(14.dp)) {
                    Text("USAGE ACCESS", color = Color.White,
                        fontWeight = FontWeight.Bold)
                    Text(
                        if (access) "Aktif — pelacakan tersedia saat Monitor berjalan"
                        else "Belum aktif — izinkan akses penggunaan aplikasi",
                        color = if (access) Cyan else Color(0xFFFFB86B),
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BUKA PENGATURAN")
                    }
                }
            }
        }

        item {
            Text("PLAYTIME PER GAME", color = Cyan,
                fontWeight = FontWeight.Bold)
        }

        if (grouped.isEmpty()) {
            item {
                Text("Belum ada sesi. Aktifkan Usage Access, favoritkan game, lalu jalankan Monitor.",
                    color = Muted, fontSize = 12.sp)
            }
        } else {
            items(grouped.size) { index ->
                val entry = grouped.entries.sortedByDescending { e ->
                    e.value.sumOf { it.endTime - it.startTime }
                }[index]
                val duration = entry.value.sumOf { it.endTime - it.startTime }
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(entry.key, color = Color.White,
                            fontWeight = FontWeight.Bold)
                        Text("${formatGameDuration(duration)} • ${entry.value.size} sesi",
                            color = Cyan, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text("RECENT SESSIONS", color = Cyan,
                fontWeight = FontWeight.Bold)
        }

        items(sessions.sortedByDescending { it.endTime }.take(30).size) { index ->
            val recent = sessions.sortedByDescending { it.endTime }.take(30)[index]
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.padding(12.dp)) {
                    Text(recent.packageName, color = Color.White, fontSize = 12.sp)
                    Text(
                        "${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(recent.startTime))} • ${formatGameDuration(recent.endTime - recent.startTime)}",
                        color = Muted, fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { refresh() }) {
                    Text("REFRESH")
                }
                OutlinedButton(onClick = {
                    saveGameSessions(context, emptyList())
                    refresh()
                }) {
                    Text("CLEAR HISTORY")
                }
            }
        }
    }
}

@Composable
private fun MonitorMetric(
    title: String,
    value: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        border = BorderStroke(1.dp, Color(0xFF263449))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    title,
                    color = Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    detail,
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PlaceholderPage(title: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("N O V A", color = Cyan, fontSize = 13.sp,
                letterSpacing = 4.sp)
            Spacer(Modifier.height(10.dp))
            Text(title, color = Color.White, fontSize = 26.sp,
                fontWeight = FontWeight.Bold)
            Text("Module initialization is planned for a future milestone.",
                color = Muted, fontSize = 12.sp)
        }
    }
}
