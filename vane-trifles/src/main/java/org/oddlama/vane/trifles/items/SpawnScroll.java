package org.oddlama.vane.trifles.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.oddlama.vane.core.config.recipes.RecipeList;
import org.oddlama.vane.core.config.recipes.ShapedRecipeDefinition;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.trifles.Trifles;
import org.oddlama.vane.annotation.item.VaneItem;
import org.oddlama.vane.trifles.event.PlayerTeleportScrollEvent;
import org.oddlama.vane.util.Util;

@VaneItem(name = "spawn_scroll", base = Material.WARPED_FUNGUS_ON_A_STICK, durability = 40, model_data = 0x760001, version = 1)
public class SpawnScroll extends Scroll {
	public SpawnScroll(Context<Trifles> context) {
		super(context, 6000);
	}

	@Override
	public RecipeList default_recipes() {
		return RecipeList.of(new ShapedRecipeDefinition("generic")
			.shape("pip", "cbe", "plp")
			.set_ingredient('b', Material.NETHERITE_SCRAP)
			.set_ingredient('p', Material.MAP)
			.set_ingredient('i', Material.CHORUS_FRUIT)
			.set_ingredient('c', Material.OAK_SAPLING)
			.set_ingredient('e', Material.ENDER_PEARL)
			.set_ingredient('l', Material.EGG)
			.result(key().toString()));
	}

	@Override
	public Location teleport_location(final ItemStack scroll, Player player, boolean imminent_teleport) {
		Location loc = null;
		for (final var world : get_module().getServer().getWorlds()) {
			if (world.getPersistentDataContainer().getOrDefault(Setspawn.IS_SPAWN_WORLD, PersistentDataContainer.BOOLEAN, false)) {
				loc = world.getSpawnLocation();
			}
		}
		// Fallback to spawn location of first world
		if (loc == null) {
			loc = get_module().getServer().getWorlds().get(0).getSpawnLocation();
		}
		return loc;
	}
}