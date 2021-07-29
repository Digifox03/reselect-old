package it.digifox03.reselect.example;

import it.digifox03.reselect.api.ReselectMethod;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

public class Reselectors {
    public interface ZombieReselector {
        @ReselectMethod
        Identifier reselect(ZombieEntity targetEntity);
    }
    private static final Identifier ZOMBIE = new Identifier("textures/entity/zombie/zombie.png");
    public static ZombieReselector zombieReselector = targetEntity -> ZOMBIE;
    public static Identifier reselectZombie(ZombieEntity zombieEntity) {
        return zombieReselector.reselect(zombieEntity);
    }
}
