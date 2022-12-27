# FinalForgeRestrictor

Improved protection for modded servers. 

This is a remake of KaiKikuchi's ForgeRestrictor!

## Compatibility

Curently supports :
 - **WorldGuard** (On all versions)
 - **GriefPreventionPlus** (On 1.7.10 and 1.12.2)
 - **PlotSquared** (On all versions)
 - **GriefPrevention** (only 1.16.5)

MCvesions:
 - Minecraft 1.7.10 *(requires Crucible)*
 - MineCraft 1.12.2 *(tested on Mohist)*
 - Minecraft 1.16.5 *(tested on CatServer and Archlight)*

## Requirements

This plugin requires: [EverNifeCore](https://www.spigotmc.org/resources/evernifecore.97739/)

Also, this plugin requires the others protection plugins to work.

## Commands

The main command is **/fres**

To add items to the list use, while holding the item, for example:
- **/fres addHand aoe** _(will add the heldItem to the AOE list)_
- **/fres addHand ranged** _(will add the heldItem to the Ranged list)_
- **/fres addHand whitelist** _(will add the heldItem to the Whitelist list)_
- **/fres list <aoe|ranged|whitelist>** _(list all restricted items from each list)_
- **/fres reload** _(reloads the plugin)_

## Permissions

[Check all PermissionNodes](src/main/java/br/com/finalcraft/finalforgerestrictor/PermissionNodes.java)
