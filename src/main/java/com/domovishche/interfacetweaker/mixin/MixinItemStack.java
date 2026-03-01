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
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import java.util.List;
import java.util.Map;
import java.util.ListIterator;

@Mixin(ItemStack.class)
public class MixinItemStack {

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

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void onGetTooltip(EntityPlayer playerIn, net.minecraft.client.util.ITooltipFlag advanced, CallbackInfoReturnable<List<String>> cir) {

        // 1. If creativeOnly = false, we do nothing (vanilla behavior)
        if (!ModConfig.creativeOnly) {
            return;
        }

        // 2. If creativeOnly = true, we remove info ONLY if the player is NOT in creative
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && !mc.player.isCreative()) {
            List<String> tooltip = cir.getReturnValue();
            ListIterator<String> iterator = tooltip.listIterator();

            while (iterator.hasNext()) {
                String line = iterator.next();
                String cleanLine = TextFormatting.getTextWithoutFormattingCodes(line);

                if (cleanLine != null) {
                    // HANDLE NUMERICAL IDs (e.g., "Stone (#0001/0)")
                    // These are often appended to the name line, so we modify the string instead of removing the line.
                    if (cleanLine.contains(" (#")) {
                        // Find where the ID starts (usually " (#") and cut the string there
                        int index = line.lastIndexOf(" (#");
                        if (index != -1) {
                            iterator.set(line.substring(0, index));
                            continue; // Move to next line
                        }
                    }

                    // HANDLE REGISTRY NAMES AND NBT (Full line removal)
                    // Registry Name: "minecraft:stone" (contains ':' but no spaces)
                    boolean isRegistryName = cleanLine.contains(":") && !cleanLine.contains(" ");
                    // NBT Count: "NBT: 1 tag(s)"
                    boolean isNbt = cleanLine.startsWith("NBT:");
                    // ID on its own line: "#0001"
                    boolean isStandaloneId = cleanLine.startsWith("#");

                    if (isRegistryName || isNbt || isStandaloneId) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}
