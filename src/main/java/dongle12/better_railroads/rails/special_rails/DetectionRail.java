package dongle12.better_railroads.rails.special_rails;

import java.lang.reflect.Field;

import dongle12.better_railroads.BetterRailroads;
import dongle12.better_railroads.util.ConfigHandler;
import dongle12.better_railroads.util.RailUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DetectionRail extends BlockRailDetector {
	
	final ItemBlock itemBlock;
    //public static final PropertyEnum<BlockRailBase.EnumRailDirection> SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class);
    
	public DetectionRail(){
		super();
        //setDefaultState(this.blockState.getBaseState().withProperty(getShapeProperty(), BlockRailBase.EnumRailDirection.NORTH_SOUTH));
        setUnlocalizedName(BetterRailroads.MODID + "." + "detection_rail");
        setRegistryName(new ResourceLocation(BetterRailroads.MODID, "detection_rail"));
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(new ResourceLocation(BetterRailroads.MODID, "detection_rail"));
        itemBlock.setUnlocalizedName(BetterRailroads.MODID + "." + "detection_rail");
        setCreativeTab(BetterRailroads.miscRailsTab);
		setHardness(0.7f);
        setSoundType(SoundType.WOOD);
	}
	
    @Override
    public Material getMaterial(IBlockState state)
    {
        return Material.WOOD;
    }

	//Registers the model
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
	
    //Changes the speed of the rail
	@Override
	public float getRailMaxSpeed(World world, EntityMinecart cart, BlockPos pos){
		
		//The default speed of a rail that 
		float retSpeed = 0.4f;
		//Look for Slopes upward
		for(EnumFacing side : EnumFacing.HORIZONTALS){
			//Look at the next block
			BlockPos nextPos = pos.offset(side);
			//Boolean to see if a rail has been found
			boolean foundRail = RailUtil.FoundRail(world, nextPos);
			//If no rail is found, check the next block up or down to find a rail
			if(!foundRail){
				if(RailUtil.FoundRail(world, nextPos.down())){
					foundRail = true;
					nextPos = nextPos.up();
				}
				else if(RailUtil.FoundRail(world,nextPos.down())){
					foundRail = true;
					nextPos = nextPos.down();
				}
			}
			if(foundRail){
				//Get the current direction of the rail found 
				BlockRailBase.EnumRailDirection dir = RailUtil.RailDirection(world, cart, pos);
				BlockRailBase.EnumRailDirection dirNext = RailUtil.RailDirection(world, cart, nextPos);

				//If the direction is not null, and the rail is ascending, start to slow
				if((dir != null && dir.isAscending()) || (dirNext != null && dirNext.isAscending())){
					retSpeed = .4f;//super.getRailMaxSpeed(world, cart, pos);
				}
				else{
					retSpeed = super.getRailMaxSpeed(world, cart, pos) * ConfigHandler.SPEED_RAIL_MULTIPLIER;
				}
			}
		}
		return retSpeed;
		
	}
	
	
	////////
	//This is just a test to see about the villages
	////////
	@Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
		super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
        if (!worldIn.isRemote)
        {
        	VillageCollection villageCollection;
        	villageCollection = worldIn.getVillageCollection();
        	if(villageCollection.getNearestVillage(pos, 10) != null){
        		System.out.println("this was actaully in a village");
        	}

        }
    }
	
	//Gets the item block
    public ItemBlock getItemBlock(){
        return itemBlock;
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //EVERYTHING BELOW ALLOWS THE RAILS TO FLOAT AND GIVES THEM BETTER BOUNDING BOXES AND COLLISION BOXES//
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final AxisAlignedBB FLAT_BOUNDING = new AxisAlignedBB(0f, 0f, 0f, 1.0f, .45f, 1.0f);
    private static final AxisAlignedBB ASCENDING_BOUNDING = new AxisAlignedBB(0f, 0f, 0f, 1.0f, .85f, 1.0f);
    private static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0f, 0f, 0f, 1.0f, -0.15f, 1.0f);
    
	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos){
		boolean railFound = RailUtil.FoundRail(world, pos.east()) || RailUtil.FoundRail(world, pos.west()) || RailUtil.FoundRail(world, pos.north()) || RailUtil.FoundRail(world, pos.south());
		boolean blockFound = RailUtil.IsBlock(world, pos.east()) || RailUtil.IsBlock(world, pos.west()) || RailUtil.IsBlock(world, pos.north()) || RailUtil.IsBlock(world, pos.south());
		if(RailUtil.FoundRail(world, pos.down())){
			return false;
		}
		else if(blockFound || railFound){
			return true;
		}
		else{
			return world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
		}
    }
	
	//USED FOR FLOATING
	@Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (!worldIn.isRemote)
        {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = getRailDirection(worldIn, pos, worldIn.getBlockState(pos), null);
            boolean flag = false;

           // if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP))
           // {
           //     flag = true;
           // }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_EAST && !worldIn.getBlockState(pos.east()).isSideSolid(worldIn, pos.east(), EnumFacing.UP))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_WEST && !worldIn.getBlockState(pos.west()).isSideSolid(worldIn, pos.west(), EnumFacing.UP))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_NORTH && !worldIn.getBlockState(pos.north()).isSideSolid(worldIn, pos.north(), EnumFacing.UP))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_SOUTH && !worldIn.getBlockState(pos.south()).isSideSolid(worldIn, pos.south(), EnumFacing.UP))
            {
                flag = true;
            }

           // if (flag && !worldIn.isAirBlock(pos))
           // {
            //    this.dropBlockAsItem(worldIn, pos, state, 0);
           //     worldIn.setBlockToAir(pos);
           // }
            else
            {
                this.updateState(state, worldIn, pos, blockIn);
            }
        }
    }
		
	//USED FOR FLOATING
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	//USED FOR FLOATING
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	//BETTER COLLISION BOXES
	@Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getBlock() == this ? getRailDirection(source, pos, state, null) : null;
        return blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending() ? ASCENDING_BOUNDING : FLAT_AABB;
    }
	//BETTER COLLISION BOXES
    @Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return COLLISION_BOX;
	}
	
	
    ////////////////////////////////////////////////////////
    //items below are needed to extend from BlockRailBase//
    ////////////////////////////////////////////////////////
    

    
}
