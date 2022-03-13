package xyz.n7mn.dev.nanamiplayersystem;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.n7mn.dev.nanamiplayersystem.command.Join;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public final class NanamiPlayerSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        try {

            boolean newLoad = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()){
                Driver driver = drivers.nextElement();

                if (driver.equals(new com.mysql.cj.jdbc.Driver())){
                    newLoad = true;
                    break;
                }
            }

            if (newLoad){
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
        }


        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        getCommand("join").setExecutor(new Join(this));
        getCommand("join").setTabCompleter(new Join(this));

        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {

                Date nowDate = new Date();
                Calendar NowDateCalendar = Calendar.getInstance();
                NowDateCalendar.setTime(nowDate);

                Calendar instance = Calendar.getInstance();
                Calendar instance2 = Calendar.getInstance();
                instance.setTime(nowDate);
                instance2.setTime(nowDate);

                List<String> list1 = getConfig().getStringList("ServerOpenTime1");
                List<String> list2 = getConfig().getStringList("ServerOpenTime2");
                String s = list1.get(instance.get(Calendar.DAY_OF_WEEK) - 1);
                String s2 = list2.get(instance.get(Calendar.DAY_OF_WEEK) - 1);

                instance.set(Calendar.HOUR, Integer.parseInt(s.split(":")[0]));
                instance.set(Calendar.MINUTE, Integer.parseInt(s.split(":")[1]));
                instance.set(Calendar.SECOND, 0);

                if (s2.length() > 0){
                    instance2.set(Calendar.HOUR, Integer.parseInt(s2.split(":")[0]));
                    instance2.set(Calendar.MINUTE, Integer.parseInt(s2.split(":")[1]));
                    instance2.set(Calendar.SECOND, 0);
                } else {
                    s2 = "23:59";
                    instance2.set(Calendar.HOUR, Integer.parseInt(s2.split(":")[0]));
                    instance2.set(Calendar.MINUTE, Integer.parseInt(s2.split(":")[1]));
                    instance2.set(Calendar.SECOND, 0);
                }

                if (NowDateCalendar.get(Calendar.HOUR) > instance2.get(Calendar.HOUR) || (NowDateCalendar.get(Calendar.HOUR) >= instance2.get(Calendar.HOUR) && NowDateCalendar.get(Calendar.MINUTE) > instance2.get(Calendar.MINUTE))){
                    s = list1.get(instance.get(Calendar.DAY_OF_WEEK));
                    s2 = list2.get(instance.get(Calendar.DAY_OF_WEEK));

                    instance.set(Calendar.DAY_OF_MONTH, NowDateCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                    instance.set(Calendar.HOUR, Integer.parseInt(s.split(":")[0]));
                    instance.set(Calendar.MINUTE, Integer.parseInt(s.split(":")[1]));

                    if (s2.length() == 0){
                        s2 = "23:59";
                    }

                    instance2.set(Calendar.DAY_OF_MONTH, NowDateCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                    instance2.set(Calendar.HOUR, Integer.parseInt(s2.split(":")[0]));
                    instance2.set(Calendar.MINUTE, Integer.parseInt(s2.split(":")[1]));

                }

                //System.out.println(new SimpleDateFormat("HH:mm").format(nowDate) + " / " + s + " / " + s2);

                if (nowDate.getTime() >= instance.getTime().getTime()){
                    //System.out.println("p1-1");
                    if (NowDateCalendar.get(Calendar.HOUR) == instance.get(Calendar.HOUR) && NowDateCalendar.get(Calendar.MINUTE) == instance.get(Calendar.MINUTE)){
                        //System.out.println("p1-2");
                        getConfig().set("JoinCheck", false);

                        String opMessage = ChatColor.translateAlternateColorCodes('&',getConfig().getString("Prefix")) + ChatColor.RESET+" 指定時間("+s+")になったため 入室権限チェックを自動解除しました。";
                        getServer().getLogger().info(opMessage);
                        this.cancel();
                    }
                } else {
                    if (nowDate.getTime() <= instance2.getTime().getTime()){
                        //System.out.println("p2-1");
                        if (NowDateCalendar.get(Calendar.HOUR) == instance2.get(Calendar.HOUR) && NowDateCalendar.get(Calendar.MINUTE) == instance2.get(Calendar.MINUTE)){
                            //System.out.println("p2-2");
                            getConfig().set("JoinCheck", false);

                            String opMessage = ChatColor.translateAlternateColorCodes('&',getConfig().getString("Prefix")) + ChatColor.RESET+" 指定時間 ("+s2+")になったため 入室権限チェックを自動解除しました。";
                            getServer().getLogger().info(opMessage);
                            this.cancel();
                        }
                    }
                }
            }
        };

        if (getConfig().getBoolean("ServerAutoOpen")){
            bukkitRunnable.runTaskTimerAsynchronously(this, 0L, 20L);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

}
