package info.tehnut.soulshardsrespawn.core.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

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
                    put('Q', new Slot(Blocks.QUARTZ_BLOCK));
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

    public ActionResult<Set<BlockPos>> match(World world, BlockPos originBlock) {
        Set<BlockPos> matched = Sets.newHashSet();
        for (int y = 0; y < shape.length; y++) {
            String line = shape[y];
            for (int x = 0; x < line.length(); x++) {
                BlockPos offset = originBlock.add(x - origin.x, 0, y - origin.y);
                BlockState state = world.getBlockState(offset);
                if (!definition.get(line.charAt(x)).test(state))
                    return new ActionResult<>(ActionResultType.FAIL, Collections.emptySet());

                matched.add(offset);
            }
        }

        return new ActionResult<>(ActionResultType.SUCCESS, matched);
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
            this(block.getStateContainer().getValidStates().toArray(new BlockState[0]));
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

            ResourceLocation itemId = new ResourceLocation(json.getAsJsonObject("catalyst").getAsJsonPrimitive("item").getAsString());
            ItemStack catalyst = new ItemStack(ForgeRegistries.ITEMS.getValue(itemId), 1);

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
                String state = entry.getAsJsonPrimitive().getAsString();
                if (state.contains("[")) {
                    String[] split = state.split("\\[");
                    split[1] = split[1].substring(0, split[1].lastIndexOf("]")); // Make sure brackets are removed from state

                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0]));
                    if (block == Blocks.AIR)
                        return Collections.singleton(block.getDefaultState());

                    StateContainer<Block, BlockState> blockState = block.getStateContainer();
                    BlockState returnState = blockState.getBaseState();

                    // Force our values into the state
                    String[] stateValues = split[1].split(","); // Splits up each value
                    for (String value : stateValues) {
                        String[] valueSplit = value.split("=");
                        Property prop = blockState.getProperty(valueSplit[0]);
                        if (prop != null && prop.parseValue(valueSplit[1]).isPresent())
                            returnState = returnState.with(prop, (Comparable) prop.parseValue(valueSplit[1]).get());
                    }
                } else {
                    states.addAll(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(state)).getStateContainer().getValidStates());
                }
            }

            return states;
        }
    }
}
