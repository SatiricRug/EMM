package net.minecraftforge.fml.common.network.internal;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage.OpenGui;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OpenGuiHandler extends SimpleChannelInboundHandler<FMLMessage.OpenGui> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final OpenGui msg) throws Exception
    {
        IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
        if (thread.func_152345_ab())
        {
            process(msg);
        }
        else
        {
            thread.func_152344_a(new Runnable()
            {
                public void run()
                {
                    OpenGuiHandler.this.process(msg);
                }
            });
        }
    }

    private void process(OpenGui msg)
    {
        EntityPlayer player = FMLClientHandler.instance().getClient().field_71439_g;
        player.openGui(msg.modId, msg.modGuiId, player.field_70170_p, msg.x, msg.y, msg.z);
        player.field_71070_bA.field_75152_c = msg.windowId;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log(Level.ERROR, cause, "OpenGuiHandler exception");
        super.exceptionCaught(ctx, cause);
    }

}
