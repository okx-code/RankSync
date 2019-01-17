package sh.okx.ranksync;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import sh.okx.ranksync.database.MySQLHandler;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {
  private final RankSync plugin;
  private String pendingVerificationName;

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
    pendingVerificationName = plugin.getConfig().getString("discord-role-to-be-removed-on-verification");
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
    Member member = plugin.getGuild().getMember(author);
    
    if (pendingVerificationName != null) {
	    final List<Role> rolesToBeScanned = plugin.getGuild().getRolesByName(pendingVerificationName, true);
	    if (rolesToBeScanned.size() != 0) {
	        controller.removeSingleRoleFromMember(member, rolesToBeScanned.get(0));
	    } else {
	    	Bukkit.getLogger().log(Level.WARNING, "Discord role: " + pendingVerificationName + " to be removed was not found!");
	    }
    }
    
    //Save the association if possible
    final MySQLHandler db = plugin.getDB();
    if (db != null) {
    	db.createUser(player.getUniqueId(), author.getId());
    }
    
    for (Role role : plugin.getRoles(player)) {
      roles.append("\n").append(role.getName());

      controller.addSingleRoleToMember(member, role).queue();
    }

    if (roles.length() == 0) {
      channel.sendMessage("You were not eligible for any roles.").queue();
    } else {
      channel.sendMessage("You have received the following role(s):" + roles).queue();
    }
  }
}
