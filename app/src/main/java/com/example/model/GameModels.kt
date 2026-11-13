package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BioluminescentViolet
import com.example.ui.theme.CryoCyan
import com.example.ui.theme.HazardRed
import com.example.ui.theme.RadTeal
import com.example.ui.theme.RustCopper
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SurplusGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.XpGold
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

enum class ResourceType(
    val displayName: String,
    val shortLabel: String,
    val color: Color,
    val isRare: Boolean = false
) {
    SCRAP("Regolith Alloy", "SCRAP", SolarAmber),
    WATER("Glacial Water", "WATER", CryoCyan),
    RATIONS("Bio-Dome Rations", "FOOD", SurplusGreen),
    BIO_FUEL("Methane-LOX Fuel", "FUEL", RustCopper),
    RESEARCH_DATA("Orbital Telemetry", "TECH", RadTeal),
    GOLD("Martian Gold Ore", "GOLD", XpGold),
    THORIUM_RODS("Kilopower RTG Rods", "THORIUM", WarningOrange, isRare = true),
    AETHER_CELLS("Solar Super-Capacitors", "AETHER", CryoCyan, isRare = true),
    MUTAGEN_CORES("Xenobiology Cores", "CORES", BioluminescentViolet, isRare = true)
}

enum class DayNightPhase(
    val displayName: String,
    val startFraction: Float,
    val endFraction: Float,
    val tagline: String,
    val scrapMultiplier: Double,
    val waterMultiplier: Double,
    val rationsMultiplier: Double,
    val researchMultiplier: Double,
    val solarPowerMultiplier: Double,
    val mutagenSpawnMultiplier: Double,
    val accentColor: Color
) {
    DAWN(
        displayName = "Frost-Sublimation Dawn",
        startFraction = 0.0f,
        endFraction = 0.15f,
        tagline = "Polar frost sublimation boosts Water Ice (+35%), Valles Dome pays +35% Gold, surface EVA active.",
        scrapMultiplier = 1.05,
        waterMultiplier = 1.35,
        rationsMultiplier = 1.15,
        researchMultiplier = 1.0,
        solarPowerMultiplier = 0.95,
        mutagenSpawnMultiplier = 1.1,
        accentColor = CryoCyan
    ),
    SCORCHING_DAY(
        displayName = "Perihelion Solar Sol",
        startFraction = 0.15f,
        endFraction = 0.55f,
        tagline = "Peak Martian insolation: Solar Arrays surge (+50% MW), Ares Prime pays +35% Gold for Regolith Alloy.",
        scrapMultiplier = 1.30,
        waterMultiplier = 0.90,
        rationsMultiplier = 1.35,
        researchMultiplier = 1.05,
        solarPowerMultiplier = 1.50,
        mutagenSpawnMultiplier = 0.6,
        accentColor = SolarAmber
    ),
    DUSK(
        displayName = "Dust-Devil Dusk",
        startFraction = 0.55f,
        endFraction = 0.70f,
        tagline = "Rising crater winds: Prime EVA harvest hour (+25% Bio-Rations), +15% Dust Squall Threat & cosmic flux.",
        scrapMultiplier = 1.05,
        waterMultiplier = 1.25,
        rationsMultiplier = 1.05,
        researchMultiplier = 1.25,
        solarPowerMultiplier = 0.80,
        mutagenSpawnMultiplier = 1.5,
        accentColor = RustCopper
    ),
    RAD_NIGHT(
        displayName = "Cryo-Radiation Night (-110°C)",
        startFraction = 0.70f,
        endFraction = 1.0f,
        tagline = "HIGH DANGER: +35% Meteor & Dust-Storm Impact, +Cosmic Ray Surge, +6% Rover Risk & Solar storage at 65%!",
        scrapMultiplier = 0.75,
        waterMultiplier = 1.05,
        rationsMultiplier = 0.80,
        researchMultiplier = 1.40,
        solarPowerMultiplier = 0.65,
        mutagenSpawnMultiplier = 2.6,
        accentColor = BioluminescentViolet
    );

    companion object {
        fun fromCycleProgress(progress: Float): DayNightPhase {
            val normalized = ((progress % 1f) + 1f) % 1f
            return entries.firstOrNull {
                normalized >= it.startFraction && normalized < it.endFraction
            } ?: RAD_NIGHT
        }
    }
}

enum class CraftItemCategory(val label: String) {
    HUNTING("EVA Harvest & Rations"),
    DEFENSE("Dome Shield & Defense")
}

enum class CraftableItemType(
    val id: String,
    val title: String,
    val category: CraftItemCategory,
    val description: String,
    val effectSummary: String,
    val scrapCost: Double,
    val fuelCost: Double = 0.0,
    val goldCost: Double = 0.0,
    val aetherCost: Double = 0.0,
    val thoriumCost: Double = 0.0,
    val coresCost: Double = 0.0,
    val baseCraftTimeSeconds: Int = 8,
    val defenseRatingBonus: Int = 0,
    val passiveFoodPerSec: Double = 0.0,
    val accentColor: Color
) {
    HARPOON_SPEAR(
        id = "HARPOON_SPEAR",
        title = "Thermal Ice-Core Lance",
        category = CraftItemCategory.HUNTING,
        description = "Heated titanium core-lance forged from sintered hull alloy. Used to harvest Martian lava-tube extremophiles & ice-pods for Bio-Rations.",
        effectSummary = "Crafts +2 Thermal Core Lances (Used in Active Lava-Tube Harvests)",
        scrapCost = 12.0,
        baseCraftTimeSeconds = 6,
        accentColor = SurplusGreen
    ),
    SNARE_TRAP(
        id = "SNARE_TRAP",
        title = "Automated Aeroponic Spore-Pod",
        category = CraftItemCategory.HUNTING,
        description = "Pressurized micro-greenhouse pod that passively cultures spirulina & tubers (+0.6 Food/s each) & supports deep-tube harvests.",
        effectSummary = "+1 Active Spore-Pod (+0.6 Food/s passive & used in Crater Harvests)",
        scrapCost = 18.0,
        fuelCost = 5.0,
        baseCraftTimeSeconds = 9,
        passiveFoodPerSec = 0.6,
        accentColor = SolarAmber
    ),
    AETHER_CROSSBOW(
        id = "AETHER_CROSSBOW",
        title = "Solar-Arc Plasma Harvester",
        category = CraftItemCategory.HUNTING,
        description = "High-velocity magnetic plasma cutter built with a Solar Super-Capacitor. Permanently boosts all EVA Harvest Rations!",
        effectSummary = "Permanent Upgrade: +40% Food from all EVA Harvests & unlocks Deep-Rift Lithic Wyrm",
        scrapCost = 35.0,
        goldCost = 20.0,
        aetherCost = 1.0,
        baseCraftTimeSeconds = 18,
        passiveFoodPerSec = 1.4,
        accentColor = CryoCyan
    ),
    SHRAPNEL_MINE(
        id = "SHRAPNEL_MINE",
        title = "Kinetic Micrometeoroid Interceptor",
        category = CraftItemCategory.DEFENSE,
        description = "Perimeter counter-charge that fortifies the habitat dome (+15 Defense) and auto-detonates incoming meteor debris.",
        effectSummary = "Crafts +2 Kinetic Interceptors (+30 Total Defense & Auto-Counter-Strike)",
        scrapCost = 16.0,
        fuelCost = 6.0,
        baseCraftTimeSeconds = 10,
        defenseRatingBonus = 15,
        accentColor = WarningOrange
    ),
    AUTO_SENTRY_TURRET(
        id = "AUTO_SENTRY_TURRET",
        title = "Point-Defense Laser Turret",
        category = CraftItemCategory.DEFENSE,
        description = "Capacitor-fed twin laser turret that automatically vaporizes incoming Micrometeoroids & Dust-Storm Debris.",
        effectSummary = "+45 Permanent Auto-Defense & +10% Rover Convoy Security",
        scrapCost = 42.0,
        goldCost = 25.0,
        aetherCost = 1.0,
        baseCraftTimeSeconds = 20,
        defenseRatingBonus = 45,
        accentColor = RadTeal
    ),
    THORIUM_PLASMA_CANNON(
        id = "THORIUM_PLASMA_CANNON",
        title = "Kilopower Ion Shield Battery",
        category = CraftItemCategory.DEFENSE,
        description = "Heavy planetary defense array fueled by a Kilopower RTG Rod. Deflects lethal Sol-10 Dust-Storm Cyclones & Meteor Swarms.",
        effectSummary = "+120 Permanent Auto-Defense & +25% Storm Salvage Gold",
        scrapCost = 75.0,
        goldCost = 45.0,
        thoriumCost = 1.0,
        coresCost = 1.0,
        baseCraftTimeSeconds = 28,
        defenseRatingBonus = 120,
        accentColor = BioluminescentViolet
    ),
    BALLISTIC_AMMO_CRATE(
        id = "BALLISTIC_AMMO_CRATE",
        title = "Point-Defense Capacitor Crate (+30 Rds)",
        category = CraftItemCategory.DEFENSE,
        description = "High-density slug & laser charge packs for Point-Defense Turrets during dust storms and armed Mars Rover Expeditions.",
        effectSummary = "+30 Defense Charges (Powers Auto-Turrets at 100% DEF & +25% Rover Loot)",
        scrapCost = 14.0,
        fuelCost = 4.0,
        baseCraftTimeSeconds = 7,
        accentColor = SolarAmber
    ),
    GAS_MASK(
        id = "GAS_MASK",
        title = "Perchlorate Dust Rebreather",
        category = CraftItemCategory.HUNTING,
        description = "Closed-loop MOXIE oxygen rebreather that filters toxic Martian perchlorate dust on Rover Expeditions.",
        effectSummary = "+1 O2 Rebreather (-3% Rover Crew Risk & -40% Cosmic Radiation)",
        scrapCost = 16.0,
        fuelCost = 4.0,
        baseCraftTimeSeconds = 11,
        accentColor = RadTeal
    ),
    HAZMAT_SUIT(
        id = "HAZMAT_SUIT",
        title = "Mark-IV Pressurized EVA Space Suit",
        category = CraftItemCategory.HUNTING,
        description = "Lead-polymer pressurized astronaut EVA suit for deep-crater rover treks and solar proton storms.",
        effectSummary = "+1 EVA Space Suit (-4% Rover Crew Risk & -70% Cosmic Ray Intake)",
        scrapCost = 28.0,
        fuelCost = 8.0,
        goldCost = 12.0,
        baseCraftTimeSeconds = 16,
        accentColor = CryoCyan
    ),
    MEDKIT_RADAWAY_PACK(
        id = "MEDKIT_RADAWAY_PACK",
        title = "Cryo-Trauma & Anti-Rad Medpack",
        category = CraftItemCategory.DEFENSE,
        description = "Pressurized bio-foam trauma kit and potassium-iodide chelators to flush cosmic radiation and heal injured Astronauts.",
        effectSummary = "+1 Flight Medkit (+35% Health) & +1 Anti-Rad Dose (-40 mSv Rads)",
        scrapCost = 12.0,
        fuelCost = 5.0,
        goldCost = 8.0,
        baseCraftTimeSeconds = 8,
        accentColor = SurplusGreen
    );
}

data class ActiveItemCrafting(
    val item: CraftableItemType,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val rushGoldCost: Double
)

enum class HuntingTarget(
    val id: String,
    val title: String,
    val subtitle: String,
    val spearsRequired: Int,
    val trapsRequired: Int,
    val requiresCrossbow: Boolean,
    val baseFoodReward: Double,
    val baseWaterReward: Double,
    val baseGoldReward: Double,
    val baseCoresReward: Double,
    val baseXpReward: Double,
    val accentColor: Color
) {
    DUNE_RAZORBACK(
        id = "DUNE_RAZORBACK",
        title = "Harvest Lava-Tube Fungus Pod",
        subtitle = "Fast-growing subterranean geothermal mushroom cluster rich in protein & water ice",
        spearsRequired = 1,
        trapsRequired = 0,
        requiresCrossbow = false,
        baseFoodReward = 10.0,
        baseWaterReward = 3.0,
        baseGoldReward = 2.5,
        baseCoresReward = 0.0,
        baseXpReward = 14.0,
        accentColor = SurplusGreen
    ),
    CANYON_PACK_STALKER(
        id = "CANYON_PACK_STALKER",
        title = "Extract Valles Brine Crustaceans",
        subtitle = "Deploy automated spore-traps into warm hydrothermal vents for high-protein biomass & gold",
        spearsRequired = 1,
        trapsRequired = 1,
        requiresCrossbow = false,
        baseFoodReward = 21.0,
        baseWaterReward = 6.0,
        baseGoldReward = 6.0,
        baseCoresReward = 0.0,
        baseXpReward = 28.0,
        accentColor = SolarAmber
    ),
    APEX_RAD_BEHEMOTH(
        id = "APEX_RAD_BEHEMOTH",
        title = "Excavate Martian Lithic Behemoth",
        subtitle = "Requires Solar-Arc Harvester: Silicon-armored lava-tube organism yielding Rations, Gold & Xeno-Core",
        spearsRequired = 2,
        trapsRequired = 1,
        requiresCrossbow = true,
        baseFoodReward = 42.0,
        baseWaterReward = 11.0,
        baseGoldReward = 14.0,
        baseCoresReward = 1.0,
        baseXpReward = 55.0,
        accentColor = BioluminescentViolet
    ),
    GLOWING_LEVIATHAN_WYRM(
        id = "GLOWING_LEVIATHAN_WYRM",
        title = "Core-Harvest Olympus Magma Leviathan",
        subtitle = "Apex Caldera Organism (Requires Harvester + 2 Lances + 2 Pods): Food, Water, Gold & +2 Xeno-Cores",
        spearsRequired = 2,
        trapsRequired = 2,
        requiresCrossbow = true,
        baseFoodReward = 68.0,
        baseWaterReward = 22.0,
        baseGoldReward = 26.0,
        baseCoresReward = 2.0,
        baseXpReward = 95.0,
        accentColor = HazardRed
    );
}

