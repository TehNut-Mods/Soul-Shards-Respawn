package info.tehnut.soulshardsrespawn.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

public class BindingEvent extends Event {

    private final EntityLivingBase entity;
    private final IBinding binding;

    public BindingEvent(EntityLivingBase entity, IBinding binding) {
        this.entity = entity;
        this.binding = binding;
    }

    public IBinding getBinding() {
        return binding;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    @Cancelable
    public static class NewBinding extends BindingEvent {
        public NewBinding(EntityLivingBase entity, IBinding binding) {
            super(entity, binding);
        }
    }

    public static class GainSouls extends BindingEvent {

        private int amount;

        public GainSouls(EntityLivingBase entity, IBinding binding, int amount) {
            super(entity, binding);

            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    public static class GetEntityName extends Event {

        private final EntityLivingBase entity;
        @Nullable
        private ResourceLocation entityId;

        public GetEntityName(EntityLivingBase entity) {
            this.entity = entity;
        }

        public EntityLivingBase getEntity() {
            return entity;
        }

        @Nullable
        public ResourceLocation getEntityId() {
            return entityId;
        }

        public void setEntityId(@Nullable ResourceLocation entityId) {
            this.entityId = entityId;
        }
    }
}
