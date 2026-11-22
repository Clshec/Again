package com.example.handycam.presentation.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.handycam.presentation.cameracontrol.CameraControlViewModel
import java.util.Locale
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Camera controls on the HOME screen: a native companion to the existing
 * browser control panel. Both surfaces update the same StreamStateHolder.
 * The preview screen and Web Control Server are deliberately left intact.
 */
@Composable
fun CameraControlsSection(
    viewModel: CameraControlViewModel,
    cameraStreamActive: Boolean,
) {
    val streaming by viewModel.isStreaming.collectAsState()
    val useAvc by viewModel.useAvc.collectAsState()
    val manualSupported by viewModel.manualExposureSupported.collectAsState()
    val manual by viewModel.manualExposureEnabled.collectAsState()
    val iso by viewModel.iso.collectAsState()
    val isoMin by viewModel.isoMin.collectAsState()
    val isoMax by viewModel.isoMax.collectAsState()
    val shutter by viewModel.shutterSpeedNs.collectAsState()
    val shutterMin by viewModel.shutterMinNs.collectAsState()
    val shutterMax by viewModel.shutterMaxNs.collectAsState()
    val autoExposure by viewModel.autoExposure.collectAsState()
    val exposure by viewModel.exposure.collectAsState()
    val exposureMin by viewModel.exposureMin.collectAsState()
    val exposureMax by viewModel.exposureMax.collectAsState()
    val autoFocus by viewModel.autoFocus.collectAsState()
    val focus by viewModel.focus.collectAsState()
    val minFocusDistance by viewModel.minFocusDistance.collectAsState()
    val whiteBalance by viewModel.whiteBalance.collectAsState()
    val whiteBalanceLocked by viewModel.whiteBalanceLocked.collectAsState()
    val supportedWhiteBalance by viewModel.availableWhiteBalanceModes.collectAsState()
    val antiBanding by viewModel.antiBanding.collectAsState()
    val supportedAntiBanding by viewModel.availableAntiBandingModes.collectAsState()
    val zoom by viewModel.zoom.collectAsState()
    val torch by viewModel.torchEnabled.collectAsState()
    var expanded by rememberSaveable { mutableStateOf(true) }
    val active = streaming && cameraStreamActive

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Camera Controls",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide" else "Show")
                }
            }
            if (expanded) {
                Text(
                    if (active) "Changes apply to the running camera stream without restarting OBS."
                    else "Start a camera stream to enable sensor controls. No preview or web server is required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SettingsToggle("Torch", torch, active, onToggle = {
                    viewModel.toggleTorch()
                })

                HorizontalDivider()
                SettingsToggle(
                    "Manual ISO + Shutter",
                    manual,
                    active && manualSupported,
                    onToggle = viewModel::setManualExposureEnabled
                )
                if (active && !manualSupported) {
                    Text(
                        "This camera or streaming mode does not expose Camera2 MANUAL_SENSOR.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (manual && manualSupported) {
                    val minimumIso = isoMin.coerceAtLeast(1)
                    val maximumIso = isoMax.coerceAtLeast(minimumIso)
                    var isoDraft by remember { mutableStateOf(iso.toString()) }
                    var editingIso by remember { mutableStateOf(false) }
                    LaunchedEffect(iso) {
                        if (!editingIso) isoDraft = iso.toString()
                    }
                    BoundedSlider(
                        title = "ISO",
                        value = iso.toFloat(),
                        low = minimumIso.toFloat(),
                        high = maximumIso.toFloat(),
                        enabled = active && maximumIso > minimumIso,
                        formatValue = { it.roundToInt().toString() },
                        onCommit = {
                            viewModel.setIso(it.roundToInt().coerceIn(minimumIso, maximumIso))
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = isoDraft,
                            onValueChange = { value ->
                                isoDraft = value.filter { it.isDigit() }.take(6)
                                editingIso = true
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Exact ISO ($minimumIso–$maximumIso)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = active,
                            isError = isoDraft.toIntOrNull()?.let {
                                it !in minimumIso..maximumIso
                            } ?: false
                        )
                        Button(
                            enabled = active && (isoDraft.toIntOrNull()?.let { it in minimumIso..maximumIso } == true),
                            onClick = {
                                val next = isoDraft.toIntOrNull() ?: return@Button
                                viewModel.setIso(next)
                                editingIso = false
                            }
                        ) { Text("Set") }
                    }

                    val minimumShutter = shutterMin.coerceAtLeast(1L)
                    val maximumShutter = shutterMax.coerceAtLeast(minimumShutter)
                    var shutterDraft by remember { mutableStateOf(shutterLabel(shutter)) }
                    var editingShutter by remember { mutableStateOf(false) }
                    LaunchedEffect(shutter) {
                        if (!editingShutter) shutterDraft = shutterLabel(shutter)
                    }
                    BoundedSlider(
                        title = "Shutter",
                        value = shutterPosition(shutter, minimumShutter, maximumShutter),
                        low = 0f,
                        high = 1f,
                        enabled = active && maximumShutter > minimumShutter,
                        formatValue = { position ->
                            shutterLabel(shutterFromPosition(position, minimumShutter, maximumShutter))
                        },
                        onCommit = { position ->
                            viewModel.setShutterSpeedNs(
                                shutterFromPosition(position, minimumShutter, maximumShutter)
                            )
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = shutterDraft,
                            onValueChange = { shutterDraft = it.take(24); editingShutter = true },
                            modifier = Modifier.weight(1f),
                            label = { Text("Shutter (1/50, 20ms, 0.02s)") },
                            singleLine = true,
                            enabled = active,
                            isError = parseShutter(shutterDraft)?.let {
                                it !in minimumShutter..maximumShutter
                            } ?: false
                        )
                        Button(
                            enabled = active && (
                                parseShutter(shutterDraft)?.let {
                                    it in minimumShutter..maximumShutter
                                } == true
                            ),
                            onClick = {
                                val next = parseShutter(shutterDraft) ?: return@Button
                                viewModel.setShutterSpeedNs(next)
                                editingShutter = false
                            }
                        ) { Text("Set") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50, 100, 250, 500).forEach { denominator ->
                            val speed = 1_000_000_000L / denominator
                            if (speed in minimumShutter..maximumShutter) {
                                OutlinedButton(
                                    enabled = active,
                                    onClick = { viewModel.setShutterSpeedNs(speed) }
                                ) { Text("1/$denominator") }
                            }
                        }
                    }
                } else if (!manual) {
                    SettingsToggle(
                        "AE lock",
                        !autoExposure,
                        active,
                        onToggle = { viewModel.setAutoExposure(!it) }
                    )
                    if (exposureMax > exposureMin) {
                        BoundedSlider(
                            title = "Exposure compensation (EV)",
                            value = exposure.toFloat(),
                            low = exposureMin.toFloat(),
                            high = exposureMax.toFloat(),
                            enabled = active,
                            formatValue = { it.roundToInt().toString() },
                            onCommit = { viewModel.setExposure(it.roundToInt()) }
                        )
                    }
                }

                HorizontalDivider()
                val whiteBalanceLabels = mapOf(
                    1 to "Auto", 2 to "Incandescent", 3 to "Fluorescent",
                    4 to "Warm fluorescent", 5 to "Daylight",
                    6 to "Cloudy daylight", 7 to "Twilight", 8 to "Shade"
                )
                CameraOptionMenu(
                    title = "White balance",
                    current = whiteBalance,
                    options = supportedWhiteBalance.map {
                        it to (whiteBalanceLabels[it] ?: "Mode $it")
                    },
                    enabled = active && !whiteBalanceLocked,
                    onChoose = viewModel::setWhiteBalance
                )
                SettingsToggle(
                    "AWB lock",
                    whiteBalanceLocked,
                    active && whiteBalance == 1,
                    onToggle = viewModel::setWhiteBalanceLocked
                )

                CameraOptionMenu(
                    title = "Anti-flicker",
                    current = antiBanding,
                    options = supportedAntiBanding.map {
                        it to when (it) {
                            0 -> "Off"
                            1 -> "50 Hz"
                            2 -> "60 Hz"
                            3 -> "Auto"
                            else -> "Mode $it"
                        }
                    },
                    enabled = active,
                    onChoose = viewModel::setAntiBanding
                )

                HorizontalDivider()
                val focusSupported = useAvc && minFocusDistance > 0f
                SettingsToggle(
                    "Auto focus",
                    autoFocus,
                    active && focusSupported,
                    onToggle = viewModel::setAutoFocus
                )
                if (active && !focusSupported) {
                    Text(
                        "Manual focus is available in H.264 on compatible lenses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!autoFocus && focusSupported) {
                    BoundedSlider(
                        title = "Manual focus",
                        value = focus.toFloat(),
                        low = 0f,
                        high = 100f,
                        enabled = active,
                        formatValue = { "${it.roundToInt()}%" },
                        onCommit = { viewModel.setFocus(it.roundToInt()) }
                    )
                }
                BoundedSlider(
                    title = "Zoom",
                    value = zoom * 100f,
                    low = 0f,
                    high = 100f,
                    enabled = active,
                    formatValue = { "${it.roundToInt()}%" },
                    onCommit = { viewModel.setZoom(it / 100f) }
                )
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
    }
}

@Composable
private fun CameraOptionMenu(
    title: String,
    current: Int,
    options: List<Pair<Int, String>>,
    enabled: Boolean,
    onChoose: (Int) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Box {
            OutlinedButton(
                onClick = { open = true },
                enabled = enabled && options.isNotEmpty()
            ) {
                Text(options.firstOrNull { it.first == current }?.second ?: "Unavailable")
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.distinctBy { it.first }.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            open = false
                            onChoose(value)
                        }
                    )
                }
            }
        }
    }
}

