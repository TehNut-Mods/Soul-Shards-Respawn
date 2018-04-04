package info.tehnut.soulshardsrespawn.command;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.command.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandSetShardEntity extends CommandBase {

    @Override
    public String getName() {
        return "setEnt";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.soulshardsrespawn.set_ent.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1)
            throw new WrongUsageException(getUsage(sender));

        ResourceLocation entityId = new ResourceLocation(args[0]);
        EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityEntry == null || !EntityLivingBase.class.isAssignableFrom(entityEntry.getEntityClass()))
            throw new CommandException("commands.soulshardsrespawn.error.not_a_entity");

        EntityPlayer user = getCommandSenderAsPlayer(sender);

        ItemStack stack = user.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            throw new CommandException("commands.soulshardsrespawn.error.not_a_shard");

        Binding binding = ((ItemSoulShard) stack.getItem()).getBinding(stack);
        if (binding == null)
            binding = new Binding(null, 0);

        ((ItemSoulShard) stack.getItem()).updateBinding(stack, binding.setBoundEntity(entityId));
        sender.sendMessage(new TextComponentTranslation("commands.soulshardsrespawn.set_ent.success", entityId).setStyle(new Style().setColor(TextFormatting.GREEN)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SoulShards.config.getEntityMap().entrySet()
                    .stream()
                    .filter(Map.Entry::getValue)
                    .map(e -> e.getKey().toString())
                    .collect(Collectors.toList()));
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
