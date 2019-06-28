package com.nisovin.magicspells.spells.targeted;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;

public class TransmuteSpell extends TargetedSpell implements TargetedLocationSpell {

	private List<Material> blockTypes;

	private Material transmuteType;
	
	public TransmuteSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		List<String> list = getConfigStringList("transmutable-types", null);
		blockTypes = new ArrayList<>();
		if (list != null && !list.isEmpty()) {
			for (String s : list) {
				Material material = Material.getMaterial(s.toUpperCase());
				if (material == null || !material.isBlock()) continue;
				blockTypes.add(material);
			}
		} else blockTypes.add(Material.IRON_BLOCK);

		String materialName = getConfigString("transmute-type", "gold_block").toUpperCase();
		transmuteType = Material.getMaterial(materialName);
		if (transmuteType == null || !transmuteType.isBlock()) {
			MagicSpells.error("TransmuteSpell '" + internalName + "' has an transmute-type defined!");
			transmuteType = null;
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block block = getTargetedBlock(player, power);
			if (block == null) return noTarget(player);
			
			SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, player, block.getLocation(), power);
			EventUtil.call(event);
			if (event.isCancelled()) return noTarget(player);
			block = event.getTargetLocation().getBlock();
			
			if (!canTransmute(block)) return noTarget(player);

			block.setType(transmuteType);
			playSpellEffects(player, block.getLocation().add(0.5, 0.5, 0.5));
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		Block block = target.getBlock();
		if (canTransmute(block)) {
			block.setType(transmuteType);
			playSpellEffects(caster, block.getLocation().add(0.5, 0.5, 0.5));
			return true;
		}

		Vector v = target.getDirection();
		block = target.clone().add(v).getBlock();
		if (canTransmute(block)) {
			block.setType(transmuteType);
			playSpellEffects(caster, block.getLocation().add(0.5, 0.5, 0.5));
			return true;
		}
		return false;
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		Block block = target.getBlock();
		if (canTransmute(block)) {
			block.setType(transmuteType);
			playSpellEffects(EffectPosition.TARGET, block.getLocation().add(0.5, 0.5, 0.5));
			return true;
		}
		return false;
	}
	
	private boolean canTransmute(Block block) {
		for (Material m : blockTypes) {
			if (m.equals(block.getType())) return true;
		}
		return false;
	}

}
