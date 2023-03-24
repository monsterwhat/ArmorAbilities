package com.playdeca.armorabilities.data;

import org.bukkit.ChatColor;

/**
 * an enum which stores the information for each ability
 */
public enum Ability {
    SPEED(false,
             "lets you run faster with each piece of Speed Armor. You will also dig faster with a full suit of Speed Armor equipped."),
    MOON(false,
            "lets you jump higher with each piece of Moon Armor. You will also take no fall damage with a full suit of Moon Armor equipped."),
    SCUBA(false,
            "lets you breathe underwater longer with each piece of Scuba Armor. You will also dig faster underwater with a full suit of Scuba Armor equipped."),
    LAVA(false,
            "lets you swim in lava longer with each piece of Lava Armor. You will also take no fire damage with a full suit of Lava Armor equipped."),
    RAGE(true,
            "strikes attackers with lightning when a full suit of Rage Armor is equipped."),
    PEACE(true,
            "makes you neutral to hostile mobs when a full suit of Peace Armor is equipped."),
    ASSASSIN(true,
            "is activated when a full suit of Assassin Armor is equipped. When sneaking, it makes you invisible to other players and increases your damage."),
    CREEPER(true,
            "makes you explode upon death when a full suit of Creeper Armor is equipped."),
    SPIDER(true,
            "lets you climb walls when a full suit of Spider Armor is equipped."),
    VAMPIRE(true,
            "sucks health from things you attack when a full suit of Vampire Armor is equipped."),
    MINER(true,
            "gives you night vision and a faster dig speed when a full suit of Miner Armor is equipped.");

    private final boolean requireFullSet;
    private final String name;
    private final String description;

    Ability(boolean requireFullSet, String description) {
        this.requireFullSet = requireFullSet;
        name = name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
        this.description = "The " + ChatColor.GOLD + name + ChatColor.WHITE + " ability " + description;
    }

    /**
     * Get the description for this ability, which describes what the ability does
     *
     * @return a string of the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determines if this ability requires all 4 armor items for the ability to take effect
     *
     * @return if the ability requires all 4 or not
     */
    public boolean requiresFullSet() {
        return requireFullSet;
    }

    /**
     * A readable name for this ability
     *
     * @return the name
     */
    @Override
    public String toString() {
        return name;
    }
}
