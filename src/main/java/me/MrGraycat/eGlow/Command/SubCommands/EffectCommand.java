package me.MrGraycat.eGlow.Command.SubCommands;

import org.bukkit.command.CommandSender;

import me.MrGraycat.eGlow.Command.SubCommand;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EffectCommand extends SubCommand {
	@Override
	public String getName() {
		return "effect";
	}

	@Override
	public String getDescription() {
		return "Activate a glow effect";
	}

	@Override
	public String getPermission() {
		return "";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"/eGlow <color>", "/eGlow blink <color> <speed>", "/eGlow <effect> <speed>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return true;
	}

	@Override
	public void perform(CommandSender sender, IEGlowPlayer ePlayer, String[] args) {
		if (ePlayer.isInBlockedWorld()) {
			ChatUtil.sendMsg(sender, Message.WORLD_BLOCKED.get(), true);
			return;
		}
		
		if (ePlayer.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
			ChatUtil.sendMsg(sender, Message.DISGUISE_BLOCKED.get(), true);
			return;
		}
		
		if (MainConfig.SETTINGS_DISABLE_GLOW_WHEN_INVISIBLE.getBoolean() && ePlayer.getGlowDisableReason().equals(GlowDisableReason.INVISIBLE)) {
			ChatUtil.sendMsg(sender, Message.INVISIBILITY_BLOCKED.get(), true);
			return;
		}

		IEGlowEffect effect = null;

		switch(args.length) {
		case(1):
			effect = DataManager.getEGlowEffect(args[0].replace("off", "none").replace("disable", "none"));
			break;
		case(2):
			effect = DataManager.getEGlowEffect(args[0] + args[1]);
			break;
		case(3):
			effect = DataManager.getEGlowEffect(args[0] + args[1] + args[2]);
			break;
		}
		
		if (effect == null) {
			sendSyntax(sender, "", true);
			sendSyntax(sender, getSyntax()[0], false);
			sendSyntax(sender, getSyntax()[1], false);
			sendSyntax(sender, getSyntax()[2], false);
			return;
		}
		
		if (ePlayer.getPlayer().hasPermission(effect.getPermission()) || DataManager.isCustomEffect(effect.getName()) && ePlayer.getPlayer().hasPermission("eglow.effect.*")) {
			if (effect.getName().equals("none")) {
				if (ePlayer.getGlowStatus() || ePlayer.getFakeGlowStatus()) {
					ePlayer.disableGlow(false);
				}
				ChatUtil.sendMsg(sender, Message.DISABLE_GLOW.get(), true);
				return;
			}
			
			if (!ePlayer.isSameGlow(effect)) {
				ePlayer.disableGlow(true);
				ePlayer.activateGlow(effect);
				ChatUtil.sendMsg(sender, Message.NEW_GLOW.get(effect.getDisplayName()), true);

				if (ePlayer.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMsg(sender, Message.UNSUPPORTED_GLOW.get(), true);
				return;
			}
			
			ChatUtil.sendMsg(sender, Message.SAME_GLOW.get(), true);
			return;
		}
		ChatUtil.sendMsg(sender, Message.NO_PERMISSION.get(), true);
	}
}