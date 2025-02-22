package me.tinyoverflow.griefprevention.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import me.tinyoverflow.griefprevention.*;
import org.bukkit.entity.Player;

public class ClaimAbandonCommand extends BaseCommand implements PlayerCommandExecutor
{
    public ClaimAbandonCommand(String commandName, GriefPrevention plugin)
    {
        super(commandName, plugin);
    }

    @Override
    public CommandAPICommand getCommand()
    {
        return new CommandAPICommand(getCommandName())
                .withPermission("griefprevention.abandonclaim")
                .withOptionalArguments(new BooleanArgument("topLevel"))
                .executesPlayer(this);
    }

    @Override
    public void run(Player player, CommandArguments arguments) throws WrapperCommandSyntaxException
    {
        final boolean abandonTopLevelClaim = (boolean) arguments.getOptional("topLevel").orElse(false);

        PlayerData playerData = getPlugin().getDataStore().getPlayerData(player.getUniqueId());

        //which claim is being abandoned?
        Claim claim = getPlugin().getDataStore().getClaimAt(player.getLocation(), true /*ignore height*/, null);
        if (claim == null)
        {
            GriefPrevention.sendMessage(player, TextMode.INSTRUCTION, Messages.AbandonClaimMissing);
            return;
        }

        //verify ownership
        if (claim.checkPermission(player, ClaimPermission.Edit, null) != null)
        {
            GriefPrevention.sendMessage(player, TextMode.ERROR, Messages.NotYourClaim);
        }

        //warn if has children and we're not explicitly deleting a top level claim
        else if (claim.children.size() > 0 && !abandonTopLevelClaim)
        {
            GriefPrevention.sendMessage(player, TextMode.INSTRUCTION, Messages.DeleteTopLevelClaim);
        }
        else
        {
            //delete it
            claim.removeSurfaceFluids(null);
            getPlugin().getDataStore().deleteClaim(claim, true, false);

            //if in a creative mode world, restore the claim area
            if (GriefPrevention.instance.creativeRulesApply(claim.getLesserBoundaryCorner()))
            {
                GriefPrevention.AddLogEntry(player.getName() + " abandoned a claim @ " +
                                            GriefPrevention.getFriendlyLocationString(claim.getLesserBoundaryCorner()));
                GriefPrevention.sendMessage(player, TextMode.WARNING, Messages.UnclaimCleanupWarning);
                GriefPrevention.instance.restoreClaim(claim, 20L * 60 * 2);
            }

            //adjust claim blocks when abandoning a top level claim
            double abandonReturnRatio = getPlugin().getPluginConfig().getClaimConfiguration().getClaimBlocksConfiguration().abandonReturnRatio;
            if (abandonReturnRatio != 1.0d && claim.parent == null && claim.ownerID.equals(playerData.playerID))
            {
                playerData.setAccruedClaimBlocks(playerData.getAccruedClaimBlocks() -
                                                 (int) Math.ceil((claim.getArea() * (1 - abandonReturnRatio))));
            }

            //tell the player how many claim blocks he has left
            int remainingBlocks = playerData.getRemainingClaimBlocks();
            GriefPrevention.sendMessage(
                    player,
                    TextMode.SUCCESS,
                    Messages.AbandonSuccess,
                    String.valueOf(remainingBlocks)
            );

            //revert any current visualization
            playerData.setVisibleBoundaries(null);

            playerData.warnedAboutMajorDeletion = false;
        }
    }
}
