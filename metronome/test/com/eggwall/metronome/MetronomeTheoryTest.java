package com.eggwall.metronome;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Host-side JVM unit test suite for {@link MetronomeTheory}.
 */
public class MetronomeTheoryTest {

    @Test
    public void testClampBpm() {
        assertEquals(MetronomeTheory.MIN_BPM, MetronomeTheory.clampBpm(-50));
        assertEquals(MetronomeTheory.MIN_BPM, MetronomeTheory.clampBpm(19));
        assertEquals(20, MetronomeTheory.clampBpm(20));
        assertEquals(120, MetronomeTheory.clampBpm(120));
        assertEquals(300, MetronomeTheory.clampBpm(300));
        assertEquals(MetronomeTheory.MAX_BPM, MetronomeTheory.clampBpm(450));
    }

    @Test
    public void testClampDropPct() {
        assertEquals(0, MetronomeTheory.clampDropPct(-10));
        assertEquals(0, MetronomeTheory.clampDropPct(0));
        assertEquals(15, MetronomeTheory.clampDropPct(18));
        assertEquals(25, MetronomeTheory.clampDropPct(25));
        assertEquals(100, MetronomeTheory.clampDropPct(100));
        assertEquals(100, MetronomeTheory.clampDropPct(135));
    }

    @Test
    public void testTempoMarkingsAndDescriptions() {
        assertEquals("GRAVE", MetronomeTheory.getTempoMarking(32));
        assertEquals("LARGO", MetronomeTheory.getTempoMarking(42));
        assertEquals("LENTO", MetronomeTheory.getTempoMarking(50));
        assertEquals("LARGHETTO", MetronomeTheory.getTempoMarking(62));
        assertEquals("ADAGIO", MetronomeTheory.getTempoMarking(70));
        assertEquals("ANDANTE", MetronomeTheory.getTempoMarking(84));
        assertEquals("MODERATO", MetronomeTheory.getTempoMarking(112));
        assertEquals("ALLEGRO", MetronomeTheory.getTempoMarking(128));
        assertEquals("VIVACE", MetronomeTheory.getTempoMarking(164));
        assertEquals("PRESTO", MetronomeTheory.getTempoMarking(188));
        assertEquals("PRESTISSIMO", MetronomeTheory.getTempoMarking(240));
        assertFalse(MetronomeTheory.getTempoDescription(128).isEmpty());
    }

    @Test
    public void testCycleBeatStateAndLabels() {
        int s = MetronomeTheory.BEAT_ACCENT;
        assertEquals("ACC", MetronomeTheory.getBeatStateLabel(s));
        s = MetronomeTheory.cycleBeatState(s);
        assertEquals(MetronomeTheory.BEAT_NORMAL, s);
        assertEquals("BEAT", MetronomeTheory.getBeatStateLabel(s));
        s = MetronomeTheory.cycleBeatState(s);
        assertEquals(MetronomeTheory.BEAT_SUB_ACCENT, s);
        assertEquals("SUB", MetronomeTheory.getBeatStateLabel(s));
        s = MetronomeTheory.cycleBeatState(s);
        assertEquals(MetronomeTheory.BEAT_MUTE, s);
        assertEquals("MUTE", MetronomeTheory.getBeatStateLabel(s));
        s = MetronomeTheory.cycleBeatState(s);
        assertEquals(MetronomeTheory.BEAT_ACCENT, s);
    }

    @Test
    public void testBeatIntervalsAndSampleCounts() {
        assertEquals(500.0, MetronomeTheory.getBeatIntervalMs(120), 1e-6);
        assertEquals(1000.0, MetronomeTheory.getBeatIntervalMs(60), 1e-6);
        assertEquals(22050.0, MetronomeTheory.getSamplesPerBeat(120, 44100), 1e-6);
        assertEquals(44100.0, MetronomeTheory.getSamplesPerBeat(60, 44100), 1e-6);
    }

    @Test
    public void testFourMeasureLockInAndDropRate() {
        // Muted beats never sound even during lock-in
        assertFalse(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_MUTE, 0, 0, 99));

