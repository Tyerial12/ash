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
    SCRAP("Scrap Metal", "SCRAP", SolarAmber),
    WATER("Purified Water", "WATER", CryoCyan),
    RATIONS("Food Rations", "FOOD", SurplusGreen),
    BIO_FUEL("Bio-Fuel", "FUEL", RustCopper),
    RESEARCH_DATA("Research Data", "TECH", RadTeal),
    GOLD("Gold Sovereigns", "GOLD", XpGold),
    THORIUM_RODS("Thorium Rods", "THORIUM", WarningOrange, isRare = true),
    AETHER_CELLS("Aether Capacitors", "AETHER", CryoCyan, isRare = true),
    MUTAGEN_CORES("Mutagen Cores", "CORES", BioluminescentViolet, isRare = true)
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
        displayName = "Condensation Dawn",
        startFraction = 0.0f,
        endFraction = 0.15f,
        tagline = "Morning mist boosts Water (+35%), Oasis Spire pays +35% Gold, grazing beasts active.",
        scrapMultiplier = 1.05,
        waterMultiplier = 1.35,
        rationsMultiplier = 1.15,
        researchMultiplier = 1.0,
        solarPowerMultiplier = 0.85,
        mutagenSpawnMultiplier = 1.1,
        accentColor = CryoCyan
    ),
    SCORCHING_DAY(
        displayName = "Scorching Day",
        startFraction = 0.15f,
        endFraction = 0.55f,
        tagline = "Peak solar glare: Solar Stations surge (+50% MW), Ironforge pays +35% Gold for Scrap.",
        scrapMultiplier = 1.30,
        waterMultiplier = 0.90,
        rationsMultiplier = 1.35,
        researchMultiplier = 1.05,
        solarPowerMultiplier = 1.50,
        mutagenSpawnMultiplier = 0.6,
        accentColor = SolarAmber
    ),
    DUSK(
        displayName = "Ashfall Dusk",
        startFraction = 0.55f,
        endFraction = 0.70f,
        tagline = "Cooling winds: Prime hunting hour (+25% Hunt Food), +15% Dusk Raid Threat & rising fallout.",
        scrapMultiplier = 1.05,
        waterMultiplier = 1.25,
        rationsMultiplier = 1.05,
        researchMultiplier = 1.25,
        solarPowerMultiplier = 0.55,
        mutagenSpawnMultiplier = 1.5,
        accentColor = RustCopper
    ),
    RAD_NIGHT(
        displayName = "Bioluminescent Night",
        startFraction = 0.70f,
        endFraction = 1.0f,
        tagline = "HIGH DANGER: +35% Mutant Swarm Threat, +Radiation Surge, +6% Expedition Risk & Solar drops to 10%!",
        scrapMultiplier = 0.75,
        waterMultiplier = 1.05,
        rationsMultiplier = 0.80,
        researchMultiplier = 1.40,
        solarPowerMultiplier = 0.10,
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
    HUNTING("Hunting & Food"),
    DEFENSE("Outpost Defense")
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
        title = "Scrap-Tipped Harpoon Spear",
        category = CraftItemCategory.HUNTING,
        description = "Barbed throwing spear forged from salvaged hull steel. Used to hunt wasteland beasts for Food Rations.",
        effectSummary = "Crafts +2 Hunting Spears (Used in Active Beast Hunts)",
        scrapCost = 12.0,
        baseCraftTimeSeconds = 6,
        accentColor = SurplusGreen
    ),
    SNARE_TRAP(
        id = "SNARE_TRAP",
        title = "High-Tension Snare Trap",
        category = CraftItemCategory.HUNTING,
        description = "Spring-loaded canyon snare that passively catches small game (+0.6 Food/s each) & baits large hunts.",
        effectSummary = "+1 Active Snare Trap (+0.6 Food/s passive & used in Canyon Hunts)",
        scrapCost = 18.0,
        fuelCost = 5.0,
        baseCraftTimeSeconds = 9,
        passiveFoodPerSec = 0.6,
        accentColor = SolarAmber
    ),
    AETHER_CROSSBOW(
        id = "AETHER_CROSSBOW",
        title = "Aether-Bolt Hunting Crossbow",
        category = CraftItemCategory.HUNTING,
        description = "High-velocity magnetic crossbow built with an Aether Capacitor. Permanently boosts all Hunting Food yields!",
        effectSummary = "Permanent Upgrade: +40% Food from all Hunts & unlocks Apex Behemoth Hunt",
        scrapCost = 35.0,
        goldCost = 20.0,
        aetherCost = 1.0,
        baseCraftTimeSeconds = 18,
        passiveFoodPerSec = 1.4,
        accentColor = CryoCyan
    ),
    SHRAPNEL_MINE(
        id = "SHRAPNEL_MINE",
        title = "Shrapnel Barricade Mine",
        category = CraftItemCategory.DEFENSE,
        description = "Perimeter blast charge that fortifies the outpost (+15 Defense) and auto-triggers on Raider Sieges.",
        effectSummary = "Crafts +2 Barricade Mines (+30 Total Defense & Auto-Counter-Strike)",
        scrapCost = 16.0,
        fuelCost = 6.0,
        baseCraftTimeSeconds = 10,
        defenseRatingBonus = 15,
        accentColor = WarningOrange
    ),
    AUTO_SENTRY_TURRET(
        id = "AUTO_SENTRY_TURRET",
        title = "Automated Sentry Turret",
        category = CraftItemCategory.DEFENSE,
        description = "Aether-targeted twin autocannon that automatically intercepts Day Raider attacks & Night Mutant Swarms.",
        effectSummary = "+45 Permanent Auto-Defense & +10% Convoy Security",
        scrapCost = 42.0,
        goldCost = 25.0,
        aetherCost = 1.0,
        baseCraftTimeSeconds = 20,
        defenseRatingBonus = 45,
        accentColor = RadTeal
    ),
    THORIUM_PLASMA_CANNON(
        id = "THORIUM_PLASMA_CANNON",
        title = "Thorium Plasma Battery",
        category = CraftItemCategory.DEFENSE,
        description = "Heavy automated citadel emplacement fueled by a Thorium Rod. Vaporizes lethal Night Mutant Swarms for Gold.",
        effectSummary = "+120 Permanent Auto-Defense & +25% Raider Bounty Gold",
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
        title = "Ballistic Ammo Crate (+30 Rds)",
        category = CraftItemCategory.DEFENSE,
        description = "Pressed brass & lead rounds for Powered Auto-Turrets during daily/night attacks and armed Wasteland Expeditions.",
        effectSummary = "+30 Ammo Rounds (Powers Auto-Turrets at 100% DEF & +25% Expedition Loot)",
        scrapCost = 14.0,
        fuelCost = 4.0,
        baseCraftTimeSeconds = 7,
        accentColor = SolarAmber
    ),
    GAS_MASK(
        id = "GAS_MASK",
        title = "Charcoal Filter Gas Mask",
        category = CraftItemCategory.HUNTING,
        description = "Activated-carbon respirator that filters radioactive fallout dust on Wasteland Expeditions.",
        effectSummary = "+1 Gas Mask (-3% Expedition Death Risk & -40% Expedition Radiation)",
        scrapCost = 16.0,
        fuelCost = 4.0,
        baseCraftTimeSeconds = 11,
        accentColor = RadTeal
    ),
    HAZMAT_SUIT(
        id = "HAZMAT_SUIT",
        title = "Lead-Lined Hazmat Suit",
        category = CraftItemCategory.HUNTING,
        description = "Full-body radiation suit for Ground-Zero scavenging runs and high-fallout Night expeditions.",
        effectSummary = "+1 Hazmat Suit (-4% Expedition Death Risk & -70% Radiation Intake)",
        scrapCost = 28.0,
        fuelCost = 8.0,
        goldCost = 12.0,
        baseCraftTimeSeconds = 16,
        accentColor = CryoCyan
    ),
    MEDKIT_RADAWAY_PACK(
        id = "MEDKIT_RADAWAY_PACK",
        title = "Field Medkit & RadAway Pack",
        category = CraftItemCategory.DEFENSE,
        description = "Emergency trauma bandages and intravenous chelating RadAway to flush radiation and heal injured Dwellers.",
        effectSummary = "+1 Field Medkit (+35% Health) & +1 RadAway (-40 mSv Rads)",
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
        title = "Stalk Dune Razorback",
        subtitle = "Fast canyon boar rich in protein & bone-scrap",
        spearsRequired = 1,
        trapsRequired = 0,
        requiresCrossbow = false,
        baseFoodReward = 22.0,
        baseWaterReward = 6.0,
        baseGoldReward = 8.0,
        baseCoresReward = 0.0,
        baseXpReward = 14.0,
        accentColor = SurplusGreen
    ),
    CANYON_PACK_STALKER(
        id = "CANYON_PACK_STALKER",
        title = "Trap Canyon Stalker Pack",
        subtitle = "Lure pack predators into a high-tension snare line for a huge meat & pelt haul",
        spearsRequired = 1,
        trapsRequired = 1,
        requiresCrossbow = false,
        baseFoodReward = 48.0,
        baseWaterReward = 14.0,
        baseGoldReward = 20.0,
        baseCoresReward = 0.0,
        baseXpReward = 28.0,
        accentColor = SolarAmber
    ),
    APEX_RAD_BEHEMOTH(
        id = "APEX_RAD_BEHEMOTH",
        title = "Hunt Apex Rad-Behemoth",
        subtitle = "Requires Aether Crossbow: Massive mutant beast yielding Food Rations, Gold & Mutagen Core",
        spearsRequired = 2,
        trapsRequired = 1,
        requiresCrossbow = true,
        baseFoodReward = 95.0,
        baseWaterReward = 25.0,
        baseGoldReward = 45.0,
        baseCoresReward = 1.0,
        baseXpReward = 55.0,
        accentColor = BioluminescentViolet
    ),
    GLOWING_LEVIATHAN_WYRM(
        id = "GLOWING_LEVIATHAN_WYRM",
        title = "Slay Glowing Leviathan Wyrm",
        subtitle = "Apex Crater Boss (Requires Crossbow + 2 Spears + 2 Traps): Huge Food, Water, Gold & +2 Mutagen Cores",
        spearsRequired = 2,
        trapsRequired = 2,
        requiresCrossbow = true,
        baseFoodReward = 155.0,
        baseWaterReward = 50.0,
        baseGoldReward = 85.0,
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
        title = "Cast Line: Blind Cave Trout",
        subtitle = "Free hand-line fishing in the subterranean aquifer for Food & Clean Water",
        requiresSnareNet = false,
        requiresHarpoonSpear = false,
        baseFoodReward = 14.0,
        baseWaterReward = 10.0,
        baseGoldReward = 4.0,
        baseXpReward = 12.0,
        accentColor = CryoCyan
    ),
    RAD_KELP_CATFISH(
        id = "RAD_KELP_CATFISH",
        title = "Net Glowing Crater Catfish",
        subtitle = "Cast a snare-net into deep kelp trenches for hefty fillets & sunken coins",
        requiresSnareNet = true,
        requiresHarpoonSpear = false,
        baseFoodReward = 30.0,
        baseWaterReward = 18.0,
        baseGoldReward = 12.0,
        baseXpReward = 24.0,
        accentColor = SurplusGreen
    ),
    ABYSSAL_VAULT_STURGEON(
        id = "ABYSSAL_VAULT_STURGEON",
        title = "Harpoon Armored Vault Sturgeon",
        subtitle = "Uses 1 Harpoon Spear: Giant underground fish packed with Food, Water & Gold",
        requiresSnareNet = false,
        requiresHarpoonSpear = true,
        baseFoodReward = 55.0,
        baseWaterReward = 30.0,
        baseGoldReward = 26.0,
        baseXpReward = 42.0,
        accentColor = SolarAmber
    );
}

