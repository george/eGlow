package me.mrgraycat.eglow.addon;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PlayerGetUpPoseEvent;
import dev.geco.gsit.api.event.PlayerPoseEvent;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.api.event.GlowColorChangeEvent;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class GSitAddon implements Listener {

    static ArrayList<Player> activePlayers = new ArrayList<>();

    public GSitAddon() {
        EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
    }

    @EventHandler
    public void poseEvent(PlayerPoseEvent event) {
        Player player = event.getPlayer();
        checkGlow(player, true);
    }

    @EventHandler
    public void unPoseEvent(PlayerGetUpPoseEvent event) {
        Player player = event.getPlayer();
        checkGlow(player, false);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        checkGlow(event.getPlayer(), false);
    }

    @EventHandler
    public void onGlowChange(GlowColorChangeEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (GSitAPI.isPosing(player)) {
                    EGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);
                    ePlayer.disableGlow(false);
                }
            }
        }.runTaskLater(EGlow.getInstance(), 2L);
    }

    private void checkGlow(Player player, boolean posing) {
        EGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);

        if (posing) {
            if (ePlayer.getGlowStatus() || ePlayer.getFakeGlowStatus()) {
                if (!activePlayers.contains(player)) activePlayers.add(player);
                ePlayer.disableGlow(false);
            }
        } else {
            if (activePlayers.contains(player)) {
                activePlayers.remove(player);
                ePlayer.activateGlow();
            }
        }
    }
}
