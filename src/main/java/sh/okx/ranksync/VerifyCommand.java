package sh.okx.ranksync;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class VerifyCommand extends Command {

  private final RankSync plugin;

  public VerifyCommand(RankSync plugin) {
    super("verify");
    this.plugin = plugin;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (!(sender instanceof ProxiedPlayer)) {
      TextComponent message = new TextComponent("You must be a player to run this command.");
      message.setColor(ChatColor.RED);
      sender.sendMessage(message);
      return;
    }

    ProxiedPlayer player = (ProxiedPlayer) sender;
    String command = plugin.getDiscordVerifyCommand() + " " + plugin.addCode(player.getUniqueId());
    sender.sendMessage(new ComponentBuilder()
        .event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to show in chat so you can copy!").create()))
        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
        .append(new ComponentBuilder("Type ").color(ChatColor.GREEN).create())
        .append(new ComponentBuilder(command).color(ChatColor.GOLD).create())
        .append(new ComponentBuilder(" on Discord as a private message to the Maestrea bot.").color(ChatColor.GREEN).create())
        .create());
  }
}