enum class FishingSpot(
    val id: String,
    val title: String,
    val subtitle: String,
    val requiresSnareNet: Boolean,
    val requiresHarpoonSpear: Boolean,
    val baseFoodReward: Double,
    val baseWaterReward: Double,
    val baseGoldReward: Double,
    val baseXpReward: Double,
    val accentColor: Color
) {
    CRATER_SHALLOWS_TROUT(
        id = "CRATER_SHALLOWS_TROUT",
        title = "Melt Core: Sub-Glacial Ice & Spirulina",
        subtitle = "Zero-gear thermal borehole extraction from Martian permafrost for Kelp Rations & Glacial Water",
        requiresSnareNet = false,
        requiresHarpoonSpear = false,
        baseFoodReward = 6.0,
        baseWaterReward = 4.5,
        baseGoldReward = 1.2,
        baseXpReward = 12.0,
        accentColor = CryoCyan
    ),
    RAD_KELP_CATFISH(
        id = "RAD_KELP_CATFISH",
        title = "Siphon Hydrothermal Kelp Basin",
        subtitle = "Deploy a spore-pod filter into warm lava-tube aquifers for nutrient kelp & gold nuggets",
        requiresSnareNet = true,
        requiresHarpoonSpear = false,
        baseFoodReward = 13.0,
        baseWaterReward = 8.0,
        baseGoldReward = 3.5,
        baseXpReward = 24.0,
        accentColor = SurplusGreen
    ),
    ABYSSAL_VAULT_STURGEON(
        id = "ABYSSAL_VAULT_STURGEON",
        title = "Deep-Lance Polar Ice-Cap Cavern",
        subtitle = "Uses 1 Thermal Core Lance: Pressurized sub-glacial pocket rich in Bio-Rations, Water & Gold Ore",
        requiresSnareNet = false,
        requiresHarpoonSpear = true,
        baseFoodReward = 24.0,
        baseWaterReward = 13.0,
        baseGoldReward = 8.0,
        baseXpReward = 42.0,
        accentColor = SolarAmber
    );
}

enum class WastelandExpeditionType(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationSeconds: Int,
    val deathChancePercent: Int = 10, // Explicit 10% chance the astronaut is lost in a dust storm!
    val goldReward: Double,
    val foodReward: Double,
    val waterReward: Double,
    val scrapReward: Double,
    val fuelReward: Double,
    val coresReward: Double = 0.0,
    val armorReward: Int = 1, // EVA Suit Plating salvaged on rover run (+12 DEF each)
    val weaponsReward: Int = 1, // Laser Defense Modules salvaged on rover run (+15 DEF each)
    val xpReward: Double,
    val accentColor: Color
) {
    SUPERMARKET_SUPPLY_RUN(
        id = "SUPERMARKET_SUPPLY_RUN",
        title = "Jezero Crater SupplyPod Rover Run",
        subtitle = "Dispatch 1 Astronaut for EVA Plating/Lasers (+27 DEF), Rations, Water Ice & Gold (10% Risk)",
        durationSeconds = 12,
        deathChancePercent = 10,
        goldReward = 12.0,
        foodReward = 16.0,
        waterReward = 12.0,
        scrapReward = 11.0,
        fuelReward = 4.5,
        armorReward = 1,
        weaponsReward = 1,
        xpReward = 40.0,
        accentColor = SurplusGreen
    ),
    OLD_BANK_VAULT_HEIST(
        id = "OLD_BANK_VAULT_HEIST",
        title = "Viking & Pathfinder Lander Salvage",
        subtitle = "Dispatch 1 Astronaut for Shielding & Lasers (+27 DEF), Gold Ore, Alloy & Propellant (10% Risk)",
        durationSeconds = 16,
        deathChancePercent = 10,
        goldReward = 26.0,
        foodReward = 8.0,
        waterReward = 7.0,
        scrapReward = 28.0,
        fuelReward = 11.0,
        armorReward = 1,
        weaponsReward = 1,
        xpReward = 65.0,
        accentColor = XpGold
    ),
    GLOWING_CRATER_CORE_RAID(
        id = "GLOWING_CRATER_CORE_RAID",
        title = "Olympus Mons Caldera RTG Recovery",
        subtitle = "Dispatch 1 Astronaut for +2 Shielding & +1 Laser (+39 DEF), Gold, Fuel & Xeno-Core (10% Risk)",
        durationSeconds = 20,
        deathChancePercent = 10,
        goldReward = 42.0,
        foodReward = 18.0,
        waterReward = 15.0,
        scrapReward = 35.0,
        fuelReward = 20.0,
        coresReward = 1.0,
        armorReward = 2,
        weaponsReward = 1,
        xpReward = 110.0,
        accentColor = BioluminescentViolet
    ),
    SUNKEN_SUBMARINE_REACTOR(
        id = "SUNKEN_SUBMARINE_REACTOR",
        title = "Valles Marineris Abyssal Rift Trek",
        subtitle = "Deep canyon rover descent for +2 Shielding & +2 Lasers (+54 DEF), Gold & +2 Cores (10% Risk)",
        durationSeconds = 25,
        deathChancePercent = 10,
        goldReward = 62.0,
        foodReward = 24.0,
        waterReward = 24.0,
        scrapReward = 55.0,
        fuelReward = 32.0,
        coresReward = 2.0,
        armorReward = 2,
        weaponsReward = 2,
        xpReward = 160.0,
        accentColor = CryoCyan
    );
}

data class ActiveDwellerExpedition(
    val type: WastelandExpeditionType,
    val remainingSeconds: Int,
    val totalSeconds: Int
)

enum class VaultOperationalPosture(
    val id: String,
    val shortLabel: String,
    val title: String,
    val summary: String,
    val outputMultiplier: Double,
    val upkeepMultiplier: Double,
    val megawattDemandMultiplier: Double,
    val accentColor: Color
) {
    ECO_CONSERVATION(
        id = "ECO_CONSERVATION",
        shortLabel = "LOW-POWER (-40% DRAIN)",
        title = "Low-Power Life-Support Protocol",
        summary = "Cuts Habitat & Bay Resource Upkeep by -40% and MW draw by -15% (-15% Output) — ideal to survive Dust Storms & restore Dome Shields!",
        outputMultiplier = 0.85,
        upkeepMultiplier = 0.60,
        megawattDemandMultiplier = 0.85,
        accentColor = SurplusGreen
    ),
    STANDARD(
        id = "STANDARD",
        shortLabel = "NOMINAL (100%)",
        title = "Nominal Mission Control Posture",
        summary = "Balanced 100% Mars Outpost resource synthesis, standard bay upkeep, and nominal Megawatt load.",
        outputMultiplier = 1.0,
        upkeepMultiplier = 1.0,
        megawattDemandMultiplier = 1.0,
        accentColor = RadTeal
    ),
    OVERCLOCK(
        id = "OVERCLOCK",
        shortLabel = "OVERDRIVE (+30%)",
        title = "High-Thrust Colony Overdrive",
        summary = "Boosts all Mars Habitat Resource & XP Output by +30%, but increases Bay Upkeep by +25% and MW draw by +15%.",
        outputMultiplier = 1.30,
        upkeepMultiplier = 1.25,
        megawattDemandMultiplier = 1.15,
        accentColor = SolarAmber
    );

    companion object {
        fun fromId(id: String): VaultOperationalPosture {
            return entries.firstOrNull { it.id == id } ?: STANDARD
        }
    }
}

enum class SiegeCountermeasureType(
    val id: String,
    val buttonLabel: String,
    val description: String,
    val defenseReductionPercent: Int,
    val bonusIntegrityRestore: Int,
    val ammoCost: Int = 0,
    val bioFuelCost: Double = 0.0,
    val scrapCost: Double = 0.0,
    val accentColor: Color
) {
    FLAK_VOLLEY(
        id = "FLAK_VOLLEY",
        buttonLabel = "LASER SALVO (8 AMMO)",
        description = "Fires point-defense laser salvos to vaporize incoming meteoroids (-25% Storm DEF Req & +10s timer).",
        defenseReductionPercent = 25,
        bonusIntegrityRestore = 0,
        ammoCost = 8,
        accentColor = SolarAmber
    ),
    EMP_PULSE(
        id = "EMP_PULSE",
        buttonLabel = "ION DEFLECTOR (20 FUEL)",
        description = "Projects an electrostatic magnetosphere pulse that disperses charged dust vortices (-35% Storm DEF Req).",
        defenseReductionPercent = 35,
        bonusIntegrityRestore = 0,
        bioFuelCost = 20.0,
        accentColor = CryoCyan
    ),
    EMERGENCY_WELD(
        id = "EMERGENCY_WELD",
        buttonLabel = "DOME SEAL (25 SCRAP)",
        description = "Sinters titanium Whipple-shield plates mid-storm (+25% Airlock Integrity immediately & -15% Storm DEF Req).",
        defenseReductionPercent = 15,
        bonusIntegrityRestore = 25,
        scrapCost = 25.0,
        accentColor = SurplusGreen
    )
}

object PickaxeTier {
    fun nameForLevel(level: Int): String = when {
        level <= 1 -> "Handheld Regolith Pickaxe"
        level == 2 -> "Titanium Core Hammer"
        level == 3 -> "Ultrasonic Basalt Splitter"
        level == 4 -> "Pneumatic Permafrost Drill"
        level in 5..6 -> "Laser-Tipped Regolith Cutter"
        level in 7..9 -> "Plasma Core Lava-Tube Bore"
        else -> "Kilopower Mantle Excavator Mk.$level"
    }
}

data class RaiderSiegeThreat(
    val id: Long,
    val title: String,
    val subtitle: String,
    val requiredDefenseRating: Int,
    val bountyGoldReward: Double,
    val bountyScrapReward: Double,
    val bountyXpReward: Double,
    val remainingSeconds: Int,
    val dayNumber: Int = 1,
    val isDay10MegaSiege: Boolean = false,
    val isNightMutantSwarm: Boolean = false,
    val bossClassTitle: String = "",
    val countermeasuresUsedCount: Int = 0,
    val weakenedByPercent: Int = 0,
    val targetFacilityId: String = "", // e.g., "ATMOSPHERIC_CONDENSER", "BIO_SLUDGE_TURBINE"
    val targetFacilityName: String = ""
)

enum class TradeTown(
    val id: String,
    val displayName: String,
    val distanceKm: Int,
    val description: String,
    val favoredResource1: ResourceType,
    val favoredResource2: ResourceType,
    val favoredPhase: DayNightPhase,
    val baseConvoyDurationSeconds: Int,
    val baseGoldPayout: Double,
    val bonusRareDropType: ResourceType,
    val accentColor: Color
) {
    IRONFORGE_BASTION(
        id = "IRONFORGE_BASTION",
        displayName = "Ares Prime Foundry Dome",
        distanceKm = 42,
        description = "Industrial crater colony. Pays +35% premium Gold for Regolith Alloy & Methane-LOX Fuel (peaks during Solar Sol).",
        favoredResource1 = ResourceType.SCRAP,
        favoredResource2 = ResourceType.BIO_FUEL,
        favoredPhase = DayNightPhase.SCORCHING_DAY,
        baseConvoyDurationSeconds = 20,
        baseGoldPayout = 55.0,
        bonusRareDropType = ResourceType.THORIUM_RODS,
        accentColor = SolarAmber
    ),
    OASIS_SPIRE(
        id = "OASIS_SPIRE",
        displayName = "Valles Bio-Dome Spire",
        distanceKm = 58,
        description = "Canyon hydroponic settlement. Pays +35% premium Gold for Glacial Water & Bio-Rations (peaks at Dawn/Dusk).",
        favoredResource1 = ResourceType.WATER,
        favoredResource2 = ResourceType.RATIONS,
        favoredPhase = DayNightPhase.DAWN,
        baseConvoyDurationSeconds = 25,
        baseGoldPayout = 70.0,
        bonusRareDropType = ResourceType.AETHER_CELLS,
        accentColor = CryoCyan
    ),
    NEON_VAULT_88(
        id = "NEON_VAULT_88",
        displayName = "Phobos Orbital Skyhook",
        distanceKm = 85,
        description = "Deep-space orbital research tether. Pays +40% premium Gold for Orbital Telemetry & Methane-LOX (peaks during Cryo-Night).",
        favoredResource1 = ResourceType.RESEARCH_DATA,
        favoredResource2 = ResourceType.BIO_FUEL,
        favoredPhase = DayNightPhase.RAD_NIGHT,
        baseConvoyDurationSeconds = 32,
        baseGoldPayout = 105.0,
        bonusRareDropType = ResourceType.MUTAGEN_CORES,
        accentColor = BioluminescentViolet
    );
}

