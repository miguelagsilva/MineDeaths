package me.vitruvis.vmorte;

import me.vitruvis.vmorte.commands.CommandVMorte;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

import static org.bukkit.Bukkit.getPluginManager;

public final class VMorte extends JavaPlugin implements Listener {
    HashMap<Player, Long> playersCooldown = new HashMap<>();
    private static VMorte instance;
    FileConfiguration messageConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (getPluginManager().getPlugin("DeluxeCombat") == null) {
            getLogger().warning("Não foi possível detetar o plugin DeluxeCombat! Todas as funcionalidades relacionadas vão ser desativadas.");
        }

        setInstance(this);
        saveDefaultConfig();
        createMessageConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("vmorte").setExecutor(new CommandVMorte());
    }

    public void createMessageConfig() {
        File messageConfigFile = new File(getDataFolder(), "messages.yml");
        if (!messageConfigFile.exists()) {
            messageConfigFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        messageConfig = YamlConfiguration.loadConfiguration(messageConfigFile);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExperiencePickup(PlayerExpChangeEvent e) {
        if (getPluginManager().getPlugin("DeluxeCombat") == null) {
            return;
        }

        DeluxeCombatAPI dc_api = new DeluxeCombatAPI();
        Player player = e.getPlayer();
        if (!dc_api.hasPvPEnabled(player)) {
            int initialValue = e.getAmount();
            int finalValue = (int) (initialValue * getConfig().getDouble("pvp-disabled-exp-modifier"));
            long currentTime = System.currentTimeMillis();

            if (getConfig().getBoolean("debug")) {
                getLogger().info("playersCooldown: " + playersCooldown.toString());
                getLogger().info("currentTime - playersCooldown.get(player) > getConfig().getLong(\"warning-interval-seconds\"): " + currentTime + " - " + playersCooldown.get(player) + " > " + getConfig().getLong("warning-interval-seconds"));
                getLogger().info("playersCooldown.containsKey(player): " + playersCooldown.containsKey(player));
            }

            if (!playersCooldown.containsKey(player) || currentTime - playersCooldown.get(player) > getConfig().getLong("warning-interval-seconds")*60*1000) {
                player.sendActionBar(MiniMessage.miniMessage().deserialize(
                        Objects.requireNonNull(messageConfig.getString("action-bar.enable-pvp"))));
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        Objects.requireNonNull(messageConfig.getString("messages.enable-pvp"))));
                playersCooldown.put(player, currentTime);
            }

            e.setAmount(finalValue);
        }
    }

    public static void setInstance(VMorte plugin) {
        instance = plugin;
    }
    public static VMorte getInstance() {
        return instance;
    }
    public FileConfiguration getMessageConfig() {
        return messageConfig;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
