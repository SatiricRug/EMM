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

package net.minecraftforge.fml.common.registry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.Validate;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.entity.passive.EntityVillager.EmeraldForItems;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.passive.EntityVillager.ItemAndEmeraldToItem;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedBookForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.PriceInfo;
/**
 * Registry for villager trading control
 *
 * @author cpw
 *
 */
public class VillagerRegistry
{
    private static final VillagerRegistry INSTANCE = new VillagerRegistry();

    private Map<Class<?>, IVillageCreationHandler> villageCreationHandlers = Maps.newHashMap();
    private List<Integer> newVillagerIds = Lists.newArrayList();
    @SideOnly(Side.CLIENT)
    private Map<Integer, ResourceLocation> newVillagers;

    private VillagerRegistry()
    {
        init();
    }

    /**
     * Allow access to the {@link net.minecraft.world.gen.structure.StructureVillagePieces} array controlling new village
     * creation so you can insert your own new village pieces
     *
     * @author cpw
     *
     */
    public interface IVillageCreationHandler
    {
        /**
         * Called when {@link net.minecraft.world.gen.structure.MapGenVillage} is creating a new village
         *
         * @param random
         * @param i
         */
        StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int i);

        /**
         * The class of the root structure component to add to the village
         */
        Class<?> getComponentClass();


