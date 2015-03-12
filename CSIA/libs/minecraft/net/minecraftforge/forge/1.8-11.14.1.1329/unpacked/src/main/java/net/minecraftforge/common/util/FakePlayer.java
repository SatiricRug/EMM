package net.minecraftforge.common.util;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

//Preliminary, simple Fake Player class
public class FakePlayer extends EntityPlayerMP
{
    public FakePlayer(WorldServer world, GameProfile name)
    {
        super(FMLCommonHandler.instance().getMinecraftServerInstance(), world, name, new ItemInWorldManager(world));
    }

    @Override public Vec3 func_174791_d(){ return new Vec3(0, 0, 0); }
    @Override public boolean func_70003_b(int i, String s){ return false; }
    @Override public void func_146105_b(IChatComponent chatmessagecomponent){}
    @Override public void func_71064_a(StatBase par1StatBase, int par2){}
    @Override public void openGui(Object mod, int modGuiId, World world, int x, int y, int z){}
    @Override public boolean func_180431_b(DamageSource source){ return true; }
    @Override public boolean func_96122_a(EntityPlayer player){ return false; }
    @Override public void func_70645_a(DamageSource source){ return; }
    @Override public void func_70071_h_(){ return; }
    @Override public void func_71027_c(int dim){ return; }
    @Override public void func_147100_a(C15PacketClientSettings pkt){ return; }
}
