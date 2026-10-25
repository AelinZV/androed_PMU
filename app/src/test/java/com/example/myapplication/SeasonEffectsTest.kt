package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SeasonEffectsTest {
    @Test fun winterFreezesAndStopsMovement() {
        val initial = SeasonEffects.initial(Season.WINTER, 1_000L, Random(1))
        val frozen = SeasonEffects.advance(Season.WINTER, initial, initial.nextEventAtMs, Random(2))
        assertEquals(SeasonBugMode.FROZEN, frozen.mode)
        assertEquals(0f, SeasonEffects.movementMultiplier(frozen), 0.001f)
    }

    @Test fun iceBreaksWithoutWaitingForAutomaticThaw() {
        val frozen = SeasonBugState(SeasonBugMode.FROZEN, effectUntilMs = 20_000L)
        val released = SeasonEffects.breakIce(frozen, 10_000L, Random(3))
        assertEquals(SeasonBugMode.MOVING, released.mode)
        assertTrue(released.nextEventAtMs > 10_000L)
    }

    @Test fun winterThawsAutomatically() {
        val frozen = SeasonBugState(SeasonBugMode.FROZEN, effectUntilMs = 8_000L)
        assertEquals(SeasonBugMode.FROZEN, SeasonEffects.advance(Season.WINTER, frozen, 7_999L).mode)
        val thawed = SeasonEffects.advance(Season.WINTER, frozen, 8_000L, Random(4))
        assertEquals(SeasonBugMode.MOVING, thawed.mode)
    }

    @Test fun springBoostIsTemporary() {
        val initial = SeasonEffects.initial(Season.SPRING, 0L, Random(5))
        val boost = SeasonEffects.advance(Season.SPRING, initial, initial.nextEventAtMs, Random(6))
        assertEquals(SeasonBugMode.BOOSTED, boost.mode)
        assertTrue(SeasonEffects.movementMultiplier(boost) > 1f)
        val finished = SeasonEffects.advance(Season.SPRING, boost, boost.effectUntilMs, Random(7))
        assertEquals(SeasonBugMode.MOVING, finished.mode)
    }

    @Test fun autumnSleepStopsAndThenWakes() {
        val initial = SeasonEffects.initial(Season.AUTUMN, 0L, Random(8))
        val sleeping = SeasonEffects.advance(Season.AUTUMN, initial, initial.nextEventAtMs, Random(9))
        assertEquals(SeasonBugMode.SLEEPING, sleeping.mode)
        assertEquals(0f, SeasonEffects.movementMultiplier(sleeping), 0.001f)
        assertEquals(SeasonBugMode.MOVING, SeasonEffects.advance(Season.AUTUMN, sleeping, sleeping.effectUntilMs, Random(10)).mode)
    }

    @Test fun summerDoesNotFreezeOrSleep() {
        var state = SeasonEffects.initial(Season.SUMMER, 0L)
        repeat(100) { index ->
            state = SeasonEffects.advance(Season.SUMMER, state, index * 10_000L)
            assertEquals(SeasonBugMode.MOVING, state.mode)
        }
    }
}
