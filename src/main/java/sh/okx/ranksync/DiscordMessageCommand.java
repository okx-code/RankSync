package sh.okx.ranksync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;

public class DiscordMessageCommand implements CommandExecutor {
	private RankSync plugin;
	
	public DiscordMessageCommand(RankSync plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 3) {
			return false;
		}
		
		Guild guild;
		try {
			guild = plugin.getJDA().getGuildsByName(args[0].replaceAll("_", " "), true).get(0);
		} catch (Exception e) {
			sender.sendMessage("Cannot access guild: " + args[0]);
			return true;
		}
		
		TextChannel channel;
		try {
			channel = guild.getTextChannelsByName(args[1], true).get(0);
		} catch (Exception e) {
			sender.sendMessage("Cannot access text channel: " + args[1]);
			return true;
		}
		
		final StringBuffer sb = new StringBuffer();
		for(int i = 1; i < args.length; i++) {
			sb.append(args[i]);
			if (i != args.length - 1) {
				sb.append(" ");
			}
		}
		
		final String buffer = ChatColor.translateAlternateColorCodes('&', sb.toString());
		channel.sendMessage(buffer).queue();
		sender.sendMessage("Message sent to " + channel.getGuild().getName() + "." + channel.getName());
		return true;
	}

}
