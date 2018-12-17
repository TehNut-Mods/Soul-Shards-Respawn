package info.tehnut.soulshards.core.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.awt.Point;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@JsonAdapter(MultiblockPattern.Serializer.class)
public class MultiblockPattern {

    public static final MultiblockPattern DEFAULT = new MultiblockPattern(
            new ItemStack(Items.DIAMOND),
            new String[] {
                    "OQO",
                    "QGQ",
                    "OQO"
            },
            new Point(1, 1),
            new HashMap<Character, Slot>() {{
                    put('O', new Slot(Blocks.OBSIDIAN));
                    put('Q', new Slot(
                            Blocks.QUARTZ_BLOCK.getDefaultState(),
                            Blocks.QUARTZ_PILLAR.getDefaultState(),
                            Blocks.CHISELED_QUARTZ_BLOCK.getDefaultState(),
                            Blocks.SMOOTH_QUARTZ.getDefaultState(),
                            Blocks.QUARTZ_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.DOUBLE),
                            Blocks.SMOOTH_QUARTZ_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.DOUBLE)
                    ));
                    put('G', new Slot(Blocks.GLOWSTONE));
             }}
    );

    private final ItemStack catalyst;
    private final String[] shape;
    private final Point origin;
    private final Map<Character, Slot> definition;

    public MultiblockPattern(ItemStack catalyst, String[] shape, Point origin, Map<Character, Slot> definition) {
        this.catalyst = catalyst;
        this.shape = shape;
        this.origin = origin;
        this.definition = definition;
        this.definition.put(' ', new Slot(Blocks.AIR.getDefaultState()));

        char originChar = shape[origin.y].charAt(origin.x);
        if (originChar == ' ' || definition.get(originChar).test(Blocks.AIR.getDefaultState()))
            throw new IllegalStateException("Origin point cannot be blank space.");

        int lineLength = shape[0].length();
        for (String line : shape) {
            if (line.length() != lineLength)
                throw new IllegalStateException("All lines in the shape must be the same size.");

            for (char letter : line.toCharArray())
                if (definition.get(letter) == null)
                    throw new IllegalStateException(letter + " is not defined.");
        }
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public TypedActionResult<Set<BlockPos>> match(World world, BlockPos originBlock) {
        Set<BlockPos> matched = Sets.newHashSet();
        for (int y = 0; y < shape.length; y++) {
            String line = shape[y];
            for (int x = 0; x < line.length(); x++) {
                BlockPos offset = originBlock.add(x - origin.x, 0, y - origin.y);
                BlockState state = world.getBlockState(offset);
                if (!definition.get(line.charAt(x)).test(state))
                    return new TypedActionResult<>(ActionResult.FAILURE, Collections.emptySet());

                matched.add(offset);
            }
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, matched);
    }

    public boolean isOriginBlock(BlockState state) {
        Slot slot = definition.get(shape[origin.y].charAt(origin.x));
        return slot.test(state);
    }

    public static class Slot implements Predicate<BlockState> {

        @JsonAdapter(SerializerBlockState.class)
        private final Set<BlockState> states;

        public Slot(BlockState... states) {
            this.states = Sets.newHashSet(states);
        }

        public Slot(Block block) {
            this(block.getStateFactory().getStates().toArray(new BlockState[0]));
        }

        @Override
        public boolean test(BlockState state) {
            return states.contains(state);
        }
    }

    public static class Serializer implements JsonDeserializer<MultiblockPattern> {
        @Override
        public MultiblockPattern deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = element.getAsJsonObject();

            Identifier itemId = new Identifier(json.getAsJsonObject("catalyst").getAsJsonPrimitive("item").getAsString());
            ItemStack catalyst = new ItemStack(Registry.ITEMS.get(itemId), 1);

            String[] shape = context.deserialize(json.getAsJsonArray("shape"), String[].class);
            Point origin = context.deserialize(json.getAsJsonObject("origin"), Point.class);
            Map<Character, Slot> definition = context.deserialize(json.getAsJsonObject("definition"), new TypeToken<Map<Character, Slot>>(){}.getType());

            return new MultiblockPattern(catalyst, shape, origin, definition);
        }
    }

    public static class SerializerBlockState implements JsonDeserializer<Set<BlockState>> {
        @Override
        public Set<BlockState> deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Set<BlockState> states = Sets.newHashSet();
            for (JsonElement entry : element.getAsJsonArray()) {
                if (entry.isJsonObject()) {
                    JsonObject json = entry.getAsJsonObject();
                    Block block = Registry.BLOCKS.get(new Identifier(json.getAsJsonPrimitive("block").getAsString()));
                    BlockState state = block.getDefaultState();
                    if (json.has("states")) {
                        JsonObject stateObject = json.getAsJsonObject("states");
                        for (Map.Entry<String, JsonElement> e : stateObject.entrySet()) {
                            Property property = block.getStateFactory().getProperty(e.getKey());
                            if (property != null) {
                                String valueString = e.getValue().getAsString();
                                Comparable value = (Comparable) property.getValue(valueString).get();
                                state = state.with(property, value);
                            }
                        }
                        states.add(state);
                    } else {
                        states.addAll(block.getStateFactory().getStates());
                    }
                }
            }

            return states;
        }
    }
}
