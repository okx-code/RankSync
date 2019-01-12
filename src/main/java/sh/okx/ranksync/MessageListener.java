package sh.okx.ranksync;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {
  private final RankSync plugin;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    User author = event.getAuthor();
    if (event.isWebhookMessage() || author.isBot()) {
      return;
    }

    MessageChannel channel = event.getChannel();
    String message = event.getMessage().getContentRaw();
    String[] parts = message.split(" ", 2);
    String command = plugin.getConfig().getString("discord-verify-command");
    if (!parts[0].equalsIgnoreCase(command)) {
      return;
    } else if (parts.length < 2) {
      channel.sendMessage("**Usage:** " + command + " <code>").queue();
      return;
    } else if (!(channel instanceof PrivateChannel)) {
      channel.sendMessage("You can only use this in a private channel!").queue();
      return;
    } else if (!plugin.getGuild().isMember(author)) {
      channel.sendMessage("You must be in the guild to use this!").queue();
      return;
    }
    UUID uuid = plugin.getCode(parts[1]);
    if (uuid == null) {
      channel.sendMessage("Invalid code! Type **/verify** in-game to get one.").queue();
      return;
    }

    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

    GuildController controller = plugin.getGuild().getController();
    StringBuilder roles = new StringBuilder();
    for (Role role : plugin.getRoles(player)) {
      roles.append("\n").append(role.getName());

      controller.addSingleRoleToMember(plugin.getGuild().getMember(author), role).queue();
    }

    if (roles.length() == 0) {
      channel.sendMessage("You were not eligible for any roles.").queue();
    } else {
      channel.sendMessage("You have received the following role(s):" + roles).queue();
    }
  }
}