enum class RareSyndicateItem(
    val id: String,
    val resourceType: ResourceType,
    val title: String,
    val usageSummary: String,
    val baseGoldCost: Double,
    val batchSize: Double = 1.0
) {
    THORIUM_FUEL_ROD(
        id = "THORIUM_FUEL_ROD",
        resourceType = ResourceType.THORIUM_RODS,
        title = "Kilopower Uranium-RTG Rod",
        usageSummary = "Used in Kilopower RTG Plants, 60s Grid Overclock, Pressurized Rover Convoys & Ion Shield Batteries.",
        baseGoldCost = 38.0,
        batchSize = 1.0
    ),
    AETHER_CAPACITOR(
        id = "AETHER_CAPACITOR",
        resourceType = ResourceType.AETHER_CELLS,
        title = "Graphene Solar Super-Capacitor",
        usageSummary = "Used in Rover Traction Drives, Heliostat Arrays, Solar-Arc Harvesters & Point-Defense Lasers.",
        baseGoldCost = 42.0,
        batchSize = 1.0
    ),
    MUTAGENIC_CORE(
        id = "MUTAGENIC_CORE",
        resourceType = ResourceType.MUTAGEN_CORES,
        title = "Martian Xenobiology Core",
        usageSummary = "Rare extremophile bio-catalyst used in Apex Mission Directives, Orbital Substations, and Ion Shields.",
        baseGoldCost = 55.0,
        batchSize = 1.0
    );
}

enum class PowerStationType(
    val id: String,
    val displayName: String,
    val description: String,
    val baseMegawattsPerLevel: Double,
    val fuelUpkeepPerLevel: Double = 0.0,
    val baseScrapCost: Double,
    val baseGoldCost: Double,
    val baseThoriumCost: Double = 0.0,
    val baseAetherCost: Double = 0.0,
    val baseCoresCost: Double = 0.0,
    val isSolarDependent: Boolean = false,
    val gridMultiplierPerLevel: Double = 0.0,
    val accentColor: Color
) {
    BIO_SLUDGE_TURBINE(
        id = "BIO_SLUDGE_TURBINE",
        displayName = "Methane-LOX Fuel Cell Turbine",
        description = "High-efficiency Sabatier turbine that burns small amounts of Methane-LOX for steady 24/7 Megawatts (MW) and retains 80% backup thermal power if fuel runs dry.",
        baseMegawattsPerLevel = 16.0,
        fuelUpkeepPerLevel = 0.09,
        baseScrapCost = 35.0,
        baseGoldCost = 20.0,
        accentColor = RustCopper
    ),
    SOLAR_HELIOSTAT_TOWER(
        id = "SOLAR_HELIOSTAT_TOWER",
        displayName = "Martian Heliostat Solar Array",
        description = "Built with Graphene Super-Capacitors & molten-salt storage. Surges +50% MW during Perihelion Sol and retains 65% thermal MW at Night with minimal fuel upkeep.",
        baseMegawattsPerLevel = 26.0,
        fuelUpkeepPerLevel = 0.02,
        baseScrapCost = 60.0,
        baseGoldCost = 35.0,
        baseAetherCost = 1.0,
        isSolarDependent = true,
        accentColor = SolarAmber
    ),
    THORIUM_FISSION_REACTOR(
        id = "THORIUM_FISSION_REACTOR",
        displayName = "Kilopower Fission RTG Reactor",
        description = "Powered by rare Kilopower RTG Rods. Generates immense 24/7 dust-storm-proof MW & +8% output/lv with closed-loop coolant.",
        baseMegawattsPerLevel = 52.0,
        fuelUpkeepPerLevel = 0.16,
        baseScrapCost = 110.0,
        baseGoldCost = 65.0,
        baseThoriumCost = 2.0,
        gridMultiplierPerLevel = 0.08,
        accentColor = WarningOrange
    ),
    AETHER_RESONANCE_SUBSTATION(
        id = "AETHER_RESONANCE_SUBSTATION",
        displayName = "Orbital Microwave Beam Substation",
        description = "Uses Super-Capacitors & Xenobiology Cores to amplify total Mars Outpost Grid MW by +20%/lv with low stabilizer draw.",
        baseMegawattsPerLevel = 32.0,
        fuelUpkeepPerLevel = 0.07,
        baseScrapCost = 150.0,
        baseGoldCost = 90.0,
        baseAetherCost = 2.0,
        baseCoresCost = 1.0,
        gridMultiplierPerLevel = 0.20,
        accentColor = CryoCyan
    );
}

data class ActiveConvoyRun(
    val destination: TradeTown,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val expectedGoldReward: Double,
    val expectedRareResource: ResourceType,
    val expectedRareAmount: Double,
    val expectedXpReward: Double
)

enum class SalvageNodeType(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryResource: ResourceType,
    val secondaryResource: ResourceType,
    val basePrimaryYield: Double,
    val baseSecondaryYield: Double,
    val baseGoldYield: Double,
    val maxDurability: Int,
    val cacheDropCount: Int,
    val isNocturnal: Boolean,
    val accentColor: Color
) {
    ARMORED_CONVOY_WRECK(
        id = "ARMORED_CONVOY_WRECK",
        title = "Crashed Ares Cargo Lander Hull",
        subtitle = "Cut titanium hull plating for Regolith Alloy, Methane-LOX & Martian Gold Ore",
        primaryResource = ResourceType.SCRAP,
        secondaryResource = ResourceType.BIO_FUEL,
        basePrimaryYield = 4.5,
        baseSecondaryYield = 1.6,
        baseGoldYield = 2.8,
        maxDurability = 4,
        cacheDropCount = 1,
        isNocturnal = false,
        accentColor = SolarAmber
    ),
    DEW_VAPOR_GEYSER(
        id = "DEW_VAPOR_GEYSER",
        title = "Sub-Surface Permafrost Ice Vein",
        subtitle = "Melt glacial permafrost pockets for Glacial Water, Bio-Rations & Gold dust",
        primaryResource = ResourceType.WATER,
        secondaryResource = ResourceType.RATIONS,
        basePrimaryYield = 4.0,
        baseSecondaryYield = 2.6,
        baseGoldYield = 2.2,
        maxDurability = 3,
        cacheDropCount = 1,
        isNocturnal = false,
        accentColor = CryoCyan
    ),
    OVERGROWN_BIO_DOME(
        id = "OVERGROWN_BIO_DOME",
        title = "Abandoned Pioneer Hydro-Pod",
        subtitle = "Salvage vacuum-sealed rations, methane fuel cells & gold Foil contacts",
        primaryResource = ResourceType.RATIONS,
        secondaryResource = ResourceType.BIO_FUEL,
        basePrimaryYield = 4.2,
        baseSecondaryYield = 2.0,
        baseGoldYield = 2.4,
        maxDurability = 3,
        cacheDropCount = 1,
        isNocturnal = false,
        accentColor = SurplusGreen
    ),
    ENCRYPTED_ORBITAL_POD(
        id = "ENCRYPTED_ORBITAL_POD",
        title = "De-Orbited Telemetry Black-Box",
        subtitle = "Crack orbital probe cores for Telemetry Data, Alloy & Gold circuitry",
        primaryResource = ResourceType.RESEARCH_DATA,
        secondaryResource = ResourceType.SCRAP,
        basePrimaryYield = 3.4,
        baseSecondaryYield = 3.6,
        baseGoldYield = 3.8,
        maxDurability = 4,
        cacheDropCount = 2,
        isNocturnal = true,
        accentColor = XpGold
    ),
    BIOLUMINESCENT_METEOR_VEIN(
        id = "BIOLUMINESCENT_METEOR_VEIN",
        title = "Chondrite Asteroid & Xeno-Core Fissure",
        subtitle = "Nocturnal impact crater rich in Xenobiology Cores, Telemetry & Gold Ore",
        primaryResource = ResourceType.MUTAGEN_CORES,
        secondaryResource = ResourceType.RESEARCH_DATA,
        basePrimaryYield = 1.0,
        baseSecondaryYield = 3.2,
        baseGoldYield = 4.5,
        maxDurability = 5,
        cacheDropCount = 2,
        isNocturnal = true,
        accentColor = BioluminescentViolet
    ),
    RADIOACTIVE_SLUDGE_TANKER(
        id = "RADIOACTIVE_SLUDGE_TANKER",
        title = "Pressurized Sabatier Propellant Tank",
        subtitle = "Siphon volatile Methane-LOX Fuel, Alloy & Gold catalyst plates",
        primaryResource = ResourceType.BIO_FUEL,
        secondaryResource = ResourceType.SCRAP,
        basePrimaryYield = 3.8,
        baseSecondaryYield = 3.6,
        baseGoldYield = 3.2,
        maxDurability = 4,
        cacheDropCount = 2,
        isNocturnal = true,
        accentColor = RustCopper
    );
}

data class SalvageNodeSlot(
    val slotIndex: Int,
    val nodeType: SalvageNodeType,
    val currentProgress: Int = 0,
    val maxProgress: Int = nodeType.maxDurability
)

data class HireableCandidate(
    val id: Long,
    val name: String,
    val callsign: String,
    val specialtyClass: WorkerClassType,
    val traitSummary: String,
    val rationsCost: Double,
    val waterCost: Double,
    val scrapCost: Double,
    val goldSigningFee: Double,
    val bonusClassXp: Double,
    val isUniqueDweller: Boolean = false,
    val resourceYieldBonusPercent: Int = 0
)

object CaravanCandidateGenerator {
    const val UNIQUE_DWELLER_CHANCE_PERCENT = 8 // Rare 8% chance to recruit a Mission Specialist Commander!

    private val firstNames = listOf("Watney", "Lewis", "Vogel", "Beck", "Johanssen", "Martinez", "Kael", "Vesper", "Juno", "Dax", "Lyra", "Tark")
    private val callsigns = listOf(
        "Red-Dust", "Star-Nav", "Ice-Core", "Sol-Walker", "MOXIE-Tech",
        "Apex-EVA", "Crater-Hound", "Kilopower", "Tharsis", "Sabatier", "Permafrost", "Phobos"
    )
    private val uniqueNames = listOf("Commander Vance", "Dr. Aria", "Capt. Gideon", "Dr. Nova", "Chief Orion", "Col. Kratos", "Valkyrie", "Zephyr")
    private val uniqueTitles = listOf(
        "Ares Mission Director", "JPL Chief Botanist", "Orbital Flight Surgeon", "Prime Astro-Engineer",
        "Kilopower Architect", "Lava-Tube Pioneer", "Valles Pathfinder", "Master Terraformer"
    )

    fun generateBoard(
        totalHiredSoFar: Int,
        commandLevel: Int,
        discountMult: Double = 1.0,
        forceUniqueRoll: Boolean? = null,
        bonusUniqueChancePercent: Int = 0
    ): List<HireableCandidate> {
        return (0..2).map { idx ->
            val targetClass = when (idx) {
                0 -> WorkerClassType.SCRAPPER
                1 -> WorkerClassType.HYDRO_ENGINEER
                else -> if (Random.nextBoolean()) WorkerClassType.NIGHT_STALKER else WorkerClassType.TECH_SAVANT
            }
            val effectiveUniqueChance = (UNIQUE_DWELLER_CHANCE_PERCENT + bonusUniqueChancePercent).coerceIn(8, 50)
            val isUnique = forceUniqueRoll ?: (Random.nextInt(100) < effectiveUniqueChance)
            val scale = (1.0 + (totalHiredSoFar + idx) * 0.35) * discountMult
            val goldFee = (18.0 * scale * (if (isUnique) 1.25 else 1.0)).roundToInt().toDouble().coerceAtLeast(12.0)
            val bonusXp = ((40.0 + commandLevel * 15.0) * (if (isUnique) 2.5 else 1.0)).roundToInt().toDouble()

            val trait = if (isUnique) {
                "★ ARES MISSION COMMANDER (8% RARE): +40% All-Resource Synthesis Ability (Habitat, Core Drill, EVA Harvest, Ice Bore & Rovers) + ${bonusXp.toInt()} Class XP!"
            } else {
                when (targetClass) {
                    WorkerClassType.SCRAPPER -> "Regolith Drill Engineer (+${bonusXp.toInt()} XP • 8% Commander Chance • Takes 2 Food & 2 Water/Sol)"
                    WorkerClassType.HYDRO_ENGINEER -> "Cryo-Hydrologist & Botanist (+${bonusXp.toInt()} XP • 8% Commander Chance • Takes 2 Food & 2 Water/Sol)"
                    WorkerClassType.NIGHT_STALKER -> "EVA Rover Pathfinder (+${bonusXp.toInt()} XP • 8% Commander Chance • Takes 2 Food & 2 Water/Sol)"
                    WorkerClassType.TECH_SAVANT -> "Astro-Systems & RTG Savant (+${bonusXp.toInt()} XP • 8% Commander Chance • Takes 2 Food & 2 Water/Sol)"
                }
            }

            HireableCandidate(
                id = System.currentTimeMillis() + idx * 17L + Random.nextLong(1000),
                name = if (isUnique) uniqueNames.random().replaceFirstChar { it.uppercase() } else firstNames.random(),
                callsign = if (isUnique) uniqueTitles.random() else callsigns.random(),
                specialtyClass = targetClass,
                traitSummary = trait,
                rationsCost = 0.0,
                waterCost = 0.0,
                scrapCost = 0.0,
                goldSigningFee = goldFee,
                bonusClassXp = bonusXp,
                isUniqueDweller = isUnique,
                resourceYieldBonusPercent = if (isUnique) 40 else 0
            )
        }
    }
}

data class VeteranPerk(
    val requiredLevel: Int,
    val title: String,
    val description: String
)

