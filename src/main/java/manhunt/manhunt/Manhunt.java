package manhunt.manhunt;

import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.HashSet;
import org.bukkit.plugin.Plugin;
import java.util.UUID;
import java.util.Set;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Manhunt extends JavaPlugin implements Listener, CommandExecutor
{
    private Set<UUID> hunters;

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        for (final String command : this.getDescription().getCommands().keySet()) {
            this.getServer().getPluginCommand(command).setExecutor((CommandExecutor)this);
        }
        this.hunters = new HashSet<UUID>();
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (command.getName().equalsIgnoreCase("hunter")) {
            if (args.length != 2) {
                this.sendInvalid(sender);
                return false;
            }
            final Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "플레이어를 찾지 못했습니다.");
                return false;
            }
            if (args[0].equalsIgnoreCase("add")) {
                this.hunters.add(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + player.getName() + "님은 이제 헌터입니다.");
                player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.COMPASS) });
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                this.hunters.remove(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + player.getName() + "님을 헌터에서 제거하였습니다.");
                player.getInventory().remove(new ItemStack(Material.COMPASS));
            }
            else {
                this.sendInvalid(sender);
            }
        }
        return false;
    }

    private void sendInvalid(final CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "명령어 사용법:");
        sender.sendMessage(ChatColor.RED + "/hunter add <이름>");
        sender.sendMessage(ChatColor.RED + "/hunter remove <이름>");
    }

    @EventHandler
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (this.hunters.contains(player.getUniqueId()) && event.hasItem() && event.getItem().getType() == Material.COMPASS && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            Player nearest = null;
            double distance = Double.MAX_VALUE;
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(player) && onlinePlayer.getWorld().equals(player.getWorld())) {
                    if (this.hunters.contains(onlinePlayer.getUniqueId())) {
                        continue;
                    }
                    final double distanceSquared = onlinePlayer.getLocation().distanceSquared(player.getLocation());
                    if (distanceSquared >= distance) {
                        continue;
                    }
                    distance = distanceSquared;
                    nearest = onlinePlayer;
                }
            }
            if (nearest == null) {
                player.sendMessage(ChatColor.RED + "추적할 플레이어가 없습니다!");
                return;
            }
            player.setCompassTarget(nearest.getLocation());
            player.sendMessage(ChatColor.GREEN + "나침반이 " + nearest.getName() + "을(를) 가리킵니다.");
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(final PlayerDeathEvent event) {
        if (this.hunters.contains(event.getEntity().getUniqueId())) {
            event.getDrops().removeIf(next -> next.getType() == Material.COMPASS);
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(final PlayerDropItemEvent event) {
        if (this.hunters.contains(event.getPlayer().getUniqueId()) && event.getItemDrop().getItemStack().getType() == Material.COMPASS) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        if (this.hunters.contains(player.getUniqueId())) {
            player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.COMPASS) });
        }
    }
}
