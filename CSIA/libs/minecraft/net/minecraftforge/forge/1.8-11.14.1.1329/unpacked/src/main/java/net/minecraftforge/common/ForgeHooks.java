package net.minecraftforge.common;

import static net.minecraft.init.Blocks.diamond_block;
import static net.minecraft.init.Blocks.diamond_ore;
import static net.minecraft.init.Blocks.emerald_block;
import static net.minecraft.init.Blocks.emerald_ore;
import static net.minecraft.init.Blocks.gold_block;
import static net.minecraft.init.Blocks.gold_ore;
import static net.minecraft.init.Blocks.lit_redstone_ore;
import static net.minecraft.init.Blocks.redstone_ore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;
public class ForgeHooks
{
    static class SeedEntry extends WeightedRandom.Item
    {
        public final ItemStack seed;
        public SeedEntry(ItemStack seed, int weight)
        {
            super(weight);
            this.seed = seed;
        }
    }
    static final List<SeedEntry> seedList = new ArrayList<SeedEntry>();

    public static ItemStack getGrassSeed(Random rand)
    {
        SeedEntry entry = (SeedEntry)WeightedRandom.func_76271_a(rand, seedList);
        if (entry == null || entry.seed == null)
        {
            return null;
        }
        return entry.seed.func_77946_l();
    }

    private static boolean toolInit = false;
    //static HashSet<List> toolEffectiveness = new HashSet<List>();

    public static boolean canHarvestBlock(Block block, EntityPlayer player, IBlockAccess world, BlockPos pos)
    {
        if (block.func_149688_o().func_76229_l())
        {
            return true;
        }

        ItemStack stack = player.field_71071_by.func_70448_g();
        IBlockState state = world.func_180495_p(pos);
        state = state.func_177230_c().func_176221_a(state, world, pos);
        String tool = block.getHarvestTool(state);
        if (stack == null || tool == null)
        {
            return player.func_146099_a(block);
        }

        int toolLevel = stack.func_77973_b().getHarvestLevel(stack, tool);
        if (toolLevel < 0)
        {
            return player.func_146099_a(block);
        }

        return toolLevel >= block.getHarvestLevel(state);
    }

    public static boolean canToolHarvestBlock(IBlockAccess world, BlockPos pos, ItemStack stack)
    {
        IBlockState state = world.func_180495_p(pos);
        state = state.func_177230_c().func_176221_a(state, world, pos);
        String tool = state.func_177230_c().getHarvestTool(state);
        if (stack == null || tool == null) return false;
        return stack.func_77973_b().getHarvestLevel(stack, tool) >= state.func_177230_c().getHarvestLevel(state);
    }

    public static float blockStrength(IBlockState state, EntityPlayer player, World world, BlockPos pos)
    {
        float hardness = state.func_177230_c().func_176195_g(world, pos);
        if (hardness < 0.0F)
        {
            return 0.0F;
        }

        if (!canHarvestBlock(state.func_177230_c(), player, world, pos))
        {
            return player.getBreakSpeed(state, pos) / hardness / 100F;
        }
        else
        {
            return player.getBreakSpeed(state, pos) / hardness / 30F;
        }
    }

    public static boolean isToolEffective(IBlockAccess world, BlockPos pos, ItemStack stack)
    {
        IBlockState state = world.func_180495_p(pos);
        state = state.func_177230_c().func_176221_a(state, world, pos);
        for (String type : stack.func_77973_b().getToolClasses(stack))
        {
            if (state.func_177230_c().isToolEffective(type, state))
                return true;
        }
        return false;
    }

    static void initTools()
    {
        if (toolInit)
        {
            return;
        }
        toolInit = true;

        Set<Block> blocks = ReflectionHelper.getPrivateValue(ItemPickaxe.class, null, 0);
        for (Block block : blocks)
        {
            block.setHarvestLevel("pickaxe", 0);
        }

        blocks = ReflectionHelper.getPrivateValue(ItemSpade.class, null, 0);
        for (Block block : blocks)
        {
            block.setHarvestLevel("shovel", 0);
        }

        blocks = ReflectionHelper.getPrivateValue(ItemAxe.class, null, 0);
        for (Block block : blocks)
        {
            block.setHarvestLevel("axe", 0);
        }

        Blocks.field_150343_Z.setHarvestLevel("pickaxe", 3);
        for (Block block : new Block[]{field_150412_bA, field_150475_bE, field_150482_ag, field_150484_ah, field_150352_o, field_150340_R, field_150450_ax, field_150439_ay})
        {
            block.setHarvestLevel("pickaxe", 2);
        }
        Blocks.field_150366_p.setHarvestLevel("pickaxe", 1);
        Blocks.field_150339_S.setHarvestLevel("pickaxe", 1);
        Blocks.field_150369_x.setHarvestLevel("pickaxe", 1);
        Blocks.field_150368_y.setHarvestLevel("pickaxe", 1);
        Blocks.field_150449_bY.setHarvestLevel("pickaxe", 0);
    }

