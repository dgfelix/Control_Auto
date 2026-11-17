package br.com.sensorauto.ui.screen.config.equipment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import br.com.sensorauto.R
import br.com.sensorauto.ui.theme.LocalLocalePreferences
import br.com.sensorauto.ui.theme.LocalThemePreferences
import br.com.sensorauto.domain.model.GearType
import br.com.sensorauto.domain.model.MotorType
import br.com.sensorauto.domain.model.Orientation
import br.com.sensorauto.ui.components.SectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun EquipmentConfigScreen(
    viewModel: EquipmentConfigViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val config = state.config
    val appearance = LocalThemePreferences.current
    val locale = LocalLocalePreferences.current
    val effectiveDarkTheme = if (appearance.followSystem) isSystemInDarkTheme()
    else appearance.darkTheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.config_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.config_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(title = stringResource(R.string.config_section_vehicle), content = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = config.vehicleBrand,
                    onValueChange = viewModel::updateVehicleBrand,
                    label = { Text(stringResource(R.string.field_brand)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = config.vehicleModel,
                    onValueChange = viewModel::updateVehicleModel,
                    label = { Text(stringResource(R.string.field_model)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = config.vehicleYear,
                    onValueChange = viewModel::updateVehicleYear,
                    label = { Text(stringResource(R.string.field_year)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = config.vehicleEngine,
                    onValueChange = viewModel::updateVehicleEngine,
                    label = { Text(stringResource(R.string.field_engine)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.field_gear),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    LabeledSwitch(
                        leftLabel = stringResource(R.string.gear_manual),
                        rightLabel = stringResource(R.string.gear_auto),
                        checked = config.gearType == GearType.AUTO,
                        onCheckedChange = { on ->
                            viewModel.updateGearType(if (on) GearType.AUTO else GearType.MANUAL)
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.field_motor),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    LabeledSwitch(
                        leftLabel = stringResource(R.string.motor_combustion),
                        rightLabel = stringResource(R.string.motor_electric),
                        checked = config.motorType == MotorType.ELECTRIC,
                        onCheckedChange = { on ->
                            viewModel.updateMotorType(if (on) MotorType.ELECTRIC else MotorType.COMBUSTION)
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.mount_orientation),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrientationCard(
                    label = stringResource(R.string.orientation_vertical),
                    rotation = 0f,
                    isSelected = config.mountOrientation == Orientation.VERTICAL,
                    onClick = { viewModel.updateOrientation(Orientation.VERTICAL) },
                    modifier = Modifier.weight(1f)
                )
                OrientationCard(
                    label = stringResource(R.string.orientation_horizontal),
                    rotation = 90f,
                    isSelected = config.mountOrientation == Orientation.HORIZONTAL,
                    onClick = { viewModel.updateOrientation(Orientation.HORIZONTAL) },
                    modifier = Modifier.weight(1f)
                )
            }
        })

        SectionCard(
            title = stringResource(R.string.config_section_device),
            headerAction = {
                TextButton(onClick = viewModel::autoDetectDevice) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.action_auto_detect), style = MaterialTheme.typography.labelSmall)
                }
            },
            content = {
                OutlinedTextField(
                    value = config.deviceBrand,
                    onValueChange = viewModel::updateDeviceBrand,
                    label = { Text(stringResource(R.string.field_brand)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = config.deviceModel,
                        onValueChange = viewModel::updateDeviceModel,
                        label = { Text(stringResource(R.string.field_model)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = config.deviceYear,
                        onValueChange = viewModel::updateDeviceYear,
                        label = { Text(stringResource(R.string.field_year)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        )

        SectionCard(title = stringResource(R.string.config_section_language)) {
            val tag = locale.languageTag
            val current = when {
                tag.startsWith("en") -> "en"
                tag.startsWith("es") -> "es"
                else -> "pt-BR"
            }
            val options = listOf(
                Triple("pt-BR", stringResource(R.string.language_chip_pt), stringResource(R.string.language_portuguese)),
                Triple("en",    stringResource(R.string.language_chip_en), stringResource(R.string.language_english)),
                Triple("es",    stringResource(R.string.language_chip_es), stringResource(R.string.language_spanish))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { (code, chip, label) ->
                    LanguageOption(
                        code = chip,
                        label = label,
                        selected = current == code,
                        onClick = { locale.update(code) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (current) {
                    "en" -> stringResource(R.string.language_desc_en)
                    "es" -> stringResource(R.string.language_desc_es)
                    else -> stringResource(R.string.language_desc_pt)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(title = stringResource(R.string.config_section_appearance)) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = appearance.followSystem,
                    role = Role.Switch,
                    onValueChange = appearance::updateFollowSystem
                ).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.appearance_follow_system), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (appearance.followSystem) stringResource(R.string.appearance_follow_system_on)
                        else stringResource(R.string.appearance_follow_system_off),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = appearance.followSystem,
                    onCheckedChange = null
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = effectiveDarkTheme,
                    enabled = !appearance.followSystem,
                    role = Role.Switch,
                    onValueChange = appearance::updateDarkTheme
                ).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.appearance_dark_theme), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (appearance.followSystem) stringResource(R.string.appearance_auto)
                        else if (effectiveDarkTheme) stringResource(R.string.appearance_dark_on)
                        else stringResource(R.string.appearance_dark_off),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = effectiveDarkTheme,
                    onCheckedChange = null,
                    enabled = !appearance.followSystem
                )
            }
            Text(
                stringResource(R.string.appearance_auto_saved),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = { viewModel.save(onSuccess = {}) },
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.action_save), style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LanguageOption(
    code: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderWidth = if (selected) 2.dp else 1.dp
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LabeledSwitch(
    leftLabel: String,
    rightLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leftLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (!checked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (!checked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = rightLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (checked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OrientationCard(
    label: String,
    rotation: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Smartphone,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}


