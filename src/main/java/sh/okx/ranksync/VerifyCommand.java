package sh.okx.ranksync;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class VerifyCommand implements CommandExecutor {
  private final RankSync plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return false;
    }

    Player player = (Player) sender;
    String s = plugin.addCode(player.getUniqueId());
    sender.sendMessage(ChatColor.GREEN + "Type "
        + ChatColor.GOLD + plugin.getConfig().getString("discord-verify-command") + " " + s
        + ChatColor.GREEN + " on Discord as a private message to the bot for this server.");
    return true;
  }
}
