package com.domovishche.interfacetweaker.mixin;

import com.domovishche.interfacetweaker.config.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public class MixinItemStack {

    // ── Tooltip ID hider ──────────────────────────────────────────────────────

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void hideAdvancedTooltipIds(
            @Nullable EntityPlayer playerIn,
            ITooltipFlag tooltipFlag,
            CallbackInfoReturnable<List<String>> cir
    ) {
        if (!ModConfig.creativeOnly) return;
        if (!tooltipFlag.isAdvanced()) return;
        if (playerIn != null && playerIn.capabilities.isCreativeMode) return;

        List<String> tooltip = cir.getReturnValue();
        if (tooltip == null || tooltip.isEmpty()) return;

        tooltip.removeIf(MixinItemStack::isVanillaIdLine);
    }

    private static boolean isVanillaIdLine(String line) {
        if (line == null || !line.startsWith("\u00a78")) return false;
        String raw = line.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();
        return raw.contains(":") || raw.matches("\\d+");
    }

    // ── Enchantment glint ─────────────────────────────────────────────────────

    /**
     * Controls glint rendering by injecting into ItemStack#hasEffect(),
     * which is the method RenderItem.hasEffect() delegates to.
     *
     * - forceGlintItems:     items in this list always show glint
     * - noGlintEnchantments: if ALL enchantments on an item are listed,
     *                        the item loses its glint
     */
    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void controlGlint(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;

        // Force glint on specific items
        if (!ModConfig.forceGlintItems.isEmpty()) {
            ResourceLocation itemName = self.getItem().getRegistryName();
            if (itemName != null && ModConfig.forceGlintItems.contains(itemName.toString())) {
                cir.setReturnValue(true);
                return;
            }
        }

        // Suppress glint when all enchantments are in the suppression list
        if (!ModConfig.noGlintEnchantments.isEmpty() && self.isItemEnchanted()) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(self);
            if (!enchantments.isEmpty()) {
                boolean allSuppressed = enchantments.keySet().stream().allMatch(enchantment -> {
                    ResourceLocation name = Enchantment.REGISTRY.getNameForObject(enchantment);
                    return name != null && ModConfig.noGlintEnchantments.contains(name.toString());
                });
                if (allSuppressed) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
