package com.example.myapplication

import org.junit.Assert.*
import org.junit.Test

class LevelRulesTest {
    @Test fun rarityDistributionBoundaries() {
        assertEquals(LevelRarity.COMMON, LevelRules.chooseRarity(54))
        assertEquals(LevelRarity.RARE, LevelRules.chooseRarity(55))
        assertEquals(LevelRarity.RARE, LevelRules.chooseRarity(89))
        assertEquals(LevelRarity.VERY_RARE, LevelRules.chooseRarity(90))
        assertEquals(LevelRarity.VERY_RARE, LevelRules.chooseRarity(98))
        assertEquals(LevelRarity.ULTRA_RARE, LevelRules.chooseRarity(99))
    }

    @Test fun rarityChancesTotalOneHundred() {
        assertEquals(100, LevelRarity.values().sumOf { it.chance })
    }

    @Test fun ultraRareCategoryContainsOnlyUltraRareAssets() {
        val ultraIds = BugCatalog.variants(LevelRarity.ULTRA_RARE).map { it.id }
        assertEquals(listOf("ultrarare1", "ultrarare2"), ultraIds)
        assertFalse(BugCatalog.variants(LevelRarity.VERY_RARE).any { it.id.startsWith("ultrarare") })
    }

    @Test fun wrongUltraRareSelectionIsRejected() {
        val invalid = LevelDefaults.templates.first().copy(ultraRareBugId = "common1")
        assertNotNull(LevelRules.validate(invalid))
    }

    @Test fun validLevelPassesValidation() {
        assertNull(LevelRules.validate(LevelDefaults.templates.first()))
    }

    @Test fun emptyNameRejected() {
        assertNotNull(LevelRules.validate(LevelDefaults.templates.first().copy(name = " ")))
    }

    @Test fun winterIsSlowerThanSummer() {
        assertTrue(LevelRules.seasonSpeedFactor(Season.WINTER) < LevelRules.seasonSpeedFactor(Season.SUMMER))
    }
}
