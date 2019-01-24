package sh.okx.ranksync;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@RequiredArgsConstructor
public class ChatListener extends ListenerAdapter implements EventListener, Listener {
  private final RankSync plugin;
  private ChatService service;

  @EventHandler
  public void on(AsyncPlayerChatEvent e) {
    service.queue(e);
  }

  @SuppressWarnings("UnstableApiUsage")
  @Override
  public void onStatusChange(StatusChangeEvent event) {
    if (event.getNewStatus() == JDA.Status.CONNECTED && service == null) {
      FileConfiguration config = plugin.getConfig();
      service = new ChatService(
          plugin.getGuild().getTextChannelsByName(config.getString("log.channel"), true).get(0),
          config.getString("log.format"),
          config.getInt("log.rate"));
      service.startAsync();
    }
  }

  public void stop() {
    service.stopAsync().awaitTerminated();
  }
}
