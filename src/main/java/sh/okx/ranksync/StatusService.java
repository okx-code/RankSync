package sh.okx.ranksync;

import com.google.common.util.concurrent.AbstractScheduledService;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.TimeUnit;

public class StatusService extends AbstractScheduledService {
  private final JDA jda;
  private final Game.GameType type;
  private final String format;

  public StatusService(JDA jda, ConfigurationSection section) {
    this.jda = jda;
    this.type = Game.GameType.valueOf(section.getString("type").toUpperCase());
    this.format = section.getString("format");
  }

  @Override
  protected void runOneIteration() {
    jda.getPresence().setGame(Game.of(type, format
        .replace("%current%", String.valueOf(Bukkit.getOnlinePlayers().size()))
        .replace("%max%", String.valueOf(Bukkit.getMaxPlayers()))));
  }

  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedDelaySchedule(20, 20, TimeUnit.SECONDS);
  }
}
