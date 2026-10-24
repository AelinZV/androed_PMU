package com.example.myapplication

/** Шансы выбора категории для пользовательского уровня: ровно 100 %. */
enum class LevelRarity(val chance: Int) {
    COMMON(55), RARE(35), VERY_RARE(9), ULTRA_RARE(1)
}
enum class Season {
    SPRING, SUMMER, AUTUMN, WINTER;

    // Фон привязан к сезону. Размещайте JPEG в res/drawable-nodpi.
    val backgroundRes: Int get() = when (this) {
        SPRING -> R.drawable.level_spring
        SUMMER -> R.drawable.level_summer
        AUTUMN -> R.drawable.level_autumn
        WINTER -> R.drawable.level_winter
    }
}

data class LevelConfig(
    val id: Long = 0L,
    val name: String,
    val season: Season,
    val maxBugs: Int,
    val speed: Int,
    val durationSeconds: Int,
    val commonBugId: String = "bee",
    val rareBugId: String = "scarab",
    val veryRareBugId: String = "superrare1",
    val ultraRareBugId: String = "ultrarare1"
)

object LevelDefaults {
    val templates = listOf(
        LevelConfig(name = "Ледяная ловушка", season = Season.WINTER, maxBugs = 10, speed = 5, durationSeconds = 120),
        LevelConfig(name = "Весенний переполох", season = Season.SPRING, maxBugs = 8, speed = 4, durationSeconds = 120),
        LevelConfig(name = "Летний хаос", season = Season.SUMMER, maxBugs = 12, speed = 7, durationSeconds = 90),
        LevelConfig(name = "Осенний сон", season = Season.AUTUMN, maxBugs = 10, speed = 5, durationSeconds = 150)
    )
}

object BugCatalog {
    data class Species(val id: String, val label: String, val rarity: LevelRarity, val image: Int)

    // Пополняется новыми ассетами без изменения БД: сохраняется стабильный id.
    val species = listOf(
        Species("bee", "Пчела", LevelRarity.COMMON, R.drawable.phcel),
        Species("scarab", "Скарабей", LevelRarity.RARE, R.drawable.skarabeychik),
        Species("spider", "Паук (из лабораторных)", LevelRarity.VERY_RARE, R.drawable.pawuchok),

        // Новые PNG: группа common — 55 % появления слота.
        Species("common1", "Божья коровка", LevelRarity.COMMON, R.drawable.common1),
        Species("common2", "Муравей", LevelRarity.COMMON, R.drawable.common2),
        Species("common3", "Кузнечик", LevelRarity.COMMON, R.drawable.common3),

        // Новые PNG: группа rare — 35 % появления слота.
        Species("rare1", "Жук-носорог", LevelRarity.RARE, R.drawable.rare1),
        Species("rare2", "Жук-навозник", LevelRarity.RARE, R.drawable.rare2),
        Species("rare3", "Пушистый паук", LevelRarity.RARE, R.drawable.rare3),

        // Суперредкие (9 %) и ультраредкие (1 %) — разные независимые слоты.
        Species("superrare1", "Яркая бабочка", LevelRarity.VERY_RARE, R.drawable.superrare1),
        Species("superrare2", "Зелёная гусеница", LevelRarity.VERY_RARE, R.drawable.superrare2),
        Species("superrare3", "Голубая стрекоза", LevelRarity.VERY_RARE, R.drawable.superrare3),
        Species("ultrarare1", "Радужная бабочка (ультра)", LevelRarity.ULTRA_RARE, R.drawable.ultrarare1),
        Species("ultrarare2", "Пятнистая гусеница (ультра)", LevelRarity.ULTRA_RARE, R.drawable.ultrarare2)
    )

    fun variants(rarity: LevelRarity) = species.filter { it.rarity == rarity }
    fun find(id: String, rarity: LevelRarity): Species =
        variants(rarity).firstOrNull { it.id == id } ?: variants(rarity).first()
}

object LevelRules {
    fun validate(level: LevelConfig): String? = when {
        level.name.isBlank() -> "Введите название уровня"
        level.name.length > 50 -> "Название не должно превышать 50 символов"
        level.maxBugs !in 1..25 -> "Число жуков должно быть от 1 до 25"
        level.speed !in 1..10 -> "Скорость должна быть от 1 до 10"
        level.durationSeconds !in 30..300 -> "Раунд должен длиться от 30 до 300 секунд"
        BugCatalog.species.none { it.id == level.commonBugId && it.rarity == LevelRarity.COMMON } -> "Неверный обычный жук"
        BugCatalog.species.none { it.id == level.rareBugId && it.rarity == LevelRarity.RARE } -> "Неверный редкий жук"
        BugCatalog.species.none { it.id == level.veryRareBugId && it.rarity == LevelRarity.VERY_RARE } -> "Неверный суперредкий жук"
        BugCatalog.species.none { it.id == level.ultraRareBugId && it.rarity == LevelRarity.ULTRA_RARE } -> "Неверный ультраредкий жук"
        else -> null
    }
    fun chooseRarity(roll: Int): LevelRarity = when (roll) {
        in 0..54 -> LevelRarity.COMMON
        in 55..89 -> LevelRarity.RARE
        in 90..98 -> LevelRarity.VERY_RARE
        99 -> LevelRarity.ULTRA_RARE
        else -> throw IllegalArgumentException("roll должен быть 0..99")
    }
    fun seasonSpeedFactor(season: Season): Float = when(season) {
        Season.WINTER -> 0.5f
        Season.SPRING -> 1.2f
        Season.SUMMER -> 1.8f
        Season.AUTUMN -> 0.9f
    }
}
