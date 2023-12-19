package me.vitruvis.vmorte.commands;

import me.vitruvis.vmorte.VMorte;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandVMorte implements CommandExecutor, TabExecutor {

    private static final VMorte plugin = VMorte.getInstance();
    private static final FileConfiguration messageConfig = plugin.getMessageConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("vmorte.admin")) {
                return false;
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(messageConfig.getString("commands.reload"))));
            try {
                File configFile = new File(plugin.getDataFolder(), "config.yml");
                plugin.getConfig().load(configFile);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            try {
                File messageConfigFile = new File(plugin.getDataFolder(), "messages.yml");
                plugin.getConfig().load(messageConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // admin
            if (sender.hasPermission("vmorte.admin")) {
                completions.add("reload");
            }
        }

        return completions.stream()
                .filter(completion -> completion.startsWith(args[args.length - 1]))
                .collect(Collectors.toList());
    }
}