enum class WorkerClassType(
    val id: String,
    val displayName: String,
    val roleSummary: String,
    val favoredPhases: List<DayNightPhase>,
    val primaryResource: ResourceType,
    val baseOutputPerSecond: Double,
    val dayProductivityMultiplier: Double,
    val nightProductivityMultiplier: Double,
    val accentColor: Color,
    val perks: List<VeteranPerk>
) {
    SCRAPPER(
        id = "SCRAPPER",
        displayName = "Regolith Drill Engineer",
        roleSummary = "Sinters Regolith Alloy (with Martian Gold Ore sifting), cracks Core Crates & mans Whipple Shields (+8 Defense each).",
        favoredPhases = listOf(DayNightPhase.DAWN, DayNightPhase.SCORCHING_DAY),
        primaryResource = ResourceType.SCRAP,
        baseOutputPerSecond = 1.15,
        dayProductivityMultiplier = 1.35,
        nightProductivityMultiplier = 0.80,
        accentColor = SolarAmber,
        perks = listOf(
            VeteranPerk(5, "Laser Sintering Cutters", "+25% Drill Engineer speed & +20% manual vein Alloy."),
            VeteranPerk(10, "Sabatier Off-Gas Siphoning", "Drill Engineers also refine +0.20 Methane-LOX Fuel/s each."),
            VeteranPerk(15, "Heated EVA Visors", "Halves the Cryo-Night (-110°C) productivity penalty."),
            VeteranPerk(20, "Mantle Bore Titans", "+45% Regolith Alloy output & auto-cracks 2x more core crates.")
        )
    ),
    HYDRO_ENGINEER(
        id = "HYDRO_ENGINEER",
        displayName = "Cryo-Hydrologist & Botanist",
        roleSummary = "Melts permafrost ice & cultivates aeroponic tubers into Glacial Water, Bio-Rations & Propellant (+30% Dawn/Dusk).",
        favoredPhases = listOf(DayNightPhase.DAWN, DayNightPhase.DUSK),
        primaryResource = ResourceType.WATER,
        baseOutputPerSecond = 0.95,
        dayProductivityMultiplier = 1.15,
        nightProductivityMultiplier = 1.15,
        accentColor = CryoCyan,
        perks = listOf(
            VeteranPerk(5, "Sublimation Recovery Membranes", "+25% Water Ice & Bio-Rations yield from Hydrologists."),
            VeteranPerk(10, "Sabatier Methanation Loop", "+0.30 Methane-LOX Fuel/s per assigned Cryo-Hydrologist."),
            VeteranPerk(15, "Closed-Loop Aeroponics", "Reduces Bio-Dome water ice upkeep by 50%."),
            VeteranPerk(20, "Sub-Glacial Fusion Pumps", "+40% all-module output & +15% Depot Storage cap.")
        )
    ),
    NIGHT_STALKER(
        id = "NIGHT_STALKER",
        displayName = "EVA Rover Pathfinder",
        roleSummary = "Surface reconnaissance specialist (+15% EVA Harvest Food & +15 Defense each), pilots Rovers & extracts Xeno-Cores.",
        favoredPhases = listOf(DayNightPhase.DUSK, DayNightPhase.RAD_NIGHT),
        primaryResource = ResourceType.MUTAGEN_CORES,
        baseOutputPerSecond = 0.07,
        dayProductivityMultiplier = 0.65,
        nightProductivityMultiplier = 1.65,
        accentColor = BioluminescentViolet,
        perks = listOf(
            VeteranPerk(5, "Lidar Terrain Optics", "+30% Xenobiology Core harvesting & +15% Rover Convoy Gold."),
            VeteranPerk(10, "Solar-Reflective EVA Suits", "Removes daytime thermal sluggishness (+35% Sol output)."),
            VeteranPerk(15, "Nocturnal Regolith Hauling", "Pathfinders bring back +0.85 Alloy/s & +0.35 Fuel/s at night."),
            VeteranPerk(20, "Olympus Vanguard Protocol", "Doubles Xenobiology Core yield during Cryo-Radiation Night.")
        )
    ),
    TECH_SAVANT(
        id = "TECH_SAVANT",
        displayName = "Astro-Systems Savant",
        roleSummary = "Optimizes Solar/RTG Grids & Point-Defense Lasers, decodes Orbital Telemetry & boosts Mission XP (+15%).",
        favoredPhases = listOf(DayNightPhase.DUSK, DayNightPhase.RAD_NIGHT),
        primaryResource = ResourceType.RESEARCH_DATA,
        baseOutputPerSecond = 0.55,
        dayProductivityMultiplier = 1.10,
        nightProductivityMultiplier = 1.35,
        accentColor = XpGold,
        perks = listOf(
            VeteranPerk(5, "Flight Computer Co-Processors", "+25% Orbital Telemetry & +15% Power Grid MW."),
            VeteranPerk(10, "Autonomous Maintenance Drones", "+10% global production across all Mars Habitat bays."),
            VeteranPerk(15, "Mission Control Simulators", "All Astronaut Specialist Classes gain +35% faster shift XP."),
            VeteranPerk(20, "Deep-Space Relay Uplink", "+35% Telemetry Data & -20% Orbital Power cooldowns.")
        )
    );

    companion object {
        fun xpForNextClassLevel(level: Int): Double {
            return 210.0 * level.toDouble().pow(1.75)
        }
    }
}

enum class BuildingCategory(val label: String) {
    EXTRACTION("Ice, Regolith & Bio-Domes"),
    REFINERY("Sabatier & Deep-Space Tech"),
    INFRASTRUCTURE("Pressurized Habs & Lava Tubes")
}

enum class BuildingType(
    val id: String,
    val displayName: String,
    val category: BuildingCategory,
    val requiredCommandLevel: Int,
    val description: String,
    val gameplayNeedSummary: String,
    val primaryResource: ResourceType,
    val baseOutputPerLevel: Double,
    val megawattDemandPerLevel: Double,
    val baseBuildTimeSeconds: Int,
    val manualSalvageBonusDesc: String,
    val waterUpkeepPerLevel: Double = 0.0,
    val baseScrapCost: Double,
    val baseWaterCost: Double = 0.0,
    val baseFuelCost: Double = 0.0,
    val baseCoresCost: Double = 0.0,
    val costGrowthFactor: Double = 1.40,
    val favoredWorkerClass: WorkerClassType?,
    val storageBonusPerLevel: Double = 0.0,
    val populationBonusPerLevel: Int = 0,
    val defenseBonusPerLevel: Int = 0
) {
    SURVIVOR_HAB_BLOCK(
        id = "SURVIVOR_HAB_BLOCK",
        displayName = "Pressurized Astronaut Hab",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 1,
        description = "Radiation-shielded pressurized quarters required to house Astronauts (+4 Crew per bay level, up to 30 Max Astronauts). Each upgrade merges lava-tube bays and expands Depot Storage.",
        gameplayNeedSummary = "REQUIRED TO RECRUIT ASTRONAUTS: +4 Max Crew/Lv (Up to 30 Max Crew), +Storage Cap/Lv & -4% Ration Upkeep/Lv",
        primaryResource = ResourceType.RATIONS,
        baseOutputPerLevel = 0.35,
        megawattDemandPerLevel = 3.5,
        baseBuildTimeSeconds = 12,
        manualSalvageBonusDesc = "+4 Max Astronaut Capacity (Max 30), +Storage Cap & +5% Crew Morale per level",
        baseScrapCost = 42.0,
        baseWaterCost = 18.0,
        favoredWorkerClass = null,
        populationBonusPerLevel = 4
    ),
    SALVAGE_YARD(
        id = "SALVAGE_YARD",
        displayName = "Regolith Smelter & Gold Sifter",
        category = BuildingCategory.EXTRACTION,
        requiredCommandLevel = 1,
        description = "High-voltage microwave regolith sinterers, gold-ore magnetic separators & methane vents. Sifts Martian Gold Ore and trace Methane-LOX Fuel while smelting Regolith Alloy.",
        gameplayNeedSummary = "GOLD ORE + METHANE TRICKLE: Max Laser Drill Lv = (2 + 2×Lv), Sifts Gold/s + Fuel/s from Regolith & +Storage Cap/Lv",
        primaryResource = ResourceType.SCRAP,
        baseOutputPerLevel = 1.55,
        megawattDemandPerLevel = 4.0,
        baseBuildTimeSeconds = 15,
        manualSalvageBonusDesc = "+25% Core Drill Alloy/Gold, Sifts Gold Ore & +Methane-LOX Fuel per Smelter upgrade level",
        baseScrapCost = 28.0,
        favoredWorkerClass = WorkerClassType.SCRAPPER
    ),
    ATMOSPHERIC_CONDENSER(
        id = "ATMOSPHERIC_CONDENSER",
        displayName = "Sub-Glacial Ice Extractor",
        category = BuildingCategory.EXTRACTION,
        requiredCommandLevel = 1,
        description = "Rodriguez-well thermal ice melters & perchlorate filters required for automated Glacial Water production (manual ice-core melting only until built). Expands storage cap per level.",
        gameplayNeedSummary = "REQUIRED FOR AUTO-WATER (MANUAL ICE MELT ONLY WITHOUT BAY): +3 Sol Water/Lv, +Storage Cap/Lv & +25% Brine Yield/Lv",
        primaryResource = ResourceType.WATER,
        baseOutputPerLevel = 1.25,
        megawattDemandPerLevel = 4.0,
        baseBuildTimeSeconds = 18,
        manualSalvageBonusDesc = "+25% Ice-Bore Yield & +3 Glacial Water every New Sol per level",
        baseScrapCost = 32.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER
    ),
    HYDROPONIC_GREENHOUSE(
        id = "HYDROPONIC_GREENHOUSE",
        displayName = "Aeroponic Potato & Kelp Dome",
        category = BuildingCategory.EXTRACTION,
        requiredCommandLevel = 1,
        description = "Pressurized full-spectrum LED bio-dome cultivating Martian potatoes & spirulina kelp required for automated Bio-Rations production (manual EVA harvest only until built).",
        gameplayNeedSummary = "REQUIRED FOR AUTO-FOOD (MANUAL EVA ONLY WITHOUT DOME): +4 Sol Food/Lv, +Storage Cap/Lv & +25% Spore-Pod Food/Lv",
        primaryResource = ResourceType.RATIONS,
        baseOutputPerLevel = 1.20,
        megawattDemandPerLevel = 5.0,
        baseBuildTimeSeconds = 21,
        manualSalvageBonusDesc = "+25% EVA Harvest Food & +4 Bio-Rations every New Sol per level",
        waterUpkeepPerLevel = 0.30,
        baseScrapCost = 40.0,
        baseWaterCost = 15.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER
    ),
    REINFORCED_SILO(
        id = "REINFORCED_SILO",
        displayName = "MOXIE O2 & Perchlorate Medbay",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 2,
        description = "Atmospheric CO2-to-O2 MOXIE scrubbers, perchlorate dust airlock showers & flight surgery medbay that scrub cosmic radiation, heal Astronauts, and add +350 Depot Storage/Lv.",
        gameplayNeedSummary = "REQUIRED FOR O2, RADS & STORM DEFENSE: Scrubs Rads, Heals Crew, +28 Dome Defense/Lv & +350 Storage/Lv",
        primaryResource = ResourceType.SCRAP,
        baseOutputPerLevel = 0.70,
        megawattDemandPerLevel = 4.0,
        baseBuildTimeSeconds = 24,
        manualSalvageBonusDesc = "Auto-scrubs Cosmic Rads, +28 Dome Defense, -4% Airlock Damage & +350 Storage/Lv",
        baseScrapCost = 85.0,
        baseWaterCost = 30.0,
        favoredWorkerClass = WorkerClassType.SCRAPPER,
        storageBonusPerLevel = 350.0,
        defenseBonusPerLevel = 28
    ),
    BIO_SLUDGE_REFINERY(
        id = "BIO_SLUDGE_REFINERY",
        displayName = "Sabatier Methane-LOX Refinery",
        category = BuildingCategory.REFINERY,
        requiredCommandLevel = 2,
        description = "Synthesizes liquid methane/oxygen rocket & turbine propellant from Martian CO2 and manufactures point-defense charges & hull repair alloy.",
        gameplayNeedSummary = "REQUIRED FOR TURRETS & DOME REPAIRS: Unlocks Laser/Shield Crafting, -10% Dome Repair Cost/Lv, +Fuel & +Storage Cap/Lv",
        primaryResource = ResourceType.BIO_FUEL,
        baseOutputPerLevel = 1.45,
        megawattDemandPerLevel = 6.0,
        baseBuildTimeSeconds = 27,
        manualSalvageBonusDesc = "-10% Airlock Dome Repair Cost & +25% Methane-LOX Yield per level",
        waterUpkeepPerLevel = 0.18,
        baseScrapCost = 115.0,
        baseWaterCost = 42.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER
    ),
    SIGNAL_RESEARCH_ARRAY(
        id = "SIGNAL_RESEARCH_ARRAY",
        displayName = "Deep-Space Telemetry Dish",
        category = BuildingCategory.REFINERY,
        requiredCommandLevel = 3,
        description = "High-gain X-band orbital dish & Doppler dust-storm radar that routes Mars Rover expeditions around lethal canyon storms and expands telemetry storage.",
        gameplayNeedSummary = "REQUIRED FOR SAFE ROVER TREKS: -1% Astronaut Loss Risk/Lv, +20% Rover Loot, +15% XP/Lv & +Storage Cap/Lv",
        primaryResource = ResourceType.RESEARCH_DATA,
        baseOutputPerLevel = 0.85,
        megawattDemandPerLevel = 9.0,
        baseBuildTimeSeconds = 30,
        manualSalvageBonusDesc = "-1% Mars Rover Expedition Risk & +15% Commander XP per level",
        baseScrapCost = 170.0,
        baseFuelCost = 40.0,
        favoredWorkerClass = WorkerClassType.TECH_SAVANT
    ),
    NOCTURNAL_EXTRACTION_RIG(
        id = "NOCTURNAL_EXTRACTION_RIG",
        displayName = "Xenobiology & Core Lab",
        category = BuildingCategory.REFINERY,
        requiredCommandLevel = 4,
        description = "Pressurized astrobiology & plasma physics lab that isolates Martian Xenobiology Cores, overclocks RTG/Solar grids, and expands high-tier storage.",
        gameplayNeedSummary = "REQUIRED FOR APEX MARS TECH: +12% Total Outpost Power MW/Lv, Unlocks Ion Shield, +Xeno-Cores & +Storage Cap/Lv",
        primaryResource = ResourceType.MUTAGEN_CORES,
        baseOutputPerLevel = 0.12,
        megawattDemandPerLevel = 14.0,
        baseBuildTimeSeconds = 34,
        manualSalvageBonusDesc = "+12% Total Power Grid MW & +1 Bonus Xeno-Core on Vein Break per level",
        baseScrapCost = 250.0,
        baseFuelCost = 85.0,
        baseCoresCost = 1.0,
        favoredWorkerClass = WorkerClassType.NIGHT_STALKER
    ),
    GEOTHERMAL_TAP(
        id = "GEOTHERMAL_TAP",
        displayName = "Tharsis Magma Borehole Tap",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 3,
        description = "Subterranean Lava-Tube Deck L5 geothermal bore that generates +7.5 MW/Lv dust-storm-proof 24/7 power, reduces Turbine Wear, and expands storage.",
        gameplayNeedSummary = "STORM-PROOF 24/7 POWER (LAVA TUBE L5): +7.5 MW/Lv Continuous Power, -10% Turbine Wear/Lv & +Storage Cap/Lv",
        primaryResource = ResourceType.BIO_FUEL,
        baseOutputPerLevel = 0.65,
        megawattDemandPerLevel = 1.0,
        baseBuildTimeSeconds = 38,
        manualSalvageBonusDesc = "+7.5 Continuous Baseline MW & -10% Generator Wear per level",
        baseScrapCost = 155.0,
        baseWaterCost = 42.0,
        favoredWorkerClass = WorkerClassType.TECH_SAVANT
    ),
    CRYO_STASIS_VAULT(
        id = "CRYO_STASIS_VAULT",
        displayName = "Martian Permafrost Cryo-Vault",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 4,
        description = "Sub-zero lava-tube preservation vaults on Deck L5 that cut Astronaut 60s & Sol Food/Water appetite by -8%/Lv and add +400 Cryo Storage/Lv.",
        gameplayNeedSummary = "CUTS ASTRONAUT CONSUMPTION (LAVA TUBE L5): -8% Crew 60s & Sol Food/Water Drain/Lv & +400 Cryo Storage/Lv",
        primaryResource = ResourceType.RATIONS,
        baseOutputPerLevel = 1.00,
        megawattDemandPerLevel = 5.0,
        baseBuildTimeSeconds = 42,
        manualSalvageBonusDesc = "-8% Astronaut Food/Water Consumption & +400 Outpost Storage per level",
        baseScrapCost = 200.0,
        baseWaterCost = 55.0,
        baseFuelCost = 38.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER,
        storageBonusPerLevel = 400.0
    ),
    VAULT_STORE_ROOM(
        id = "VAULT_STORE_ROOM",
        displayName = "Lava-Tube Logistics Depot",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 1,
        description = "Pressurized basalt lava-tube cargo cavern with automated racking required to raise resource caps for higher-level habitat bay expansions.",
        gameplayNeedSummary = "UPGRADABLE RESOURCE STORAGE (LAVA TUBE L6): +1,000+ Escalating Max Resource Storage/Lv & +10% Core Crate Yield/Lv",
        primaryResource = ResourceType.SCRAP,
        baseOutputPerLevel = 0.85,
        megawattDemandPerLevel = 2.0,
        baseBuildTimeSeconds = 46,
        manualSalvageBonusDesc = "+1,000+ Escalating Max Resource Storage & +10% Core Crate Yield per level",
        baseScrapCost = 38.0,
        baseWaterCost = 12.0,
        favoredWorkerClass = WorkerClassType.SCRAPPER,
        storageBonusPerLevel = 1000.0
    );
}

