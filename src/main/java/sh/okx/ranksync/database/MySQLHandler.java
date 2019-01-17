package sh.okx.ranksync.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javafx.util.Pair;

import java.sql.*;
import java.util.UUID;

public class MySQLHandler {

	private final Plugin plugin;
	public MySQLHandler(Plugin plugin) {
		this.plugin = plugin;
		connect();
		createDatabase();
	}
	
	private static HikariDataSource hikari;
	
    public void connect() {
        FileConfiguration cfg = plugin.getConfig();
        connect(cfg);
    }

    private void connect(final FileConfiguration cfg){
        String host = cfg.getString("MySQL.host");
        Integer port = cfg.getInt("MySQL.port");
        String user = cfg.getString("MySQL.user");
        String database = cfg.getString("MySQL.database");
        String password = cfg.getString("MySQL.password");

        HikariConfig config1 = new HikariConfig();
		config1.setDriverClassName("com.mysql.jdbc.Driver");
		config1.setMinimumIdle(3);
		config1.setMaximumPoolSize(5);
		config1.setConnectionTimeout(5000);
		config1.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +"?autoReconnect=true&autoReconnectForPools=true&interactiveClient=true&characterEncoding=UTF-8");
		config1.setUsername(user);
		config1.setPassword(password);
		hikari = new HikariDataSource(config1);
		
		Bukkit.getConsoleSender().sendMessage("Discord MySQL connection success");
    }

    private boolean isConnected(){
        try {
			return hikari != null && hikari.getConnection() != null;
		} catch (SQLException e) {
			return false;
		}
    }

    public void disconnect(){
        if(!isConnected()){
            hikari.close();
        }
    }

    private void createDatabase()
    {
        try
        {
            Connection connection = hikari.getConnection();
            	
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ultimatediscord( `id` BIGINT NOT NULL AUTO_INCREMENT , `uuid` varchar(36) NOT NULL , `discordid` TEXT NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
            ps.execute();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public boolean userExists(UUID uuid)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            boolean ret = rs.next();
            connection.close();
            return ret;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean userExists(String id)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE discordid =?");
            if (!Bukkit.getServer().getOnlineMode()) {
                ps.setString(1, id);
            }
            ResultSet rs = ps.executeQuery();
            boolean ret = rs.next();
            connection.close();
            return ret;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean createUser(UUID uuid, String identity)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            
            if (userExists(uuid)) {
            	return false;
            }
            
            if (userExists(identity)) {
            	return false;
            }
            
            PreparedStatement ps = connection.prepareStatement("INSERT INTO ultimatediscord(`uuid`,`discordid`) VALUES (?, ?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, identity);
            ps.execute();
            connection.close();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }



    public Pair<UUID, String> getValue(UUID uuid)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	String ret = rs.getString("uuid");
            	String ret2 = rs.getString("discordid");
            	connection.close();
                return new Pair<UUID, String>(UUID.fromString(ret), ret2);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Pair<UUID, String> getValue(String id)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE discordid = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	String ret = rs.getString("uuid");
            	String ret2 = rs.getString("discordid");
            	connection.close();
                return new Pair<UUID, String>(UUID.fromString(ret), ret2);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public void deleteUser(UUID uuid)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ultimatediscord WHERE uuid=?");
            ps.setString(1, uuid.toString());
            ps.execute();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void deleteUser(String id)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ultimatediscord WHERE discordid=?");
            ps.setString(1,id);
            ps.execute();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
