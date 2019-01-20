package sh.okx.ranksync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class DiscordMessageCommand implements CommandExecutor {
	private RankSync plugin;
	private Guild guild;
	
	public DiscordMessageCommand(RankSync plugin) {
		this.plugin = plugin;
		guild = plugin.getGuild();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 2) {
			return false;
		}
		
		TextChannel channel;
		try {
			channel = guild.getTextChannelsByName(args[0], true).get(0);
		} catch (Exception e) {
			sender.sendMessage("Cannot access text channel");
			return true;
		}
		
		final StringBuffer sb = new StringBuffer();
		for(int i = 1; i < args.length; i++) {
			sb.append(args);
			if (i != args.length - 1) {
				sb.append(" ");
			}
		}
		
		channel.sendMessage(sb.toString()).queue();
		
		return true;
	}

}
