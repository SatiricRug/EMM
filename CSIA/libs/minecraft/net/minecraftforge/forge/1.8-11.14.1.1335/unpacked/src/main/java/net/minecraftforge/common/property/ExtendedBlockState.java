package net.minecraftforge.common.property;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.BlockState.StateImplementation;
import net.minecraft.block.state.IBlockState;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;

import net.minecraft.block.state.BlockState.StateImplementation;
public class ExtendedBlockState extends BlockState
{
    private final ImmutableSet<IUnlistedProperty<?>> unlistedProperties;

    public ExtendedBlockState(Block blockIn, IProperty[] properties, IUnlistedProperty<?>[] unlistedProperties)
    {
        super(blockIn, properties, buildUnlistedMap(unlistedProperties));
        ImmutableSet.Builder<IUnlistedProperty<?>> builder = ImmutableSet.<IUnlistedProperty<?>>builder();
        for(IUnlistedProperty<?> property : unlistedProperties)
        {
            builder.add(property);
        }
        this.unlistedProperties = builder.build();
    }

    private static ImmutableMap<IUnlistedProperty<?>, Optional<?>> buildUnlistedMap(IUnlistedProperty<?>[] unlistedProperties)
    {
        ImmutableMap.Builder<IUnlistedProperty<?>, Optional<?>> builder = ImmutableMap.<IUnlistedProperty<?>, Optional<?>>builder();
        for(IUnlistedProperty<?> p : unlistedProperties)
        {
            builder.put(p, Optional.absent());
        }
        return builder.build();
    }

    @Override
    protected StateImplementation createState(Block block, ImmutableMap properties, ImmutableMap unlistedProperties)
    {
        return new ExtendedStateImplementation(block, properties, unlistedProperties, null);
    }

    protected static class ExtendedStateImplementation extends StateImplementation implements IExtendedBlockState
    {
        private final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;
        private Map<Map<IProperty, Comparable>, IBlockState> normalMap;

        protected ExtendedStateImplementation(Block block, ImmutableMap properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties, ImmutableTable<IProperty, Comparable, IBlockState> table)
        {
            super(block, properties);
            this.unlistedProperties = unlistedProperties;
            this.field_177238_c = table;
        }

        @Override
        public IBlockState func_177226_a(IProperty property, Comparable value)
        {
            if (!this.func_177228_b().containsKey(property))
            {
                throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + func_177230_c().func_176194_O());
            }
            else if (!property.func_177700_c().contains(value))
            {
                throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.field_149771_c.func_177774_c(func_177230_c()) + ", it is not an allowed value");
            }
            else
            {
                if(this.func_177228_b().get(property) == value)
                {
                    return this;
                }
                if(Iterables.all(unlistedProperties.values(), Predicates.<Optional<?>>equalTo(Optional.absent())))
                { // no dynamic properties present, looking up in the normal table
                    return super.func_177226_a(property, value);
                }
                Map<IProperty, Comparable> map = new HashMap<IProperty, Comparable>(func_177228_b());
                map.put(property, value);
                ImmutableTable<IProperty, Comparable, IBlockState> table = field_177238_c;
                table = ((StateImplementation)table.get(property, value)).getPropertyValueTable();
                return new ExtendedStateImplementation(func_177230_c(), ImmutableMap.copyOf(map), unlistedProperties, table);
            }
        }

        public <V> IExtendedBlockState withProperty(IUnlistedProperty<V> property, V value)
        {
            if(!this.unlistedProperties.containsKey(property))
            {
                throw new IllegalArgumentException("Cannot set unlisted property " + property + " as it does not exist in " + func_177230_c().func_176194_O());
            }
            if(!property.isValid(value))
            {
                throw new IllegalArgumentException("Cannot set unlisted property " + property + " to " + value + " on block " + Block.field_149771_c.func_177774_c(func_177230_c()) + ", it is not an allowed value");
            }
            Map<IUnlistedProperty<?>, Optional<?>> newMap = new HashMap<IUnlistedProperty<?>, Optional<?>>(unlistedProperties);
            newMap.put(property, Optional.fromNullable(value));
            if(Iterables.all(newMap.values(), Predicates.<Optional<?>>equalTo(Optional.absent())))
            { // no dynamic properties, lookup normal state
                return (IExtendedBlockState) normalMap.get(func_177228_b());
            }
            return new ExtendedStateImplementation(func_177230_c(), func_177228_b(), ImmutableMap.copyOf(newMap), field_177238_c);
        }

        public Collection<IUnlistedProperty<?>> getUnlistedNames()
        {
            return Collections.unmodifiableCollection(unlistedProperties.keySet());
        }

        public <V>V getValue(IUnlistedProperty<V> property)
        {
            if(!this.unlistedProperties.containsKey(property))
            {
                throw new IllegalArgumentException("Cannot get unlisted property " + property + " as it does not exist in " + func_177230_c().func_176194_O());
            }
            return property.getType().cast(this.unlistedProperties.get(property).orNull());
        }

        public ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties()
        {
            return unlistedProperties;
        }

        @Override
        public void func_177235_a(Map map)
        {
            this.normalMap = map;
            super.func_177235_a(map);
        }
    }
}
