//package info.tehnut.soulshardsrespawn.command;
//
//import net.minecraft.command.CommandBase;
//import net.minecraft.command.CommandException;
//import net.minecraft.command.ICommandSender;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.util.text.Style;
//import net.minecraft.util.text.TextComponentTranslation;
//import net.minecraft.util.text.TextFormatting;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class CommandKillCageBorn extends CommandBase {
//
//    @Override
//    public String getName() {
//        return "killall";
//    }
//
//    @Override
//    public String getUsage(ICommandSender sender) {
//        return "commands.soulshardsrespawn.kill_all.usage";
//    }
//
//    @Override
//    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//        EntityPlayer user = getCommandSenderAsPlayer(sender);
//        List<Entity> toKill = server.getWorld(user.dimension).loadedEntityList
//                .stream()
//                .filter(e -> e instanceof EntityLivingBase)
//                .filter(e -> e.getEntityData().getBoolean("cageBorn"))
//                .collect(Collectors.toList());
//
//        int killCount = toKill.size();
//        toKill.forEach(Entity::setDead);
//        sender.sendMessage(new TextComponentTranslation("commands.soulshardsrespawn.kill_all.success", killCount).setStyle(new Style().setColor(TextFormatting.GREEN)));
//    }
//}