        /**
         * Build an instance of the village component {@link net.minecraft.world.gen.structure.StructureVillagePieces}
         * @param villagePiece
         * @param startPiece
         * @param pieces
         * @param random
         * @param p1
         * @param p2
         * @param p3
         * @param facing
         * @param p5
         */
        Object buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, @SuppressWarnings("rawtypes") List pieces, Random random, int p1,
                int p2, int p3, EnumFacing facing, int p5);
    }

    public static VillagerRegistry instance()
    {
        return INSTANCE;
    }

    /**
     * Register your villager id
     * @param id
     */
    public void registerVillagerId(int id)
    {
        if (newVillagerIds.contains(id))
        {
            FMLLog.severe("Attempt to register duplicate villager id %d", id);
            throw new RuntimeException();
        }
        newVillagerIds.add(id);
    }
    /**
     * Register a new skin for a villager type
     *
     * @param villagerId
     * @param villagerSkin
     */
    @SideOnly(Side.CLIENT)
    public void registerVillagerSkin(int villagerId, ResourceLocation villagerSkin)
    {
        if (newVillagers == null)
        {
            newVillagers = Maps.newHashMap();
        }
        newVillagers.put(villagerId, villagerSkin);
    }

    /**
     * Register a new village creation handler
     *
     * @param handler
     */
    public void registerVillageCreationHandler(IVillageCreationHandler handler)
    {
        villageCreationHandlers.put(handler.getComponentClass(), handler);
    }

    /**
     * Callback to setup new villager types
     *
     * @param villagerType
     * @param defaultSkin
     */
    @SideOnly(Side.CLIENT)
    public static ResourceLocation getVillagerSkin(int villagerType, ResourceLocation defaultSkin)
    {
        if (instance().newVillagers != null && instance().newVillagers.containsKey(villagerType))
        {
            return instance().newVillagers.get(villagerType);
        }
        return defaultSkin;
    }

    /**
     * Returns a list of all added villager types
     *
     * @return newVillagerIds
     */
    public static Collection<Integer> getRegisteredVillagers()
    {
        return Collections.unmodifiableCollection(instance().newVillagerIds);
    }

    public static void addExtraVillageComponents(@SuppressWarnings("rawtypes") ArrayList components, Random random, int i)
    {
        @SuppressWarnings("unchecked")
        List<StructureVillagePieces.PieceWeight> parts = components;
        for (IVillageCreationHandler handler : instance().villageCreationHandlers.values())
        {
            parts.add(handler.getVillagePieceWeight(random, i));
        }
    }

    public static Object getVillageComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, @SuppressWarnings("rawtypes") List pieces, Random random,
            int p1, int p2, int p3, EnumFacing facing, int p5)
    {
        return instance().villageCreationHandlers.get(villagePiece.field_75090_a).buildComponent(villagePiece, startPiece, pieces, random, p1, p2, p3, facing, p5);
    }

    public void register(VillagerProfession prof)
    {
        //blah
    }

    private boolean hasInit = false;
    private List<VillagerProfession> professions = Lists.newArrayList();



    private void init()
    {
        if (hasInit)
            return;

        VillagerProfession prof = new VillagerProfession("minecraft:farmer", "minecraft:textures/entity/villager/farmer.png");
        {
            register(prof);
            (new VillagerCareer(prof, "farmer"    )).init(VanillaTrades.trades[0][0]);
            (new VillagerCareer(prof, "fisherman" )).init(VanillaTrades.trades[0][1]);
            (new VillagerCareer(prof, "shepherd"  )).init(VanillaTrades.trades[0][2]);
            (new VillagerCareer(prof, "fletcher"  )).init(VanillaTrades.trades[0][3]);
        }
        prof = new VillagerProfession("minecraft:librarian", "minecraft:textures/entity/villager/librarian.png");
        {
            register(prof);
            (new VillagerCareer(prof, "librarian")).init(VanillaTrades.trades[1][0]);
        }
        prof = new VillagerProfession("minecraft:priest", "minecraft:textures/entity/villager/priest.png");
        {
            register(prof);
            (new VillagerCareer(prof, "cleric")).init(VanillaTrades.trades[2][0]);
        }
        prof = new VillagerProfession("minecraft:smith", "minecraft:textures/entity/villager/smith.png");
        {
            register(prof);
            (new VillagerCareer(prof, "armor" )).init(VanillaTrades.trades[3][0]);
            (new VillagerCareer(prof, "weapon")).init(VanillaTrades.trades[3][1]);
            (new VillagerCareer(prof, "tool"  )).init(VanillaTrades.trades[3][2]);
        }
        prof = new VillagerProfession("minecraft:butcher", "minecraft:textures/entity/villager/butcher.png");
        {
            register(prof);
            (new VillagerCareer(prof, "butcher")).init(VanillaTrades.trades[4][0]);
            (new VillagerCareer(prof, "leather")).init(VanillaTrades.trades[4][1]);
        }
    }

    public static class VillagerProfession
    {
        private ResourceLocation name;
        private ResourceLocation texture;
        private List<VillagerCareer> careers = Lists.newArrayList();

        public VillagerProfession(String name, String texture)
        {
            this.name = new ResourceLocation(name);
            this.texture = new ResourceLocation(texture);
        }

        private void register(VillagerCareer career)
        {
            Validate.isTrue(!careers.contains(career), "Attempted to register career that is already registered.");
            Validate.isTrue(career.profession == this, "Attempted to register career for the wrong profession.");
            career.id = careers.size();
            careers.add(career);
        }
    }

    public static class VillagerCareer
    {
        private VillagerProfession profession;
        private String name;
        private int id;
        public VillagerCareer(VillagerProfession parent, String name)
        {
            this.profession = parent;
            this.name = name;
            parent.register(this);
        }

        private VillagerCareer init(EntityVillager.ITradeList[][] traids)
        {
            return this;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (!(o instanceof VillagerCareer)) return false;
            VillagerCareer oc = (VillagerCareer)o;
            return name.equals(oc.name) && profession == oc.profession;
        }
    }

    /**
     * Hook called when spawning a Villager, sets it's profession to a random registered profession.
     *
     * @param entity The new entity
     * @param rand The world's RNG
     */
    public static void setRandomProfession(EntityVillager entity, Random rand)
    {
        //TODO: Grab id range from internal registry
        entity.func_70938_b(rand.nextInt(5));
    }

    //TODO: Figure out a good generic system for this. Put on hold for Patches.

    private static class VanillaTrades
    {
        //This field is moved from EntityVillager over to here.
        //Moved to inner class to stop static initializer issues.
        //It is nasty I know but it's vanilla.
        private static final ITradeList[][][][] trades =
        {
            {
                {
                    {
                        new EmeraldForItems(Items.field_151015_O, new PriceInfo(18, 22)),
                        new EmeraldForItems(Items.field_151174_bG, new PriceInfo(15, 19)),
                        new EmeraldForItems(Items.field_151172_bF, new PriceInfo(15, 19)),
                        new ListItemForEmeralds(Items.field_151025_P, new PriceInfo(-4, -2))
                    },
                    {
                        new EmeraldForItems(Item.func_150898_a(Blocks.field_150423_aK), new PriceInfo(8, 13)),
                        new ListItemForEmeralds(Items.field_151158_bO, new PriceInfo(-3, -2))
                    },
                    {
                        new EmeraldForItems(Item.func_150898_a(Blocks.field_150440_ba), new PriceInfo(7, 12)),
                        new ListItemForEmeralds(Items.field_151034_e, new PriceInfo(-5, -7))
                    },
                    {
                        new ListItemForEmeralds(Items.field_151106_aX, new PriceInfo(-6, -10)),
                        new ListItemForEmeralds(Items.field_151105_aU, new PriceInfo(1, 1))
                    }
                },
                {
                    {
                        new EmeraldForItems(Items.field_151007_F, new PriceInfo(15, 20)),
                        new EmeraldForItems(Items.field_151044_h, new PriceInfo(16, 24)),
                        new ItemAndEmeraldToItem(Items.field_151115_aP, new PriceInfo(6, 6), Items.field_179566_aV, new PriceInfo(6, 6))
                    },
                    {
                        new ListEnchantedItemForEmeralds(Items.field_151112_aM, new PriceInfo(7, 8))
                    }
                },
                {
                    {
                        new EmeraldForItems(Item.func_150898_a(Blocks.field_150325_L), new PriceInfo(16, 22)),
                        new ListItemForEmeralds(Items.field_151097_aZ, new PriceInfo(3, 4))
                    },
                    {
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 0), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 1), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 2), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 3), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 4), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 5), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 6), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 7), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 8), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 9), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 10), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 11), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 12), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 13), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 14), new PriceInfo(1, 2)),
                        new ListItemForEmeralds(new ItemStack(Blocks.field_150325_L, 1, 15), new PriceInfo(1, 2))
                    }
                },
                {
                    {
                        new EmeraldForItems(Items.field_151007_F, new PriceInfo(15, 20)),
                        new ListItemForEmeralds(Items.field_151032_g, new PriceInfo(-12, -8))
                    },
                    {
                        new ListItemForEmeralds(Items.field_151031_f, new PriceInfo(2, 3)),
                        new ItemAndEmeraldToItem(Item.func_150898_a(Blocks.field_150351_n), new PriceInfo(10, 10), Items.field_151145_ak, new PriceInfo(6, 10))
                    }
                }
            },
            {
                {
                    {
                        new EmeraldForItems(Items.field_151121_aF, new PriceInfo(24, 36)),
                        new ListEnchantedBookForEmeralds()
                    },
                    {
                        new EmeraldForItems(Items.field_151122_aG, new PriceInfo(8, 10)),
                        new ListItemForEmeralds(Items.field_151111_aL, new PriceInfo(10, 12)),
                        new ListItemForEmeralds(Item.func_150898_a(Blocks.field_150342_X), new PriceInfo(3, 4))
                    },
                    {
                        new EmeraldForItems(Items.field_151164_bB, new PriceInfo(2, 2)),
                        new ListItemForEmeralds(Items.field_151113_aN, new PriceInfo(10, 12)),
                        new ListItemForEmeralds(Item.func_150898_a(Blocks.field_150359_w), new PriceInfo(-5, -3))
                    },
                    {
                        new ListEnchantedBookForEmeralds()
                    },
                    {
                        new ListEnchantedBookForEmeralds()
                    },
                    {
                        new ListItemForEmeralds(Items.field_151057_cb, new PriceInfo(20, 22))
                    }
                }
            },
            {
                {
                    {
                        new EmeraldForItems(Items.field_151078_bh, new PriceInfo(36, 40)),
                        new EmeraldForItems(Items.field_151043_k, new PriceInfo(8, 10))
                    },
                    {
                        new ListItemForEmeralds(Items.field_151137_ax, new PriceInfo(-4, -1)),
                        new ListItemForEmeralds(new ItemStack(Items.field_151100_aR, 1, EnumDyeColor.BLUE.func_176767_b()),
                        new PriceInfo(-2, -1))
                    },
                    {
                        new ListItemForEmeralds(Items.field_151061_bv, new PriceInfo(7, 11)),
                        new ListItemForEmeralds(Item.func_150898_a(Blocks.field_150426_aN), new PriceInfo(-3, -1))
                    },
                    {
                        new ListItemForEmeralds(Items.field_151062_by, new PriceInfo(3, 11))
                    }
                }
            },
            {
                {
                    {
                        new EmeraldForItems(Items.field_151044_h, new PriceInfo(16, 24)),
                        new ListItemForEmeralds(Items.field_151028_Y, new PriceInfo(4, 6))
                    },
                    {
                        new EmeraldForItems(Items.field_151042_j, new PriceInfo(7, 9)),
                        new ListItemForEmeralds(Items.field_151030_Z, new PriceInfo(10, 14))
                    },
                    {
                        new EmeraldForItems(Items.field_151045_i, new PriceInfo(3, 4)),
                        new ListEnchantedItemForEmeralds(Items.field_151163_ad, new PriceInfo(16, 19))
                    },
                    {
                        new ListItemForEmeralds(Items.field_151029_X, new PriceInfo(5, 7)),
                        new ListItemForEmeralds(Items.field_151022_W, new PriceInfo(9, 11)),
                        new ListItemForEmeralds(Items.field_151020_U, new PriceInfo(5, 7)),
                        new ListItemForEmeralds(Items.field_151023_V, new PriceInfo(11, 15))
                    }
                },
                {
                    {
                        new EmeraldForItems(Items.field_151044_h, new PriceInfo(16, 24)),
                        new ListItemForEmeralds(Items.field_151036_c, new PriceInfo(6, 8))
                    },
                    {
                        new EmeraldForItems(Items.field_151042_j, new PriceInfo(7, 9)),
                        new ListEnchantedItemForEmeralds(Items.field_151040_l, new PriceInfo(9, 10))
                    },
                    {
                        new EmeraldForItems(Items.field_151045_i, new PriceInfo(3, 4)),
                        new ListEnchantedItemForEmeralds(Items.field_151048_u, new PriceInfo(12, 15)),
                        new ListEnchantedItemForEmeralds(Items.field_151056_x, new PriceInfo(9, 12))
                    }
                },
                {
                    {
                        new EmeraldForItems(Items.field_151044_h, new PriceInfo(16, 24)),
                        new ListEnchantedItemForEmeralds(Items.field_151037_a, new PriceInfo(5, 7))
                    },
                    {
                        new EmeraldForItems(Items.field_151042_j, new PriceInfo(7, 9)),
                        new ListEnchantedItemForEmeralds(Items.field_151035_b, new PriceInfo(9, 11))
                    },
                    {
                        new EmeraldForItems(Items.field_151045_i, new PriceInfo(3, 4)),
                        new ListEnchantedItemForEmeralds(Items.field_151046_w, new PriceInfo(12, 15))
                    }
                }
            },
            {
                {
                    {
                        new EmeraldForItems(Items.field_151147_al, new PriceInfo(14, 18)),
                        new EmeraldForItems(Items.field_151076_bf, new PriceInfo(14, 18))
                    },
                    {
                        new EmeraldForItems(Items.field_151044_h, new PriceInfo(16, 24)),
                        new ListItemForEmeralds(Items.field_151157_am, new PriceInfo(-7, -5)),
                        new ListItemForEmeralds(Items.field_151077_bg, new PriceInfo(-8, -6))
                    }
                },
                {
                    {
                        new EmeraldForItems(Items.field_151116_aA, new PriceInfo(9, 12)),
                        new ListItemForEmeralds(Items.field_151026_S, new PriceInfo(2, 4))
                    },
                    {
                        new ListEnchantedItemForEmeralds(Items.field_151027_R, new PriceInfo(7, 12))
                    },
                    {
                        new ListItemForEmeralds(Items.field_151141_av, new PriceInfo(8, 10))
                    }
                }
            }
        };
    }
}