data class ActiveBuildingConstruction(
    val buildingType: BuildingType,
    val targetLevel: Int,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val rushGoldCost: Double
)

data class ActiveDoorUpgrade(
    val targetLevel: Int,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val rushGoldCost: Double
)

enum class SkillBranch(val displayName: String, val tagline: String, val accentColor: Color) {
    INDUSTRIAL_SALVAGE(
        displayName = "Salvage & Armory",
        tagline = "Personal Plasma Rig power, Hunting & Defense crafting mastery & construction discounts.",
        accentColor = SolarAmber
    ),
    GUILD_WORKFORCE(
        displayName = "Trade, Convoys & Crew",
        tagline = "Inter-town Gold trade margins, Convoy speed, Worker hiring discounts & Class XP.",
        accentColor = RadTeal
    ),
    CHRONO_ANOMALY(
        displayName = "Power Grid & Chrono",
        tagline = "Power Station MW output, Thorium reactor efficiency, & Day-Night Phase Powers.",
        accentColor = BioluminescentViolet
    ),
    OVERSEER_LEGACY(
        displayName = "Overseer Legacy (Prestige)",
        tagline = "Permanent Prestige Core upgrades that persist across all Vault Ascensions!",
        accentColor = XpGold
    )
}

data class SkillNodeDefinition(
    val id: String,
    val branch: SkillBranch,
    val tier: Int,
    val title: String,
    val description: String,
    val maxRank: Int,
    val prerequisiteId: String? = null,
    val skillPointCostPerRank: Int = 1,
    val baseResearchCost: Double,
    val baseGoldCost: Double = 0.0,
    val baseCoresCost: Double = 0.0,
    val prestigeCoreCostPerRank: Int = 0,
    val effectSummaryPerRank: String
)

object SkillTreeCatalog {
    val nodes: List<SkillNodeDefinition> = listOf(
        // Branch 1: Salvage & Armory
        SkillNodeDefinition(
            id = "IND_ROTARY_SAWS",
            branch = SkillBranch.INDUSTRIAL_SALVAGE,
            tier = 1,
            title = "Carbide Plasma & Spear Forge",
            description = "Supercharges your personal Salvage Rig and boosts Hunting Food yields & Scrap extraction.",
            maxRank = 5,
            prerequisiteId = null,
            skillPointCostPerRank = 1,
            baseResearchCost = 18.0,
            effectSummaryPerRank = "+25% Salvage & Hunting Food Yield, +18% Scrap per rank"
        ),
        SkillNodeDefinition(
            id = "IND_HYDRO_FILTRATION",
            branch = SkillBranch.INDUSTRIAL_SALVAGE,
            tier = 2,
            title = "Snare & Condenser Mastery",
            description = "Increases passive Food from Snare Traps and boosts Purified Water & Rations output.",
            maxRank = 5,
            prerequisiteId = "IND_ROTARY_SAWS",
            skillPointCostPerRank = 1,
            baseResearchCost = 45.0,
            baseGoldCost = 25.0,
            effectSummaryPerRank = "+22% Water, Rations & Snare Trap output per rank"
        ),
        SkillNodeDefinition(
            id = "IND_MODULAR_PREFAB",
            branch = SkillBranch.INDUSTRIAL_SALVAGE,
            tier = 3,
            title = "Armory & Prefab Scaffolding",
            description = "Lowers resource costs for Town Buildings, Power Stations & Defense Turrets.",
            maxRank = 4,
            prerequisiteId = "IND_HYDRO_FILTRATION",
            skillPointCostPerRank = 1,
            baseResearchCost = 90.0,
            baseGoldCost = 50.0,
            effectSummaryPerRank = "-8% building & power station upgrade cost per rank"
        ),
        SkillNodeDefinition(
            id = "IND_HIGH_OCTANE_CRACKING",
            branch = SkillBranch.INDUSTRIAL_SALVAGE,
            tier = 4,
            title = "High-Octane Munitions & Fuel",
            description = "Boosts Bio-Fuel refining, Outpost Defense Rating (+20%/rk), and Outpost XP gain.",
            maxRank = 5,
            prerequisiteId = "IND_MODULAR_PREFAB",
            skillPointCostPerRank = 2,
            baseResearchCost = 150.0,
            baseGoldCost = 90.0,
            baseCoresCost = 2.0,
            effectSummaryPerRank = "+25% Bio-Fuel, +20% Defense Rating & +15% XP per rank"
        ),
        SkillNodeDefinition(
            id = "IND_FORGE_OVERDRIVE",
            branch = SkillBranch.INDUSTRIAL_SALVAGE,
            tier = 5,
            title = "Automated Mega-Foundry",
            description = "Apex town industrial network that multiplies all salvaged and refined resources.",
            maxRank = 3,
            prerequisiteId = "IND_HIGH_OCTANE_CRACKING",
            skillPointCostPerRank = 2,
            baseResearchCost = 280.0,
            baseGoldCost = 180.0,
            baseCoresCost = 5.0,
            effectSummaryPerRank = "+30% global resource output & +25% Vault capacity per rank"
        ),

        // Branch 2: Trade, Convoys & Crew
        SkillNodeDefinition(
            id = "WRK_GUILD_DRILLS",
            branch = SkillBranch.GUILD_WORKFORCE,
            tier = 1,
            title = "Merchant & Hiring Charter",
            description = "Boosts Gold earned from selling to other towns (+15%/rk) and lowers Worker Hiring cost (-10%/rk).",
            maxRank = 5,
            prerequisiteId = null,
            skillPointCostPerRank = 1,
            baseResearchCost = 18.0,
            effectSummaryPerRank = "+15% Trade Gold, -10% Hiring Cost & +15% Worker output per rank"
        ),
        SkillNodeDefinition(
            id = "WRK_VETERAN_ACADEMY",
            branch = SkillBranch.GUILD_WORKFORCE,
            tier = 2,
            title = "Convoy Escort & Guild Academy",
            description = "Increases Convoy Gold/Rare payouts and accelerates Survivor Class XP gain.",
            maxRank = 5,
            prerequisiteId = "WRK_GUILD_DRILLS",
            skillPointCostPerRank = 1,
            baseResearchCost = 45.0,
            baseGoldCost = 30.0,
            effectSummaryPerRank = "+20% Convoy Rewards & +25% Survivor Class XP gain per rank"
        ),
        SkillNodeDefinition(
            id = "WRK_CIRCADIAN_AUTO_SHIFT",
            branch = SkillBranch.GUILD_WORKFORCE,
            tier = 3,
            title = "Circadian Shift Harmonizer",
            description = "Reduces off-phase worker fatigue and powers the Auto-Shift optimization protocol.",
            maxRank = 3,
            prerequisiteId = "WRK_VETERAN_ACADEMY",
            skillPointCostPerRank = 1,
            baseResearchCost = 95.0,
            baseGoldCost = 60.0,
            effectSummaryPerRank = "+15% off-phase worker resilience & +10% Auto-Shift bonus per rank"
        ),
        SkillNodeDefinition(
            id = "WRK_CROSS_CLASS_SYNERGY",
            branch = SkillBranch.GUILD_WORKFORCE,
            tier = 4,
            title = "Cross-Guild Syndicate",
            description = "Each active hired Survivor Class boosts the output of all other active classes & lowers Black-Market Gold prices.",
            maxRank = 4,
            prerequisiteId = "WRK_CIRCADIAN_AUTO_SHIFT",
            skillPointCostPerRank = 2,
            baseResearchCost = 160.0,
            baseGoldCost = 110.0,
            baseCoresCost = 2.0,
            effectSummaryPerRank = "+12% class synergy & -6% Rare Resource Gold price per rank"
        ),
        SkillNodeDefinition(
            id = "WRK_SOVEREIGN_COMMAND",
            branch = SkillBranch.GUILD_WORKFORCE,
            tier = 5,
            title = "Wasteland Trade Monopoly",
            description = "Dominates inter-town trade routes, doubling rare convoy drops and boosting Town XP.",
            maxRank = 3,
            prerequisiteId = "WRK_CROSS_CLASS_SYNERGY",
            skillPointCostPerRank = 2,
            baseResearchCost = 290.0,
            baseGoldCost = 220.0,
            baseCoresCost = 5.0,
            effectSummaryPerRank = "+30% Gold from all sources, +25% Tech & +20% Outpost XP per rank"
        ),

        // Branch 3: Power Grid & Chrono
        SkillNodeDefinition(
            id = "CHR_SOLAR_COLLECTORS",
            branch = SkillBranch.CHRONO_ANOMALY,
            tier = 1,
            title = "High-Voltage Grid Conductors",
            description = "Increases Megawatt (MW) output of all Power Stations and boosts Dawn/Day production.",
            maxRank = 5,
            prerequisiteId = null,
            skillPointCostPerRank = 1,
            baseResearchCost = 20.0,
            effectSummaryPerRank = "+18% Power Station MW & +15% Daylight production per rank"
        ),
        SkillNodeDefinition(
            id = "CHR_BIOLUM_SENSORS",
            branch = SkillBranch.CHRONO_ANOMALY,
            tier = 2,
            title = "Thorium Enrichment & Radar",
            description = "Amplifies Thorium Reactor output, Anomaly spawn rate, and Mutagen Core extraction.",
            maxRank = 5,
            prerequisiteId = "CHR_SOLAR_COLLECTORS",
            skillPointCostPerRank = 1,
            baseResearchCost = 55.0,
            baseGoldCost = 35.0,
            effectSummaryPerRank = "+22% Anomaly spawn rate & +20% Thorium/Mutagen efficiency per rank"
        ),
        SkillNodeDefinition(
            id = "CHR_PHASE_CAPACITORS",
            branch = SkillBranch.CHRONO_ANOMALY,
            tier = 3,
            title = "Aether Phase Capacitors",
            description = "Supercharges manual Solar Flare & Eclipse Shroud powers and reduces cooldowns.",
            maxRank = 4,
            prerequisiteId = "CHR_BIOLUM_SENSORS",
            skillPointCostPerRank = 1,
            baseResearchCost = 105.0,
            baseGoldCost = 70.0,
            baseCoresCost = 1.0,
            effectSummaryPerRank = "+20% Phase Shift buff potency & -10% cooldown per rank"
        ),
        SkillNodeDefinition(
            id = "CHR_NOCTURNAL_DOMINION",
            branch = SkillBranch.CHRONO_ANOMALY,
            tier = 4,
            title = "Nocturnal Grid Dominion",
            description = "Harnesses radioactive night storms to power substations and supercharge Night Stalkers.",
            maxRank = 4,
            prerequisiteId = "CHR_PHASE_CAPACITORS",
            skillPointCostPerRank = 2,
            baseResearchCost = 175.0,
            baseGoldCost = 120.0,
            baseCoresCost = 3.0,
            effectSummaryPerRank = "+28% Rad-Night output & +1 extra Mutagen Core per Night Anomaly"
        ),
        SkillNodeDefinition(
            id = "CHR_ASTRAL_CONVERGENCE",
            branch = SkillBranch.CHRONO_ANOMALY,
            tier = 5,
            title = "Fusion Singularity Grid",
            description = "Harmonizes Thorium and Aether stations so Power Overclock bonuses double across the outpost.",
            maxRank = 3,
            prerequisiteId = "CHR_NOCTURNAL_DOMINION",
            skillPointCostPerRank = 2,
            baseResearchCost = 310.0,
            baseGoldCost = 240.0,
            baseCoresCost = 6.0,
            effectSummaryPerRank = "+35% all-phase output & +40% Power Grid Overclock multiplier"
        ),

        // Branch 4: Overseer Legacy (Permanent Prestige Branch)
        SkillNodeDefinition(
            id = "LEG_CRYO_AWAKENING",
            branch = SkillBranch.OVERSEER_LEGACY,
            tier = 1,
            title = "Cryo-Supply Awakening Protocol",
            description = "Permanent Prestige Legacy: Start every new ascended Vault with bonus Scrap, Food, Water & Bio-Fuel and boost all production.",
            maxRank = 5,
            prerequisiteId = null,
            skillPointCostPerRank = 0,
            baseResearchCost = 0.0,
            prestigeCoreCostPerRank = 1,
            effectSummaryPerRank = "+20% Permanent Global Production & +40 Scrap/+25 Food/+25 Water on Prestige start per rank"
        ),
        SkillNodeDefinition(
            id = "LEG_NANO_SCHEMATICS",
            branch = SkillBranch.OVERSEER_LEGACY,
            tier = 2,
            title = "Nano-Fabrication Schematics",
            description = "Permanent Prestige Legacy: High-speed robotic welding reduces Room Construction & Armory Item Crafting timers.",
            maxRank = 5,
            prerequisiteId = "LEG_CRYO_AWAKENING",
            skillPointCostPerRank = 0,
            baseResearchCost = 0.0,
            prestigeCoreCostPerRank = 1,
            effectSummaryPerRank = "-12% Room Build Time & -12% Item Crafting Time per rank"
        ),
        SkillNodeDefinition(
            id = "LEG_HYDRO_RECYCLING",
            branch = SkillBranch.OVERSEER_LEGACY,
            tier = 3,
            title = "Genetic Vault Lineage & Ration Loop",
            description = "Permanent Prestige Legacy: Lowers Daily Worker Food & Water Upkeep and increases Rare Unique Vault Dweller recruitment rate.",
            maxRank = 5,
            prerequisiteId = "LEG_NANO_SCHEMATICS",
            skillPointCostPerRank = 0,
            baseResearchCost = 0.0,
            prestigeCoreCostPerRank = 2,
            effectSummaryPerRank = "-10% Daily Worker Food/Water Upkeep & +3% Unique Dweller Chance per rank"
        ),
        SkillNodeDefinition(
            id = "LEG_FUSION_OVERCLOCK",
            branch = SkillBranch.OVERSEER_LEGACY,
            tier = 4,
            title = "Overseer Auxiliary Fusion Core",
            description = "Permanent Prestige Legacy: Provides permanent auxiliary Vault Megawatts (MW) and amplifies Automated Turret & Blast Door Defense.",
            maxRank = 5,
            prerequisiteId = "LEG_HYDRO_RECYCLING",
            skillPointCostPerRank = 0,
            baseResearchCost = 0.0,
            prestigeCoreCostPerRank = 2,
            effectSummaryPerRank = "+10 Base Vault MW & +25% Total Defense Rating per rank"
        )
    )
}

