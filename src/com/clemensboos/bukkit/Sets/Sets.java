package com.clemensboos.bukkit.Sets;

import java.io.*;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.General.General;

public class Sets extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");

    public String Name;
    public String Version;

    private Hashtable<String, Hashtable<Byte, ItemStack>> sets = new Hashtable<String, Hashtable<Byte, ItemStack>>();
    private String startSet = "";

    public Sets(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
    {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    public void onEnable()
    {
        PluginDescriptionFile pdFile = this.getDescription();
        Name = pdFile.getName();
        Version = pdFile.getVersion();

        log.info("[" + Name + "] version [" + Version + "] loaded");

        setupCommands();

        loadSets();
    }

    public void onDisable()
    {
        log.info("[" + Name + "] version " + Version + " disabled");
    }

    private void loadSets()
    {
        File setFile = new File(getDataFolder(), "sets.txt");

        try
        {
            FileInputStream fis = new FileInputStream(setFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            InputStreamReader isr = new InputStreamReader(bis);
            BufferedReader br = new BufferedReader(isr);

            String setName = null;
            Hashtable<Byte, ItemStack> currentSet = null;
            while (br.ready())
            {
                String line = br.readLine();
                if (line.charAt(0) == '#')
                    continue;
                if (line.charAt(0) == '=')
                    startSet = line.substring(1);
                else if (line.charAt(0) == '@')
                {
                    if (currentSet != null)
                        sets.put(setName, currentSet);
                    setName = line.substring(1);
                    currentSet = new Hashtable<Byte, ItemStack>();
                }
                else
                {
                    if (currentSet != null)
                    {
                        String[] parts = line.split(":");
                        if (parts.length < 3 || parts.length > 4)
                            continue;
                        Byte slot = Byte.valueOf(parts[0]);
                        Integer itemId = Integer.valueOf(parts[1]);
                        Integer count = Integer.valueOf(parts[2]);
                        Byte data = 0;
                        if (parts.length >= 4)
                            data = Byte.valueOf(parts[3]);
                        ItemStack item = new ItemStack(itemId, count, data);
                        currentSet.put(slot, item);
                    }
                }
            }
            if (currentSet != null)
                sets.put(setName, currentSet);

            fis.close();
            bis.close();
            isr.close();
            br.close();

            log.info("[" + Name + "] successfully loaded");
        }
        catch (FileNotFoundException e)
        {
            log.info("[" + Name + "] Could not load sets: Config file not found.");
        }
        catch (IOException e)
        {
            log.info("[" + Name + "] Could not load sets: Couldn't load config file.");
            e.printStackTrace();
        }
    }

    public void giveStartItems(Player player)
    {
        if (!startSet.equals("") && sets.containsKey(startSet))
            giveSet(player, startSet);
        else if (startSet.equals(""))
            player.sendMessage(ChatColor.RED + "No start set defined");
        else
            player.sendMessage(ChatColor.RED + "Invalid start set");
    }

    public boolean giveSet(Player player, String setName)
    {
        log.info("[" + Name + "] giving " + player.getName() + " set " + setName);
        if (!sets.containsKey(setName))
            return false;
        Hashtable<Byte, ItemStack> items = sets.get(setName);
        player.getInventory().clear();
        for (Byte i = 0; i < Byte.MAX_VALUE; i++)
        {
            if (items.containsKey(i))
            {
                ItemStack item = items.get(i);
                switch (i)
                {
                    case 100:
                        player.getInventory().setBoots(item);
                        break;
                    case 101:
                        player.getInventory().setLeggings(item);
                        break;
                    case 102:
                        player.getInventory().setChestplate(item);
                        break;
                    case 103:
                        player.getInventory().setHelmet(item);
                        break;
                    default:
                        if (i >= 0 && i <= 35)
                            player.getInventory().setItem(i, item);
                        break;
                }
            }
        }
        return true;
    }

    private void setupCommands()
    {
        Plugin pgeneral = this.getServer().getPluginManager().getPlugin("General");

        if (pgeneral != null)
        {
            General general = (General)pgeneral;
            general.l.register_command("/startitems", "Gives you the start items");
            general.l.register_command("/clearinventory", "Clears your inventory");
            general.l.register_command("/set <SetName>", "Gives you the set with the name <SetName>");
        }
    }

    @Override
    public boolean onCommand(Player player, Command command, String commandLabel, String[] args)
    {
        String commandName = command.getName();

        if (commandName.equalsIgnoreCase("set"))
        {
            if (args.length == 1)
            {
                String set = args[0];
                if (!giveSet(player, set))
                {
                    player.sendMessage(ChatColor.RED + "Set \"" + set + "\" doesn't exist");
                }
                return true;
            }
        }

        if (commandName.equalsIgnoreCase("startitems"))
        {
            giveStartItems(player);
            return true;
        }

        if (commandName.equalsIgnoreCase("clearinventory"))
        {
            player.getInventory().clear();
            return true;
        }

        return false;
    }
}
