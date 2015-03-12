/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     cpw - implementation
 */

package net.minecraftforge.fml.common;

import java.io.File;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * @author cpw
 *
 */
public class FMLContainer extends DummyModContainer implements WorldAccessContainer
{
    public FMLContainer()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId="FML";
        meta.name="Forge Mod Loader";
        meta.version=Loader.instance().getFMLVersionString();
        meta.credits="Made possible with help from many people";
        meta.authorList=Arrays.asList("cpw", "LexManos", "Player");
        meta.description="The Forge Mod Loader provides the ability for systems to load mods " +
                    "from the file system. It also provides key capabilities for mods to be able " +
                    "to cooperate and provide a good modding environment. ";
        meta.url="https://github.com/MinecraftForge/FML/wiki";
        meta.updateUrl="https://github.com/MinecraftForge/FML/wiki";
        meta.screenshots=new String[0];
        meta.logoFile="";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void modConstruction(FMLConstructionEvent evt)
    {
        NetworkRegistry.INSTANCE.register(this, this.getClass(), null, evt.getASMHarvestedData());
        FMLNetworkHandler.registerChannel(this, evt.getSide());
    }

    @NetworkCheckHandler
    public boolean checkModLists(Map<String,String> modList, Side side)
    {
        return Loader.instance().checkRemoteModList(modList,side);
    }
    @Override
    public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info)
    {
        NBTTagCompound fmlData = new NBTTagCompound();
        NBTTagList modList = new NBTTagList();
        for (ModContainer mc : Loader.instance().getActiveModList())
        {
            NBTTagCompound mod = new NBTTagCompound();
            mod.func_74778_a("ModId", mc.getModId());
            mod.func_74778_a("ModVersion", mc.getVersion());
            modList.func_74742_a(mod);
        }
        fmlData.func_74782_a("ModList", modList);

        NBTTagCompound registries = new NBTTagCompound();
        fmlData.func_74782_a("Registries", registries);
        FMLLog.fine("Gathering id map for writing to world save %s", info.func_76065_j());
        GameData.GameDataSnapshot dataSnapshot = GameData.takeSnapshot();

        for (Map.Entry<String, GameData.GameDataSnapshot.Entry> e : dataSnapshot.entries.entrySet())
        {
            NBTTagCompound data = new NBTTagCompound();
            registries.func_74782_a(e.getKey(), data);

            NBTTagList ids = new NBTTagList();
            for (Entry<String, Integer> item : e.getValue().ids.entrySet())
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.func_74778_a("K", item.getKey());
                tag.func_74768_a("V", item.getValue());
                ids.func_74742_a(tag);
            }
            data.func_74782_a("ids", ids);

            NBTTagList aliases = new NBTTagList();
            for (Entry<String, String> entry : e.getValue().aliases.entrySet())
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.func_74778_a("K", entry.getKey());
                tag.func_74778_a("V", entry.getValue());
                aliases.func_74742_a(tag);
            }
            data.func_74782_a("aliases", aliases);

            NBTTagList subs = new NBTTagList();
            for (String entry : e.getValue().substitutions)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.func_74778_a("K", entry);
                subs.func_74742_a(tag);
            }
            data.func_74782_a("substitutions", subs);

            int[] blocked = new int[e.getValue().blocked.size()];
            int idx = 0;
            for (Integer i : e.getValue().blocked)
            {
                blocked[idx++] = i;
            }
            data.func_74783_a("blocked", blocked);
        }
        return fmlData;
    }

    @Override
    public void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag)
    {
        if (tag.func_74764_b("ModList"))
        {
            NBTTagList modList = tag.func_150295_c("ModList", (byte)10);
            for (int i = 0; i < modList.func_74745_c(); i++)
            {
                NBTTagCompound mod = modList.func_150305_b(i);
                String modId = mod.func_74779_i("ModId");
                String modVersion = mod.func_74779_i("ModVersion");
                ModContainer container = Loader.instance().getIndexedModList().get(modId);
                if (container == null)
                {
                    FMLLog.log("fml.ModTracker", Level.ERROR, "This world was saved with mod %s which appears to be missing, things may not work well", modId);
                    continue;
                }
                if (!modVersion.equals(container.getVersion()))
                {
                    FMLLog.log("fml.ModTracker", Level.INFO, "This world was saved with mod %s version %s and it is now at version %s, things may not work well", modId, modVersion, container.getVersion());
                }
            }
        }

        List<String> failedElements = null;

        if (tag.func_74764_b("ModItemData")) // Pre 1.7
        {
            GameData.GameDataSnapshot snapshot = new GameData.GameDataSnapshot();
            GameData.GameDataSnapshot.Entry items = new GameData.GameDataSnapshot.Entry();
            snapshot.entries.put("fml:blocks", new GameData.GameDataSnapshot.Entry());
            snapshot.entries.put("fml:items", items);

            FMLLog.info("Attempting to convert old world data to new system. This may be trouble!");
            NBTTagList modList = tag.func_150295_c("ModItemData", (byte)10);
            for (int i = 0; i < modList.func_74745_c(); i++)
            {
                NBTTagCompound data = modList.func_150305_b(i);
                String forcedModId = data.func_74764_b("ForcedModId") ? data.func_74779_i("ForcedModId") : null;
                String forcedName = data.func_74764_b("ForcedName") ? data.func_74779_i("ForcedName") : null;
                if (forcedName == null)
                {
                    FMLLog.warning("Found unlabelled item in world save, this may cause problems. The item type %s:%d will not be present", data.func_74779_i("ItemType"), data.func_74762_e("ordinal"));
                }
                else
                {
                    // all entries are Items, blocks were only saved through their ItemBlock
                    String itemLabel = String.format("%s:%s", forcedModId != null ? forcedModId : data.func_74779_i("ModId"), forcedName);
                    items.ids.put(itemLabel, data.func_74762_e("ItemId"));
                }
            }
            failedElements = GameData.injectSnapshot(snapshot, true, true);

        }
        else if (tag.func_74764_b("ItemData")) // 1.7
        {
            GameData.GameDataSnapshot snapshot = new GameData.GameDataSnapshot();
            GameData.GameDataSnapshot.Entry blocks = new GameData.GameDataSnapshot.Entry();
            GameData.GameDataSnapshot.Entry items = new GameData.GameDataSnapshot.Entry();
            snapshot.entries.put("fml:blocks", blocks);
            snapshot.entries.put("fml:items", items);

            NBTTagList list = tag.func_150295_c("ItemData", 10);
            for (int i = 0; i < list.func_74745_c(); i++)
            {
                NBTTagCompound e = list.func_150305_b(i);
                String name = e.func_74779_i("K");

                if (name.charAt(0) == '\u0001')
                    blocks.ids.put(name.substring(1), e.func_74762_e("V"));
                else if (name.charAt(0) == '\u0002')
                    items.ids.put(name.substring(1), e.func_74762_e("V"));
            }

            Set<Integer> blockedIds = new HashSet<Integer>();
            if (!tag.func_74764_b("BlockedItemIds")) // no blocked id info -> old 1.7 save
            {
                // old early 1.7 save potentially affected by the registry mapping bug
                // fix the ids the best we can...
                GameData.fixBrokenIds(blocks, items, blockedIds);
            }
            else
            {
                for (int id : tag.func_74759_k("BlockedItemIds"))
                {
                    blockedIds.add(id);
                }
            }
            blocks.blocked.addAll(blockedIds);
            items.blocked.addAll(blockedIds);

            list = tag.func_150295_c("BlockAliases", 10);
            for (int i = 0; i < list.func_74745_c(); i++)
            {
                NBTTagCompound dataTag = list.func_150305_b(i);
                blocks.aliases.put(dataTag.func_74779_i("K"), dataTag.func_74779_i("V"));
            }

            if (tag.func_150297_b("BlockSubstitutions", 9))
            {
                list = tag.func_150295_c("BlockSubstitutions", 10);
                for (int i = 0; i < list.func_74745_c(); i++)
                {
                    NBTTagCompound dataTag = list.func_150305_b(i);
                    blocks.substitutions.add(dataTag.func_74779_i("K"));
                }
            }

            list = tag.func_150295_c("ItemAliases", 10);
            for (int i = 0; i < list.func_74745_c(); i++)
            {
                NBTTagCompound dataTag = list.func_150305_b(i);
                items.aliases.put(dataTag.func_74779_i("K"), dataTag.func_74779_i("V"));
            }

            if (tag.func_150297_b("ItemSubstitutions", 9))
            {
                list = tag.func_150295_c("ItemSubstitutions", 10);
                for (int i = 0; i < list.func_74745_c(); i++)
                {
                    NBTTagCompound dataTag = list.func_150305_b(i);
                    items.substitutions.add(dataTag.func_74779_i("K"));
                }
            }
            failedElements = GameData.injectSnapshot(snapshot, true, true);
        }
        else if (tag.func_74764_b("Registries")) // 1.8, genericed out the 'registries' list
        {
            GameData.GameDataSnapshot snapshot = new GameData.GameDataSnapshot();
            NBTTagCompound regs = tag.func_74775_l("Registries");
            for (String key : (Set<String>)regs.func_150296_c())
            {
                GameData.GameDataSnapshot.Entry entry = new GameData.GameDataSnapshot.Entry();
                snapshot.entries.put(key, entry);

                NBTTagList list = regs.func_74775_l(key).func_150295_c("ids", 10);
                for (int x = 0; x < list.func_74745_c(); x++)
                {
                    NBTTagCompound e = list.func_150305_b(x);
                    entry.ids.put(e.func_74779_i("K"), e.func_74762_e("V"));
                }

                list = regs.func_74775_l(key).func_150295_c("aliases", 10);
                for (int x = 0; x < list.func_74745_c(); x++)
                {
                    NBTTagCompound e = list.func_150305_b(x);
                    entry.aliases.put(e.func_74779_i("K"), e.func_74779_i("V"));
                }

                list = regs.func_74775_l(key).func_150295_c("substitutions", 10);
                for (int x = 0; x < list.func_74745_c(); x++)
                {
                    NBTTagCompound e = list.func_150305_b(x);
                    entry.substitutions.add(e.func_74779_i("K"));
                }

                int[] blocked = regs.func_74775_l(key).func_74759_k("blocked");
                for (int i : blocked)
                {
                    entry.blocked.add(i);
                }
            }
            failedElements = GameData.injectSnapshot(snapshot, true, true);
        }

        if (failedElements != null && !failedElements.isEmpty())
        {
            String text = "Forge Mod Loader could not load this save.\n\n" +
            "There are "+failedElements.size()+" unassigned blocks and items in this save.\n" +
                    "You will not be able to load until they are present again.\n\n" +
                    "Missing Blocks/Items:\n";

            for (String s : failedElements) text += s + "\n";

            StartupQuery.notify(text);
            StartupQuery.abort();
        }
    }


    @Override
    public Certificate getSigningCertificate()
    {
        Certificate[] certificates = getClass().getProtectionDomain().getCodeSource().getCertificates();
        return certificates != null ? certificates[0] : null;
    }

    @Override
    public File getSource()
    {
        return FMLSanityChecker.fmlLocation;
    }

    @Override
    public Class<?> getCustomResourcePackClass()
    {
        return getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
    }

    @Override
    public String getGuiClassName()
    {
        return "net.minecraftforge.fml.client.FMLConfigGuiFactory";
    }

    @Override
    public Object getMod()
    {
        return this;
    }
}