enum class WastelandExpeditionType(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationSeconds: Int,
    val deathChancePercent: Int = 10, // Explicit 10% chance the dweller dies!
    val goldReward: Double,
    val foodReward: Double,
    val waterReward: Double,
    val scrapReward: Double,
    val fuelReward: Double,
    val coresReward: Double = 0.0,
    val xpReward: Double,
    val accentColor: Color
) {
    SUPERMARKET_SUPPLY_RUN(
        id = "SUPERMARKET_SUPPLY_RUN",
        title = "RuinedSupermarket Scavenge",
        subtitle = "Send 1 Dweller for Food, Water & Gold (10% Chance Dweller Dies!)",
        durationSeconds = 10,
        deathChancePercent = 10,
        goldReward = 32.0,
        foodReward = 36.0,
        waterReward = 28.0,
        scrapReward = 24.0,
        fuelReward = 10.0,
        xpReward = 40.0,
        accentColor = SurplusGreen
    ),
    OLD_BANK_VAULT_HEIST(
        id = "OLD_BANK_VAULT_HEIST",
        title = "Collapsed Bank & Foundry Run",
        subtitle = "Send 1 Dweller for heavy Gold Bullion, Scrap & Reactor Fuel (10% Chance Dweller Dies!)",
        durationSeconds = 14,
        deathChancePercent = 10,
        goldReward = 68.0,
        foodReward = 18.0,
        waterReward = 16.0,
        scrapReward = 65.0,
        fuelReward = 26.0,
        xpReward = 65.0,
        accentColor = XpGold
    ),
    GLOWING_CRATER_CORE_RAID(
        id = "GLOWING_CRATER_CORE_RAID",
        title = "Ground-Zero Reactor Core Raid",
        subtitle = "Send 1 Dweller into deep radiation for Gold, Fuel & Mutagen Core (10% Chance Dweller Dies!)",
        durationSeconds = 18,
        deathChancePercent = 10,
        goldReward = 110.0,
        foodReward = 40.0,
        waterReward = 35.0,
        scrapReward = 80.0,
        fuelReward = 45.0,
        coresReward = 1.0,
        xpReward = 110.0,
        accentColor = BioluminescentViolet
    ),
    SUNKEN_SUBMARINE_REACTOR(
        id = "SUNKEN_SUBMARINE_REACTOR",
        title = "Sunken Ballistic Sub Salvage",
        subtitle = "Deep trench dive for massive Scrap, Bio-Fuel, Gold & +2 Mutagen Cores (10% Base Death Risk)",
        durationSeconds = 22,
        deathChancePercent = 10,
        goldReward = 155.0,
        foodReward = 55.0,
        waterReward = 55.0,
        scrapReward = 125.0,
        fuelReward = 75.0,
        coresReward = 2.0,
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
        shortLabel = "ECO (-40% DRAIN)",
        title = "Eco-Conservation Protocol",
        summary = "Cuts Vault & Room Resource Upkeep by -40% and MW draw by -15% (-15% Output) — ideal to exit Resource Deficits & restore Defenses!",
        outputMultiplier = 0.85,
        upkeepMultiplier = 0.60,
        megawattDemandMultiplier = 0.85,
        accentColor = SurplusGreen
    ),
    STANDARD(
        id = "STANDARD",
        shortLabel = "STANDARD (100%)",
        title = "Standard Bunker Posture",
        summary = "Balanced 100% Vault resource production, standard room upkeep, and normal Megawatt draw.",
        outputMultiplier = 1.0,
        upkeepMultiplier = 1.0,
        megawattDemandMultiplier = 1.0,
        accentColor = RadTeal
    ),
    OVERCLOCK(
        id = "OVERCLOCK",
        shortLabel = "OVERCLOCK (+30%)",
        title = "High-Yield Overclock",
        summary = "Boosts all Vault Resource & XP Output by +30%, but increases Room Upkeep by +25% and MW draw by +15%.",
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
        buttonLabel = "FLAK VOLLEY (8 AMMO)",
        description = "Fires concentrated flak salvos to shred incoming raiders (-25% Siege DEF Req & +10s timer).",
        defenseReductionPercent = 25,
        bonusIntegrityRestore = 0,
        ammoCost = 8,
        accentColor = SolarAmber
    ),
    EMP_PULSE(
        id = "EMP_PULSE",
        buttonLabel = "EMP PULSE (20 FUEL)",
        description = "Detonates an electromagnetic shockwave that cripples Warlord armor (-35% Siege DEF Req).",
        defenseReductionPercent = 35,
        bonusIntegrityRestore = 0,
        bioFuelCost = 20.0,
        accentColor = CryoCyan
    ),
    EMERGENCY_WELD(
        id = "EMERGENCY_WELD",
        buttonLabel = "DOOR WELD (25 SCRAP)",
        description = "Welds emergency blast plates mid-siege (+25% Door Integrity immediately & -15% Siege DEF Req).",
        defenseReductionPercent = 15,
        bonusIntegrityRestore = 25,
        scrapCost = 25.0,
        accentColor = SurplusGreen
    )
}

