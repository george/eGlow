package me.mrgraycat.eglow.command.subcommand;

import me.mrgraycat.eglow.addon.citizens.EGlowCitizensTrait;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SubCommand {

	protected static final EGlow INSTANCE = EGlow.getInstance();

	public abstract String getName();
	public abstract String getDescription();
	public abstract String getPermission();

	public abstract String[] getSyntax();

	public abstract boolean isPlayerCmd();

	public abstract void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args);

	/**
	 * Sends the correct command syntax
	 * @param sender The receiver of the message
	 */
	public void sendSyntax(CommandSender sender) {
		sendSyntax(sender, getSyntax());
	}

	/**
	 * Send correct command syntax
	 * @param sender console/player that will receive this message
	 * @param syntaxMessages correct command syntax
	 */
	private void sendSyntax(CommandSender sender, String[] syntaxMessages) {
		AtomicBoolean sentPrefix = new AtomicBoolean(true);

		Arrays.stream(syntaxMessages).forEach(message -> {
			ChatUtil.sendPlainMessage(sender, Message.INCORRECT_USAGE.get(message), sentPrefix.get());

			sentPrefix.set(false);
		});
	}
	
	/**
	 * Get the IEGlowEntity instance of the player/npc being targeted
	 * @param sender console/player to send message to if something should be incorrect
	 * @param args command args to retrieve the required info
	 * @return IEGlowEntity instance of the targeted player/npc
	 */
	public List<IEGlowPlayer> getTarget(CommandSender sender, String[] args) {
		if (args.length >= 2) {
			List<IEGlowPlayer> results = new ArrayList<>();
			
			if (args[1].toLowerCase().contains("npc:")) {
				if (INSTANCE.getCitizensAddon() == null) {
					ChatUtil.sendMessage(sender, Message.CITIZENS_NOT_INSTALLED.get(), true);
					return null;
				}
					
				String argument = args[1].toLowerCase().replace("npc:", "");
				NPC npc = null;
				
				if (argument.equals("s") || argument.equals("sel") || argument.equals("selected")) {
					try {
						npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
					} catch (NullPointerException e) {
						ChatUtil.sendMessage(sender, Message.CITIZENS_NPC_NOT_FOUND.get(), true);
					}
				} else {
					try {
						npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(argument));
					} catch (NumberFormatException e) {
						ChatUtil.sendMessage(sender, "&f'&e" + argument + "&f' &cis an invalid NPC ID", true);
					}
				}
				
				if (npc == null) {
					ChatUtil.sendMessage(sender, Message.CITIZENS_NPC_NOT_FOUND.get(), true);
					return null;
				}
				
				if (!npc.isSpawned()) {
					ChatUtil.sendMessage(sender, Message.CITIZENS_NPC_NOT_SPAWNED.get(), true);
					return null;
				}
				
				if (!INSTANCE.getCitizensAddon().traitCheck(npc)) {
					ChatUtil.sendMessage(sender, Message.PREFIX.get() + "&cYour Citizens plugin is outdated&f!", true);
					return null;
				}
				try {
					results.add(npc.getOrAddTrait(EGlowCitizensTrait.class).getEGlowNPC());
					
					return results;
				} catch(NoSuchMethodError e) {
					ChatUtil.sendToConsole("&cYour Citizens version is outdated please use 2.0.27 or later", true);
				}
				
			} else {
				if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("all")) {
					results.addAll(DataManager.getGlowPlayers());
					return results;
				}
				
				Player player = Bukkit.getPlayer(args[1].toLowerCase());
				
				if (player == null) {
					ChatUtil.sendMessage(sender, Message.PLAYER_NOT_FOUND.get(), true);
					return null;
				}
				
				IEGlowPlayer ePlayer = DataManager.getEGlowPlayer(player);
				
				if (ePlayer == null) {
					ChatUtil.sendMessage(sender, Message.PLAYER_NOT_FOUND.get(), true);
					return null;
				}
				
				if (ePlayer.isInBlockedWorld() && args.length >= 3 && !args[2].equalsIgnoreCase("glowonjoin")) {
					ChatUtil.sendMessage(sender, Message.OTHER_PLAYER_IN_DISABLED_WORLD.get(ePlayer), true);
					return null;
				}
				
				results.add(ePlayer);
				
				return results;
			}
		}
		return null;
	}
}