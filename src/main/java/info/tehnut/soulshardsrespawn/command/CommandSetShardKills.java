//package info.tehnut.soulshardsrespawn.command;
//
//import info.tehnut.soulshardsrespawn.core.data.Binding;
//import info.tehnut.soulshardsrespawn.core.data.Tier;
//import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
//import net.minecraft.command.*;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.item.ItemStack;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.util.EnumHand;
//import net.minecraft.util.text.Style;
//import net.minecraft.util.text.TextComponentTranslation;
//import net.minecraft.util.text.TextFormatting;
//
//public class CommandSetShardKills extends CommandBase {
//
//    @Override
//    public String getName() {
//        return "setKills";
//    }
//
//    @Override
//    public String getUsage(ICommandSender sender) {
//        return "commands.soulshardsrespawn.set_kills.usage";
//    }
//
//    @Override
//    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//        if (args.length != 1)
//            throw new WrongUsageException(getUsage(sender));
//
//        int killCount;
//        try {
//            killCount = Math.min(Integer.parseInt(args[0]), Tier.maxKills);
//        } catch (NumberFormatException e) {
//            throw new NumberInvalidException("commands.generic.num.invalid", args[0]);
//        }
//
//        EntityPlayer user = getCommandSenderAsPlayer(sender);
//
//        ItemStack stack = user.getHeldItem(EnumHand.MAIN_HAND);
//        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
//            throw new CommandException("commands.soulshardsrespawn.error.not_a_shard");
//
//        Binding binding = ((ItemSoulShard) stack.getItem()).getBinding(stack);
//        if (binding == null)
//            binding = new Binding(null, 0);
//
//        ((ItemSoulShard) stack.getItem()).updateBinding(stack, binding.setKills(killCount));
//        sender.sendMessage(new TextComponentTranslation("commands.soulshardsrespawn.set_kills.success", killCount).setStyle(new Style().setColor(TextFormatting.GREEN)));
//    }
//}
