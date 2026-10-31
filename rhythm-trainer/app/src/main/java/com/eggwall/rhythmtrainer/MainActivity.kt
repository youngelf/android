package com.eggwall.rhythmtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private var showHelpState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppI18n.initFromIntent(this, intent)
        showHelpState.value = intent?.getBooleanExtra("show_help", false) == true ||
            intent?.getBooleanExtra("demo_help", false) == true
        enableEdgeToEdge()
        setContent {
            RhythmTrainerTheme {
                RhythmTrainerApp(
                    showHelp = showHelpState.value,
                    onShowHelpChange = { showHelpState.value = it }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        AppI18n.initFromIntent(this, intent)
        showHelpState.value = intent.getBooleanExtra("show_help", false) ||
            intent.getBooleanExtra("demo_help", false)
    }
}

private val AppColorScheme = lightColorScheme(
    primary = Color(0xFF1E3A8A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun RhythmTrainerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RhythmTrainerApp(
    showHelp: Boolean = false,
    onShowHelpChange: (Boolean) -> Unit = {},
    viewModel: RhythmViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    if (showHelp) {
        HelpModalDialog(onDismiss = { onShowHelpChange(false) })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = AppI18n.tr("Rhythm Trainer"),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppI18n.tr("Single-Frequency Musical Rhythm Practice (440 Hz)"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onShowHelpChange(true) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Primary Control Row: Time Signature Drop-Down, Edit Mode Toggle, Play/Pause
            ControlHeaderCard(
                timeSignature = state.timeSignature,
                isEditMode = state.isEditMode,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                loopPlayback = state.loopPlayback,
                onSelectTimeSignature = viewModel::selectTimeSignature,
                onToggleEditMode = viewModel::toggleEditMode,
                onTogglePlayPause = viewModel::togglePlayPause,
                onBpmChange = viewModel::setBpm,
                onToggleLoop = viewModel::toggleLoopPlayback
            )

            // 2. Countdown Banner (4 .. 3 .. 2 .. 1) when Play is pressed
            AnimatedVisibility(visible = state.countdownValue != null) {
                CountdownBanner(countdownValue = state.countdownValue ?: 4)
            }

            // 3. Musical Notation Score with Bar Lines (and no clef/staff)
            RhythmScoreView(
                timeSignature = state.timeSignature,
                measures = state.measures,
                isEditMode = state.isEditMode,
                selectedPosition = state.selectedPosition,
                activePlayingPosition = state.activePlayingPosition,
                onElementClick = { pos ->
                    viewModel.selectElement(pos)
                }
            )

            // 4. Either Edit Mode Panel (assign note durations) OR Play/View Mode Duration Readout
            if (state.isEditMode) {
                EditModePanel(
                    state = state,
                    onAssignDuration = viewModel::assignSelectedDuration,
                    onAddMeasure = viewModel::addMeasure,
                    onRemoveMeasure = viewModel::removeLastMeasure,
                    onRandomize = viewModel::randomizeRhythm,
                    onResetQuarterNotes = viewModel::resetToQuarterNotes,
                    onExitEditMode = { viewModel.setEditMode(false) }
                )
            } else {
                NoteDurationSummaryCard(
                    state = state,
                    onEnterEditMode = { viewModel.setEditMode(true) }
                )
            }
        }
    }
}

@Composable
private fun ControlHeaderCard(
    timeSignature: TimeSignature,
    isEditMode: Boolean,
    isPlaying: Boolean,
    bpm: Int,
    loopPlayback: Boolean,
    onSelectTimeSignature: (TimeSignature) -> Unit,
    onToggleEditMode: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onBpmChange: (Int) -> Unit,
    onToggleLoop: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // (1) Time Signature Drop-Down List (4/4, 3/4, 2/2, 5/4, 7/4)
                Box {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = AppI18n.tr("Time: ${timeSignature.label}"),
                            maxLines = 1,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Time Signature"
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        TimeSignature.entries.forEach { sig ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${sig.label}  (${sig.quarterBeatsPerBar} quarter beats/bar)",
                                        fontWeight = if (sig == timeSignature) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onSelectTimeSignature(sig)
                                }
                            )
                        }
                    }
                }

                // (4) Edit Mode Toggle Button
                FilledTonalButton(
                    onClick = onToggleEditMode,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppI18n.tr(if (isEditMode) "Done Editing" else "Edit"),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        fontSize = 12.sp
                    )
                }

                // (3) Play / Pause Button
                Button(
                    onClick = onTogglePlayPause,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFDC2626) else Color(0xFF059669)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppI18n.tr(if (isPlaying) "Pause" else "Play"),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        fontSize = 12.sp
                    )
                }
            }

            // Tempo Slider & Loop Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = AppI18n.tr("Tempo: $bpm BPM"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(112.dp)
                )
                Slider(
                    value = bpm.toFloat(),
                    onValueChange = { onBpmChange(it.toInt()) },
                    valueRange = 40f..200f,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = loopPlayback,
                    onClick = onToggleLoop,
                    label = { Text(AppI18n.tr("Loop")) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Loop",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CountdownBanner(countdownValue: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF3C7)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Count-In Clicks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "Counting down 4 .. 3 .. 2 .. 1 before rhythm starts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB45309)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 4 downTo 1) {
                    val active = n == countdownValue
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (active) Color(0xFFD97706) else Color(0xFFFDE68A))
                            .border(
                                width = if (active) 2.dp else 1.dp,
                                color = Color(0xFF92400E),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = n.toString(),
                            color = if (active) Color.White else Color(0xFF92400E),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditModePanel(
    state: RhythmUiState,
    onAssignDuration: (NoteDuration, Boolean) -> Unit,
    onAddMeasure: () -> Unit,
    onRemoveMeasure: () -> Unit,
    onRandomize: () -> Unit,
    onResetQuarterNotes: () -> Unit,
    onExitEditMode: () -> Unit
) {
    val selectedElement = state.selectedElement
    val maxUnitsInBar = state.timeSignature.sixteenthUnitsPerBar

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Mode: Assign Note & Rest Durations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = buildString {
                            append("Tap any note/rest above, then choose a duration below. ")
                            if (selectedElement != null) {
                                append(
                                    "Selected: Bar ${state.selectedPosition.measureIndex + 1}, " +
                                        "Item ${state.selectedPosition.elementIndex + 1} " +
                                        "(${selectedElement.duration.durationLabel(selectedElement.isRest)})"
                                )
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Note Duration Palette
            Text(
                text = "Assign Note Duration:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteDuration.entries.forEach { duration ->
                    val enabled = duration.sixteenthUnits <= maxUnitsInBar
                    val isCurrent = selectedElement?.duration == duration && selectedElement.isRest == false
                    DurationPickerTile(
                        duration = duration,
                        isRest = false,
                        isSelected = isCurrent,
                        enabled = enabled,
                        onClick = { onAssignDuration(duration, false) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Rest Duration Palette
            Text(
                text = "Assign Rest Duration:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteDuration.entries.forEach { duration ->
                    val enabled = duration.sixteenthUnits <= maxUnitsInBar
                    val isCurrent = selectedElement?.duration == duration && selectedElement.isRest == true
                    DurationPickerTile(
                        duration = duration,
                        isRest = true,
                        isSelected = isCurrent,
                        enabled = enabled,
                        onClick = { onAssignDuration(duration, true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bar Management & Preset Actions
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddMeasure,
                    enabled = state.measures.size < 8
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Bar")
                }

                OutlinedButton(
                    onClick = onRemoveMeasure,
                    enabled = state.measures.size > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Remove Bar")
                }

                OutlinedButton(onClick = onRandomize) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Randomize")
                }

                OutlinedButton(onClick = onResetQuarterNotes) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Quarter Notes")
                }

                Button(onClick = onExitEditMode) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Exit Edit Mode")
                }
            }
        }
    }
}

@Composable
private fun DurationPickerTile(
    duration: NoteDuration,
    isRest: Boolean,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val borderColor = when {
        !enabled -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MusicalSymbolIcon(
            duration = duration,
            isRest = isRest,
            tint = contentColor,
            symbolSize = 40.dp
        )
        Text(
            text = duration.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = if (isRest) "${duration.fractionText} Rest" else "${duration.fractionText} Note",
            fontSize = 10.sp,
            color = contentColor.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun NoteDurationSummaryCard(
    state: RhythmUiState,
    onEnterEditMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = AppI18n.tr("Rhythm Note Durations (${state.timeSignature.label})"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppI18n.tr("Ready to play. Press Play for a 4..3..2..1 click count-in followed by the rhythm."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onEnterEditMode) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(AppI18n.tr("Edit"))
                }
            }

            state.measures.forEachIndexed { barIdx, measure ->
                val summary = measure.elements.joinToString("  •  ") { el ->
                    el.duration.durationLabel(el.isRest)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = AppI18n.tr("Bar ${barIdx + 1}:  $summary"),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpModalDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val helpScroll = rememberScrollState()
    val titles = listOf(
        AppI18n.str(context, R.string.help_sec1_title),
        AppI18n.str(context, R.string.help_sec2_title),
        AppI18n.str(context, R.string.help_sec3_title)
    )
    val bodies = listOf(
        AppI18n.str(context, R.string.help_sec1_body),
        AppI18n.str(context, R.string.help_sec2_body),
        AppI18n.str(context, R.string.help_sec3_body)
    )
    val accentColors = listOf(
        Color(0xFF1E3A8A),
        Color(0xFF0F766E),
        Color(0xFFB45309)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(540.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppI18n.str(context, R.string.help_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = AppI18n.str(context, R.string.got_it),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val primaryCol = MaterialTheme.colorScheme.primary
                val trackCol = MaterialTheme.colorScheme.surfaceVariant
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .drawWithContent {
                            drawContent()
                            val trackW = 6.dp.toPx()
                            val trackX = size.width - trackW
                            drawRoundRect(
                                color = trackCol,
                                topLeft = Offset(trackX, 0f),
                                size = Size(trackW, size.height),
                                cornerRadius = CornerRadius(trackW / 2f, trackW / 2f)
                            )
                            val maxVal = helpScroll.maxValue.toFloat()
                            val thumbRatio = if (maxVal > 0f) {
                                (size.height / (size.height + maxVal)).coerceIn(0.18f, 1f)
                            } else {
                                1f
                            }
                            val thumbH = size.height * thumbRatio
                            val thumbY = if (maxVal > 0f) {
                                (helpScroll.value.toFloat() / maxVal) * (size.height - thumbH)
                            } else {
                                0f
                            }
                            drawRoundRect(
                                color = primaryCol,
                                topLeft = Offset(trackX, thumbY),
                                size = Size(trackW, thumbH),
                                cornerRadius = CornerRadius(trackW / 2f, trackW / 2f)
                            )
                        }
                        .verticalScroll(helpScroll)
                        .padding(end = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in titles.indices) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, accentColors[i].copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = titles[i],
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColors[i]
                                )
                                Text(
                                    text = bodies[i],
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
