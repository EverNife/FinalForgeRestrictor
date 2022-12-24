package br.com.finalcraft.finalforgerestrictor.command;

import br.com.finalcraft.evernifecore.argumento.MultiArgumentos;
import br.com.finalcraft.evernifecore.commands.finalcmd.annotations.Arg;
import br.com.finalcraft.evernifecore.commands.finalcmd.annotations.FinalCMD;
import br.com.finalcraft.evernifecore.ecplugin.ECPluginManager;
import br.com.finalcraft.evernifecore.fancytext.FancyText;
import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.util.FCCommandUtil;
import br.com.finalcraft.evernifecore.util.FCItemUtils;
import br.com.finalcraft.evernifecore.util.FCTextUtil;
import br.com.finalcraft.evernifecore.util.pageviwer.PageViewer;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.PermissionNodes;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictedItem;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictionType;
import br.com.finalcraft.finalforgerestrictor.config.settings.FFResSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@FinalCMD(
        aliases = {"finalforgerestrictor","forgerestrictor","forgerestrict", "fres"}
)
public class CMDForgeRestrict {

    @FCLocale(lang = LocaleType.PT_BR, text = "§4§l ▶ §cEsse item [%item_name%] já está restrito!")
    @FCLocale(lang = LocaleType.EN_US, text = "§4§l ▶ §cThis item [%item_name%] is already restricted!")
    public static LocaleMessage THIS_ITEM_IS_ALREADY_RESTRICTED;

    @FCLocale(lang = LocaleType.PT_BR, text = "§4§l ▶ §cSe você quer sobrescrever esse item, [CliqueAqui]!", hover = "Clique aqui para Sobrescrever!")
    @FCLocale(lang = LocaleType.EN_US, text = "§d§l ▶ §eIf you wanna Override this item, [ClickHere]!", hover = "Click here to Override!")
    public static LocaleMessage IF_YOU_WANNA_OVERRIDE_CLICK_HERE;

    @FinalCMD.SubCMD(
            subcmd = {"addHand"},
            locales = {
                    @FCLocale(lang = LocaleType.PT_BR, text = "§bAdiciona o item da mão à alista específicada!"),
                    @FCLocale(lang = LocaleType.EN_US, text = "§bAdd HeldItem to the specified restricted items list!"),
            },
            permission = PermissionNodes.COMMAND_ADDHAND
    )
    public void addHand(Player player, ItemStack heldItem, @Arg(name = "<>") RestrictionType restrictionType, @Arg(name = "[Range]") Integer range, MultiArgumentos argumentos) {
        if (range == null){
            range = restrictionType.getDefaultRange();
        }

        RestrictedItem restrictedItem = FFResSettings.getRestrictedItem(heldItem);

        if (restrictedItem != null){
            THIS_ITEM_IS_ALREADY_RESTRICTED
                    .addPlaceholder("%item_name%", restrictedItem.serialize())
                    .send(player);

            Integer finalRange = range;
            RestrictedItem finalRestrictedItem = restrictedItem;
            IF_YOU_WANNA_OVERRIDE_CLICK_HERE
                    .addAction(FCCommandUtil.dynamicCommand(() -> {
                        FFResSettings.removeRestrictedItem(finalRestrictedItem.getItemStack());
                        addHand(player, heldItem, restrictionType, finalRange, argumentos);
                    }))
                    .send(player);
            return;
        }

        restrictedItem = new RestrictedItem(
                heldItem,
                heldItem.getType(),
                heldItem.getDurability() == 0 && !argumentos.getFlag("-exact").isSet() ? null : heldItem.getDurability(),
                range,
                restrictionType
        );

        FFResSettings.addRestrictedItem(restrictedItem);

        player.sendMessage("§a§l(+)§r§2 [" + restrictionType.getKey() + "]: " + restrictedItem.serialize());
    }

    @FinalCMD.SubCMD(
            subcmd = {"list"},
            locales = {
                    @FCLocale(lang = LocaleType.PT_BR, text = "§bLista todos os itens restritos!"),
                    @FCLocale(lang = LocaleType.EN_US, text = "§bList all restricted items!"),
            },
            permission = PermissionNodes.COMMAND_LIST
    )
    public void list(CommandSender sender, @Arg(name = "<>") RestrictionType restrictionType, @Arg(name = "[Page]", context = "[0:*]") Integer page) {
        List<RestrictedItem> restrictedItems = FFResSettings.RESTRICTED_ITEMS.values().stream()
                .filter(restrictedItem -> restrictedItem.getType() == restrictionType)
                .collect(Collectors.toList());

        PageViewer.targeting(RestrictedItem.class)
                .withSuplier(() -> restrictedItems)
                .extracting(RestrictedItem::serialize)
                .setComparator(null)
                .setFormatHeader(FCTextUtil.alignCenter(" §a§l[§c§lFinal.§bForgeRestrictor§a§l]§r ", "§e§m-§r") + "\n")
                .setFormatLine(
                        FancyText.of("§7#  %number%:  ")
                                .append("§c[Remove]").setHoverText("§cClick Here to Remove this Restricted Item!").setRunCommandAction("%remove_item_command%")
                                .append("§a %value%").setHoverText("%item_localized_name%")
                )
                .addPlaceholder("%item_localized_name%", restrictedItem -> FCItemUtils.getLocalizedName(restrictedItem.getItemStack()))
                .addPlaceholder("%remove_item_command%", restrictedItem -> FCCommandUtil.dynamicCommand(() -> {
                    FFResSettings.removeRestrictedItem(restrictedItem.getItemStack());
                    list(sender, restrictionType, page);//List this page again, recalculating supplier!
                }))
                .setCooldown(-1)
                .build()
                .send(page, sender);
    }

    @FinalCMD.SubCMD(
            subcmd = {"reload"},
            locales = {
                    @FCLocale(lang = LocaleType.PT_BR, text = "Recarrega o Plguin!")
            },
            permission = PermissionNodes.COMMAND_RELOAD
    )
    public void reload(CommandSender sender){
        ECPluginManager.reloadPlugin(sender, FinalForgeRestrictor.instance);
    }

}

