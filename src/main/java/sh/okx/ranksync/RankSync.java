package sh.okx.ranksync;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.milkbowl.vault.permission.Permission;
import sh.okx.ranksync.database.MySQLHandler;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RankSync extends JavaPlugin {
  private final Cache<String, UUID> codes = CacheBuilder.newBuilder().maximumSize(100).build();
  private Permission permission;
  private JDA jda;
  private ChatListener chatListener;
  private StatusService status;
  private MySQLHandler db;
  
  
  public String addCode(UUID uuid) {
    for (Map.Entry<String, UUID> code : codes.asMap().entrySet()) {
      if (code.getValue().equals(uuid)) {
        codes.asMap().remove(code.getKey());
      }
    }

    String key = randomCode();
    codes.put(key, uuid);
    return key;
  }

  public UUID getCode(String code) {
    return codes.asMap().remove(code);
  }

  private String randomCode() {
    String code = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    if (codes.asMap().containsKey(code)) {
      return randomCode();
    } else {
      return code;
    }
  }

  @Override
  public void onEnable() {
    saveDefaultConfig();
    getServer().getPluginManager().registerEvents(chatListener = new ChatListener(this), this);
    try {
      jda = new JDABuilder(getConfig().getString("bot-token"))
          .addEventListener(new MessageListener(this))
          .addEventListener(chatListener)
          .build();
    } catch (LoginException e) {
      throw new RuntimeException(e);
    }
    
    try {
    	db = new MySQLHandler(this);
    } catch (Exception e) {
    	e.printStackTrace();
    }

    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    permission = rsp.getProvider();

    getCommand("verify").setExecutor(new VerifyCommand(this));
    status = new StatusService(jda, getConfig().getConfigurationSection("presence"));
    status.startAsync();
  }

  @Override
  public void onDisable() {
    chatListener.stop();
    status.stopAsync().awaitTerminated();
  }

  public Set<Role> getRoles(OfflinePlayer player) {
    Map<String, Role> roleMap = new HashMap<>();
    for (String entry : getConfig().getStringList("ranks")) {
      String[] parts = entry.split(":");
      roleMap.put(entry, getGuild().getRolesByName(parts[1], true).get(0));
    }

    Set<Role> roles = new HashSet<>();
    for (String rank : permission.getPlayerGroups(null, player)) {
      for (String entry : getConfig().getStringList("ranks")) {
        String[] parts = entry.split(":");
        if (parts[0].equalsIgnoreCase(rank)) {
          roles.add(getGuild().getRolesByName(parts[1], true).get(0));
          break;
        }
      }
    }
    return roles;
  }

  public Guild getGuild() {
    return jda.getGuildsByName(getConfig().getString("guild-name"), true).get(0);
  }
  
  public MySQLHandler getDB() {
	  return db;
  }
}
