package com.minecraft_among_us.plugin.tasks;

import com.minecraft_among_us.plugin.AmongUsPlayer;
import com.minecraft_among_us.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimonTask extends Task {

    private final static String TASK_NAME = "Simon";

    private Inventory inventory;
    private List<Integer> path;
    private boolean showingSteps;
    private int timesMaster;
    private int timesPlayer;
    private int currentRound;
    private final int nbRounds = 5;

    public SimonTask(AmongUsPlayer auPlayer) {
        super(TASK_NAME, "Memorize and repeat the Simon", TaskType.LONG, auPlayer);
        this.inventory = this.createInventory();
        this.path = createPath();
        this.showingSteps = false;
        this.timesMaster = 0;
        this.timesPlayer = 0;
        this.currentRound = 0;
    }

    @Override
    public void execute() {
        Player player = (Player) auPlayer.toBukkitPlayer();
        player.openInventory(this.inventory);
        this.markAllRoundAsPending();
        this.markSuccessfulRoundsAsSuccessful();
        this.showSteps(this.currentRound);
    }

    private Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.DROPPER, this.name);

        for (int i = 0; i < 9; i++) {
            this.setUntouchedItem(inventory, i);
        }

        return inventory;
    }

    private ArrayList<Integer> createPath() {
        Random random = new Random();
        ArrayList<Integer> path = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int slot = random.nextInt(9);
            path.add(slot);
        }
        return path;
    }


    private void showSteps(int round) {
        this.timesMaster = 0;
        this.timesPlayer = 0;
        this.showingSteps = true;
        // After waiting 20 ticks, show a step every 10 ticks.
        Bukkit.getScheduler().runTaskTimer(Plugin.getPlugin(), (task) -> {
            this.inventory.setItem(this.path.get(this.timesMaster), new ItemStack(Material.LIGHT_BLUE_CONCRETE));

            // 5 ticks later, change it back.
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(), () -> {
                this.setUntouchedItem(this.inventory, this.path.get(this.timesMaster));
                this.timesMaster++;
                if (this.timesMaster > round) {
                    this.showingSteps = false;
                    task.cancel();
                }
            }, 5L);

        }, 20L, 10L);
    }

    private void nextRound() {
        this.currentRound++;
        // Strict compare : nbRounds is a size.
        if(this.currentRound < nbRounds) {
            showSteps(this.currentRound);
        } else {
            this.finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        ((Player) this.auPlayer.toBukkitPlayer()).closeInventory();
        Bukkit.broadcastMessage("J'ai kill le truc bb");
    }

    public void playerPlay(Inventory inventory, int slot) {
        if (!this.showingSteps && inventory.equals(this.inventory)) {
            if (slot == this.path.get(this.timesPlayer)) {
                this.timesPlayer++;
                if(!(this.timesPlayer <= this.currentRound)) {
                    this.markRoundAsSuccessful();
                    this.nextRound();
                }
            } else {
                this.markRoundAsFailed();
                this.showSteps(this.currentRound);
            }
        }
    }

    private void setUntouchedItem(Inventory inventory, int slot) {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_CONCRETE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(Integer.toString(slot + 1));
        item.setItemMeta(itemMeta);
        inventory.setItem(slot, item);
    }



    private void markRoundAsSuccessful() {
       this.markRoundAsSuccessful(this.currentRound);
    }

    private void markRoundAsSuccessful(int round) {
        ((Player) this.auPlayer.toBukkitPlayer()).getInventory().setItem(round, new ItemStack(Material.GREEN_CONCRETE) );
    }

    private void markRoundAsFailed() {
        ((Player) this.auPlayer.toBukkitPlayer()).getInventory().setItem(this.currentRound, new ItemStack(Material.RED_CONCRETE) );
    }


    private void markAllRoundAsPending() {
        for (int i = 0; i < nbRounds; i++) {
            ((Player) this.auPlayer.toBukkitPlayer()).getInventory().setItem(i, new ItemStack(Material.GRAY_CONCRETE) );
        }
    }

    private void markSuccessfulRoundsAsSuccessful() {
        for (int i = 0; i < currentRound; i++) {
            this.markRoundAsSuccessful(i);
        }
    }


    public static class Listener implements org.bukkit.event.Listener {

        @EventHandler
        public void onTaskLaunch(PlayerInteractEvent e) {
            if (e.getHand().equals(EquipmentSlot.HAND)
                    && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                    && e.getClickedBlock().getLocation().equals(new Location(Plugin.getDefaultWorld(), -314, 31, -161))) {
                Player player = e.getPlayer();
                AmongUsPlayer auPlayer = AmongUsPlayer.getPlayer(player.getUniqueId());
                Task task = auPlayer.getTask(TASK_NAME);
                if (task != null && !task.finished) {
                    task.execute();
                }
            }
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (e.getView().getTitle().equals(TASK_NAME)) {
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                AmongUsPlayer auPlayer = AmongUsPlayer.getPlayer(player.getUniqueId());
                ItemStack currentItem = e.getCurrentItem();
                if (currentItem != null) {
                    SimonTask task = (SimonTask) auPlayer.getTask(TASK_NAME);
                    task.playerPlay(e.getClickedInventory(), e.getSlot());
                }
            }
        }
    }
}
