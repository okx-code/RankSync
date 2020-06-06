package sh.okx.ranksync;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import net.md_5.bungee.api.ChatColor;

public class DiscordMessageCommand extends Command {
	private RankSync plugin;
	
	public DiscordMessageCommand(RankSync plugin) {
		super("discordmsg", "ranksync.msg");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length < 3) {
			sender.sendMessage("/discordmsg [server_name] [channel_name] [message]");
			return;
		}

		Guild guild;
		try {
			guild = plugin.getJDA().getGuildsByName(args[0].replaceAll("_", " "), true).get(0);
		} catch (Exception e) {
			sender.sendMessage("Cannot access guild: " + args[0]);
			return;
		}

		TextChannel channel;
		try {
			channel = guild.getTextChannelsByName(args[1], true).get(0);
		} catch (Exception e) {
			sender.sendMessage("Cannot access text channel: " + args[1]);
			return;
		}

		final String buffer = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
		channel.sendMessage(buffer).queue();
		sender.sendMessage("Message sent to " + channel.getGuild().getName() + "." + channel.getName());
		return;
	}
}
