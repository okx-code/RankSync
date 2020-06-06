package sh.okx.ranksync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import sh.okx.ranksync.database.MySQLHandler;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RankSync extends Plugin {

  private final Map<String, Code> codes = new MaxSizeHashMap<>(255);
  private JDA jda;
  private StatusService status;
  private MySQLHandler db;
  private Configuration config;


  public String addCode(UUID uuid) {
    for (Map.Entry<String, Code> code : codes.entrySet()) {
      Code c = code.getValue();
      if (c.getUniqueId().equals(uuid)) {
        codes.remove(code.getKey());
      }
    }

    String key = randomCode();
    codes.put(key, new Code(uuid, System.currentTimeMillis()));
    return key;
  }

  public Code getCode(String code) {
    return codes.remove(code);
  }

  private String randomCode() {
    String code = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    if (codes.containsKey(code)) {
      return randomCode();
    } else {
      return code;
    }
  }

  @Override
  public void onEnable() {
    saveDefaultConfig();
    config = getConfig();

    try {
      jda = JDABuilder.createDefault(config.getString("bot-token"))
          .addEventListeners(new MessageListener(this, config))
          .build();
    } catch (LoginException e) {
      throw new RuntimeException(e);
    }

    try {
      db = new MySQLHandler(config);
    } catch (Exception e) {
      e.printStackTrace();
    }

    getProxy().getPluginManager().registerCommand(this, new VerifyCommand(this));
    getProxy().getPluginManager().registerCommand(this, new DiscordMessageCommand(this));
    status = new StatusService(jda, config.getSection("presence"));
    status.startAsync();
  }

  private void saveDefaultConfig() {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
    }

    File file = new File(getDataFolder(), "config.yml");

    if (!file.exists()) {
      try (InputStream in = getResourceAsStream("config.yml")) {
        Files.copy(in, file.toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private Configuration getConfig() {
    try {
      return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onDisable() {
    if (status != null) {
      status.stopAsync().awaitTerminated();
    }
    jda.shutdown();
  }

  public Set<Role> getRoles(UUID player) {
    Map<String, Role> roleMap = new HashMap<>();
    for (String entry : config.getStringList("ranks")) {
      String[] parts = entry.split(":");
      roleMap.put(entry, getGuild().getRolesByName(parts[1], true).get(0));
    }

    Set<Role> roles = new HashSet<>();
    LuckPerms luckPerms = LuckPermsProvider.get();
    Set<String> groups = luckPerms.getUserManager().getUser(player).getNodes().stream()
        .filter(NodeType.INHERITANCE::matches)
        .map(NodeType.INHERITANCE::cast)
        .map(InheritanceNode::getGroupName)
        .collect(Collectors.toSet());
    for (String rank : groups) {
      for (String entry : config.getStringList("ranks")) {
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
    return jda.getGuildsByName(config.getString("guild-name"), true).get(0);
  }

  public JDA getJDA() {
    return jda;
  }

  public MySQLHandler getDB() {
    return db;
  }

  public String getDiscordVerifyCommand() {
    return config.getString("discord-verify-command");
  }
}
