package info.tehnut.soulshardsrespawn.core.util.compat;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import info.tehnut.soulshardsrespawn.SoulShards;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CompatibilityPlugin {

    String value();

    class Gather {
        public static Set<ICompatibilityPlugin> gather(ASMDataTable dataTable) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Set<ICompatibilityPlugin> loaders = Sets.newHashSet();
            Set<ASMDataTable.ASMData> discoveredLoaders = dataTable.getAll(CompatibilityPlugin.class.getName());

            for (ASMDataTable.ASMData data : discoveredLoaders) {
                try {
                    if (!Loader.isModLoaded((String) data.getAnnotationInfo().get("value")))
                        continue;

                    Class<?> asmClass = Class.forName(data.getClassName());
                    if (!ICompatibilityPlugin.class.isAssignableFrom(asmClass)) {
                        SoulShards.LOGGER.error("Class at {} was annotated with @CompatibilityPlugin but is not an ICompatibilityPlugin.", data.getClassName());
                        continue;
                    }

                    SoulShards.LOGGER.debug("Discovered a compatibility plugin at {}", data.getClassName());
                    loaders.add((ICompatibilityPlugin) asmClass.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            SoulShards.LOGGER.debug("Discovered {} plugin(s) in {}", loaders.size(), stopwatch.stop());
            return loaders;
        }
    }
}