enum class VaultMapStation(
    val id: String,
    val shortTitle: String,
    val floorIndex: Int, // 0 = Surface, 1 = B1, 2 = B2, 3 = B3, 4 = B4
    val wingColumn: Int, // 0 = Left Wing, 1 = Right Wing
    val buildingType: BuildingType? = null,
    val accentColor: Color
) {
    SURFACE_MINE_SHAFT(
        id = "SURFACE_MINE_SHAFT",
        shortTitle = "Surface Crater Drill",
        floorIndex = 0,
        wingColumn = 0,
        buildingType = null,
        accentColor = SolarAmber
    ),
    UNDERGROUND_FISHING_SUMP(
        id = "UNDERGROUND_FISHING_SUMP",
        shortTitle = "Sub-Glacial Ice Sump",
        floorIndex = 0,
        wingColumn = 1,
        buildingType = null,
        accentColor = CryoCyan
    ),
    ROOM_1_QUARTERS(
        id = "ROOM_1_QUARTERS",
        shortTitle = "#1 Astronaut Hab",
        floorIndex = 1,
        wingColumn = 0,
        buildingType = BuildingType.SURVIVOR_HAB_BLOCK,
        accentColor = SurplusGreen
    ),
    ROOM_2_SMELTERY(
        id = "ROOM_2_SMELTERY",
        shortTitle = "#2 Regolith Refinery",
        floorIndex = 1,
        wingColumn = 1,
        buildingType = BuildingType.SALVAGE_YARD,
        accentColor = SolarAmber
    ),
    ROOM_3_WATER_PURIFIER(
        id = "ROOM_3_WATER_PURIFIER",
        shortTitle = "#3 Glacial Ice Melter",
        floorIndex = 2,
        wingColumn = 0,
        buildingType = BuildingType.ATMOSPHERIC_CONDENSER,
        accentColor = CryoCyan
    ),
    ROOM_4_HYDROPONICS(
        id = "ROOM_4_HYDROPONICS",
        shortTitle = "#4 Aeroponic Bio-Dome",
        floorIndex = 2,
        wingColumn = 1,
        buildingType = BuildingType.HYDROPONIC_GREENHOUSE,
        accentColor = SurplusGreen
    ),
    ROOM_5_MEDBAY_AIRLOCK(
        id = "ROOM_5_MEDBAY_AIRLOCK",
        shortTitle = "#5 MOXIE O2 & Medbay",
        floorIndex = 3,
        wingColumn = 0,
        buildingType = BuildingType.REINFORCED_SILO,
        accentColor = RadTeal
    ),
    ROOM_6_MUNITIONS_REFINERY(
        id = "ROOM_6_MUNITIONS_REFINERY",
        shortTitle = "#6 Sabatier CH4 Plant",
        floorIndex = 3,
        wingColumn = 1,
        buildingType = BuildingType.BIO_SLUDGE_REFINERY,
        accentColor = RustCopper
    ),
    ROOM_7_RADAR_ARRAY(
        id = "ROOM_7_RADAR_ARRAY",
        shortTitle = "#7 Deep-Space Telemetry",
        floorIndex = 4,
        wingColumn = 0,
        buildingType = BuildingType.SIGNAL_RESEARCH_ARRAY,
        accentColor = XpGold
    ),
    ROOM_8_MUTAGEN_LAB(
        id = "ROOM_8_MUTAGEN_LAB",
        shortTitle = "#8 Astrobiology Lab",
        floorIndex = 4,
        wingColumn = 1,
        buildingType = BuildingType.NOCTURNAL_EXTRACTION_RIG,
        accentColor = BioluminescentViolet
    ),
    ROOM_9_GEOTHERMAL(
        id = "ROOM_9_GEOTHERMAL",
        shortTitle = "#9 Areothermal Vent",
        floorIndex = 5,
        wingColumn = 0,
        buildingType = BuildingType.GEOTHERMAL_TAP,
        accentColor = WarningOrange
    ),
    ROOM_10_CRYO_VAULT(
        id = "ROOM_10_CRYO_VAULT",
        shortTitle = "#10 Cryo Seed Vault",
        floorIndex = 5,
        wingColumn = 1,
        buildingType = BuildingType.CRYO_STASIS_VAULT,
        accentColor = CryoCyan
    ),
    ROOM_11_STORE_ROOM(
        id = "ROOM_11_STORE_ROOM",
        shortTitle = "#11 Cargo Hold Depot",
        floorIndex = 6,
        wingColumn = 0,
        buildingType = BuildingType.VAULT_STORE_ROOM,
        accentColor = SolarAmber
    );
}

data class RareAnomaly(
    val id: Long,
    val title: String,
    val subtitle: String,
    val phase: DayNightPhase,
    val scrapReward: Double,
    val waterReward: Double,
    val goldReward: Double,
    val researchReward: Double,
    val mutagenCoresReward: Double,
    val xpReward: Double,
    val remainingSeconds: Int
)

data class OfflineReport(
    val elapsedSeconds: Long,
    val cyclesCompleted: Int,
    val scrapEarned: Double,
    val waterEarned: Double,
    val rationsEarned: Double,
    val fuelEarned: Double,
    val researchEarned: Double,
    val goldEarned: Double,
    val mutagenCoresEarned: Double,
    val outpostXpEarned: Double,
    val levelsGained: Int,
    val anomaliesAutoHarvested: Int
)

data class LevelUpBannerState(
    val title: String,
    val subtitle: String,
    val rewardSummary: String,
    val isCommandLevel: Boolean = true
)

object CommandLevelProgression {
    const val MAX_COMMAND_LEVEL = 100

    fun xpRequiredForLevel(level: Int): Double {
        // Challenging Level 1..100 XP curve: Lv.1 = 320 XP, Lv.10 = 38k XP, Lv.50 = 1.08M XP, Lv.100 = 4.6M XP
        return (320.0 * level.toDouble().pow(2.08)).roundToInt().toDouble()
    }

    fun rankTitleForLevel(level: Int): String = when {
        level <= 1 -> "Lone Mars Pioneer"
        level in 2..3 -> "Crater Regolith Prospector"
        level in 4..6 -> "Pressurized Hab Builder"
        level in 7..9 -> "Dome Airlock Warden"
        level in 10..14 -> "Sol-10 Storm Veteran"
        level in 15..24 -> "Lava-Tube Base Director"
        level in 25..39 -> "Kilopower Reactor Commander"
        level in 40..59 -> "Ares Colony Flight Director"
        level in 60..79 -> "Planetary Terraform Archon"
        level in 80..99 -> "Supreme Mars Sovereign"
        else -> "Level 100 Grand Ares Legend"
    }

    fun skillPointsForReachingLevel(newLevel: Int): Int {
        return if (newLevel % 5 == 0) 2 else 1
    }

    fun globalProductionBonus(level: Int): Double {
        return 1.0 + (level - 1) * 0.10
    }
}

enum class GridLoadBreakerMode(
    val id: String,
    val shortLabel: String,
    val title: String,
    val summary: String,
    val megawattDemandMultiplier: Double,
    val defenseBonusMultiplier: Double,
    val oxygenBonusPerSec: Double,
    val drillYieldBonusMultiplier: Double,
    val accentColor: Color
) {
    BALANCED(
        id = "BALANCED",
        shortLabel = "BALANCED",
        title = "Balanced Habitat Bus Breakers",
        summary = "100% standard Megawatt distribution across all Mars habitat bays, point-defense turrets, and MOXIE life support.",
        megawattDemandMultiplier = 1.0,
        defenseBonusMultiplier = 1.0,
        oxygenBonusPerSec = 0.0,
        drillYieldBonusMultiplier = 1.0,
        accentColor = RadTeal
    ),
    LIFE_SUPPORT_FIRST(
        id = "LIFE_SUPPORT_FIRST",
        shortLabel = "MOXIE O2 (-22% MW)",
        title = "Priority MOXIE Life Support & Scrubbers",
        summary = "Sheds non-essential industrial load (-22% MW demand) and boosts MOXIE O2 scrubbers (+1.2 O2/s).",
        megawattDemandMultiplier = 0.78,
        defenseBonusMultiplier = 0.95,
        oxygenBonusPerSec = 1.2,
        drillYieldBonusMultiplier = 0.90,
        accentColor = CryoCyan
    ),
    DEFENSE_SURGE(
        id = "DEFENSE_SURGE",
        shortLabel = "DOME SHIELD (+25% DEF)",
        title = "Perimeter Laser & Whipple Shield Surge",
        summary = "Routes high-voltage capacitor banks to Point-Defense Turrets & Airlock Bulkheads (+25% Defense Rating, +10% MW draw).",
        megawattDemandMultiplier = 1.10,
        defenseBonusMultiplier = 1.25,
        oxygenBonusPerSec = 0.0,
        drillYieldBonusMultiplier = 0.90,
        accentColor = SolarAmber
    ),
    DRILL_OVERDRIVE(
        id = "DRILL_OVERDRIVE",
        shortLabel = "CORE DRILL (+35% ORE)",
        title = "Subterranean Lava-Tube Borehole Overdrive",
        summary = "Overclocks deep-crust laser drills & regolith smelters (+35% Deep Drill & Laser Drill yield, +12% MW draw).",
        megawattDemandMultiplier = 1.12,
        defenseBonusMultiplier = 0.95,
        oxygenBonusPerSec = 0.0,
        drillYieldBonusMultiplier = 1.35,
        accentColor = BioluminescentViolet
    );

    val effectSummary: String
        get() = summary

    companion object {
        fun fromId(id: String): GridLoadBreakerMode {
            return entries.firstOrNull { it.id == id } ?: BALANCED
        }
    }
}

