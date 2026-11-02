package com.nova.gamingsuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF080B14)
private val SurfaceDark = Color(0xFF111827)
private val Cyan = Color(0xFF36E6F2)
private val Purple = Color(0xFF9B72FF)
private val Muted = Color(0xFF8995A9)

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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("N O V A", color = Cyan, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Text("GAMING SUITE", color = Color.White, fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold)
                    Text("YOUR GAMING CONTROL CENTER",
                        color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
                }
                Surface(
                    color = Color(0xFF12313A),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CORE 1.0", color = Cyan,
                        modifier = Modifier.padding(12.dp), fontSize = 11.sp)
                }
            }
        }

        item {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24414D))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("SYSTEM STATUS", color = Cyan, fontSize = 10.sp,
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Welcome to NOVA.",
                        color = Color.White, fontSize = 23.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Your device. Your game. Your control.",
                        color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF163C32),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("● READY", color = Color(0xFF61E6AD),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Monitoring belum dimulai", color = Muted, fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            Text("DEVICE OVERVIEW", color = Muted, fontSize = 11.sp,
                letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("CPU", "—", "Temperature", Cyan, Modifier.weight(1f))
                MetricCard("GPU", "—", "Temperature", Purple, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("RAM", "—", "Available", Purple, Modifier.weight(1f))
                MetricCard("BATTERY", "—", "Temperature", Cyan, Modifier.weight(1f))
            }
        }

        item {
            Surface(color = SurfaceDark, shape = RoundedCornerShape(18.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Radar, null, tint = Cyan)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("NOVA ENGINE", color = Color.White,
                            fontWeight = FontWeight.Bold)
                        Text("Detection modules will be initialized in upcoming milestones.",
                            color = Muted, fontSize = 11.sp)
                    }
                }
            }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF202C3E))
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(name, color = accent, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            Text(value, color = Color.White, fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold)
            Text(detail, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PlaceholderPage(title: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("N O V A", color = Cyan, fontSize = 13.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(10.dp))
            Text(title, color = Color.White, fontSize = 26.sp,
                fontWeight = FontWeight.Bold)
            Text("Module initialization is planned for a future milestone.",
                color = Muted, fontSize = 12.sp)
        }
    }
}
