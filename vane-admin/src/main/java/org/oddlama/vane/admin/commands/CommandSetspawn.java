package org.oddlama.vane.admin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.oddlama.vane.annotation.command.Description;
import org.oddlama.vane.annotation.command.Name;
import org.oddlama.vane.annotation.command.Usage;
import org.oddlama.vane.core.Module;
import org.oddlama.vane.core.command.Command;

@Name("setspawn")
@Usage("")
@Description("Sets the global spawn location to your current position.")
public class CommandSetspawn extends Command {
	public CommandSetspawn(Module module) {
		super(module);

		// Add help
		params().fixed("help").ignore_case().exec(this::print_help);
		// Command parameters
		params().exec_player(this::set_spawn);
	}

	private void set_spawn(Player player, Module module) {
		player.getWorld().setSpawnLocation(player.getLocation());
		player.sendMessage("§aSpawn §7set!");
	}
}