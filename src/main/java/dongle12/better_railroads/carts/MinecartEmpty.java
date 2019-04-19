package dongle12.better_railroads.carts;

import dongle12.better_railroads.items.ModItems;
import dongle12.better_railroads.util.ConfigHandler;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class MinecartEmpty extends EntityMinecartEmpty{
	
	public boolean connected = false;
	public MinecartEmpty connectedCart;
	boolean leadCart = false;

	public MinecartEmpty(World world, double x, double y, double z) {
		super(world, x, y, z);
	}
	
	public MinecartEmpty(World world){
		super(world);
	}

	@Override
	public float getMaxCartSpeedOnRail(){
		return ConfigHandler.MAX_CART_SPEED;
	}
	
	@Override
    public boolean canBePushed()
    {
        return true;
    }
	
	public void setConnectedCart(MinecartEmpty cart){
		connectedCart = cart;
	}
	
	@Override
	public void onUpdate(){
		super.onUpdate();
		if(connected && !leadCart){
			this.motionX = connectedCart.motionX;
			this.motionZ = connectedCart.motionZ;
		}
	}
    
    public void killMinecart(DamageSource source)
    {
        this.setDead();

        if (this.world.getGameRules().getBoolean("doEntityDrops"))
        {
            ItemStack itemstack = new ItemStack(ModItems.empty_cart, 1);

            if (this.hasCustomName())
            {
                itemstack.setStackDisplayName(this.getCustomNameTag());
            }

            this.entityDropItem(itemstack, 0.0F);
        }
    }
    
}
