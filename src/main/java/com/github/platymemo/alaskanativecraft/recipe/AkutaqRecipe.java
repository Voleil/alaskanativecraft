package com.github.platymemo.alaskanativecraft.recipe;

import com.github.platymemo.alaskanativecraft.item.AlaskaItems;
import com.github.platymemo.alaskanativecraft.tags.AlaskaTags;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class AkutaqRecipe extends SpecialCraftingRecipe {
    private static final StatusEffect[] POSSIBLE_EFFECTS = {
            StatusEffects.ABSORPTION,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE,
            StatusEffects.FIRE_RESISTANCE,
            StatusEffects.HASTE,
            StatusEffects.STRENGTH,
            StatusEffects.SPEED,
            StatusEffects.JUMP_BOOST,
            StatusEffects.SATURATION
    };
    private static final int[] EFFECT_DURATIONS = {400, 200, 200, 200, 200, 100, 250, 200, 10};


    public AkutaqRecipe(Identifier id) {
        super(id);
    }

    // Can't use SuspiciousStewItem.addEffectToStew because it overwrites the list tag each time
    private static void addEffectToAkutaq(@NotNull ItemStack stew, StatusEffect effect, int duration) {
        NbtCompound compoundTag = stew.getOrCreateNbt();
        NbtList listTag = compoundTag.getList("Effects", 10);

        boolean effectExists = false;

        byte effectId = (byte) StatusEffect.getRawId(effect);
        int actualDuration = duration;
        for (int i = 0; i < listTag.size(); ++i) {
            NbtCompound previousEffect = listTag.getCompound(i);
            if (previousEffect.contains("EffectDuration", 3) && effectId == previousEffect.getByte("EffectId")) {
                actualDuration += previousEffect.getInt("EffectDuration");
                previousEffect.putInt("EffectDuration", actualDuration);
                effectExists = true;
            }
        }

        if (!effectExists) {
            NbtCompound newEffect = new NbtCompound();
            newEffect.putByte("EffectId", effectId);
            newEffect.putInt("EffectDuration", actualDuration);
            listTag.add(newEffect);
        }

        compoundTag.put("Effects", listTag);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inv, World world) {
        boolean hasMeat = false;
        boolean hasBerries = false;
        boolean hasBowl = false;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack itemStack = inv.getStack(i);
            if (!itemStack.isEmpty()) {

                // Need at least 1 berry but can have more
                if (itemStack.isIn(AlaskaTags.AKUTAQ_BERRIES)) {
                    hasBerries = true;
                }
                // Can only have one piece of meat
                else if (itemStack.isIn(AlaskaTags.AKUTAQ_MEATS) && !hasMeat) {
                    hasMeat = true;
                } else {

                    // Checks for bowl, if not a bowl or anything but a bowl, returns false
                    if (itemStack.getItem() != Items.BOWL || hasBowl) {
                        return false;
                    }

                    hasBowl = true;
                }
            }
        }

        return hasMeat && hasBerries && hasBowl;
    }

    @Override
    public ItemStack craft(@NotNull CraftingInventory inv) {
        ItemStack akutaq = new ItemStack(AlaskaItems.AKUTAQ, 1);
        Random random = new Random();

        ItemStack currentItemstack;
        for (int i = 0; i < inv.size(); ++i) {
            currentItemstack = inv.getStack(i);
            if (!currentItemstack.isEmpty() && currentItemstack.isIn(AlaskaTags.AKUTAQ_BERRIES)) {
                int randomEffect = random.nextInt(8);

                // Add effect
                addEffectToAkutaq(akutaq, POSSIBLE_EFFECTS[randomEffect], EFFECT_DURATIONS[randomEffect]);
            }
        }

        return akutaq;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AlaskaRecipes.AKUTAQ;
    }
}
