package br.com.sensorauto.ui.screen.config.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.sensorauto.domain.model.SensorType
import br.com.sensorauto.ui.components.OutlineButton
import br.com.sensorauto.ui.components.PrimaryButton
import br.com.sensorauto.ui.components.SectionCard
import br.com.sensorauto.ui.components.SyncedSlider
import br.com.sensorauto.ui.theme.GreenSuccess
import br.com.sensorauto.ui.theme.RedAlert
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SensorConfigScreen(
    onBack: () -> Unit,
    viewModel: SensorConfigViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configuração de Sensores",
                style = MaterialTheme.typography.headlineSmall
            )

            state.sensors.forEach { item ->
                SectionCard(title = item.label, icon = Icons.Default.Sensors) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AvailabilityBadge(available = item.isAvailable)
                        Switch(
                            checked = item.isEnabled,
                            onCheckedChange = { viewModel.toggleSensor(item.type, it) },
                            enabled = item.isAvailable,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    if (item.isEnabled && item.isAvailable) {
                        Spacer(Modifier.height(8.dp))
                        if (item.isIntervalSensor) {
                            CameraIntervalSlider(
                                value = item.frequencyHz,
                                onValueChange = { viewModel.updateFrequency(item.type, it) }
                            )
                        } else {
                            SyncedSlider(
                                label = "Hz",
                                value = item.frequencyHz,
                                range = item.freqRange,
                                onValueChange = { viewModel.updateFrequency(item.type, it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            OutlineButton(
                text = "Calibrar Nível",
                onClick = { /* fase futura: abre BubbleLevelScreen */ }
            )

            PrimaryButton(
                text = "Salvar",
                onClick = {
                    viewModel.save {
                        scope.launch { snackbarHostState.showSnackbar("Configurações salvas com sucesso") }
                    }
                },
                enabled = !state.isSaving
            )

            Spacer(Modifier.height(16.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CameraIntervalSlider(value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Intervalo entre fotos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%.1f seg".format(value / 10f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 5f..100f,
            steps = 94,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0.5s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("10.0s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AvailabilityBadge(available: Boolean) {
    Surface(
        color = if (available) GreenSuccess.copy(alpha = 0.15f) else RedAlert.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = if (available) "Disponível" else "Indisponível",
            color = if (available) GreenSuccess else RedAlert,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