/** Apply slider values at the end of a drag to avoid flooding Camera2 requests. */
@Composable
private fun BoundedSlider(
    title: String,
    value: Float,
    low: Float,
    high: Float,
    enabled: Boolean,
    formatValue: (Float) -> String,
    onCommit: (Float) -> Unit
) {
    val safeHigh = maxOf(low, high)
    var dragging by remember { mutableStateOf(false) }
    var position by remember { mutableFloatStateOf(value.coerceIn(low, safeHigh)) }
    LaunchedEffect(value, low, safeHigh) {
        if (!dragging) position = value.coerceIn(low, safeHigh)
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(formatValue(position), style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = position.coerceIn(low, maxOf(low + 0.0001f, safeHigh)),
            onValueChange = { position = it; dragging = true },
            onValueChangeFinished = {
                onCommit(position.coerceIn(low, safeHigh))
                dragging = false
            },
            valueRange = low..maxOf(low + 0.0001f, safeHigh),
            enabled = enabled && safeHigh > low
        )
    }
}

private fun shutterPosition(value: Long, minimum: Long, maximum: Long): Float {
    if (maximum <= minimum) return 0f
    val clamped = value.coerceIn(minimum, maximum)
    return ((ln(clamped.toDouble()) - ln(minimum.toDouble())) /
        (ln(maximum.toDouble()) - ln(minimum.toDouble()))).toFloat().coerceIn(0f, 1f)
}

