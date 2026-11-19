package br.com.sensorauto.ui.screen.config.bubble

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.sensorauto.R
import br.com.sensorauto.ui.components.PrimaryButton
import br.com.sensorauto.ui.components.SectionCard
import br.com.sensorauto.ui.theme.GreenSuccess
import br.com.sensorauto.ui.theme.RedAlert
import br.com.sensorauto.ui.theme.SensorAutoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun BubbleLevelScreen(
    onBack: () -> Unit,
    viewModel: BubbleLevelViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display.rotation
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
    }

    LaunchedEffect(rotation, configuration.orientation) {
        viewModel.updateDisplayRotation(rotation)
    }

    BubbleLevelContent(
        state = state,
        onBack = onBack,
        onCalibrate = { viewModel.calibrate() },
    )
}

@Composable
fun BubbleLevelContent(
    state: BubbleLevelUiState,
    onBack: () -> Unit,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        BubbleLevelLandscapeContent(
            state = state,
            onBack = onBack,
            onCalibrate = onCalibrate,
            modifier = modifier,
        )
    } else {
        BubbleLevelPortraitContent(
            state = state,
            onBack = onBack,
            onCalibrate = onCalibrate,
            modifier = modifier,
        )
    }
}

@Composable
private fun BubbleLevelPortraitContent(
    state: BubbleLevelUiState,
    onBack: () -> Unit,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderRow(onBack = onBack)

        LevelStatusBadge(isLevel = state.isLevel)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            BubbleLevelWidget(
                pitchDeg = state.pitchDeg,
                rollDeg = state.rollDeg,
                isLevel = state.isLevel,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
            )
        }

        AxisCard(pitchDeg = state.pitchDeg, rollDeg = state.rollDeg)

        Spacer(Modifier.height(12.dp))

        PrimaryButton(
            text = stringResource(R.string.action_calibrate_zero),
            onClick = onCalibrate,
        )
    }
}

@Composable
private fun BubbleLevelLandscapeContent(
    state: BubbleLevelUiState,
    onBack: () -> Unit,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                BubbleLevelWidget(
                    pitchDeg = state.pitchDeg,
                    rollDeg = state.rollDeg,
                    isLevel = state.isLevel,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LevelStatusBadge(isLevel = state.isLevel)

                CompactAxisCard(pitchDeg = state.pitchDeg, rollDeg = state.rollDeg)

                PrimaryButton(
                    text = stringResource(R.string.action_calibrate_zero),
                    onClick = onCalibrate,
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
            )
        }
        Text(
            text = stringResource(R.string.bubble_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AxisCard(pitchDeg: Float, rollDeg: Float) {
    SectionCard(title = stringResource(R.string.bubble_axis_reading)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            AxisReadout(label = stringResource(R.string.bubble_pitch_h), value = pitchDeg)
            AxisReadout(label = stringResource(R.string.bubble_roll_l), value = rollDeg)
        }
    }
}

@Composable
private fun CompactAxisCard(pitchDeg: Float, rollDeg: Float) {
    MaterialSurface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.bubble_pitch_h),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "%.1f°".format(pitchDeg),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.bubble_roll_l),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "%.1f°".format(rollDeg),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun LevelStatusBadge(isLevel: Boolean) {
    val color = if (isLevel) GreenSuccess else RedAlert
    val icon = if (isLevel) Icons.Default.CheckCircle else Icons.Default.Warning
    val text = if (isLevel) stringResource(R.string.bubble_leveled) else stringResource(R.string.bubble_adjust)

    MaterialSurface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AxisReadout(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "%.1f°".format(value),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(name = "Portrait", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun BubbleLevelPreviewPortrait() {
    SensorAutoTheme {
        BubbleLevelContent(
            state = BubbleLevelUiState(pitchDeg = 0.2f, rollDeg = -0.1f, isLevel = true),
            onBack = {},
            onCalibrate = {},
        )
    }
}

@Preview(name = "Landscape", showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun BubbleLevelPreviewLandscape() {
    SensorAutoTheme {
        BubbleLevelLandscapeContent(
            state = BubbleLevelUiState(pitchDeg = 5.4f, rollDeg = -12.1f, isLevel = false),
            onBack = {},
            onCalibrate = {},
        )
    }
}
