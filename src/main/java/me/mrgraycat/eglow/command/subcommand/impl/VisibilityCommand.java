package me.mrgraycat.eglow.command.subcommand.impl;

import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowVisibility;
import me.mrgraycat.eglow.util.packet.PacketUtil;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import me.mrgraycat.eglow.command.subcommand.SubCommand;
import org.bukkit.command.CommandSender;

public class VisibilityCommand extends SubCommand {

	@Override
	public String getName() {
		return "visibility";
	}

	@Override
	public String getDescription() {
		return "Set the way you see the glowing.";
	}

	@Override
	public String getPermission() {
		return "eglow.command.visibility";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow visibility <all/other/own/none>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (args.length >= 2) {
			if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT)) {
				ChatUtil.sendMessage(sender, Message.UNSUPPORTED_GLOW.get(), true);
				return;
			}

			if (!args[1].equalsIgnoreCase("all") && !args[1].equalsIgnoreCase("other") && !args[1].equalsIgnoreCase("own") && !args[1].equalsIgnoreCase("none")) {
				sendSyntax(sender);
				return;
			}

			GlowVisibility oldVisibility = ePlayer.getGlowVisibility();
			GlowVisibility newVisibility = GlowVisibility.valueOf(args[1].toUpperCase());

			if (!ePlayer.getGlowVisibility().equals(newVisibility) && ePlayer.getSaveData())
				ePlayer.setSaveData(true);

			ePlayer.setGlowVisibility(newVisibility);

			if (oldVisibility != newVisibility)
				PacketUtil.forceUpdateGlow(ePlayer);

			ChatUtil.sendMessage(sender, Message.VISIBILITY_CHANGE.get(newVisibility.name()), true);
		} else {
			sendSyntax(sender);
		}
	}
}