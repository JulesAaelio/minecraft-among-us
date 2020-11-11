package com.minecraft_among_us.plugin.tasks;

import com.minecraft_among_us.plugin.AmongUsPlayer;
import com.minecraft_among_us.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public abstract class Task {

    protected final String name;
    protected final String description;
    protected final TaskType type;
    protected AmongUsPlayer auPlayer;
    protected boolean finished;

    public Task(String name, String description, TaskType type, AmongUsPlayer auPlayer) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.finished = false;
        this.auPlayer = auPlayer;
    }

    public abstract void execute();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void finish() {
        this.finished = true;
        Player player = (Player) auPlayer.toBukkitPlayer();
        player.sendTitle("§aTask completed", null, 5, 30, 5);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, SoundCategory.AMBIENT, 1.0F, 0.6F);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(), () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, SoundCategory.AMBIENT, 1.0F, 0.8F), 3L);
    }

    public TaskType getType() {
        return type;
    }

    public boolean isFinished() {
        return finished;
    }
}
