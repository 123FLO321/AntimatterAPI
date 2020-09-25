package muramasa.antimatter.capability;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Data;
import muramasa.antimatter.Ref;
import muramasa.antimatter.cover.Cover;
import muramasa.antimatter.cover.CoverInstance;
import muramasa.antimatter.tool.AntimatterToolType;
import muramasa.antimatter.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CoverHandler<T extends TileEntity> implements ICoverHandler<T>, ICapabilityHandler {

    protected T tile;
    protected EnumMap<Direction, CoverInstance<T>> covers = new EnumMap<>(Direction.class);
    protected List<String> validCovers = new ObjectArrayList<>();

    public CoverHandler(T tile, Cover... validCovers) {
        this.tile = tile;
        this.validCovers.add(Data.COVER_NONE.getId());
        for (Cover c : validCovers) this.validCovers.add(c.getId());
        for (Direction d : Ref.DIRS) covers.put(d, new CoverInstance<>(Data.COVER_NONE, tile, d));
    }

    @Override
    public boolean set(Direction side, @Nonnull Cover newCover) {
        if (getTileFacing() == side || !isValid(side, newCover)) return false;
        covers.get(side).onRemove(side);
        covers.put(side, new CoverInstance<>(newCover, getTile(), side)); //Emplace newCover, calls onPlace!

        //TODO add newCover.onPlace and newCover.onRemove to customize sounds
        tile.getWorld().playSound(null, tile.getPos(), SoundEvents.BLOCK_METAL_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        Utils.markTileForRenderUpdate(getTile());
        tile.markDirty();
        return true;
    }

    @Override
    public CoverInstance<T> get(Direction side) {
        return covers.get(side); //Should never return null, as COVER_NONE is inserted for every direction
    }

    public CoverInstance<?>[] getAll() {
        return covers.values().toArray(new CoverInstance[0]);
    }

    @Override
    public Direction getTileFacing() {
        return Direction.NORTH;
    }

    @Override
    public T getTile() {
        if (tile == null) throw new NullPointerException("CoverHandler cannot have a null tile");
        return tile;
    }

    @Override
    public void onUpdate() {
        for (Map.Entry<Direction, CoverInstance<T>> e : covers.entrySet()) {
            e.getValue().onUpdate(e.getKey());
        }
    }

    @Override
    public void onRemove() {
        for (Map.Entry<Direction, CoverInstance<T>> e : covers.entrySet()) {
            e.getValue().onRemove(e.getKey());
        }
    }

    @Override
    public boolean onInteract(PlayerEntity player, Hand hand, Direction side, @Nullable AntimatterToolType type) {
        return false;
    }

    @Override
    public boolean placeCover(PlayerEntity player, Direction side, ItemStack stack, Cover cover) {
        if (!get(side).isEmpty() || !set(side, cover)) return false;
        if (!player.isCreative()) stack.shrink(1);
        return true;
    }

    @Override
    public boolean removeCover(PlayerEntity player, Direction side) {
        System.out.println(get(side).getId());
        if (get(side).isEmpty() || !set(side, Data.COVER_NONE)) return false;
        if (!player.isCreative()) player.dropItem(get(side).getCover().getDroppedStack(), false);
        player.playSound(SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    @Override
    public boolean hasCover(@Nonnull Direction side, @Nonnull Cover cover) {
        return get(side).isEqual(cover);
    }

    @Override
    public boolean isValid(@Nonnull Direction side, @Nonnull Cover replacement) {
        return (get(side).isEmpty() || replacement.isEqual(Data.COVER_NONE)) && validCovers.contains(replacement.getId());
    }

    /** NBT **/
    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        byte side = 0;
        for (Map.Entry<Direction, CoverInstance<T>> e : covers.entrySet()) {
            CoverInstance<?> cover = e.getValue();
            if (!cover.isEmpty()) {
                int dir = e.getKey().getIndex();
                side |= (1 << dir);
                CompoundNBT nbt = cover.serialize();
                nbt.putString(Ref.TAG_MACHINE_COVER_ID, cover.getId());
                tag.put(Ref.TAG_MACHINE_COVER_NAME.concat(Integer.toString(dir)), nbt);
            }
        }
        tag.putByte(Ref.TAG_MACHINE_COVER_SIDE, side);
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        byte sides = tag.getByte(Ref.TAG_MACHINE_COVER_SIDE);
        for (int i = 0; i < Ref.DIRS.length; i++) {
            if ((sides & (1 << i)) > 0) {
                CompoundNBT nbt = tag.getCompound(Ref.TAG_MACHINE_COVER_NAME.concat(Integer.toString(i)));
                covers.put(Ref.DIRS[i], new CoverInstance<>(AntimatterAPI.get(Cover.class, nbt.getString(Ref.TAG_MACHINE_COVER_ID)), tile, Ref.DIRS[i])).deserialize(nbt);
            } else {
                covers.put(Ref.DIRS[i], new CoverInstance<>(Data.COVER_NONE, tile, Ref.DIRS[i]));
            }
        }
    }

    @Override
    public Capability<?> getCapability() {
        return AntimatterCaps.COVERABLE_HANDLER_CAPABILITY;
    }
}
