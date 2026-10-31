package com.eggwall.rhythmtrainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RhythmUiState(
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val measures: List<Measure> = RhythmBuilder.defaultMeasuresFor(TimeSignature.FOUR_FOUR),
    val isEditMode: Boolean = false,
    val selectedPosition: ElementPosition = ElementPosition(0, 0),
    val isPlaying: Boolean = false,
    val countdownValue: Int? = null,
    val activePlayingPosition: ElementPosition? = null,
    val bpm: Int = 100,
    val loopPlayback: Boolean = false
) {
    val selectedElement: RhythmElement?
        get() = measures.getOrNull(selectedPosition.measureIndex)
            ?.elements
            ?.getOrNull(selectedPosition.elementIndex)
}

class RhythmViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RhythmUiState())
    val uiState: StateFlow<RhythmUiState> = _uiState.asStateFlow()

    private val audioEngine = RhythmAudioEngine(viewModelScope)

    fun selectTimeSignature(newSignature: TimeSignature) {
        stopPlaybackInternal()
        _uiState.update { state ->
            val updatedMeasures = RhythmBuilder.defaultMeasuresFor(newSignature)
            state.copy(
                timeSignature = newSignature,
                measures = updatedMeasures,
                selectedPosition = ElementPosition(0, 0),
                isPlaying = false,
                countdownValue = null,
                activePlayingPosition = null
            )
        }
    }

    fun toggleEditMode() {
        val enteringEdit = !_uiState.value.isEditMode
        if (enteringEdit) {
            stopPlaybackInternal()
        }
        _uiState.update { state ->
            state.copy(
                isEditMode = enteringEdit,
                isPlaying = false,
                countdownValue = null,
                activePlayingPosition = null,
                selectedPosition = coercePosition(state.measures, state.selectedPosition)
            )
        }
    }

    fun setEditMode(enabled: Boolean) {
        if (_uiState.value.isEditMode != enabled) {
            toggleEditMode()
        }
    }

    fun selectElement(position: ElementPosition) {
        _uiState.update { state ->
            state.copy(selectedPosition = coercePosition(state.measures, position))
        }
    }

    /**
     * In Edit Mode, assigns a new [NoteDuration] and [isRest] status to the currently selected
     * note/rest while preserving the bar's exact time signature length.
     */
    fun assignSelectedDuration(duration: NoteDuration, isRest: Boolean) {
        _uiState.update { state ->
            if (!state.isEditMode || state.measures.isEmpty()) return@update state
            val pos = coercePosition(state.measures, state.selectedPosition)
            val currentMeasure = state.measures[pos.measureIndex]
            val (updatedMeasure, newElemIdx) = RhythmBuilder.updateElementInMeasure(
                measure = currentMeasure,
                elementIndex = pos.elementIndex,
                newDuration = duration,
                newIsRest = isRest,
                barUnits = state.timeSignature.sixteenthUnitsPerBar
            )
            val newMeasures = state.measures.toMutableList()
            newMeasures[pos.measureIndex] = updatedMeasure
            state.copy(
                measures = newMeasures,
                selectedPosition = ElementPosition(pos.measureIndex, newElemIdx)
            )
        }
    }

    fun toggleSelectedRest() {
        val current = _uiState.value.selectedElement ?: return
        assignSelectedDuration(current.duration, !current.isRest)
    }

    fun addMeasure() {
        stopPlaybackInternal()
        _uiState.update { state ->
            if (state.measures.size >= 8) return@update state
            val newBar = RhythmBuilder.quarterNoteBar(state.timeSignature)
            val updated = state.measures + newBar
            state.copy(
                measures = updated,
                selectedPosition = ElementPosition(updated.lastIndex, 0)
            )
        }
    }

    fun removeLastMeasure() {
        stopPlaybackInternal()
        _uiState.update { state ->
            if (state.measures.size <= 1) return@update state
            val updated = state.measures.dropLast(1)
            state.copy(
                measures = updated,
                selectedPosition = coercePosition(updated, state.selectedPosition)
            )
        }
    }

    fun randomizeRhythm() {
        stopPlaybackInternal()
        _uiState.update { state ->
            val count = state.measures.size.coerceAtLeast(1)
            val randomMeasures = List(count) {
                RhythmBuilder.randomMeasure(state.timeSignature)
            }
            state.copy(
                measures = randomMeasures,
                selectedPosition = ElementPosition(0, 0),
                isPlaying = false,
                countdownValue = null,
                activePlayingPosition = null
            )
        }
    }

    fun resetToQuarterNotes() {
        stopPlaybackInternal()
        _uiState.update { state ->
            val count = state.measures.size.coerceAtLeast(1)
            val resetMeasures = List(count) {
                RhythmBuilder.quarterNoteBar(state.timeSignature)
            }
            state.copy(
                measures = resetMeasures,
                selectedPosition = ElementPosition(0, 0),
                isPlaying = false,
                countdownValue = null,
                activePlayingPosition = null
            )
        }
    }

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(40, 200)
        _uiState.update { it.copy(bpm = clamped) }
    }

    fun toggleLoopPlayback() {
        _uiState.update { it.copy(loopPlayback = !it.loopPlayback) }
    }

    fun togglePlayPause() {
        val current = _uiState.value
        if (current.isPlaying) {
            stopPlaybackInternal()
            _uiState.update {
                it.copy(
                    isPlaying = false,
                    countdownValue = null,
                    activePlayingPosition = null
                )
            }
        } else {
            // Exit edit mode automatically if user presses Play
            val stateForPlay = if (current.isEditMode) {
                _uiState.update { it.copy(isEditMode = false) }
                _uiState.value
            } else {
                current
            }
            _uiState.update {
                it.copy(
                    isPlaying = true,
                    countdownValue = RhythmAudioEngine.COUNTDOWN_START,
                    activePlayingPosition = null
                )
            }
            audioEngine.startPlayback(
                measures = stateForPlay.measures,
                bpm = stateForPlay.bpm,
                loop = stateForPlay.loopPlayback,
                onCountdownTick = { tick ->
                    _uiState.update { it.copy(countdownValue = tick) }
                },
                onActiveElementChanged = { activePos ->
                    _uiState.update { it.copy(activePlayingPosition = activePos) }
                },
                onPlaybackCompleted = {
                    _uiState.update {
                        it.copy(
                            isPlaying = false,
                            countdownValue = null,
                            activePlayingPosition = null
                        )
                    }
                }
            )
        }
    }

    private fun stopPlaybackInternal() {
        audioEngine.stopPlayback()
    }

    private fun coercePosition(measures: List<Measure>, pos: ElementPosition): ElementPosition {
        if (measures.isEmpty()) return ElementPosition(0, 0)
        val mIdx = pos.measureIndex.coerceIn(0, measures.lastIndex)
        val elems = measures[mIdx].elements
        val eIdx = if (elems.isEmpty()) 0 else pos.elementIndex.coerceIn(0, elems.lastIndex)
        return ElementPosition(mIdx, eIdx)
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaybackInternal()
    }
}
