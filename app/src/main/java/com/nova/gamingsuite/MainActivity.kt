package com.nova.gamingsuite

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF080B14)
private val SurfaceDark = Color(0xFF111827)
private val Cyan = Color(0xFF36E6F2)
private val Purple = Color(0xFF9B72FF)
private val Muted = Color(0xFF8995A9)

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
    val tabs = listOf("Home", "Games", "Monitor", "Tools")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.SportsEsports,
        Icons.Default.MonitorHeart,
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
            else -> PlaceholderPage(tabs[selected], Modifier.padding(padding))
        }
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
