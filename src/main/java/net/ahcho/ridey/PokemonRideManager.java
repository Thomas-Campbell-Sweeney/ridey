package net.ahcho.ridey;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.api.types.ElementalType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.UUID;

public class PokemonRideManager {
    private static final HashMap<UUID, Double> staminaMap = new HashMap<>();
    private static final double STAMINA_DRAIN_RATE = 0.1;
    private static final double MAX_STAMINA = 100.0;

    public static boolean canFly(Pokemon pokemon) {
        return pokemon.getTypes().stream()
                .map(ElementalType::getName)
                .map(String::toLowerCase)
                .anyMatch(type -> type.equals("flying") || type.equals("dragon"));
    }

    public static double getSpeed(Pokemon pokemon) {
        // Convert Pokemon's speed stat to a reasonable minecraft speed
        return 0.1 + (pokemon.getStats().getBase("speed") / 500.0);
    }

    public static double getStamina(UUID pokemonId) {
        return staminaMap.getOrDefault(pokemonId, MAX_STAMINA);
    }

    public static void initializeStamina(UUID pokemonId) {
        staminaMap.putIfAbsent(pokemonId, MAX_STAMINA);
    }

    public static void drainStamina(UUID pokemonId) {
        double currentStamina = staminaMap.getOrDefault(pokemonId, MAX_STAMINA);
        staminaMap.put(pokemonId, Math.max(0, currentStamina - STAMINA_DRAIN_RATE));
    }

    public static void regenerateStamina(UUID pokemonId, Pokemon pokemon) {
        // Regenerate based on Pokemon's HP percentage
        double regenRate = (pokemon.getHealth() / (double) pokemon.getMaxHealth()) * 0.05;
        double currentStamina = staminaMap.getOrDefault(pokemonId, 0.0);
        staminaMap.put(pokemonId, Math.min(MAX_STAMINA, currentStamina + regenRate));
    }

    public static void handleFlying(PlayerEntity player, Vec3d movement, double speed) {
        Vec3d velocity = movement.multiply(speed);
        player.getVehicle().setVelocity(velocity);
        player.getVehicle().velocityModified = true;
    }
} 