enum class TurretAmmoType(
    val id: String,
    val shortLabel: String,
    val title: String,
    val summary: String,
    val defenseBonusMultiplier: Double,
    val nightSwarmBonusMultiplier: Double,
    val warlordArmorBonusMultiplier: Double,
    val accentColor: Color
) {
    STANDARD_FMJ(
        id = "STANDARD_FMJ",
        shortLabel = "KINETIC SLUG",
        title = "Standard Railgun Kinetic Slugs",
        summary = "Reliable standard-issue point-defense & EVA rifle slugs (100% baseline Defense & Recon efficiency).",
        defenseBonusMultiplier = 1.0,
        nightSwarmBonusMultiplier = 1.0,
        warlordArmorBonusMultiplier = 1.0,
        accentColor = SolarAmber
    ),
    ARMOR_PIERCING(
        id = "ARMOR_PIERCING",
        shortLabel = "TUNGSTEN SABOT (+20% DEF)",
        title = "Tungsten Hyper-Velocity Sabot",
        summary = "Shatters dense bolide meteors & Sol-10 Rogue Mech plating (+20% Total Defense & +35% vs Bosses).",
        defenseBonusMultiplier = 1.20,
        nightSwarmBonusMultiplier = 1.10,
        warlordArmorBonusMultiplier = 1.35,
        accentColor = XpGold
    ),
    INCENDIARY_NAPALM(
        id = "INCENDIARY_NAPALM",
        shortLabel = "PLASMA TORCH (+30% NIGHT)",
        title = "Solar-Plasma Thermal Shells",
        summary = "Vaporizes cryo-night ice shards & rogue drone swarms (+15% Defense & +30% vs Night Hazards).",
        defenseBonusMultiplier = 1.15,
        nightSwarmBonusMultiplier = 1.30,
        warlordArmorBonusMultiplier = 1.10,
        accentColor = WarningOrange
    ),
    EMP_ARC_SHELLS(
        id = "EMP_ARC_SHELLS",
        shortLabel = "EMP PULSE (ANTI-SABOTAGE)",
        title = "Overcharged EMP Ion Munitions",
        summary = "Disrupts Electrostatic Dust-Storm arcs & rogue syndicate crawlers (+18% Defense & halves bay incident penalties).",
        defenseBonusMultiplier = 1.18,
        nightSwarmBonusMultiplier = 1.15,
        warlordArmorBonusMultiplier = 1.25,
        accentColor = CryoCyan
    );

    val effectSummary: String
        get() = summary

    companion object {
        fun fromId(id: String): TurretAmmoType {
            return entries.firstOrNull { it.id == id } ?: STANDARD_FMJ
        }
    }
}

enum class SubterraneanBossType(
    val id: String,
    val depthThresholdMeters: Int,
    val title: String,
    val subtitle: String,
    val relicRewardCount: Int,
    val mutagenCoresReward: Double,
    val goldReward: Double,
    val xpReward: Double,
    val accentColor: Color
) {
    MAGMA_BROODMOTHER(
        id = "MAGMA_BROODMOTHER",
        depthThresholdMeters = 300,
        title = "Tharsis Magma-Vent Lithovore (300m)",
        subtitle = "Colossal silicon-armored extremophile nesting in the 300m geothermal fissure. Blocks deeper drilling until cleared!",
        relicRewardCount = 1,
        mutagenCoresReward = 2.0,
        goldReward = 65.0,
        xpReward = 140.0,
        accentColor = WarningOrange
    ),
    ARMORED_TUNNEL_LEVIATHAN(
        id = "ARMORED_TUNNEL_LEVIATHAN",
        depthThresholdMeters = 900,
        title = "Noctis Borehole Leviathan (900m)",
        subtitle = "Subterranean basalt-crushing worm blocking deep-crust pipes at 900m. Rich in Ares Relics & Pu-238!",
        relicRewardCount = 2,
        mutagenCoresReward = 4.0,
        goldReward = 140.0,
        xpReward = 280.0,
        accentColor = HazardRed
    ),
    AETHER_CORE_AUTOMATON(
        id = "AETHER_CORE_AUTOMATON",
        depthThresholdMeters = 1800,
        title = "Ares-1 Autonomous Tunnel Boring Boss (1,800m)",
        subtitle = "Runaway nuclear-powered Ares-1 excavation construct guarding the 1,800m deep mantle core.",
        relicRewardCount = 3,
        mutagenCoresReward = 6.0,
        goldReward = 260.0,
        xpReward = 500.0,
        accentColor = BioluminescentViolet
    );

    val hazardDescription: String
        get() = subtitle

    companion object {
        fun fromId(id: String): SubterraneanBossType? {
            return entries.firstOrNull { it.id == id }
        }

        fun nextBossForDepth(depth: Int): SubterraneanBossType? {
            return entries.firstOrNull { it.depthThresholdMeters > depth }
        }
    }
}

enum class VaultRationingPolicy(
    val id: String,
    val shortLabel: String,
    val title: String,
    val summary: String,
    val foodWaterUpkeepMultiplier: Double,
    val moraleBonusDelta: Int,
    val outputMultiplier: Double,
    val staminaRecoveryDeltaPerTick: Int,
    val accentColor: Color
) {
    HALF_RATIONS(
        id = "HALF_RATIONS",
        shortLabel = "HALF RATIONS (-35% BIO/H2O)",
        title = "Emergency Apollo-13 Rationing Protocol",
        summary = "Cuts Astronaut 60s & daily Bio-Ration/Water consumption by -35% during ice shortages, at the cost of -15% Morale & -10% Output.",
        foodWaterUpkeepMultiplier = 0.65,
        moraleBonusDelta = -15,
        outputMultiplier = 0.90,
        staminaRecoveryDeltaPerTick = 0,
        accentColor = WarningOrange
    ),
    STANDARD_RATIONS(
        id = "STANDARD_RATIONS",
        shortLabel = "STANDARD ISSUE (100%)",
        title = "Standard NASA/Ares Crew Rationing",
        summary = "Balanced 100% Bio-Ration & Water allocation with steady Astronaut Morale and normal EVA stamina.",
        foodWaterUpkeepMultiplier = 1.0,
        moraleBonusDelta = 0,
        outputMultiplier = 1.0,
        staminaRecoveryDeltaPerTick = 1,
        accentColor = RadTeal
    ),
    DOUBLE_RATIONS(
        id = "DOUBLE_RATIONS",
        shortLabel = "HIGH-CALORIE (+22% OUTPUT)",
        title = "High-Calorie Hydroponic Feast Protocol",
        summary = "Boosts Crew Output by +22%, Morale by +18%, and doubles EVA Stamina recovery, while taking +35% more Bio-Rations & Water.",
        foodWaterUpkeepMultiplier = 1.35,
        moraleBonusDelta = 18,
        outputMultiplier = 1.22,
        staminaRecoveryDeltaPerTick = 2,
        accentColor = SurplusGreen
    );

    val effectSummary: String
        get() = summary

    companion object {
        fun fromId(id: String): VaultRationingPolicy {
            return entries.firstOrNull { it.id == id } ?: STANDARD_RATIONS
        }
    }
}

enum class WastelandSeason(
    val id: String,
    val title: String,
    val shortLabel: String,
    val weatherSummary: String,
    val solarMwMultiplier: Double,
    val waterCondenserMultiplier: Double,
    val hydroponicsFoodMultiplier: Double,
    val scrapSalvageMultiplier: Double,
    val dailyFuelHeatingMultiplier: Double,
    val dailyScrapCorrosionMultiplier: Double,
    val raidThreatMultiplier: Double,
    val moraleDelta: Int,
    val accentColor: Color
) {
    SCORCHING_FALLOUT(
        id = "SCORCHING_FALLOUT",
        title = "Perihelion Solar Glare",
        shortLabel = "PERIHELION GLARE",
        weatherSummary = "+25% Solar MW, -15% Glacial Ice Melter yield • Clear Martian skies supercharge solar arrays but sublimate surface frost.",
        solarMwMultiplier = 1.25,
        waterCondenserMultiplier = 0.85,
        hydroponicsFoodMultiplier = 1.0,
        scrapSalvageMultiplier = 1.0,
        dailyFuelHeatingMultiplier = 1.0,
        dailyScrapCorrosionMultiplier = 1.0,
        raidThreatMultiplier = 1.0,
        moraleDelta = 0,
        accentColor = SolarAmber
    ),
    ACID_MONSOON(
        id = "ACID_MONSOON",
        title = "Valles Marineris Vapor Thaw",
        shortLabel = "VAPOR THAW",
        weatherSummary = "+35% Ice Melter & +10% Aeroponics, -20% Solar MW, +10% Daily Perchlorate Maintenance • Dense ice-fog saturates condensers.",
        solarMwMultiplier = 0.80,
        waterCondenserMultiplier = 1.35,
        hydroponicsFoodMultiplier = 1.10,
        scrapSalvageMultiplier = 1.0,
        dailyFuelHeatingMultiplier = 1.0,
        dailyScrapCorrosionMultiplier = 1.10,
        raidThreatMultiplier = 0.95,
        moraleDelta = -4,
        accentColor = CryoCyan
    ),
    NUCLEAR_WINTER(
        id = "NUCLEAR_WINTER",
        title = "Aphelion -125°C Deep Freeze",
        shortLabel = "APHELION FREEZE",
        weatherSummary = "-45% Solar MW, -20% Ice Melter, +35% Daily Methane Heater Upkeep • Extreme orbital winter taxes habitat heating & solar arrays.",
        solarMwMultiplier = 0.55,
        waterCondenserMultiplier = 0.80,
        hydroponicsFoodMultiplier = 0.90,
        scrapSalvageMultiplier = 1.05,
        dailyFuelHeatingMultiplier = 1.35,
        dailyScrapCorrosionMultiplier = 1.05,
        raidThreatMultiplier = 0.90,
        moraleDelta = -8,
        accentColor = CryoCyan
    ),
    ASH_STORM_BLACKOUT(
        id = "ASH_STORM_BLACKOUT",
        title = "Planet-Encircling Dust-Storm Blackout",
        shortLabel = "DUST-STORM BLACKOUT",
        weatherSummary = "+25% Regolith Salvage, -60% Solar MW, +15% Storm Hazard Threat • Choking iron-oxide dust clouds blanket solar arrays.",
        solarMwMultiplier = 0.40,
        waterCondenserMultiplier = 0.95,
        hydroponicsFoodMultiplier = 1.0,
        scrapSalvageMultiplier = 1.25,
        dailyFuelHeatingMultiplier = 1.10,
        dailyScrapCorrosionMultiplier = 1.12,
        raidThreatMultiplier = 1.15,
        moraleDelta = -6,
        accentColor = WarningOrange
    ),
    BIOLUMINESCENT_BLOOM(
        id = "BIOLUMINESCENT_BLOOM",
        title = "Martian Aurora & Bio-Dome Bloom",
        shortLabel = "AURORA BLOOM",
        weatherSummary = "+35% Aeroponics Bio-Rations, +15% Ice Melter & +10% Astronaut Morale • Crustal magnetic auroras & algal bloom revitalize the colony.",
        solarMwMultiplier = 1.05,
        waterCondenserMultiplier = 1.15,
        hydroponicsFoodMultiplier = 1.35,
        scrapSalvageMultiplier = 1.10,
        dailyFuelHeatingMultiplier = 0.90,
        dailyScrapCorrosionMultiplier = 0.95,
        raidThreatMultiplier = 1.05,
        moraleDelta = 10,
        accentColor = BioluminescentViolet
    );

    val modifierSummary: String
        get() = weatherSummary

    companion object {
        const val DAYS_PER_SEASON = 5

        fun forDay(dayCounter: Int): WastelandSeason {
            val safeDay = dayCounter.coerceAtLeast(1)
            val seasonIndex = ((safeDay - 1) / DAYS_PER_SEASON) % entries.size
            return entries[seasonIndex]
        }

        fun nextSeasonForDay(dayCounter: Int): WastelandSeason {
            val safeDay = dayCounter.coerceAtLeast(1)
            val nextSeasonIndex = (((safeDay - 1) / DAYS_PER_SEASON) + 1) % entries.size
            return entries[nextSeasonIndex]
        }

        fun daysRemainingInSeason(dayCounter: Int): Int {
            val safeDay = dayCounter.coerceAtLeast(1)
            return DAYS_PER_SEASON - ((safeDay - 1) % DAYS_PER_SEASON)
        }
    }
}

enum class VaultCascadeStage(
    val stageNumber: Int,
    val title: String,
    val shortBadge: String,
    val accentColor: Color
) {
    STAGE_0_STABLE(
        stageNumber = 0,
        title = "Stage 0: All Habitat Systems Nominal",
        shortBadge = "NOMINAL (0 HAZARDS)",
        accentColor = SurplusGreen
    ),
    STAGE_1_WARNING(
        stageNumber = 1,
        title = "Stage 1: Early Telemetry Caution",
        shortBadge = "STAGE 1 CAUTION",
        accentColor = SolarAmber
    ),
    STAGE_2_SUBSYSTEM_STRAIN(
        stageNumber = 2,
        title = "Stage 2: Compounding Life-Support Strain",
        shortBadge = "STAGE 2 STRAIN",
        accentColor = WarningOrange
    ),
    STAGE_3_CRITICAL_CASCADE(
        stageNumber = 3,
        title = "Stage 3: Critical Decompression Cascade",
        shortBadge = "STAGE 3 CASCADE",
        accentColor = HazardRed
    )
}

