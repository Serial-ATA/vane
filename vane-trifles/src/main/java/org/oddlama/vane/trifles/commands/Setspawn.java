package org.oddlama.vane.admin.commands;

import org.bukkit.entity.Player;
import org.oddlama.vane.trifles.Trifles;
import org.oddlama.vane.annotation.command.Name;
import org.oddlama.vane.core.command.Command;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.util.LazyLocation;

@Name("setspawn")
public class Setspawn extends Command<Trifles> {
	public static final NamespacedKey IS_SPAWN_WORLD = Util.namespaced_key("vane", "is_spawn_world");

	public Setspawn(Context<Trifles> context) {
		super(context);
		// Add help
		params().fixed("help").ignore_case().exec(this::print_help);
		// Command parameters
		params().exec_player(this::set_spawn);
	}

	private void set_spawn(Player player) {
		final var loc = player.getLocation();

		// Unset spawn tag in all worlds
		for (final var world : get_module().getServer().getWorlds()) {
			world.getPersistentDataContainer().remove(IS_SPAWN_WORLD);
		}

		// Set spawn and mark as default world
		final var world = player.getWorld();
		world.setSpawnLocation(loc);
		world.getPersistentDataContainer().set(IS_SPAWN_WORLD, PersistentDataType.BOOLEAN, true);
		player.sendMessage("§aSpawn §7set!");
	}
}