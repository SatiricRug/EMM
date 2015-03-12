package net.minecraftforge.client.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader.VanillaLoader;
import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;

/*
 * Central hub for custom model loaders.
 */
public class ModelLoaderRegistry
{
    private static final Set<ICustomModelLoader> loaders = new HashSet<ICustomModelLoader>();
    private static final Map<ResourceLocation, IModel> cache = new HashMap<ResourceLocation, IModel>();

    /*
     * Makes system aware of your loader.
     */
    public static void registerLoader(ICustomModelLoader loader)
    {
        loaders.add(loader);
        ((IReloadableResourceManager)Minecraft.func_71410_x().func_110442_L()).func_110542_a(new IResourceManagerReloadListener()
        {
            public void func_110549_a(IResourceManager manager)
            {
                for(ICustomModelLoader loader : loaders) loader.func_110549_a(manager);
            }
        });
    }

    public static boolean loaded(ResourceLocation location)
    {
        return cache.containsKey(location);
    }


    public static ResourceLocation getActualLocation(ResourceLocation location)
    {
        if(location.func_110623_a().startsWith("builtin/")) return location;
        return new ResourceLocation(location.func_110624_b(), "models/" + location.func_110623_a());
    }

    public static IModel getModel(ResourceLocation location)
    {
        ResourceLocation actual = getActualLocation(location);
        if(cache.containsKey(location)) return cache.get(location);
        ICustomModelLoader accepted = null;
        for(ICustomModelLoader loader : loaders)
        {
            try
            {
                if(loader.accepts(actual))
                {
                    if(accepted != null)
                    {
                        FMLLog.severe("2 loaders (%s and %s) want to load the same model %s", accepted, loader, location);
                        throw new IllegalStateException("2 loaders want to load the same model");
                    }
                    accepted = loader;
                }
            }
            catch(Exception e)
            {
                FMLLog.log(Level.ERROR, e, "Exception checking if model %s can be loaded with loader %s, skipping", location, loader);
            }
        }

        // no custom loaders found, try vanilla one
        if(accepted == null)
        {
            if(VanillaLoader.instance.accepts(actual)) accepted = VanillaLoader.instance;
        }

        IModel model;
        if(accepted == null)
        {
            FMLLog.severe("no suitable loader found for the model %s, skipping", location);
            model = getMissingModel();
        }
        else try
        {
            model = accepted.loadModel(actual);
        }
        catch(Exception e)
        {
            FMLLog.log(Level.ERROR, e, "Exception loading model %s with loader %s, skipping", location, accepted);
            model = getMissingModel();
        }
        cache.put(location, model);
        return model;
    }

    public static IModel getMissingModel()
    {
        return ModelLoader.VanillaLoader.instance.getLoader().getMissingModel();
    }

    public static void clearModelCache()
    {
        cache.clear();
    }
}