    public static int getTotalArmorValue(EntityPlayer player)
    {
        int ret = 0;
        for (int x = 0; x < player.field_71071_by.field_70460_b.length; x++)
        {
            ItemStack stack = player.field_71071_by.field_70460_b[x];
            if (stack != null && stack.func_77973_b() instanceof ISpecialArmor)
            {
                ret += ((ISpecialArmor)stack.func_77973_b()).getArmorDisplay(player, stack, x);
            }
            else if (stack != null && stack.func_77973_b() instanceof ItemArmor)
            {
                ret += ((ItemArmor)stack.func_77973_b()).field_77879_b;
            }
        }
        return ret;
    }

    static
    {
        seedList.add(new SeedEntry(new ItemStack(Items.field_151014_N), 10));
        initTools();
    }

    /**
     * Called when a player uses 'pick block', calls new Entity and Block hooks.
     */
    public static boolean onPickBlock(MovingObjectPosition target, EntityPlayer player, World world)
    {
        ItemStack result = null;
        boolean isCreative = player.field_71075_bZ.field_75098_d;
        TileEntity te = null;

        if (target.field_72313_a == MovingObjectType.BLOCK)
        {
            IBlockState state = world.func_180495_p(target.func_178782_a());

            if (state.func_177230_c().isAir(world, target.func_178782_a()))
            {
                return false;
            }

            if (isCreative && GuiScreen.func_146271_m())
                te = world.func_175625_s(target.func_178782_a());

            result = state.func_177230_c().getPickBlock(target, world, target.func_178782_a());
        }
        else
        {
            if (target.field_72313_a != MovingObjectType.ENTITY || target.field_72308_g == null || !isCreative)
            {
                return false;
            }

            result = target.field_72308_g.getPickedResult(target);
        }

        if (result == null)
        {
            return false;
        }

        if (te != null)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            te.func_145841_b(nbt);
            result.func_77983_a("BlockEntityTag", nbt);

            NBTTagCompound display = new NBTTagCompound();
            result.func_77983_a("display", display);

            NBTTagList lore = new NBTTagList();
            display.func_74782_a("Lore", lore);
            lore.func_74742_a(new NBTTagString("(+NBT)"));
        }

        for (int x = 0; x < 9; x++)
        {
            ItemStack stack = player.field_71071_by.func_70301_a(x);
            if (stack != null && stack.func_77969_a(result) && ItemStack.func_77970_a(stack, result))
            {
                player.field_71071_by.field_70461_c = x;
                return true;
            }
        }

        if (!isCreative)
        {
            return false;
        }

        int slot = player.field_71071_by.func_70447_i();
        if (slot < 0 || slot >= 9)
        {
            slot = player.field_71071_by.field_70461_c;
        }

