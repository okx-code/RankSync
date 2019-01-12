package sh.okx.ranksync;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public class ChatService extends AbstractScheduledService {
  private final TextChannel channel;
  private final String format;
  private final int time;
  private final Queue<AsyncPlayerChatEvent> events = new ConcurrentLinkedQueue<>();

  public void queue(AsyncPlayerChatEvent event) {
    events.add(event);
  }

  @Override
  protected void runOneIteration() {
    if (channel.getJDA().getStatus() != JDA.Status.CONNECTED) {
      return;
    }

    AsyncPlayerChatEvent event;
    StringBuilder messages = new StringBuilder();
    while ((event = events.poll()) != null) {
      String message = format
          .replace("%user%", escape(event.getPlayer().getName()))
          .replace("%message%", escape(event.getMessage()));
      if (message.length() + messages.length() >= 2000) {
        break;
      } else if (messages.length() > 0) {
        messages.append("\n");
      }
      messages.append(message);
    }
    if (messages.length() > 0) {
      channel.sendMessage(ChatColor.stripColor(messages.toString())).complete();
    }
  }

  private String escape(String string) {
    return string.replace("`", "\\`")
        .replace("_", "\\_")
        .replace("*", "\\*");
  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(time, time, TimeUnit.MILLISECONDS);
  }
}
