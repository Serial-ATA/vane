package org.oddlama.vane.trifles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.oddlama.vane.annotation.config.ConfigBoolean;
import org.oddlama.vane.annotation.config.ConfigInt;
import org.oddlama.vane.annotation.config.ConfigLong;
import org.oddlama.vane.annotation.lang.LangMessage;
import org.oddlama.vane.core.Listener;
import org.oddlama.vane.core.data.CooldownData;
import org.oddlama.vane.core.lang.TranslatedMessage;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.util.Util;

public class ItemFinder extends Listener<Trifles> {
	public static final NamespacedKey LAST_FIND_TIME = Util.namespaced_key("vane_trifles", "last_item_find_time");

	@ConfigInt(def = 2, min = 1, max = 10, desc = "The radius of chunks in which containers (and possibly entities) are checked for matching items.")
	public int config_radius;

	@ConfigBoolean(def = true, desc = "Also search entities such as players, mobs, minecarts, ...")
	public boolean config_search_entities;

	public ItemFinder(Context<Trifles> context) {
		super(context.group("item_finder", "Enables players to search for items in nearby containers by either middle-clicking a similar item in their inventory or by using the `/finditem <item>` command."));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void on_player_click_inventory(final InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}

		final var item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR) {
			return;
		}

		// Shift-rightclick
		if (!(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getClick() == ClickType.SHIFT_RIGHT)) {
			return;
		}

		find_item(player, item.getType());
		event.setCancelled(true);
	}

	private boolean is_container(final Block block) {
		return block.getState() instanceof Container;
	}

	private void indicate_match_at(@NotNull Player player, @NotNull Location location) {
		player.spawnParticle(Particle.ASH, location, 100, 0.0, 0.0, 0.0, 1.0);
	}

	public void find_item(@NotNull final Player player, @NotNull final Material material) {
		if (!cooldown_data.check_or_update_cooldown(player)) {
			lang_cooldown.send_action_bar(player);
			return;
		}

		// Find chests in configured radius and sort them.
		boolean any_found = false;
		final var world = player.getWorld();
		final var origin_chunk = player.getChunk();
		for (int cx = origin_chunk.getX() - config_radius; cx <= origin_chunk.getX() + config_radius; ++cx) {
			for (int cz = origin_chunk.getZ() - config_radius; cz <= origin_chunk.getZ() + config_radius; ++cz) {
				if (!world.isChunkLoaded(cx, cz)) {
					continue;
				}
				final var chunk = world.getChunkAt(cx, cz);
				for (final var tile_entity : chunk.getTileEntities(this::is_container, false)) {
					if (tile_entity instanceof Container container) {
						if (container.getInventory().contains(material)) {
							indicate_match_at(player, container.getLocation());
							any_found = true;
						}
					}
				}
				if (config_search_entities) {
					for (final var entity : chunk.getEntities()) {
						if (entity instanceof InventoryHolder holder) {
							if (holder.getInventory().contains(material)) {
								indicate_match_at(player, entity.getLocation());
								any_found = true;
							}
						}
					}
				}
			}
		}

		if (any_found) {
			player.playSound(player, Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 5.0f);
		} else {
			player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 1.0f, 5.0f);
		}
	}
}