        // First 4 measures (measureCount 0, 1, 2, 3) always play 100% regardless of dropPct
        for (int bar = 0; bar < 4; bar++) {
            assertTrue(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_ACCENT, bar, 95, 0));
            assertTrue(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_NORMAL, bar, 100, 0));
        }

        // Measure 5+ (measureCount >= 4) respects dropPct threshold
        assertFalse(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_NORMAL, 4, 30, 10));
        assertFalse(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_NORMAL, 4, 30, 29));
        assertTrue(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_NORMAL, 4, 30, 30));
        assertTrue(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_NORMAL, 4, 30, 99));
        assertFalse(MetronomeTheory.shouldPlayClick(MetronomeTheory.BEAT_ACCENT, 5, 100, 99));
    }

    @Test
    public void testSpeedTrainerProgression() {
        // Disabled trainer keeps base BPM
        assertEquals(100, MetronomeTheory.computeTrainerBpm(100, 12, 0, 4, 180));
        // +4 BPM every 4 bars
        assertEquals(100, MetronomeTheory.computeTrainerBpm(100, 0, 4, 4, 180));
        assertEquals(100, MetronomeTheory.computeTrainerBpm(100, 3, 4, 4, 180));
        assertEquals(104, MetronomeTheory.computeTrainerBpm(100, 4, 4, 4, 180));
        assertEquals(108, MetronomeTheory.computeTrainerBpm(100, 8, 4, 4, 180));
        // Respects target ceiling
        assertEquals(120, MetronomeTheory.computeTrainerBpm(100, 40, 4, 4, 120));
    }

    @Test
    public void testTapTempoCalculation() {
        assertEquals(-1, MetronomeTheory.computeTapTempoBpm(null, 0));
        assertEquals(-1, MetronomeTheory.computeTapTempoBpm(new long[] { 1000L }, 1));

        // 500 ms intervals -> 120 BPM
        long[] taps120 = new long[] { 1000L, 1500L, 2000L, 2500L };
        assertEquals(120, MetronomeTheory.computeTapTempoBpm(taps120, 4));

        // Old tap before a >3000 ms pause is ignored; recent 600 ms interval -> 100 BPM
        long[] tapsWithGap = new long[] { 1000L, 6000L, 6600L, 7200L };
        assertEquals(100, MetronomeTheory.computeTapTempoBpm(tapsWithGap, 4));
    }

    @Test
    public void testPendulumAngleSymmetry() {
        // Even beat 0 swings from -maxDeg (phase 0) through 0 (phase 0.5) to +maxDeg (phase 1.0)
        assertEquals(-38.0f, MetronomeTheory.computePendulumAngleDeg(0, 0.0, 38.0f), 1e-4f);
        assertEquals(0.0f, MetronomeTheory.computePendulumAngleDeg(0, 0.5, 38.0f), 1e-4f);
        assertEquals(38.0f, MetronomeTheory.computePendulumAngleDeg(0, 1.0, 38.0f), 1e-4f);

        // Odd beat 1 swings from +maxDeg back to -maxDeg
        assertEquals(38.0f, MetronomeTheory.computePendulumAngleDeg(1, 0.0, 38.0f), 1e-4f);
        assertEquals(-38.0f, MetronomeTheory.computePendulumAngleDeg(1, 1.0, 38.0f), 1e-4f);
    }

    @Test
    public void testTimeSignaturesAndSubdivisionsIntegrity() {
        assertTrue(MetronomeTheory.TIME_SIGNATURES.length >= 9);
        for (MetronomeTheory.TimeSignature ts : MetronomeTheory.TIME_SIGNATURES) {
            assertEquals(ts.numerator, ts.defaultBeatStates.length);
            assertEquals(MetronomeTheory.BEAT_ACCENT, ts.defaultBeatStates[0]);
        }
        assertTrue(MetronomeTheory.SUBDIVISIONS.length >= 5);
        for (MetronomeTheory.Subdivision sub : MetronomeTheory.SUBDIVISIONS) {
            assertEquals(0.0, sub.pulseOffsets[0], 1e-6);
        }
    }
}
