package muramasa.antimatter.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.capability.energy.ItemEnergyHandler;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.util.Utils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.forge.TesseractCaps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static muramasa.antimatter.Data.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialTool extends DiggerItem implements IAntimatterTool {

    protected final String domain;
    protected final AntimatterToolType type;

    protected final int energyTier;
    protected final long maxEnergy;

    public MaterialTool(String domain, AntimatterToolType type, Properties properties) {
        super(type.getBaseAttackDamage(), type.getBaseAttackSpeed(), AntimatterItemTier.NULL, type.getToolType(), properties);
        this.domain = domain;
        this.type = type;
        this.energyTier = -1;
        this.maxEnergy = -1;
        AntimatterAPI.register(IAntimatterTool.class, this);
    }

    public MaterialTool(String domain, AntimatterToolType type, Properties properties, int energyTier) {
        super(type.getBaseAttackDamage(), type.getBaseAttackSpeed(), AntimatterItemTier.NULL, type.getToolType(), properties);
        this.domain = domain;
        this.type = type;
        this.energyTier = energyTier;
        this.maxEnergy = type.getBaseMaxEnergy() * energyTier;
        AntimatterAPI.register(IAntimatterTool.class, this);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getId() {
        return type.isPowered() ? String.join("_", type.getId(), Ref.VN[energyTier].toLowerCase(Locale.ENGLISH)) : type.getId();
    }

    @Nonnull
    @Override
    public AntimatterToolType getAntimatterToolType() {
        return type;
    }

    /*
    @Nonnull
    @Override
    public Set<Tag<Block>> getToolTypes(ItemStack stack) {
        return getToolTypes();
    }*/

    /**
     * Returns -1 if its not a powered tool
     **/
    public int getEnergyTier() {
        return energyTier;
    }

    @Nonnull
    @Override
    public ItemStack asItemStack(@Nonnull Material primary, @Nonnull Material secondary) {
        return resolveStack(primary, secondary, 0, maxEnergy);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> list) {
        onGenericFillItemGroup(group, list, maxEnergy);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return Utils.doesStackHaveToolTypes(stack, WRENCH, ELECTRIC_WRENCH, SCREWDRIVER, ELECTRIC_SCREWDRIVER, CROWBAR, WIRE_CUTTER); // ???
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        AntimatterToolType type = this.getAntimatterToolType();
        if (type.getEffectiveMaterials().contains(state.getMaterial())) {
            return true;
        }
        if (type.getEffectiveBlocks().contains(state.getBlock())) {
            return true;
        }
        return state.is(getAntimatterToolType().getToolType()) && net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(getTier(stack), state);
    }

    @Override
    public void onUseTick(Level p_41428_, LivingEntity p_41429_, ItemStack p_41430_, int p_41431_) {
        super.onUseTick(p_41428_, p_41429_, p_41430_, p_41431_);
    }



    /*
    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return getPrimaryMaterial(stack).getDisplayName().appendSibling(new StringTextComponent(type.getId()));
    }
     */

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        onGenericAddInformation(stack, tooltip, flag);
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return type.getUseAction();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return type.getUseAction() == UseAnim.NONE ? super.getUseDuration(stack) : 72000;
    }

    /*
    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {
        return Utils.isToolEffective(this, state) && getTier(stack).getLevel() >= state.getHarvestLevel();
    }*/

    /*
    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable Player player, @Nullable BlockState blockState) {
        return getToolTypes().contains(tool) ? getTier(stack).getLevel() : -1;
    }*/

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (getId().equals("branch_cutter")) {
            return Math.round((float) getTier(stack).getUses() / 4);
        }
        return getTier(stack).getUses();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return onGenericHitEntity(stack, target, attacker, 0.75F, 0.75F);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isCorrectToolForDrops(stack, state) ? getTier(stack).getSpeed() : 1.0F;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        return onGenericBlockDestroyed(stack, world, state, pos, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        return onGenericItemUse(ctx);
    }

    public void handleRenderHighlight(Player entity, LevelRenderer levelRenderer, Camera camera, HitResult target, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        onGenericHighlight(entity, levelRenderer, camera, target, partialTicks, poseStack, multiBufferSource);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
        return type.getBlockBreakability();
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return type.getActualTags().contains(BlockTags.MINEABLE_WITH_AXE);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slotType, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slotType == EquipmentSlot.MAINHAND) {
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", type.getBaseAttackDamage() + getTier(stack).getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", type.getBaseAttackSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

//    @Override
//    public ActionResultType onItemUse(ItemUseContext ctx) {
//        return onGenericItemUse(ctx);

    //TODO functionality moved to BlockMachine.onBlockActivated
    //TODO determine if other mods need smart interaction on
    //TODO blocks that *don't* extend BlockMachine
//        TileEntity tile = Utils.getTile(world, pos);
//        if (tile == null) return EnumActionResult.PASS;
//        EnumActionResult result = EnumActionResult.PASS;
//        if (tile.hasCapability(GTCapabilities.CONFIGURABLE, facing)) {
//            Direction targetSide = Utils.getInteractSide(facing, hitX, hitY, hitZ);
//            IInteractHandler interactHandler = tile.getCapability(GTCapabilities.CONFIGURABLE, targetSide);
//            if (interactHandler != null) {
//                if (type != null && interactHandler.onInteract(player, hand, targetSide, type)) {
//                    damage(stack, type.getDamageCrafting(), player, true);
//                    result = EnumActionResult.SUCCESS;
//                }
//            }
//        }
//        if (tile.hasCapability(GTCapabilities.COVERABLE, facing)) {
//            Direction targetSide = Utils.getInteractSide(facing, hitX, hitY, hitZ);
//            ICoverHandler coverHandler = tile.getCapability(GTCapabilities.COVERABLE, targetSide);
//            if (coverHandler != null) {
//                if (type != null && coverHandler.onInteract(player, hand, targetSide, type)) {
//                    damage(stack, type.getDamageCrafting(), player, true);
//                    result = EnumActionResult.SUCCESS;
//                }
//            }
//        }
//        return result;
//    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (!type.isPowered()) {
            return super.damageItem(stack, amount, entity, onBroken);
        }
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return 0;
        }
        return damage(stack, amount);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return getTier(stack).getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return !type.isPowered() && getTier(toRepair).getRepairIngredient().test(repair);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (type.getActualTags().contains(BlockTags.MINEABLE_WITH_AXE) && enchantment.category == EnchantmentCategory.WEAPON) {
            return true;
        }
        return type.isPowered() ? enchantment != Enchantments.UNBREAKING : super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public boolean hasContainerItem(ItemStack stack) {
        return type.hasContainer();
    }

    public ItemStack getContainerItem(ItemStack oldStack) {
        return getGenericContainerItem(oldStack);
    }


    @Override
    public int getBarColor(ItemStack stack) {
        // return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
        if (type.isPowered()) return getCurrentEnergy(stack) > 0 ? 0x00BFFF : super.getBarColor(stack);
        return super.getBarColor(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!type.isPowered()) return super.getBarWidth(stack);
        long currentEnergy = getCurrentEnergy(stack);
        if (currentEnergy > 0) {
            double maxAmount = getMaxEnergy(stack), difference = maxAmount - currentEnergy;
            return (int)( 13*(difference / maxAmount));
        }
        return super.getBarWidth(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (type.isPowered()) return true;
        return super.isBarVisible(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (type.isPowered()) {
            //TODO: not lv
            return new ItemEnergyHandler.Provider(() -> new ToolEnergyHandler(stack, maxEnergy, 8 * (int) Math.pow(4, this.energyTier), 8 * (int) Math.pow(4, this.energyTier), 1, 1));
        }
        return null;
    }

    private LazyOptional<ToolEnergyHandler> getCastedHandler(ItemStack stack) {
        return stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY).cast();
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag nbt = super.getShareTag(stack);
        CompoundTag inner = getCastedHandler(stack).map(ItemEnergyHandler::serializeNBT).orElse(null);
        if (inner != null) {
            if (nbt == null) nbt = new CompoundTag();
            if (nbt.contains(Ref.TAG_TOOL_DATA)) {
                nbt.getCompound(Ref.TAG_TOOL_DATA).merge(inner);
            } else {
                nbt.put(Ref.TAG_TOOL_DATA, inner);
            }
        }
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null) {
            getCastedHandler(stack).ifPresent(t -> t.deserializeNBT(nbt.getCompound(Ref.TAG_TOOL_DATA)));
        }
    }
}