package info.tehnut.soulshardsrespawn.compat.bloodmagic;

import WayofTime.bloodmagic.api.BloodMagicPlugin;
import WayofTime.bloodmagic.api.IBloodMagicAPI;
import WayofTime.bloodmagic.api.impl.BloodMagicAPI;
import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import info.tehnut.soulshardsrespawn.api.CageSpawnEvent;
import info.tehnut.soulshardsrespawn.core.util.compat.CompatibilityPlugin;
import info.tehnut.soulshardsrespawn.core.util.compat.ICompatibilityPlugin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@CompatibilityPlugin("bloodmagic")
public class BloodMagicCompatibilityPlugin implements ICompatibilityPlugin {

    @BloodMagicPlugin.Inject
    public static final IBloodMagicAPI BLOOD_MAGIC_API = null;

    public static boolean active;

    @Override
    public void handleConfiguration(Configuration configuration) {
        active = configuration.getBoolean("spawnsRequireLP", "compat.bloodmagic", false, "Requires each mob spawned to cost LP.");

        if (active)
            MinecraftForge.EVENT_BUS.register(this);
        else
            MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public void onCageSpawn(CageSpawnEvent event) {
        if (BLOOD_MAGIC_API == null) // Disabled or API failed to inject
            return;

        if (event.getShardBinding().getOwner() == null) { // Require owner to get network
            event.setCanceled(true);
            return;
        }

        EntityEntry entityEntry = EntityRegistry.getEntry(event.getToSpawn().getClass());
        if (entityEntry == null)
            return; // Should never happen

        // Spawns will syphon half of what the entity is sacrificed for
        int syphon = ((BloodMagicAPI) BLOOD_MAGIC_API).getValueManager().getSacrificial().getOrDefault(entityEntry.getRegistryName(), 25) / 2;
        if (syphon == 0) { // Disabled for sacrifice means no spawn
            event.setCanceled(true);
            return;
        }

        syphon *= event.getToSpawn().getHealth();

        SoulNetwork soulNetwork = NetworkHelper.getSoulNetwork(event.getShardBinding().getOwner());

        // Not enough LP to spawn. Cause nausea
        if (soulNetwork.getCurrentEssence() < syphon) {
            soulNetwork.causeNausea();
            event.setCanceled(true);
            return;
        }

        // And finally we can syphon
        soulNetwork.syphon(syphon);
    }
}