        player.field_71071_by.func_70299_a(slot, result);
        player.field_71071_by.field_70461_c = slot;
        return true;
    }

    //Optifine Helper Functions u.u, these are here specifically for Optifine
    //Note: When using Optfine, these methods are invoked using reflection, which
    //incurs a major performance penalty.
    public static void onLivingSetAttackTarget(EntityLivingBase entity, EntityLivingBase target)
    {
        MinecraftForge.EVENT_BUS.post(new LivingSetAttackTargetEvent(entity, target));
    }

    public static boolean onLivingUpdate(EntityLivingBase entity)
    {
        return MinecraftForge.EVENT_BUS.post(new LivingUpdateEvent(entity));
    }

    public static boolean onLivingAttack(EntityLivingBase entity, DamageSource src, float amount)
    {
        return !MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(entity, src, amount));
    }

    public static float onLivingHurt(EntityLivingBase entity, DamageSource src, float amount)
    {
        LivingHurtEvent event = new LivingHurtEvent(entity, src, amount);
        return (MinecraftForge.EVENT_BUS.post(event) ? 0 : event.ammount);
    }

    public static boolean onLivingDeath(EntityLivingBase entity, DamageSource src)
    {
        return MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
    }

    public static boolean onLivingDrops(EntityLivingBase entity, DamageSource source, ArrayList<EntityItem> drops, int lootingLevel, boolean recentlyHit)
    {
        return MinecraftForge.EVENT_BUS.post(new LivingDropsEvent(entity, source, drops, lootingLevel, recentlyHit));
    }

    public static float[] onLivingFall(EntityLivingBase entity, float distance, float damageMultiplier)
    {
        LivingFallEvent event = new LivingFallEvent(entity, distance, damageMultiplier);
        return (MinecraftForge.EVENT_BUS.post(event) ? null : new float[]{event.distance, event.damageMultiplier});
    }

    public static boolean isLivingOnLadder(Block block, World world, BlockPos pos, EntityLivingBase entity)
    {
        boolean isSpectator = (entity instanceof EntityPlayer && ((EntityPlayer)entity).func_175149_v());
        if (isSpectator) return false;
        if (!ForgeModContainer.fullBoundingBoxLadders)
        {
            return block != null && block.isLadder(world, pos, entity);
        }
        else
        {
            AxisAlignedBB bb = entity.func_174813_aQ();
            int mX = MathHelper.func_76128_c(bb.field_72340_a);
            int mY = MathHelper.func_76128_c(bb.field_72338_b);
            int mZ = MathHelper.func_76128_c(bb.field_72339_c);
            for (int y2 = mY; y2 < bb.field_72337_e; y2++)
            {
                for (int x2 = mX; x2 < bb.field_72336_d; x2++)
                {
                    for (int z2 = mZ; z2 < bb.field_72334_f; z2++)
                    {
                        BlockPos tmp = new BlockPos(x2, y2, z2);
                        if (world.func_180495_p(tmp).func_177230_c().isLadder(world, tmp, entity))
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public static void onLivingJump(EntityLivingBase entity)
    {
        MinecraftForge.EVENT_BUS.post(new LivingJumpEvent(entity));
    }

    public static EntityItem onPlayerTossEvent(EntityPlayer player, ItemStack item, boolean includeName)
    {
        player.captureDrops = true;
        EntityItem ret = player.func_146097_a(item, false, includeName);
        player.capturedDrops.clear();
        player.captureDrops = false;

        if (ret == null)
        {
            return null;
        }

        ItemTossEvent event = new ItemTossEvent(ret, player);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            return null;
        }

        player.func_71012_a(event.entityItem);
        return event.entityItem;
    }

    public static float getEnchantPower(World world, BlockPos pos)
    {
        return world.func_180495_p(pos).func_177230_c().getEnchantPowerBonus(world, pos);
    }

    public static ChatComponentTranslation onServerChatEvent(NetHandlerPlayServer net, String raw, ChatComponentTranslation comp)
    {
        ServerChatEvent event = new ServerChatEvent(net.field_147369_b, raw, comp);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            return null;
        }
        return event.component;
    }


    static final Pattern URL_PATTERN = Pattern.compile(
            //         schema                          ipv4            OR           namespace                 port     path         ends
            //   |-----------------|        |-------------------------|  |----------------------------|    |---------| |--|   |---------------|
            "((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_\\.]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
            Pattern.CASE_INSENSITIVE);

    public static IChatComponent newChatWithLinks(String string)
    {
        // Includes ipv4 and domain pattern
        // Matches an ip (xx.xxx.xx.xxx) or a domain (something.com) with or
        // without a protocol or path.
        IChatComponent ichat = new ChatComponentText("");
        Matcher matcher = URL_PATTERN.matcher(string);
        int lastEnd = 0;
        String remaining = string;

        // Find all urls
        while (matcher.find())
        {
            int start = matcher.start();
            int end = matcher.end();

            // Append the previous left overs.
            ichat.func_150258_a(string.substring(lastEnd, start));
            lastEnd = end;
            String url = string.substring(start, end);
            IChatComponent link = new ChatComponentText(url);

            try
            {
                // Add schema so client doesn't crash.
                if ((new URI(url)).getScheme() == null)
                    url = "http://" + url;
            }
            catch (URISyntaxException e)
            {
                // Bad syntax bail out!
                ichat.func_150258_a(url);
                continue;
            }

            // Set the click event and append the link.
            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            link.func_150256_b().func_150241_a(click);
            ichat.func_150257_a(link);
        }

        // Append the rest of the message.
        ichat.func_150258_a(string.substring(lastEnd));
        return ichat;
    }

    public static boolean canInteractWith(EntityPlayer player, Container openContainer)
    {
        PlayerOpenContainerEvent event = new PlayerOpenContainerEvent(player, openContainer);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() == Event.Result.DEFAULT ? event.canInteractWith : event.getResult() == Event.Result.ALLOW ? true : false;
    }

    public static int onBlockBreakEvent(World world, GameType gameType, EntityPlayerMP entityPlayer, BlockPos pos)
    {
        // Logic from tryHarvestBlock for pre-canceling the event
        boolean preCancelEvent = false;
        if (gameType.func_77145_d() && entityPlayer.func_70694_bm() != null && entityPlayer.func_70694_bm().func_77973_b() instanceof ItemSword)
            preCancelEvent = true;

        if (gameType.func_82752_c())
        {
            if (gameType == WorldSettings.GameType.SPECTATOR)
                preCancelEvent = true;

            if (!entityPlayer.func_175142_cm())
            {
                ItemStack itemstack = entityPlayer.func_71045_bC();
                if (itemstack == null || !itemstack.func_179544_c(world.func_180495_p(pos).func_177230_c()))
                    preCancelEvent = true;
            }
        }

        // Tell client the block is gone immediately then process events
        if (world.func_175625_s(pos) == null)
        {
            S23PacketBlockChange packet = new S23PacketBlockChange(world, pos);
            packet.field_148883_d = Blocks.field_150350_a.func_176223_P();
            entityPlayer.field_71135_a.func_147359_a(packet);
        }

        // Post the block break event
        IBlockState state = world.func_180495_p(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer);
        event.setCanceled(preCancelEvent);
        MinecraftForge.EVENT_BUS.post(event);

        // Handle if the event is canceled
        if (event.isCanceled())
        {
            // Let the client know the block still exists
            entityPlayer.field_71135_a.func_147359_a(new S23PacketBlockChange(world, pos));

            // Update any tile entity data for this block
            TileEntity tileentity = world.func_175625_s(pos);
            if (tileentity != null)
            {
                Packet pkt = tileentity.func_145844_m();
                if (pkt != null)
                {
                    entityPlayer.field_71135_a.func_147359_a(pkt);
                }
            }
        }
        return event.isCanceled() ? -1 : event.getExpToDrop();
    }

    public static boolean onPlaceItemIntoWorld(ItemStack itemstack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        // handle all placement events here
        int meta = itemstack.func_77952_i();
        int size = itemstack.field_77994_a;
        NBTTagCompound nbt = null;
        if (itemstack.func_77978_p() != null)
        {
            nbt = (NBTTagCompound)itemstack.func_77978_p().func_74737_b();
        }

        if (!(itemstack.func_77973_b() instanceof ItemBucket)) // if not bucket
        {
            world.captureBlockSnapshots = true;
        }

        boolean flag = itemstack.func_77973_b().func_180614_a(itemstack, player, world, pos, side, hitX, hitY, hitZ);
        world.captureBlockSnapshots = false;

        if (flag)
        {
            // save new item data
            int newMeta = itemstack.func_77952_i();
            int newSize = itemstack.field_77994_a;
            NBTTagCompound newNBT = null;
            if (itemstack.func_77978_p() != null)
            {
                newNBT = (NBTTagCompound)itemstack.func_77978_p().func_74737_b();
            }
            net.minecraftforge.event.world.BlockEvent.PlaceEvent placeEvent = null;
            List<net.minecraftforge.common.util.BlockSnapshot> blockSnapshots = (List<BlockSnapshot>)world.capturedBlockSnapshots.clone();
            world.capturedBlockSnapshots.clear();

            // make sure to set pre-placement item data for event
            itemstack.func_77964_b(meta);
            itemstack.field_77994_a = size;
            if (nbt != null)
            {
                itemstack.func_77982_d(nbt);
            }
            if (blockSnapshots.size() > 1)
            {
                placeEvent = ForgeEventFactory.onPlayerMultiBlockPlace(player, blockSnapshots, side);
            }
            else if (blockSnapshots.size() == 1)
            {
                placeEvent = ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshots.get(0), side);
            }

            if (placeEvent != null && (placeEvent.isCanceled()))
            {
                flag = false; // cancel placement
                // revert back all captured blocks
                for (net.minecraftforge.common.util.BlockSnapshot blocksnapshot : blockSnapshots)
                {
                    world.restoringBlockSnapshots = true;
                    blocksnapshot.restore(true, false);
                    world.restoringBlockSnapshots = false;
                }
            }
            else
            {
                // Change the stack to its new content
                itemstack.func_77964_b(newMeta);
                itemstack.field_77994_a = newSize;
                if (nbt != null)
                {
                    itemstack.func_77982_d(newNBT);
                }

                for (BlockSnapshot snap : blockSnapshots)
                {
                    int updateFlag = snap.flag;
                    IBlockState oldBlock = snap.replacedBlock;
                    IBlockState newBlock = world.func_180495_p(snap.pos);
                    if (newBlock != null && !(newBlock.func_177230_c().hasTileEntity(newBlock))) // Containers get placed automatically
                    {
                        newBlock.func_177230_c().func_176213_c(world, snap.pos, newBlock);
                    }

                    world.markAndNotifyBlock(snap.pos, null, oldBlock, newBlock, updateFlag);
                }
                player.func_71064_a(StatList.field_75929_E[Item.func_150891_b(itemstack.func_77973_b())], 1);
            }
        }
        world.capturedBlockSnapshots.clear();

        return flag;
    }

    public static boolean onAnvilChange(ContainerRepair container, ItemStack left, ItemStack right, IInventory outputSlot, String name, int baseCost)
    {
        AnvilUpdateEvent e = new AnvilUpdateEvent(left, right, name, baseCost);
        if (MinecraftForge.EVENT_BUS.post(e)) return false;
        if (e.output == null) return true;

        outputSlot.func_70299_a(0, e.output);
        container.field_82854_e = e.cost;
        container.field_82856_l = e.materialCost;
        return false;
    }

    public static float onAnvilRepair(EntityPlayer player, ItemStack output, ItemStack left, ItemStack right)
    {
        AnvilRepairEvent e = new AnvilRepairEvent(player, left, right, output);
        MinecraftForge.EVENT_BUS.post(e);
        return e.breakChance;
    }

    public static boolean onNoteChange(TileEntityNote te, byte old)
    {
        NoteBlockEvent.Change e = new NoteBlockEvent.Change(te.func_145831_w(), te.func_174877_v(), te.func_145831_w().func_180495_p(te.func_174877_v()), old, te.field_145879_a);
        if (MinecraftForge.EVENT_BUS.post(e))
        {
            te.field_145879_a = old;
            return false;
        }
        te.field_145879_a = (byte)e.getVanillaNoteId();
        return true;
    }

    /**
     * Default implementation of IRecipe.func_179532_b {getRemainingItems} because
     * this is just copy pasted over a lot of recipes.
     *
     * Another use case for java 8 but sadly we can't use it!
     *
     * @param inv Crafting inventory
     * @return Crafting inventory contents after the recipe.
     */
    public static ItemStack[] defaultRecipeGetRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] ret = new ItemStack[inv.func_70302_i_()];
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = getContainerItem(inv.func_70301_a(i));
        }
        return ret;
    }

    private static ThreadLocal<EntityPlayer> craftingPlayer = new ThreadLocal<EntityPlayer>();
    public static void setCraftingPlayer(EntityPlayer player)
    {
        craftingPlayer.set(player);
    }
    public static EntityPlayer getCraftingPlayer()
    {
        return craftingPlayer.get();
    }
    public static ItemStack getContainerItem(ItemStack stack)
    {
        if (stack == null) return null;

        if (stack.func_77973_b().hasContainerItem(stack))
        {
            stack = stack.func_77973_b().getContainerItem(stack);
            if (stack != null && stack.func_77984_f() && stack.func_77960_j() > stack.func_77958_k())
            {
                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(craftingPlayer.get(), stack));
                return null;
            }
            return stack;
        }
        return null;
    }

    public static WorldGeneratorBonusChest getBonusChest(Random rand)
    {
        return new WorldGeneratorBonusChest(ChestGenHooks.getItems(ChestGenHooks.BONUS_CHEST, rand), ChestGenHooks.getCount(ChestGenHooks.BONUS_CHEST, rand));
    }

    public static boolean isInsideOfMaterial(Material material, Entity entity, BlockPos pos)
    {
        IBlockState state = entity.field_70170_p.func_180495_p(pos);
        Block block = state.func_177230_c();
        double eyes = entity.field_70163_u + (double)entity.func_70047_e();

        double filled = 1.0f; //If it's not a liquid assume it's a solid block
        if (block instanceof IFluidBlock)
        {
            filled = ((IFluidBlock)block).getFilledPercentage(entity.field_70170_p, pos);
        }
        else if (block instanceof BlockLiquid)
        {
            filled = BlockLiquid.func_149801_b(block.func_176201_c(state));
        }

        if (filled < 0)
        {
            filled *= -1;
            //filled -= 0.11111111F; //Why this is needed.. not sure...
            return eyes > (double)(pos.func_177956_o() + 1 + (1 - filled));
        }
        else
        {
            return eyes < (double)(pos.func_177956_o() + 1 + filled);
        }
    }

    public static boolean onPlayerAttackTarget(EntityPlayer player, Entity target)
    {
        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, target))) return false;
        ItemStack stack = player.func_71045_bC();
        if (stack != null && stack.func_77973_b().onLeftClickEntity(stack, player, target)) return false;
        return true;
    }
}
