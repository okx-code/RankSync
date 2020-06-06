package sh.okx.ranksync;

import com.google.common.util.concurrent.AbstractScheduledService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;

import java.util.concurrent.TimeUnit;

public class StatusService extends AbstractScheduledService {
  private final JDA jda;
  private final int activity;
  private final String format;

  public StatusService(JDA jda, Configuration section) {
    this.jda = jda;
    this.activity = section.getInt("type");
    this.format = section.getString("format");
  }

  @Override
  protected void runOneIteration() {
    int playerLimit = ProxyServer.getInstance().getConfig().getPlayerLimit();
    int players = ProxyServer.getInstance().getPlayers().size();
    jda.getPresence().setActivity(Activity.of(ActivityType.fromKey(activity), format
        .replace("%current%", String.valueOf(players))
        .replace("%max%", String.valueOf(playerLimit))));
  }

  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedDelaySchedule(20, 20, TimeUnit.SECONDS);
  }
}
