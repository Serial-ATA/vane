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

@VaneItem(name = "lodestone_scroll", base = Material.WARPED_FUNGUS_ON_A_STICK, durability = 15, model_data = 0x760001, version = 1)
public class LodestoneScroll extends Scroll {
	public static final NamespacedKey LODESTONE_LOCATION = Util.namespaced_key("vane", "lodestone_location");

	@LangMessage
	public TranslatedMessage lang_teleport_no_bound_lodestone;
	@LangMessage
	public TranslatedMessage lang_teleport_missing_lodestone;
	@LangMessage
	public TranslatedMessage lang_bound_lore;

	public LodestoneScroll(Context<Trifles> context) {
		super(context, 6000);
	}

	@Override
	public RecipeList default_recipes() {
		return RecipeList.of(new ShapedRecipeDefinition("generic")
			.shape("pip", "cbe", "plp")
			.set_ingredient('b', Material.NETHERITE_SCRAP)
			.set_ingredient('p', Material.MAP)
			.set_ingredient('i', Material.CHORUS_FRUIT)
			.set_ingredient('c', Material.NETHERITE_SCRAP)
			.set_ingredient('e', Material.ENDER_PEARL)
			.set_ingredient('l', Material.CLOCK)
			.result(key().toString()));
	}

	private Location get_lodestone_location(final ItemStack scroll) {
		if (!scroll.hasItemMeta()) {
			return null;
		}
		return Util.storage_get_location(scroll.getItemMeta().getPersistentDataContainer(), LODESTONE_LOCATION, null);
	}

	@Override
	public Location teleport_location(final ItemStack scroll, Player player, boolean imminent_teleport) {
		final var lodestone_location = get_lodestone_location(scroll);
		final var lodestone = lodestone_location == null ? null : lodestone_location.getBlock();

		if (imminent_teleport) {
			if (lodestone_location == null) {
				lang_teleport_no_bound_lodestone.send_action_bar(player);
			} else if (lodestone == null || lodestone.getType() != Material.LODESTONE) {
				lang_teleport_missing_lodestone.send_action_bar(player);
			}
		}

		return lodestone == null ? null : lodestone.getLocation().add(0.5, 1.005, 0.5);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void on_player_interact(final PlayerInteractEvent event) {
		if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useItemInHand() == Event.Result.DENY) {
			return;
		}

		final var block = event.getClickedBlock();
		if (block.getType() != Material.LODESTONE) {
			return;
		}

		// Only if player sneak-right-clicks
		final var player = event.getPlayer();
		if (!player.isSneaking() || event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		// With a lodestone scroll
		final var item = player.getEquipment().getItem(event.getHand());
		final var custom_item = get_module().core.item_registry().get(item);
		if (!(custom_item instanceof LodestoneScroll scroll) || !scroll.enabled()) {
			return;
		}

		// Save lodestone location
		item.editMeta(meta -> {
			Util.storage_set_location(meta.getPersistentDataContainer(), LODESTONE_LOCATION, block.getLocation().add(0.5, 0.5, 0.5));
			meta.lore(List.of(lang_bound_lore
				.format("§a" + block.getWorld().getName(), "§b" + block.getX(), "§b" + block.getY(), "§b" + block.getZ())
				.decoration(TextDecoration.ITALIC, false)));
		});

		// Effects and sound
		// TODO: effect
		swing_arm(player, event.getHand());
		block.getWorld().playSound(block.getLocation(), Sound.RESPAWN_ANCHOR_REFILL, SoundCategory.BLOCKS, 1.0f, 3.0f);

		// Prevent offhand from triggering (e.g. plcaing torches)
		event.setUseInteractedBlock(Event.Result.DENY);
		event.setUseItemInHand(Event.Result.DENY);
	}
}