enum class VaultTechEra(
    val eraNumber: Int,
    val title: String,
    val shortBadge: String,
    val milestoneSummary: String,
    val accentColor: Color
) {
    ERA_1_SURVIVAL_BUNKER(
        eraNumber = 1,
        title = "Era I: Ares Landing Pod Shelter",
        shortBadge = "ERA I • LANDER POD",
        milestoneSummary = "Excavate regolith with Laser Drill (0 Heat/0 Cost), build Glacial Ice Melter & Aeroponics to automate Water & Bio-Rations.",
        accentColor = SolarAmber
    ),
    ERA_2_HYDRO_INDUSTRIAL(
        eraNumber = 2,
        title = "Era II: Pressurized Lava-Tube Grid",
        shortBadge = "ERA II • LAVA-TUBE",
        milestoneSummary = "Expand Cargo Hold Cap via Bay Upgrades, assign Astronauts, and balance per-sol life-support upkeep.",
        accentColor = CryoCyan
    ),
    ERA_3_DEEP_THERMAL(
        eraNumber = 3,
        title = "Era III: Tharsis Areothermal Colony",
        shortBadge = "ERA III • AREOTHERMAL",
        milestoneSummary = "Tap Level B5 Areothermal Vents & Seed Vaults, drill past 300m Crustal Bosses, and survive Aphelion Freeze.",
        accentColor = RadTeal
    ),
    ERA_4_ATOMIC_AUTOMATION(
        eraNumber = 4,
        title = "Era IV: Kilopower Fission Automation",
        shortBadge = "ERA IV • KILOPOWER",
        milestoneSummary = "Run Kilopower & He-3 Fusion Reactors, automate Dome Defense Lasers, and master Multi-Sol Directives.",
        accentColor = BioluminescentViolet
    ),
    ERA_5_EDEN_METROPOLIS(
        eraNumber = 5,
        title = "Era V: Self-Sustaining Ares Terra-Dome",
        shortBadge = "ERA V • TERRA-DOME",
        milestoneSummary = "Complete the 4-Phase Terraforming Core, achieve Tier V Certification, and Ascend for Permanent Prestige.",
        accentColor = XpGold
    );

    val subtitle: String
        get() = milestoneSummary
}

data class VaultDirectiveStatus(
    val tier: Int,
    val title: String,
    val objectiveSummary: String,
    val progressText: String,
    val progressFraction: Float,
    val isCompleted: Boolean,
    val goldReward: Double,
    val researchReward: Double,
    val coresReward: Double,
    val permanentBlueprintSummary: String
) {
    val tierIndex: Int
        get() = (tier - 1).coerceAtLeast(0)
    val objectiveDescription: String
        get() = objectiveSummary
    val rewardGold: Double
        get() = goldReward
    val rewardResearch: Double
        get() = researchReward
    val rewardMutagenCores: Double
        get() = coresReward
    val rewardSummary: String
        get() = "+${goldReward.toInt()} Gold, +${researchReward.toInt()} Tech, +${coresReward.toInt()} Core(s) & $permanentBlueprintSummary"
}

/**
 * Mars Astronaut 7-stat S.P.E.C.I.A.L. Mission Aptitude system.
 */
enum class SpecialStat(
    val code: String,
    val fullName: String,
    val roomRoleSummary: String,
    val accentColor: Color
) {
    STRENGTH("S", "Strength", "Solar/RTG Power Arrays, Areothermal Vents & Regolith Sintering", SolarAmber),
    PERCEPTION("P", "Perception", "Glacial Ice Melter, Railgun Munitions & Meteor Defense Aim", CryoCyan),
    ENDURANCE("E", "Endurance", "Sabatier Methane Plant, Cargo Hold & Cryo Seed Vault", RustCopper),
    CHARISMA("C", "Charisma", "Astronaut Crew Quarters & Deep-Space Telemetry Dish", XpGold),
    INTELLIGENCE("I", "Intelligence", "MOXIE O2 Scrubbers, Medbay & Astrobiology Core Lab", RadTeal),
    AGILITY("A", "Agility", "Aeroponic Bio-Dome & Rapid Bio-Ration Harvesting", SurplusGreen),
    LUCK("L", "Luck", "Rare Gold Ore Strikes, Overclock Payouts & Lower Incident Risk", BioluminescentViolet)
}

data class SpecialProfile(
    val strength: Int,
    val perception: Int,
    val endurance: Int,
    val charisma: Int,
    val intelligence: Int,
    val agility: Int,
    val luck: Int
) {
    fun statFor(stat: SpecialStat): Int = when (stat) {
        SpecialStat.STRENGTH -> strength
        SpecialStat.PERCEPTION -> perception
        SpecialStat.ENDURANCE -> endurance
        SpecialStat.CHARISMA -> charisma
        SpecialStat.INTELLIGENCE -> intelligence
        SpecialStat.AGILITY -> agility
        SpecialStat.LUCK -> luck
    }

    val totalPoints: Int
        get() = strength + perception + endurance + charisma + intelligence + agility + luck

    fun compactSummary(): String =
        "S:$strength P:$perception E:$endurance C:$charisma I:$intelligence A:$agility L:$luck"
}

val BuildingType.primarySpecialStat: SpecialStat
    get() = when (this) {
        BuildingType.SURVIVOR_HAB_BLOCK -> SpecialStat.CHARISMA
        BuildingType.SALVAGE_YARD -> SpecialStat.STRENGTH
        BuildingType.ATMOSPHERIC_CONDENSER -> SpecialStat.PERCEPTION
        BuildingType.HYDROPONIC_GREENHOUSE -> SpecialStat.AGILITY
        BuildingType.REINFORCED_SILO -> SpecialStat.INTELLIGENCE
        BuildingType.BIO_SLUDGE_REFINERY -> SpecialStat.ENDURANCE
        BuildingType.SIGNAL_RESEARCH_ARRAY -> SpecialStat.CHARISMA
        BuildingType.NOCTURNAL_EXTRACTION_RIG -> SpecialStat.INTELLIGENCE
        BuildingType.GEOTHERMAL_TAP -> SpecialStat.STRENGTH
        BuildingType.CRYO_STASIS_VAULT -> SpecialStat.ENDURANCE
        BuildingType.VAULT_STORE_ROOM -> SpecialStat.ENDURANCE
    }

val BuildingType.falloutRoomTitle: String
    get() = when (this) {
        BuildingType.SURVIVOR_HAB_BLOCK -> "Astronaut Crew Quarters (C)"
        BuildingType.SALVAGE_YARD -> "Regolith Sintering Foundry (S)"
        BuildingType.ATMOSPHERIC_CONDENSER -> "Glacial Ice Melter (P)"
        BuildingType.HYDROPONIC_GREENHOUSE -> "Aeroponic Bio-Dome (A)"
        BuildingType.REINFORCED_SILO -> "MOXIE O2 & Medbay (I)"
        BuildingType.BIO_SLUDGE_REFINERY -> "Sabatier Methane Plant (E)"
        BuildingType.SIGNAL_RESEARCH_ARRAY -> "Deep-Space Telemetry Dish (C)"
        BuildingType.NOCTURNAL_EXTRACTION_RIG -> "Astrobiology Core Lab (I)"
        BuildingType.GEOTHERMAL_TAP -> "Areothermal Magma Vent (S)"
        BuildingType.CRYO_STASIS_VAULT -> "Cryo-Seed & Ration Vault (E)"
        BuildingType.VAULT_STORE_ROOM -> "Pressurized Cargo Hold (E)"
    }

val WorkerClassType.primarySpecialStat: SpecialStat
    get() = when (this) {
        WorkerClassType.SCRAPPER -> SpecialStat.STRENGTH
        WorkerClassType.HYDRO_ENGINEER -> SpecialStat.PERCEPTION
        WorkerClassType.NIGHT_STALKER -> SpecialStat.AGILITY
        WorkerClassType.TECH_SAVANT -> SpecialStat.INTELLIGENCE
    }

fun WorkerClassType.computeSpecialProfile(
    classLevel: Int = 1,
    isUniqueDweller: Boolean = false
): SpecialProfile {
    val lvBonus = ((classLevel.coerceAtLeast(1) - 1) / 2).coerceIn(0, 4)
    val uniqueBonus = if (isUniqueDweller) 2 else 0
    fun clampStat(base: Int): Int = (base + lvBonus + uniqueBonus).coerceIn(1, 10)
    return when (this) {
        WorkerClassType.SCRAPPER -> SpecialProfile(
            strength = clampStat(7),
            perception = clampStat(4),
            endurance = clampStat(6),
            charisma = clampStat(3),
            intelligence = clampStat(3),
            agility = clampStat(4),
            luck = clampStat(5)
        )
        WorkerClassType.HYDRO_ENGINEER -> SpecialProfile(
            strength = clampStat(4),
            perception = clampStat(7),
            endurance = clampStat(5),
            charisma = clampStat(4),
            intelligence = clampStat(5),
            agility = clampStat(6),
            luck = clampStat(4)
        )
        WorkerClassType.NIGHT_STALKER -> SpecialProfile(
            strength = clampStat(5),
            perception = clampStat(6),
            endurance = clampStat(6),
            charisma = clampStat(3),
            intelligence = clampStat(4),
            agility = clampStat(7),
            luck = clampStat(6)
        )
        WorkerClassType.TECH_SAVANT -> SpecialProfile(
            strength = clampStat(3),
            perception = clampStat(5),
            endurance = clampStat(4),
            charisma = clampStat(6),
            intelligence = clampStat(8),
            agility = clampStat(4),
            luck = clampStat(5)
        )
    }
}

/**
 * Modular 1x / 2x / 3x Mars Habitat Bay Merging Tiers:
 * Upgrading a module expands and links adjacent lava-tube bays into wider multi-bay pressurized domes!
 */
enum class RoomMergeTier(
    val bayWidth: Int,
    val badgeLabel: String,
    val mergeTitle: String,
    val outputMergeBonusPercent: Int,
    val accentColor: Color
) {
    UNBUILT_0X(
        bayWidth = 0,
        badgeLabel = "0x UNSEALED",
        mergeTitle = "Unsealed Lava-Tube Cavity",
        outputMergeBonusPercent = 0,
        accentColor = WarningOrange
    ),
    SINGLE_1X(
        bayWidth = 1,
        badgeLabel = "1x SINGLE POD",
        mergeTitle = "Single Pressurized Pod (1x Bay)",
        outputMergeBonusPercent = 0,
        accentColor = CryoCyan
    ),
    DOUBLE_2X(
        bayWidth = 2,
        badgeLabel = "2x DUAL HAB BAY",
        mergeTitle = "Dual Linked Habitat Bay (2x Wide • +25% Merge Synergy)",
        outputMergeBonusPercent = 25,
        accentColor = SolarAmber
    ),
    TRIPLE_3X(
        bayWidth = 3,
        badgeLabel = "3x TRIPLE DOME",
        mergeTitle = "Triple Pressurized Dome (3x Wide • +55% Merge Synergy)",
        outputMergeBonusPercent = 55,
        accentColor = SurplusGreen
    )
}

/**
 * Subterranean Mars Habitat Incidents triggered by failed Bay Overclocks or planetary hazards.
 */
enum class VaultIncidentType(
    val id: String,
    val title: String,
    val emergencyPrompt: String,
    val resolveButtonLabel: String,
    val accentColor: Color
) {
    VAULT_FIRE(
        id = "VAULT_FIRE",
        title = "🔥 O2 SCRUBBER FLASH FIRE",
        emergencyPrompt = "Overclocked MOXIE electrolysis coils sparked a pressurized O2 flash fire! Vent Halon immediately to restore output.",
        resolveButtonLabel = "🧯 VENT HALON & EXTINGUISH (+25 XP)",
        accentColor = HazardRed
    ),
    RADROACH_SWARM(
        id = "RADROACH_SWARM",
        title = "🌪 PERCHLORATE DUST INFILTRATION",
        emergencyPrompt = "Toxic Martian perchlorate dust breached the airlock seals! Purge filters to resume production.",
        resolveButtonLabel = "💨 PURGE PERCHLORATE FILTERS (+25 XP)",
        accentColor = WarningOrange
    ),
    MOLERAT_SHORT(
        id = "MOLERAT_SHORT",
        title = "❄ CRYOGENIC COOLANT RUPTURE",
        emergencyPrompt = "Sub-zero ammonia coolant lines ruptured inside the lava-tube wall! Seal valves and splice conduits.",
        resolveButtonLabel = "🔧 SEAL COOLANT & SPLICE (+25 XP)",
        accentColor = SolarAmber
    ),
    RAIDER_BREACH(
        id = "RAIDER_BREACH",
        title = "☄ MICROMETEOROID HULL PUNCTURE",
        emergencyPrompt = "High-velocity micrometeoroid punctured the habitat pressure bladder! Apply epoxy patch and repressurize.",
        resolveButtonLabel = "🛡 PATCH HULL & REPRESSURIZE (+25 XP)",
        accentColor = HazardRed
    )
}

data class RushRoomPreview(
    val buildingType: BuildingType,
    val roomLevel: Int,
    val mergeTier: RoomMergeTier,
    val primarySpecial: SpecialStat,
    val matchingSpecialScore: Int,
    val incidentRiskPercent: Int,
    val instantResourceAmount: Double,
    val instantResourceLabel: String,
    val bonusGoldCaps: Double,
    val xpReward: Double,
    val canRush: Boolean,
    val statusNote: String
)
