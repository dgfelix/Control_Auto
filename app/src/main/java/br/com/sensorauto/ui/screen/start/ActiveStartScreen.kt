package br.com.sensorauto.ui.screen.start

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.sensorauto.R
import br.com.sensorauto.ui.components.SensorChart
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActiveStartScreen(viewModel: ActiveStartViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

        if (cameraGranted) {
            viewModel.setupCamera(context, lifecycleOwner)
        }

        viewModel.startSession(
            hasLocationPermission = locationGranted,
            hasCameraPermission = cameraGranted
        )
    }

    // Pede permissão de localização ao entrar na tela (modo preview) para o mapa
    // já aparecer centralizado no usuário mesmo antes de iniciar a sessão.
    val previewLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startPreviewLocationUpdates()
    }

    LaunchedEffect(Unit) {
        viewModel.reloadConfig()

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            viewModel.setupCamera(context, lifecycleOwner)
        }

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            viewModel.startPreviewLocationUpdates()
        } else {
            previewLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun onStartClicked() {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission && hasCameraPermission) {
            viewModel.startSession(hasLocationPermission = true, hasCameraPermission = true)
        } else {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.status == StartStatus.RUNNING || state.status == StartStatus.PAUSED) {
            SessionActiveTag(status = state.status)
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.active_session_id_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.sessionName.ifEmpty { stringResource(R.string.common_dash) },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.active_elapsed_time_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.elapsedMs.toTimestamp(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth()) {
                    val (distValue, distUnit) = formatDistance(state.distanceKm)
                    TelemetryCell(
                        icon = Icons.Default.Route,
                        label = stringResource(R.string.active_distance),
                        value = distValue,
                        unit = distUnit,
                        modifier = Modifier.weight(1f)
                    )
                    CellDivider()
                    TelemetryCell(
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.active_speed),
                        value = "%.0f".format(state.speedKmh),
                        unit = stringResource(R.string.unit_kmh),
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth()) {
                    val dash = stringResource(R.string.common_dash)
                    TelemetryCell(
                        icon = Icons.Default.Sensors,
                        label = stringResource(R.string.active_accel),
                        value = state.accelSamples.lastOrNull()
                            ?.let { "%.2f".format(it[0]) } ?: dash,
                        unit = stringResource(R.string.unit_ms2),
                        modifier = Modifier.weight(1f)
                    )
                    CellDivider()
                    TelemetryCell(
                        icon = Icons.Default.BatteryFull,
                        label = stringResource(R.string.active_battery),
                        value = "${state.batteryPercent}",
                        unit = stringResource(R.string.unit_percent),
                        valueColor = if (state.batteryPercent < 20) Color(0xFFEB5757)
                                     else Color(0xFF27AE60),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        val accelColors   = listOf(Color(0xFFF5A623), Color(0xFFEB5757), Color(0xFF27AE60))
        val gyroColors    = listOf(Color(0xFF00658A), Color(0xFF27AE60), Color(0xFF9C27B0))
        val magnetoColors = listOf(Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFFE91E63))
        val lastAccel    = state.accelSamples.lastOrNull()
        val lastGyro     = state.gyroSamples.lastOrNull()
        val lastMagneto  = state.magnetoSamples.lastOrNull()

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp
        ) {
            Column {
                SensorChart(
                    title = stringResource(R.string.active_chart_accel),
                    icon = Icons.Default.StackedLineChart,
                    series = listOf(
                        state.accelSamples.map { it[0] },
                        state.accelSamples.map { it[1] },
                        state.accelSamples.map { it[2] }
                    ),
                    seriesColors = accelColors,
                    yRange = -20f..20f,
                    axisLabels = listOf("X", "Y", "Z"),
                    axisValues = lastAccel?.toList(),
                    axisUnit = stringResource(R.string.unit_ms2),
                    enabled = state.accelEnabled
                )

                HorizontalDivider()

                SensorChart(
                    title = stringResource(R.string.active_chart_gyro),
                    icon = Icons.Default.Cached,
                    series = listOf(
                        state.gyroSamples.map { it[0] },
                        state.gyroSamples.map { it[1] },
                        state.gyroSamples.map { it[2] }
                    ),
                    seriesColors = gyroColors,
                    yRange = -10f..10f,
                    axisLabels = listOf(
                        stringResource(R.string.axis_roll),
                        stringResource(R.string.axis_pitch),
                        stringResource(R.string.axis_yaw)
                    ),
                    axisValues = lastGyro?.toList(),
                    axisUnit = stringResource(R.string.unit_rads),
                    enabled = state.gyroEnabled
                )

                HorizontalDivider()

                SensorChart(
                    title = stringResource(R.string.active_chart_magneto),
                    icon = Icons.Default.Explore,
                    series = listOf(
                        state.magnetoSamples.map { it[0] },
                        state.magnetoSamples.map { it[1] },
                        state.magnetoSamples.map { it[2] }
                    ),
                    seriesColors = magnetoColors,
                    yRange = -100f..100f,
                    axisLabels = listOf("X", "Y", "Z"),
                    axisValues = lastMagneto?.toList(),
                    axisUnit = stringResource(R.string.unit_ut),
                    enabled = state.magnetoEnabled
                )
            }
        }

        // Card do Mapa
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.active_trail_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TrailMap(
                    trail = state.locationTrail,
                    lastLat = state.lastLat,
                    lastLng = state.lastLng,
                    distanceKm = state.distanceKm,
                    enabled = state.gpsEnabled
                )
            }
        }

        HorizontalDivider()
        
        if (state.status == StartStatus.RUNNING && state.photoCaptureActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.active_photo_capture_on),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state.status) {
                StartStatus.IDLE -> {
                    Button(
                        onClick = ::onStartClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_start_session), style = MaterialTheme.typography.titleMedium)
                    }
                }
                StartStatus.RUNNING -> {
                    OutlinedButton(
                        onClick = viewModel::pauseSession,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.action_pause),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                    Button(
                        onClick = viewModel::stopSession,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.action_stop),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
                StartStatus.PAUSED -> {
                    OutlinedButton(
                        onClick = viewModel::stopSession,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.action_stop),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                    Button(
                        onClick = viewModel::resumeSession,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.action_resume),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TelemetryCell(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .height(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun CellDivider() {
    Surface(
        modifier = Modifier
            .width(1.dp)
            .height(80.dp)
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    ) {}
}

@Composable
private fun TrailMap(
    trail: List<Pair<Double, Double>>,
    lastLat: Double?,
    lastLng: Double?,
    distanceKm: Float,
    enabled: Boolean = true
) {
    val defaultPosition = LatLng(-15.78, -47.93) // Brasília como fallback
    val closeZoom = 18f
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            if (lastLat != null && lastLng != null) LatLng(lastLat, lastLng) else defaultPosition,
            closeZoom
        )
    }

    LaunchedEffect(lastLat, lastLng) {
        if (lastLat != null && lastLng != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(lastLat, lastLng), closeZoom)
            )
        }
    }

    val trailPoints = trail.map { LatLng(it.first, it.second) }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = lastLat != null),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false
            )
        ) {
            if (trailPoints.size >= 2) {
                Polyline(
                    points = trailPoints,
                    color = Color(0xFFF5A623),
                    width = 8f
                )
            }
        }

        val (distValue, distUnit) = formatDistance(distanceKm)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            shape = MaterialTheme.shapes.large,
            color = (if (enabled) Color(0xFFF5A623) else Color.Gray).copy(alpha = 0.92f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$distValue $distUnit",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!enabled) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Gray.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = Color.Gray.copy(alpha = 0.95f)
                ) {
                    Text(
                        text = stringResource(R.string.sensor_gps_disabled),
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionActiveTag(status: StartStatus) {
    val isRunning = status == StartStatus.RUNNING
    val bg = if (isRunning) Color(0xFF27AE60) else Color(0xFFF5A623)
    val label = if (isRunning) stringResource(R.string.session_active_running)
    else stringResource(R.string.session_active_paused)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = bg.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(bg, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                color = bg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Formata distância preservando precisão em metros.
 *  < 1 km  → "857" m
 *  ≥ 1 km  → "1.587" km (3 casas = metros reais)
 */
private fun formatDistance(distanceKm: Float): Pair<String, String> {
    val meters = distanceKm * 1000f
    return if (meters < 1000f) {
        "%.0f".format(meters) to "m"
    } else {
        "%.3f".format(distanceKm) to "km"
    }
}

private fun Long.toTimestamp(): String {
    val totalSeconds = this / 1000
    val centis = (this % 1000) / 10
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d.%02d".format(h, m, s, centis)
}
