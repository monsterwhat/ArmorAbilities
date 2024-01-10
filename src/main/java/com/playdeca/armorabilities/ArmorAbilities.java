package com.playdeca.armorabilities;

import com.playdeca.armorabilities.data.AbilityManager;
import com.playdeca.armorabilities.data.ConfigData;
import com.playdeca.armorabilities.listeners.CombatListeners;
import com.playdeca.armorabilities.listeners.InventoryClick;
import com.playdeca.armorabilities.listeners.JoinListeners;
import com.playdeca.armorabilities.listeners.PlayerMoveListeners;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class ArmorAbilities extends JavaPlugin implements Listener {

    private ConfigData configData;
    private AbilityManager manager;
    private AbilityCheckerTask task;
    private static ArmorAbilities instance;

    @Override
    public void onEnable() {

        instance = this;
        File configFile = new File(getDataFolder() + "/config.yml");

        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        configData = new ConfigData(getConfig());
        manager = new AbilityManager(this);
        task = new AbilityCheckerTask(this);
        task.runTaskTimer(this, 20, 20);

        //init event listeners
        initListeners();

        //commands
        Objects.requireNonNull(this.getCommand("ability")).setExecutor(new Commands(this));

        //add all currently online players (if a /reload was triggered or server took a while starting up)
        task.addPlayers();
        Bukkit.getLogger().info("[ArmorAbilities] Enabled.");
    }

    public void initListeners(){
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new CombatListeners(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClick(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListeners(this), this);
        getServer().getPluginManager().registerEvents(new JoinListeners(this), this);
        Bukkit.getLogger().info("[ArmorAbilities] Registered listeners.");
    }

    public static ArmorAbilities getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        getServer().clearRecipes();
    }

    public ConfigData getData() {
        return configData;
    }

    public AbilityManager getManager() {
        return manager;
    }

    public AbilityCheckerTask getTask() {
        return task;
    }
}
