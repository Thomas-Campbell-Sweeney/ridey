package net.ahcho.ridey;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ridey implements ModInitializer {
    public static final String MOD_ID = "ridey";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Ridey - Server-side Pokemon Riding Mod");
        
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof PokemonEntity pokemonEntity) {
                Pokemon pokemon = pokemonEntity.getPokemon();
                
                if (pokemon.getOwnerUUID() != null && pokemon.getOwnerUUID().equals(player.getUuid())) {
                    if (player.isSneaking() && player.hasVehicle()) {
                        player.stopRiding();
                        return ActionResult.SUCCESS;
                    }
                    
                    if (!player.hasVehicle()) {
                        PokemonRideManager.initializeStamina(pokemon.getUuid());
                        player.startRiding(pokemonEntity, true);
                        
                        if (PokemonRideManager.canFly(pokemon)) {
                            player.sendMessage(Text.literal("§bThis Pokémon can fly! Jump to fly up, sneak to fly down."), true);
                        }
                        
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}
