package me.tinyoverflow.griefprevention.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import me.tinyoverflow.griefprevention.CustomLogEntryTypes;
import me.tinyoverflow.griefprevention.GriefPrevention;
import me.tinyoverflow.griefprevention.Messages;
import me.tinyoverflow.griefprevention.TextMode;
import org.bukkit.entity.Player;

public class DeleteAllAdminClaimsCommand extends BaseCommand implements PlayerCommandExecutor
{
    public DeleteAllAdminClaimsCommand(String commandName, GriefPrevention plugin)
    {
        super(commandName, plugin);
    }

    @Override
    public CommandAPICommand getCommand()
    {
        return new CommandAPICommand(this.getCommandName())
                .withPermission("griefprevention.deletealladminclaims")
                .executesPlayer(this);
    }

    @Override
    public void run(Player player, CommandArguments commandArguments) throws WrapperCommandSyntaxException
    {
        if (!player.hasPermission("griefprevention.deleteclaims"))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NoDeletePermission);
            return;
        }

        //delete all admin claims
        this.getPlugin().getDataStore().deleteClaimsForPlayer(null, true);  //null for owner id indicates an administrative claim
        GriefPrevention.sendMessage(player, TextMode.Success, Messages.AllAdminDeleted);
        GriefPrevention.AddLogEntry(player.getName() + " deleted all administrative claims.", CustomLogEntryTypes.AdminActivity);
        this.getPlugin().getDataStore().getPlayerData(player.getUniqueId()).setVisibleBoundaries(null);
    }
}
