package com.gigosaurus.armorabilities.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigData {

    private final ArrayList<AbilityInfo> abilities = new ArrayList<>(Ability.values().length);
    private int jumpNum;
    private int speedNum;
    private int speedHasteNum;
    private int scubaTime;
    private int scubaHasteNum;
    private int lavaTime;
    private int rageLightningDamage;
    private int rageFireTime;
    private int creeperAbilityExplosion;
    private boolean creeperBlockDamage = true;
    private int minerHasteNum;
    private int assassinDamage;
    private double vampirePercent;

    public ConfigData(FileConfiguration config) {
        for (Ability ability : Ability.values()) {
            ConfigurationSection cfg = config.getConfigurationSection(ability.name());

            if (cfg != null) {
                // int item = conf.getInt("Item");
                String item = cfg.getString("Item");
                List<String> types = cfg.getStringList("ArmorTypes");

                if ((types != null) && !types.isEmpty()) {
                    abilities.add(new AbilityInfo(ability, item, types));

                    switch (ability) {
                        case MOON:
                            jumpNum = cfg.getInt("JumpBoost");
                            break;
                        case SCUBA:
                            scubaHasteNum = cfg.getInt("Haste");
                            scubaTime = cfg.getInt("ScubaTime");
                            break;
                        case SPEED:
                            speedNum = cfg.getInt("SpeedBoost");
                            speedHasteNum = cfg.getInt("Haste");
                            break;
                        case LAVA:
                            lavaTime = cfg.getInt("LavaTime");
                            break;
                        case RAGE:
                            rageLightningDamage = cfg.getInt("LightningDamage");
                            rageFireTime = cfg.getInt("FireTime");
                            break;
                        case CREEPER:
                            creeperAbilityExplosion = cfg.getInt("ExplosionSize");
                            creeperBlockDamage = cfg.getBoolean("BlockDamage");
                            break;
                        case MINER:
                            minerHasteNum = cfg.getInt("Haste");
                            break;
                        case ASSASSIN:
                            assassinDamage = cfg.getInt("SneakDamage");
                            break;
                        case VAMPIRE:
                            vampirePercent = cfg.getDouble("VampirePercent", 25);
                            break;
                    }
                }
            }
        }
    }

    public AbilityInfo getInfo(String name) {
        for (AbilityInfo info : abilities) {
            if (name.equalsIgnoreCase(info.getAbility().name())) {
                return info;
            }
        }
        return null;
    }

    public ArrayList<AbilityInfo> getInfo() {
        return abilities;
    }

    public int getCreeperAbilityExplosion() {
        return creeperAbilityExplosion;
    }

    public int getRageLightningDamage() {
        return rageLightningDamage;
    }

    public int getRageFireTime() {
        return rageFireTime;
    }

    public int getAssassinDamage() {
        return assassinDamage;
    }

    public boolean isCreeperBlockDamage() {
        return creeperBlockDamage;
    }

    int getSpeedNum() {
        return speedNum;
    }

    int getSpeedHasteNum() {
        return speedHasteNum;
    }

    int getJumpNum() {
        return jumpNum;
    }

    public int getMinerHasteNum() {
        return minerHasteNum;
    }

    public int getScubaHasteNum() {
        return scubaHasteNum;
    }

    public int getLavaTime() {
        return lavaTime;
    }

    public int getScubaTime() {
        return scubaTime;
    }

    public double getVampirePercent() {
        return vampirePercent;
    }
}
