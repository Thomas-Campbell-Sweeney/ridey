package net.ahcho.ridey.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.ahcho.ridey.PokemonRideManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {
    
    @Inject(method = "canBeControlledByRider", at = @At("HEAD"), cancellable = true)
    private void onCanBeControlledByRider(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PokemonEntity self = (PokemonEntity)(Object)this;
        if (self.hasPassengers() && self.getFirstPassenger() instanceof PlayerEntity player) {
            Pokemon pokemon = self.getPokemon();
            PokemonRideManager.initializeStamina(pokemon.getUuid());
            double stamina = PokemonRideManager.getStamina(pokemon.getUuid());
            double speed = PokemonRideManager.getSpeed(pokemon);

            // Handle movement
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            boolean jumping = player.input.jumping;
            boolean sneaking = player.input.sneaking;
            float forward = player.input.movementForward;
            float sideways = player.input.movementSideways;

            // Calculate horizontal movement
            if (forward != 0 || sideways != 0) {
                float moveAngle = yaw + (sideways < 0 ? 90 : sideways > 0 ? -90 : 0);
                if (forward < 0) {
                    moveAngle -= 180;
                }
                moveAngle *= 0.017453292F; // Convert to radians

                double motionX = -MathHelper.sin(moveAngle) * speed;
                double motionZ = MathHelper.cos(moveAngle) * speed;
                double motionY = self.getVelocity().y;

                // Handle flying
                if (PokemonRideManager.canFly(pokemon)) {
                    if (stamina > 0 && jumping) {
                        motionY = 0.4;
                        PokemonRideManager.drainStamina(pokemon.getUuid());
                    } else if (sneaking) {
                        motionY = -0.4;
                    } else {
                        motionY *= 0.8; // Slow vertical momentum
                    }
                } else {
                    // Ground movement - apply gravity
                    motionY -= 0.08;
                }

                Vec3d movement = new Vec3d(motionX, motionY, motionZ);
                self.move(MovementType.SELF, movement);
                self.velocityModified = true;
            }

            // Handle stamina regeneration
            if (!jumping) {
                PokemonRideManager.regenerateStamina(pokemon.getUuid(), pokemon);
            }
        }
    }

    @Inject(method = "isLogicalSideForUpdatingMovement", at = @At("HEAD"), cancellable = true)
    private void onIsLogicalSideForUpdatingMovement(CallbackInfoReturnable<Boolean> cir) {
        Entity vehicle = (Entity)(Object)this;
        if (vehicle.hasPassengers() && vehicle.getFirstPassenger() instanceof PlayerEntity) {
            cir.setReturnValue(true);
        }
    }
} 