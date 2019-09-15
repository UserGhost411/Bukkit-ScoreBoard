package cf.userghost411.scoreboard;

import org.bukkit.Bukkit;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import java.io.File;
import org.bukkit.ChatColor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
//============== Vault API =========================
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
//==================================================
/**
 *
 * @author UserGhost411
 */
public class Main extends JavaPlugin implements Listener{
    Objective objective;
    Scoreboard board;
    File configFile;
    FileConfiguration config;
    Boolean Vault = false;
    private static Economy econ = null;
    //String Prefix="§bInfinty§fI§4D §e§l/§r ";
    
    @Override
    public void onDisable() {
        /*
         // for auto save config
         consolelog("Saving Config!");
         try {config.save(configFile);} catch (IOException e) {e.printStackTrace();}
        */
        consolelog("InfinityID Official Plugin Deactivated!");
    }
    @Override
    public void onEnable() {
        configFile = new File(getDataFolder(), "config.yml");
        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), configFile);
        }
        config = new YamlConfiguration();
        try {
           config.load(configFile);
           Vault = config.getBoolean("Vault");
           loadapi();
        } catch (Exception e) {
            e.printStackTrace();
        }
        PluginDescriptionFile pdfFile = this.getDescription();
        consolelog( pdfFile.getName() + " version " + pdfFile.getVersion() + " Activated!" );
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    public static Economy getEconomy() {
        return econ;
    }
    private void loadapi(){
        if(Vault){
            if (!setupEconomy()) {
            consolelog("Vault Error");
            Vault = false;
            }
        }
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {return false;}
        consolelog("Registering Vault");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) {return false;}
        econ = rsp.getProvider();
        return econ != null;
    }
    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
            out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        if(config.getBoolean("ScoreBoard.Enable")){
        //consolelog("Serving ScoreBoard");
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                scoreboard(e.getPlayer());
            }
        }, 0, 30);
    }
    }
    public void scoreboard(Player myplayer) {
        if(!config.getBoolean("ScoreBoard.Enable")){
        return;
        }
        Player p = Bukkit.getServer().getPlayer(UUID.fromString(myplayer.getUniqueId().toString()));
        if(p != null){
        String ping = "0ms";
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        String servername = config.getString("ScoreBoard.Title").replaceAll("&", "§");
        objective = board.registerNewObjective("InfinityID", "InfinityID");
        objective.setDisplayName(servername);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        int a = Bukkit.getOnlinePlayers().size();
        int b = Bukkit.getMaxPlayers();
            try {
                Object entityPlayer = myplayer.getClass().getMethod("getHandle").invoke(myplayer);
                ping =  entityPlayer.getClass().getField("ping").get(entityPlayer).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        String money = "0";
        if(Vault){
            try {
                OfflinePlayer player1 = myplayer;
                Double playerBalance = (econ.getBalance(player1));
                DecimalFormat df2 = new DecimalFormat( "#,###,###,###");
                money = df2.format(playerBalance).toString();
                String groupingSeparator = String.valueOf(df2.getDecimalFormatSymbols().getGroupingSeparator());
                money = money.replace(groupingSeparator, ".");
            }
            catch(Exception e) {
                money = "" + e.getMessage();
            }}else{
                money = "Disabled";       
            }
    String Rank = "Default";
    if(myplayer.hasPermission("infinityid.default")){
        Rank = "Default";}
    if(myplayer.hasPermission("infinityid.vip")){
        Rank = ChatColor.GREEN + "VIP";}
    if(myplayer.hasPermission("infinityid.vipp")){
        Rank = ChatColor.GREEN + "VIP" + ChatColor.RED + "+";}
    if(myplayer.hasPermission("infinityid.mvp")){
        Rank = ChatColor.AQUA + "MVP";}
    if(myplayer.hasPermission("infinityid.staff")){
        Rank = ChatColor.LIGHT_PURPLE + "STAFF";}
    if(myplayer.hasPermission("infinityid.admin")){
        Rank = ChatColor.LIGHT_PURPLE + "Admin";}
    if(myplayer.isOp()){
        Rank = ChatColor.DARK_AQUA + "Operator";}
    
        List<String> list = config.getStringList("ScoreBoard.Data");
        for (int i = 0; i < list.size(); i++) {
        String logx = list.get(i).toString().replaceAll("&", "§").replaceAll("%player%", myplayer.getDisplayName().toString())//.replaceAll("[name]", myplayer.getDisplayName().toString())
        .replaceAll("%rank%", Rank).replaceAll("%ping%", ping).replaceAll("%players%", a+"").replaceAll("%money%", money).replaceAll("%maxplayer%", b+"");   
        Score score1 = objective.getScore(logx);    
        score1.setScore(list.size()-i);
        }
        try{
        myplayer.getPlayer().setScoreboard(board);
        }catch (Exception e) {
            //getLogger().info("Error ScoreBoard:"+e.getMessage().toString());
        }
        }else{return;}
    }
    private void consolelog(String strx){
    ConsoleCommandSender console = this.getServer().getConsoleSender();
    console.sendMessage("[§bUG-ScoreBoard§r] §f"+strx);
    }
}