object PickaxeTier {
    fun nameForLevel(level: Int): String = when {
        level <= 1 -> "Rusty Iron Pickaxe"
        level == 2 -> "Reinforced Steel Pickaxe"
        level == 3 -> "Carbide Quarry Pickaxe"
        level == 4 -> "Pneumatic Impact Drill"
        level in 5..6 -> "Laser-Tipped Bore Pickaxe"
        level in 7..9 -> "Plasma Core Excavator"
        else -> "Atomic Fission Pickaxe Mk.$level"
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
    val weakenedByPercent: Int = 0
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
        displayName = "Ironforge Bastion",
        distanceKm = 42,
        description = "Armored foundry town. Pays +35% premium Gold for Scrap Metal & Bio-Fuel (peaks during Scorching Day).",
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
        displayName = "Oasis Spire",
        distanceKm = 58,
        description = "Canyon agricultural hub. Pays +35% premium Gold for Purified Water & Food Rations (peaks at Dawn/Dusk).",
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
        displayName = "Neon Vault 88",
        distanceKm = 85,
        description = "High-tech syndicate enclave. Pays +40% premium Gold for Research Data & Bio-Fuel (peaks during Rad-Night).",
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
        title = "Thorium Fission Rod",
        usageSummary = "Used in Thorium Power Stations, 60s Grid Overclock, Armored Convoys & Plasma Defense Batteries.",
        baseGoldCost = 38.0,
        batchSize = 1.0
    ),
    AETHER_CAPACITOR(
        id = "AETHER_CAPACITOR",
        resourceType = ResourceType.AETHER_CELLS,
        title = "Aether Super-Capacitor",
        usageSummary = "Used in Armored Convoy Engines, Solar Substations, Aether Crossbows & Auto Sentry Turrets.",
        baseGoldCost = 42.0,
        batchSize = 1.0
    ),
    MUTAGENIC_CORE(
        id = "MUTAGENIC_CORE",
        resourceType = ResourceType.MUTAGEN_CORES,
        title = "Refined Mutagenic Core",
        usageSummary = "Rare bio-catalyst used in Apex Skill Tree nodes, Aether Substations, and Plasma Defense.",
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
        displayName = "Bio-Sludge Combustion Turbine",
        description = "Burns Bio-Fuel to provide steady baseline Megawatts (MW). Higher reactor levels consume progressively more Bio-Fuel/s.",
        baseMegawattsPerLevel = 12.0,
        fuelUpkeepPerLevel = 0.30,
        baseScrapCost = 35.0,
        baseGoldCost = 20.0,
        accentColor = RustCopper
    ),
    SOLAR_HELIOSTAT_TOWER(
        id = "SOLAR_HELIOSTAT_TOWER",
        displayName = "Heliostat Solar Power Tower",
        description = "Built with Aether Capacitors. Generates massive MW during Scorching Day (+50%); higher levels burn Bio-Fuel for thermal salt backup.",
        baseMegawattsPerLevel = 22.0,
        fuelUpkeepPerLevel = 0.12,
        baseScrapCost = 60.0,
        baseGoldCost = 35.0,
        baseAetherCost = 1.0,
        isSolarDependent = true,
        accentColor = SolarAmber
    ),
    THORIUM_FISSION_REACTOR(
        id = "THORIUM_FISSION_REACTOR",
        displayName = "Thorium Fission Power Plant",
        description = "Powered by rare Thorium Rods. Generates immense 24/7 MW & +8% output/lv, but higher reactor levels take much more Bio-Fuel!",
        baseMegawattsPerLevel = 45.0,
        fuelUpkeepPerLevel = 0.45,
        baseScrapCost = 110.0,
        baseGoldCost = 65.0,
        baseThoriumCost = 2.0,
        gridMultiplierPerLevel = 0.08,
        accentColor = WarningOrange
    ),
    AETHER_RESONANCE_SUBSTATION(
        id = "AETHER_RESONANCE_SUBSTATION",
        displayName = "Aether High-Voltage Substation",
        description = "Uses Aether Capacitors & Mutagen Cores to amplify total Grid MW by +20%/lv; higher reactor levels take more Bio-Fuel.",
        baseMegawattsPerLevel = 25.0,
        fuelUpkeepPerLevel = 0.25,
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
        title = "Armored Paymaster Wreck",
        subtitle = "Crack convoy strongboxes for Gold Sovereigns, Scrap & Caches",
        primaryResource = ResourceType.SCRAP,
        secondaryResource = ResourceType.BIO_FUEL,
        basePrimaryYield = 10.0,
        baseSecondaryYield = 3.5,
        baseGoldYield = 14.0,
        maxDurability = 4,
        cacheDropCount = 1,
        isNocturnal = false,
        accentColor = SolarAmber
    ),
    DEW_VAPOR_GEYSER(
        id = "DEW_VAPOR_GEYSER",
        title = "Subterranean Dew & Coin Pocket",
        subtitle = "Siphon condensed canyon water, rations & sunken caravan gold",
        primaryResource = ResourceType.WATER,
        secondaryResource = ResourceType.RATIONS,
        basePrimaryYield = 9.0,
        baseSecondaryYield = 6.0,
        baseGoldYield = 11.0,
        maxDurability = 3,
        cacheDropCount = 1,
        isNocturnal = false,
        accentColor = CryoCyan
    ),
    OVERGROWN_BIO_DOME(
        id = "OVERGROWN_BIO_DOME",
        title = "Overgrown Ag-Dome Vault",
        subtitle = "Harvest wild rations, bio-fuel & old-world trade bullion",
        primaryResource = ResourceType.RATIONS,
        secondaryResource = ResourceType.BIO_FUEL,
        basePrimaryYield = 9.5,
        baseSecondaryYield = 4.5,
        baseGoldYield = 12.0,
        maxDurability = 3,
        cacheDropCount = 1,
        isNocturnal = false,
        accentColor = SurplusGreen
    ),
    ENCRYPTED_ORBITAL_POD(
        id = "ENCRYPTED_ORBITAL_POD",
        title = "Pre-Fall Syndicate Pay-Pod",
        subtitle = "Crack black-box vaults for Gold Bullion, Research Data & Scrap",
        primaryResource = ResourceType.RESEARCH_DATA,
        secondaryResource = ResourceType.SCRAP,
        basePrimaryYield = 7.5,
        baseSecondaryYield = 8.0,
        baseGoldYield = 22.0,
        maxDurability = 4,
        cacheDropCount = 2,
        isNocturnal = true,
        accentColor = XpGold
    ),
    BIOLUMINESCENT_METEOR_VEIN(
        id = "BIOLUMINESCENT_METEOR_VEIN",
        title = "Bioluminescent Gold & Core Vein",
        subtitle = "Nocturnal crystal fissure rich in Gold, Mutagen Cores & Tech",
        primaryResource = ResourceType.MUTAGEN_CORES,
        secondaryResource = ResourceType.RESEARCH_DATA,
        basePrimaryYield = 1.0,
        baseSecondaryYield = 7.0,
        baseGoldYield = 26.0,
        maxDurability = 5,
        cacheDropCount = 2,
        isNocturnal = true,
        accentColor = BioluminescentViolet
    ),
    RADIOACTIVE_SLUDGE_TANKER(
        id = "RADIOACTIVE_SLUDGE_TANKER",
        title = "Rad-Sludge Escort Tanker",
        subtitle = "Drain volatile Bio-Fuel, heavy Scrap & escort Gold safes",
        primaryResource = ResourceType.BIO_FUEL,
        secondaryResource = ResourceType.SCRAP,
        basePrimaryYield = 8.5,
        baseSecondaryYield = 8.0,
        baseGoldYield = 18.0,
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
    const val UNIQUE_DWELLER_CHANCE_PERCENT = 8 // Rare 8% chance to gain a Unique Vault Dweller!

    private val firstNames = listOf("Kael", "Vesper", "Rook", "Nyx", "Juno", "Dax", "Sora", "Marrow", "Vane", "Lyra", "Tark", "Zed")
    private val callsigns = listOf(
        "Ironjaw", "Gearwright", "Mistwalker", "Cinder", "Sparkplug",
        "Wraith", "Rusthound", "Volt", "Dune-Strider", "Hex", "Welder", "Echo"
    )
    private val uniqueNames = listOf("Vance", "Aria", "Gideon", "Nova", "Orion", "Kratos", "valkyrie", "Zephyr")
    private val uniqueTitles = listOf(
        "Prime Overseer", "Pre-War Prodigy", "Vault-Tec Chief", "Apex Harvester",
        "Quantum Savant", "Core Titan", "Aether Vanguard", "Master Forger"
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
                "★ UNIQUE VAULT DWELLER (8% RARE): +40% All-Resource Gathering Ability (Vault, Pickaxe, Hunting, Fishing & Expeditions) + ${bonusXp.toInt()} Class XP!"
            } else {
                when (targetClass) {
                    WorkerClassType.SCRAPPER -> "Vault Miner & Guard (+${bonusXp.toInt()} XP • 8% Awaken Chance • Takes 2 Food & 2 Water/Day)"
                    WorkerClassType.HYDRO_ENGINEER -> "Water & Farm Tech (+${bonusXp.toInt()} XP • 8% Awaken Chance • Takes 2 Food & 2 Water/Day)"
                    WorkerClassType.NIGHT_STALKER -> "Wasteland Scout & Guard (+${bonusXp.toInt()} XP • 8% Awaken Chance • Takes 2 Food & 2 Water/Day)"
                    WorkerClassType.TECH_SAVANT -> "Reactor & Turret Engineer (+${bonusXp.toInt()} XP • 8% Awaken Chance • Takes 2 Food & 2 Water/Day)"
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
        displayName = "Wasteland Scrapper",
        roleSummary = "Dismantles Raw Salvage Caches & mans perimeter barricades (+8 Defense each). +35% Day Scrap.",
        favoredPhases = listOf(DayNightPhase.DAWN, DayNightPhase.SCORCHING_DAY),
        primaryResource = ResourceType.SCRAP,
        baseOutputPerSecond = 2.4,
        dayProductivityMultiplier = 1.35,
        nightProductivityMultiplier = 0.80,
        accentColor = SolarAmber,
        perks = listOf(
            VeteranPerk(5, "Plasma Torch Cutters", "+25% Scrapper speed & +20% manual node Scrap."),
            VeteranPerk(10, "Byproduct Siphoning", "Scrappers also refine +0.40 Bio-Fuel/s each."),
            VeteranPerk(15, "Floodlight Visors", "Halves the Rad-Night productivity penalty."),
            VeteranPerk(20, "Titan Wreck Breakers", "+45% Scrap output & auto-dismantles 2x more caches.")
        )
    ),
    HYDRO_ENGINEER(
        id = "HYDRO_ENGINEER",
        displayName = "Hydro & Bio Engineer",
        roleSummary = "Refines salvaged moisture & hunted game into Water, Food Rations & Fuel (+30% Dawn/Dusk).",
        favoredPhases = listOf(DayNightPhase.DAWN, DayNightPhase.DUSK),
        primaryResource = ResourceType.WATER,
        baseOutputPerSecond = 1.9,
        dayProductivityMultiplier = 1.15,
        nightProductivityMultiplier = 1.15,
        accentColor = CryoCyan,
        perks = listOf(
            VeteranPerk(5, "Vapor Recovery Membranes", "+25% Water & Rations yield from hired engineers."),
            VeteranPerk(10, "Catalytic Sludge Cracking", "+0.65 Bio-Fuel/s per assigned Hydro-Engineer."),
            VeteranPerk(15, "Closed-Loop Irrigation", "Reduces Greenhouse water upkeep by 50%."),
            VeteranPerk(20, "Cryo-Fusion Turbines", "+40% all-facility output & +15% Storage cap.")
        )
    ),
    NIGHT_STALKER(
        id = "NIGHT_STALKER",
        displayName = "Nocturnal Stalker",
        roleSummary = "Master hunter (+15% Hunt Food & +15 Defense each), escorts convoys & harvests Mutagen Rigs.",
        favoredPhases = listOf(DayNightPhase.DUSK, DayNightPhase.RAD_NIGHT),
        primaryResource = ResourceType.MUTAGEN_CORES,
        baseOutputPerSecond = 0.14,
        dayProductivityMultiplier = 0.65,
        nightProductivityMultiplier = 1.65,
        accentColor = BioluminescentViolet,
        perks = listOf(
            VeteranPerk(5, "Geiger Echo-Optics", "+30% Mutagen Core harvesting & +15% Convoy Gold."),
            VeteranPerk(10, "UV Thermal Cloaks", "Removes daytime heat sluggishness (+35% Day output)."),
            VeteranPerk(15, "Shadow Cache Hauling", "Stalkers bring back +1.8 Scrap/s & +0.8 Fuel/s at night."),
            VeteranPerk(20, "Apex Wraith Protocol", "Doubles Mutagen Core yield during Bioluminescent Night.")
        )
    ),
    TECH_SAVANT(
        id = "TECH_SAVANT",
        displayName = "Cyber-Tech Savant",
        roleSummary = "Optimizes Power Stations & Sentry Turrets, decodes Research Data & boosts Outpost XP (+15%).",
        favoredPhases = listOf(DayNightPhase.DUSK, DayNightPhase.RAD_NIGHT),
        primaryResource = ResourceType.RESEARCH_DATA,
        baseOutputPerSecond = 1.1,
        dayProductivityMultiplier = 1.10,
        nightProductivityMultiplier = 1.35,
        accentColor = XpGold,
        perks = listOf(
            VeteranPerk(5, "Neural Co-Processors", "+25% Research Data & +15% Power Grid MW."),
            VeteranPerk(10, "Automated Drone Relays", "+10% global production across all Outpost structures."),
            VeteranPerk(15, "Mentor Broadcasts", "All hired Survivor Classes gain +35% faster shift XP."),
            VeteranPerk(20, "Singularity Uplink", "+35% Research Data & -20% Phase Shift cooldowns.")
        )
    );

    companion object {
        fun xpForNextClassLevel(level: Int): Double {
            return 210.0 * level.toDouble().pow(1.75)
        }
    }
}

enum class BuildingCategory(val label: String) {
    EXTRACTION("Salvage & Extraction"),
    REFINERY("Refining & Tech"),
    INFRASTRUCTURE("Housing & Vaults")
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
    val costGrowthFactor: Double = 1.42,
    val favoredWorkerClass: WorkerClassType?,
    val storageBonusPerLevel: Double = 0.0,
    val populationBonusPerLevel: Int = 0,
    val defenseBonusPerLevel: Int = 0
) {
    SURVIVOR_HAB_BLOCK(
        id = "SURVIVOR_HAB_BLOCK",
        displayName = "Vault Living Quarters",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 1,
        description = "Radiation-shielded dormitories required to house Dwellers (up to 15 Max Vault Dwellers). Higher levels draw more life-support MW.",
        gameplayNeedSummary = "REQUIRED TO HIRE DWELLERS: +3 Max Dwellers/Lv (Up to 15 Max Dwellers) & -5% Daily Food/Water Upkeep/Lv",
        primaryResource = ResourceType.RATIONS,
        baseOutputPerLevel = 0.8,
        megawattDemandPerLevel = 3.0,
        baseBuildTimeSeconds = 12,
        manualSalvageBonusDesc = "+3 Max Dweller Capacity (Max 15) & +5% Morale per level",
        baseScrapCost = 28.0,
        baseWaterCost = 10.0,
        favoredWorkerClass = null,
        populationBonusPerLevel = 3
    ),
    SALVAGE_YARD(
        id = "SALVAGE_YARD",
        displayName = "Vault Ore & Gold Smeltery",
        category = BuildingCategory.EXTRACTION,
        requiredCommandLevel = 1,
        description = "High-voltage magnetic crushers & gold furnaces. Higher levels draw heavier industrial MW.",
        gameplayNeedSummary = "REQUIRED FOR PICKAXE & GOLD: Max Pickaxe Lv = (2 + 2×Lv), +25% Gold Smelting/Lv & Auto-Scrap",
        primaryResource = ResourceType.SCRAP,
        baseOutputPerLevel = 3.2,
        megawattDemandPerLevel = 4.0,
        baseBuildTimeSeconds = 15,
        manualSalvageBonusDesc = "+25% Pickaxe Scrap/Gold Yield & +2 Max Pickaxe Tier per level",
        baseScrapCost = 22.0,
        favoredWorkerClass = WorkerClassType.SCRAPPER
    ),
    ATMOSPHERIC_CONDENSER(
        id = "ATMOSPHERIC_CONDENSER",
        displayName = "Subterranean Water Purifier",
        category = BuildingCategory.EXTRACTION,
        requiredCommandLevel = 1,
        description = "Deep-aquifer filtration turbines that supply daily Dweller drinking water & unlock deep crater fishing.",
        gameplayNeedSummary = "REQUIRED FOR WATER & FISHING: +6 Daily Water/Lv, Unlocks Deep Fishing & +25% Fish Yield/Lv",
        primaryResource = ResourceType.WATER,
        baseOutputPerLevel = 2.6,
        megawattDemandPerLevel = 4.0,
        baseBuildTimeSeconds = 18,
        manualSalvageBonusDesc = "+25% Fishing Yield & +6 Clean Water every New Day per level",
        baseScrapCost = 26.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER
    ),
    HYDROPONIC_GREENHOUSE(
        id = "HYDROPONIC_GREENHOUSE",
        displayName = "UV Hydroponic Farm",
        category = BuildingCategory.EXTRACTION,
        requiredCommandLevel = 1,
        description = "High-intensity UV crop bays that feed your Dwellers each day. Higher levels draw heavy UV lamp MW.",
        gameplayNeedSummary = "REQUIRED FOR DWELLER FOOD: +8 Daily Food/Lv, +25% Hunting & Trap Food/Lv & Auto-Rations",
        primaryResource = ResourceType.RATIONS,
        baseOutputPerLevel = 2.5,
        megawattDemandPerLevel = 5.0,
        baseBuildTimeSeconds = 21,
        manualSalvageBonusDesc = "+25% Hunting Food & +8 Food Rations every New Day per level",
        waterUpkeepPerLevel = 0.30,
        baseScrapCost = 32.0,
        baseWaterCost = 12.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER
    ),
    REINFORCED_SILO(
        id = "REINFORCED_SILO",
        displayName = "Decontamination Airlock & Medbay",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 2,
        description = "Lead-shielded decontamination showers, trauma medbay & armored airlock that scrub Vault radiation and heal Dwellers.",
        gameplayNeedSummary = "REQUIRED FOR RADS, HEALING & SIEGE: Scrubs Rads, Heals Dwellers, +28 Defense/Lv & +500 Storage/Lv",
        primaryResource = ResourceType.SCRAP,
        baseOutputPerLevel = 1.5,
        megawattDemandPerLevel = 4.0,
        baseBuildTimeSeconds = 24,
        manualSalvageBonusDesc = "Auto-scrubs Vault Rads, +28 Defense, -4% Door Damage & +500 Storage/Lv",
        baseScrapCost = 70.0,
        baseWaterCost = 25.0,
        favoredWorkerClass = WorkerClassType.SCRAPPER,
        storageBonusPerLevel = 500.0,
        defenseBonusPerLevel = 28
    ),
    BIO_SLUDGE_REFINERY(
        id = "BIO_SLUDGE_REFINERY",
        displayName = "Bio-Fuel & Munitions Refinery",
        category = BuildingCategory.REFINERY,
        requiredCommandLevel = 2,
        description = "Distills reactor fuel and manufactures ammunition & repair alloys for Vault defenses.",
        gameplayNeedSummary = "REQUIRED FOR TURRETS & REPAIRS: Unlocks Mine/Turret Crafting, -10% Door Repair Cost/Lv & +Fuel",
        primaryResource = ResourceType.BIO_FUEL,
        baseOutputPerLevel = 2.0,
        megawattDemandPerLevel = 7.0,
        baseBuildTimeSeconds = 27,
        manualSalvageBonusDesc = "-10% Vault Door Repair Cost & +25% Bio-Fuel Yield per level",
        waterUpkeepPerLevel = 0.25,
        baseScrapCost = 95.0,
        baseWaterCost = 36.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER
    ),
    SIGNAL_RESEARCH_ARRAY(
        id = "SIGNAL_RESEARCH_ARRAY",
        displayName = "Wasteland Radar & Recon Array",
        category = BuildingCategory.REFINERY,
        requiredCommandLevel = 3,
        description = "Long-range telemetry dish that guides wasteland expeditions away from lethal ambushes.",
        gameplayNeedSummary = "REQUIRED FOR SAFE EXPEDITIONS: -1% Dweller Death Risk/Lv, +20% Expedition Loot & +15% XP/Lv",
        primaryResource = ResourceType.RESEARCH_DATA,
        baseOutputPerLevel = 1.8,
        megawattDemandPerLevel = 9.0,
        baseBuildTimeSeconds = 30,
        manualSalvageBonusDesc = "-1% Dweller Expedition Death Risk & +15% Vault XP per level",
        baseScrapCost = 140.0,
        baseFuelCost = 32.0,
        favoredWorkerClass = WorkerClassType.TECH_SAVANT
    ),
    NOCTURNAL_EXTRACTION_RIG(
        id = "NOCTURNAL_EXTRACTION_RIG",
        displayName = "Deep-Core Aether & Mutagen Lab",
        category = BuildingCategory.REFINERY,
        requiredCommandLevel = 4,
        description = "High-energy particle collider that synthesizes Mutagen Cores and overclocks Vault Power Generators.",
        gameplayNeedSummary = "REQUIRED FOR APEX TECH: +12% Total Vault Power MW/Lv, Unlocks Plasma Shield & +Mutagen Cores",
        primaryResource = ResourceType.MUTAGEN_CORES,
        baseOutputPerLevel = 0.25,
        megawattDemandPerLevel = 14.0,
        baseBuildTimeSeconds = 34,
        manualSalvageBonusDesc = "+12% Total Power Grid MW & +1 Bonus Core on Vein Break per level",
        baseScrapCost = 210.0,
        baseFuelCost = 70.0,
        baseCoresCost = 1.0,
        favoredWorkerClass = WorkerClassType.NIGHT_STALKER
    ),
    GEOTHERMAL_TAP(
        id = "GEOTHERMAL_TAP",
        displayName = "Deep-Core Geothermal Tap",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 3,
        description = "Subterranean Floor B5 magma bore that generates +7.5 MW/Lv clean 24/7 power and reduces Generator Wear.",
        gameplayNeedSummary = "CLEAN 24/7 POWER (FLOOR B5): +7.5 MW/Lv Continuous Power (0 MW Draw!) & -10% Generator Wear/Lv",
        primaryResource = ResourceType.BIO_FUEL,
        baseOutputPerLevel = 1.4,
        megawattDemandPerLevel = 1.0,
        baseBuildTimeSeconds = 38,
        manualSalvageBonusDesc = "+7.5 Continuous Baseline MW & -10% Generator Wear per level",
        baseScrapCost = 125.0,
        baseWaterCost = 35.0,
        favoredWorkerClass = WorkerClassType.TECH_SAVANT
    ),
    CRYO_STASIS_VAULT(
        id = "CRYO_STASIS_VAULT",
        displayName = "Cryo-Stasis & Ration Vault",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 4,
        description = "Deep-freeze preservation vaults on Floor B5 that cut Dweller 60s & daily Food/Water appetite by -8%/Lv.",
        gameplayNeedSummary = "CUTS DWELLER HUNGER (FLOOR B5): -8% Dweller 60s & Daily Food/Water Drain/Lv & +400 Storage/Lv",
        primaryResource = ResourceType.RATIONS,
        baseOutputPerLevel = 2.2,
        megawattDemandPerLevel = 5.0,
        baseBuildTimeSeconds = 42,
        manualSalvageBonusDesc = "-8% Dweller Food/Water Appetite Drain & +400 Vault Storage per level",
        baseScrapCost = 165.0,
        baseWaterCost = 45.0,
        baseFuelCost = 30.0,
        favoredWorkerClass = WorkerClassType.HYDRO_ENGINEER,
        storageBonusPerLevel = 400.0
    ),
    VAULT_STORE_ROOM(
        id = "VAULT_STORE_ROOM",
        displayName = "Vault Supply Store Room",
        category = BuildingCategory.INFRASTRUCTURE,
        requiredCommandLevel = 1,
        description = "Reinforced subterranean cargo bay with expandable high-density shelving that vastly increases max stored Scrap, Water, Food, Bio-Fuel & Tech when upgraded.",
        gameplayNeedSummary = "UPGRADABLE RESOURCE STORAGE (FLOOR B6): +1,000+ Escalating Max Resource Storage/Lv & +10% Crate Yield/Lv",
        primaryResource = ResourceType.SCRAP,
        baseOutputPerLevel = 1.8,
        megawattDemandPerLevel = 2.0,
        baseBuildTimeSeconds = 46,
        manualSalvageBonusDesc = "+1,000+ Escalating Max Resource Storage & +10% Salvage Crate Yield per level",
        baseScrapCost = 30.0,
        baseWaterCost = 8.0,
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
        shortTitle = "Surface Ore Mine",
        floorIndex = 0,
        wingColumn = 0,
        buildingType = null,
        accentColor = SolarAmber
    ),
    UNDERGROUND_FISHING_SUMP(
        id = "UNDERGROUND_FISHING_SUMP",
        shortTitle = "Sub-Vault Fishing Sump",
        floorIndex = 0,
        wingColumn = 1,
        buildingType = null,
        accentColor = CryoCyan
    ),
    ROOM_1_QUARTERS(
        id = "ROOM_1_QUARTERS",
        shortTitle = "#1 Living Quarters",
        floorIndex = 1,
        wingColumn = 0,
        buildingType = BuildingType.SURVIVOR_HAB_BLOCK,
        accentColor = SurplusGreen
    ),
    ROOM_2_SMELTERY(
        id = "ROOM_2_SMELTERY",
        shortTitle = "#2 Ore & Gold Smeltery",
        floorIndex = 1,
        wingColumn = 1,
        buildingType = BuildingType.SALVAGE_YARD,
        accentColor = SolarAmber
    ),
    ROOM_3_WATER_PURIFIER(
        id = "ROOM_3_WATER_PURIFIER",
        shortTitle = "#3 Water Purifier",
        floorIndex = 2,
        wingColumn = 0,
        buildingType = BuildingType.ATMOSPHERIC_CONDENSER,
        accentColor = CryoCyan
    ),
    ROOM_4_HYDROPONICS(
        id = "ROOM_4_HYDROPONICS",
        shortTitle = "#4 Hydroponic Farm",
        floorIndex = 2,
        wingColumn = 1,
        buildingType = BuildingType.HYDROPONIC_GREENHOUSE,
        accentColor = SurplusGreen
    ),
    ROOM_5_MEDBAY_AIRLOCK(
        id = "ROOM_5_MEDBAY_AIRLOCK",
        shortTitle = "#5 Decon & Medbay",
        floorIndex = 3,
        wingColumn = 0,
        buildingType = BuildingType.REINFORCED_SILO,
        accentColor = RadTeal
    ),
    ROOM_6_MUNITIONS_REFINERY(
        id = "ROOM_6_MUNITIONS_REFINERY",
        shortTitle = "#6 Fuel & Munitions",
        floorIndex = 3,
        wingColumn = 1,
        buildingType = BuildingType.BIO_SLUDGE_REFINERY,
        accentColor = RustCopper
    ),
    ROOM_7_RADAR_ARRAY(
        id = "ROOM_7_RADAR_ARRAY",
        shortTitle = "#7 Recon Radar",
        floorIndex = 4,
        wingColumn = 0,
        buildingType = BuildingType.SIGNAL_RESEARCH_ARRAY,
        accentColor = XpGold
    ),
    ROOM_8_MUTAGEN_LAB(
        id = "ROOM_8_MUTAGEN_LAB",
        shortTitle = "#8 Deep Mutagen Lab",
        floorIndex = 4,
        wingColumn = 1,
        buildingType = BuildingType.NOCTURNAL_EXTRACTION_RIG,
        accentColor = BioluminescentViolet
    ),
    ROOM_9_GEOTHERMAL(
        id = "ROOM_9_GEOTHERMAL",
        shortTitle = "#9 Geothermal Tap",
        floorIndex = 5,
        wingColumn = 0,
        buildingType = BuildingType.GEOTHERMAL_TAP,
        accentColor = WarningOrange
    ),
    ROOM_10_CRYO_VAULT(
        id = "ROOM_10_CRYO_VAULT",
        shortTitle = "#10 Cryo Ration Vault",
        floorIndex = 5,
        wingColumn = 1,
        buildingType = BuildingType.CRYO_STASIS_VAULT,
        accentColor = CryoCyan
    ),
    ROOM_11_STORE_ROOM(
        id = "ROOM_11_STORE_ROOM",
        shortTitle = "#11 Supply Store Room",
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
        level <= 1 -> "Lone Pickaxe Survivor"
        level in 2..3 -> "Crater Bedrock Miner"
        level in 4..6 -> "Bunker Shelter Builder"
        level in 7..9 -> "Vault Gate Defender"
        level in 10..14 -> "Day-10 Siege Veteran"
        level in 15..24 -> "Subterranean Vault Warden"
        level in 25..39 -> "Nuclear Reactor Commander"
        level in 40..59 -> "Wasteland Fortress Overseer"
        level in 60..79 -> "Apex Atomic Archon"
        level in 80..99 -> "Supreme Vault Sovereign"
        else -> "Level 100 Grand Vault Legend"
    }

    fun skillPointsForReachingLevel(newLevel: Int): Int {
        return if (newLevel % 5 == 0) 2 else 1
    }

    fun globalProductionBonus(level: Int): Double {
        return 1.0 + (level - 1) * 0.10
    }
}
