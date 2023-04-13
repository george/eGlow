package me.mrgraycat.eglow.command.subcommands.admin;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.addon.internal.AdvancedGlowVisibilityAddon;
import me.mrgraycat.eglow.command.SubCommand;
import me.mrgraycat.eglow.config.EGlowCustomEffectsConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.config.playerdata.EGlowPlayerdataManager;
import me.mrgraycat.eglow.util.EnumUtil.GlowDisableReason;
import me.mrgraycat.eglow.util.data.DataManager;
import me.mrgraycat.eglow.util.data.EGlowEffect;
import me.mrgraycat.eglow.util.data.EGlowPlayer;
import me.mrgraycat.eglow.util.text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReloadCommand extends SubCommand {

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getDescription() {
		return "Reload the plugin";
	}

	@Override
	public String getPermission() {
		return "eglow.command.reload";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow reload"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender sender, EGlowPlayer ePlayer, String[] args) {
		if (EGlowMainConfig.reloadConfig() && EGlowMessageConfig.reloadConfig() && EGlowCustomEffectsConfig.reloadConfig()) {
			EGlowPlayerdataManager.setMysql_Failed(false);
			DataManager.addEGlowEffects();
			DataManager.addCustomEffects();

			for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
				ePlayer = DataManager.getEGlowPlayer(onlinePlayer);

				if (ePlayer == null)
					continue;

				ePlayer.updatePlayerTabname();

				EGlowEffect effect = ePlayer.getForceGlow();

				if (effect != null) {
					ePlayer.activateGlow(effect);
					continue;
				}

				if (MainConfig.WORLD_ENABLE.getBoolean() && ePlayer.isInBlockedWorld()) {
					if (ePlayer.isGlowing()) {
						ePlayer.disableGlow(false);
						ePlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD, false);
						ChatUtil.sendMsg(ePlayer.getPlayer(), Message.WORLD_BLOCKED_RELOAD.get(), true);
					}
				} else {
					if (ePlayer.getGlowDisableReason() != null && ePlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
						if (ePlayer.setGlowDisableReason(GlowDisableReason.NONE, false)) {
							ePlayer.activateGlow();
							ChatUtil.sendMsg(ePlayer.getPlayer(), Message.WORLD_ALLOWED.get(), true);
						}
					}
				}
			}

			if (MainConfig.ADVANCED_GLOW_VISIBILITY_ENABLE.getBoolean()) {
				if (getInstance().getAdvancedGlowVisibility() == null)
					getInstance().setAdvancedGlowVisibility(new AdvancedGlowVisibilityAddon());
			} else {
				if (getInstance().getAdvancedGlowVisibility() != null)
					getInstance().getAdvancedGlowVisibility().shutdown();
				getInstance().setAdvancedGlowVisibility(null);
			}

			try {
				String alias = MainConfig.COMMAND_ALIAS.getString();

				if (MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && alias != null && Bukkit.getServer().getPluginCommand(alias) == null) {
					final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
					commandMapField.setAccessible(true);
					CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
					commandMap.register(alias, alias, Objects.requireNonNull(EGlow.getEGlowInstance().getCommand("eglow"), "Unable to retrieve eGlow command to register alias"));
				}
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				ChatUtil.reportError(e);
			}

			ChatUtil.sendMsg(sender, Message.RELOAD_SUCCESS.get(), true);
		} else {
			ChatUtil.sendMsg(sender, Message.RELOAD_SUCCESS.get(), true);
		}
	}
}