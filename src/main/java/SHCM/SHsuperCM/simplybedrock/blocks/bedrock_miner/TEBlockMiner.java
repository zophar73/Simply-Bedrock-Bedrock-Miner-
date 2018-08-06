package SHCM.SHsuperCM.simplybedrock.blocks.bedrock_miner;

import SHCM.SHsuperCM.simplybedrock.SimplyBedrock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ITickable;

import javax.annotation.Nullable;

public class TEBlockMiner extends TileEntityLockable implements ITickable, IInventory {

    public ItemStack fuelItem = ItemStack.EMPTY;
    public int fuelAmount = 0;
    public int progress = 0;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        fuelItem = new ItemStack(compound.getCompoundTag("FuelItem"));
        fuelAmount = compound.getInteger("FuelAmount");
        progress = compound.getInteger("Progress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbtFuelItem = new NBTTagCompound();
        fuelItem.writeToNBT(nbtFuelItem);
        compound.setTag("FuelItem",nbtFuelItem);
        compound.setInteger("FuelAmount", fuelAmount);
        compound.setInteger("Progress", progress);
        return super.writeToNBT(compound);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos,999,getUpdateTag());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        fuelAmount = tag.getInteger("FuelAmount");
        progress = tag.getInteger("Progress");
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        compound.setInteger("FuelAmount", fuelAmount);
        compound.setInteger("Progress", progress);
        return compound;
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            if(progress >= 6000) {
                world.setBlockState(pos.down(),Blocks.AIR.getDefaultState());
                progress = 0;
            } else if (world.getBlockState(getPos().down()).getBlock() == Blocks.BEDROCK) {
                if (fuelAmount > 0) {
                    progress++;
                    fuelAmount--;
                } else {
                    int fuel = TileEntityFurnace.getItemBurnTime(fuelItem);
                    if(fuel > 0) {
                        fuelAmount += fuel;
                        decrStackSize(0,-1);
                    }
                }
            } else
                progress = 0;

            sync();
        }
    }

    private void sync() {
        world.markBlockRangeForRenderUpdate(pos, pos);
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
        markDirty();
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return fuelItem.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return fuelItem;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        fuelItem.setCount(count < 0 ? fuelItem.getCount() + count : count);
        return fuelItem;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack item = fuelItem;
        fuelItem = ItemStack.EMPTY;
        return item;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        fuelItem = stack;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem().getItemBurnTime(stack) > 0;
    }

    @Override
    public int getField(int id) {return 0;}
    @Override
    public void setField(int id, int value) {}
    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {fuelItem = ItemStack.EMPTY;}

    @Override
    public String getName() {
        return "Bedrock Miner";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerBlockMiner(this, playerInventory);
    }

    @Override
    public String getGuiID() {
        return SimplyBedrock.MODID + ":bedrock_miner_container";
    }
}