package me.mrgraycat.eglow.addon.citizens;

import me.mrgraycat.eglow.EGlow;
import me.mrgraycat.eglow.addon.GlowAddon;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.trait.ScoreboardTrait;

public class CitizensAddon extends GlowAddon {

	/**
	 * Loads in the custom EGlow trait for citizen NPCs
	 *
	 * @throws IllegalArgumentException thrown when the trait is already registered (Ignored)
	 */
	public CitizensAddon(EGlow instance) {
		super(instance);

		try {
			CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(EGlowCitizensTrait.class).withName("eGlow"));
		} catch (IllegalArgumentException ignored) {
		}
	}

	/**
	 * Check to see if required traits exist & were successfully applied to the NPC
	 *
	 * @param npc Citizens NPC
	 * @return true if required traits exist and are applied, false if not
	 * @throws NoClassDefFoundError thrown when using an old version of Citizens where the scoreboardTrait doesn't exist
	 */
	public boolean traitCheck(NPC npc) {
		try {
			if (!npc.hasTrait(ScoreboardTrait.class))
				npc.addTrait(ScoreboardTrait.class);
		} catch (NoClassDefFoundError ignored) {
			return false;
		}

		if (!npc.hasTrait(EGlowCitizensTrait.class)) {
			npc.addTrait(EGlowCitizensTrait.class);
		}

		return true;
	}
}