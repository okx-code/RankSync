package sh.okx.ranksync;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import sh.okx.ranksync.database.MySQLHandler;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter implements EventListener {
  private final RankSync plugin;
  private final Configuration config;
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
    String command = config.getString("discord-verify-command");
    pendingVerificationName = config.getString("discord-role-to-be-removed-on-verification");
    Guild guild = plugin.getGuild();
    if (!parts[0].equalsIgnoreCase(command)) {
      return;
    } else if (parts.length < 2) {
      channel.sendMessage("**Usage:** " + command + " <code>").queue();
      return;
    } else if (!(channel instanceof PrivateChannel)) {
      channel.sendMessage("You can only use this in a private channel!").queue();
      return;
    }

    Member member = guild.retrieveMember(author).complete();
    if (member == null) {
      channel.sendMessage("You must be in the guild to use this!").queue();
      return;
    }
    Code code = plugin.getCode(parts[1]);
    UUID uuid = code.getUniqueId();
    if (uuid == null || System.currentTimeMillis() - code.getTimestamp() > 180_000) {
      channel.sendMessage("Invalid code! Type **/verify** in-game to get one.").queue();
      return;
    }

    StringBuilder roles = new StringBuilder();
    
    if (pendingVerificationName != null) {
	    final List<Role> rolesToBeScanned = guild.getRolesByName(pendingVerificationName, true);
	    if (rolesToBeScanned.size() != 0) {
	        guild.removeRoleFromMember(member, rolesToBeScanned.get(0)).queue();
	    } else {
	    	plugin.getLogger().warning("Discord role: " + pendingVerificationName + " to be removed was not found!");
	    }
    }
    
    //Save the association if possible + check for anyone swapping codes
    final MySQLHandler db = plugin.getDB();
    if (db != null) {
    	if (!db.userExists(uuid)) {
    		db.createUser(uuid, author.getId());
    	} else {
    		String knownAssociation = db.getValue(uuid);
    		if (knownAssociation != null && !author.getId().equals(knownAssociation)) {
    			channel.sendMessage("Does not fit prior associations made. Please double check with a Discord Admin.").queue();
    			return;
    		}
    	}
    }
    
    for (Role role : plugin.getRoles(uuid)) {
      roles.append("\n").append(role.getName());

      guild.addRoleToMember(member, role).queue();
    }

    if (roles.length() == 0) {
      channel.sendMessage(config.getString("messages.not-eligible")).queue();
    } else {
      channel.sendMessage("You have received the following role(s):" + roles).queue();
    }
  }
}
