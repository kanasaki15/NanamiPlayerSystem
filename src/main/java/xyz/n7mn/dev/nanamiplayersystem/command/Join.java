package xyz.n7mn.dev.nanamiplayersystem.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Join implements CommandExecutor, TabCompleter {

    private final Plugin plugin;

    public Join(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Prefix")) + ChatColor.RESET + " ";

        if (!sender.isOp()){
            sender.sendMessage(prefix + ChatColor.RED + "権限がありません。");
            return true;
        }

        if (args.length != 1 && args.length != 2){
            sender.sendMessage(prefix + "/join (add|remove) (PlayerName|PermName) or /join (true|false)");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        return list;
    }
}
