package me.tinyoverflow.griefprevention.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ClaimBlocksConfiguration
{
    @Setting
    @Comment("The amount of claim blocks a new player starts with.")
    public int initial = 100;

    @Setting
    @Comment("The ratio of claim blocks the player will get refunded if they abandon a claim.")
    public float abandonReturnRatio = 0.75f;

    @Setting
    public ClaimBlocksAccruedConfiguration accrued = new ClaimBlocksAccruedConfiguration();
}