private fun shutterFromPosition(value: Float, minimum: Long, maximum: Long): Long {
    if (maximum <= minimum) return minimum
    return exp(
        ln(minimum.toDouble()) +
            value.coerceIn(0f, 1f) * (ln(maximum.toDouble()) - ln(minimum.toDouble()))
    ).roundToLong().coerceIn(minimum, maximum)
}

private fun shutterLabel(ns: Long): String {
    if (ns <= 0L) return "Auto"
    val seconds = ns / 1_000_000_000.0
    return if (seconds <= 1.0) {
        "1/" + (1.0 / seconds).roundToInt()
    } else {
        String.format(Locale.US, "%.2fs", seconds)
    }
}

private fun parseShutter(input: String): Long? {
    val value = input.trim().lowercase(Locale.US)
    val seconds = when {
        value.startsWith("1/") ->
            value.substring(2).toDoubleOrNull()?.takeIf { it > 0.0 }?.let { 1.0 / it }
        value.endsWith("ms") -> value.removeSuffix("ms").toDoubleOrNull()?.div(1000.0)
        value.endsWith("s") -> value.removeSuffix("s").toDoubleOrNull()
        else -> value.toDoubleOrNull()
    }
    return seconds?.takeIf { it.isFinite() && it > 0.0 && it < 1000.0 }
        ?.let { (it * 1_000_000_000.0).roundToLong() }
}
