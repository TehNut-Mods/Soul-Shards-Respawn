package info.tehnut.soulshardsrespawn.command;

import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

public class CommandSetShardOwner extends CommandBase {

    @Override
    public String getName() {
        return "setOwner";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.soulshardsrespawn.set_owner.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1)
            throw new WrongUsageException(getUsage(sender));

        EntityPlayer user = getCommandSenderAsPlayer(sender);
        EntityPlayer player = args.length == 0 ?  user : getPlayer(server, sender, args[0]);

        ItemStack stack = user.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            throw new CommandException("commands.soulshardsrespawn.error.not_a_shard");

        Binding binding = ((ItemSoulShard) stack.getItem()).getBinding(stack);
        if (binding == null)
            binding = new Binding(null, 0);

        ((ItemSoulShard) stack.getItem()).updateBinding(stack, binding.setOwner(player.getGameProfile().getId()));
        sender.sendMessage(new TextComponentTranslation("commands.soulshardsrespawn.set_owner.success", player.getDisplayName()).setStyle(new Style().setColor(TextFormatting.GREEN)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
