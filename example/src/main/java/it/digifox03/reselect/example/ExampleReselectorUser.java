package it.digifox03.reselect.example;

import it.digifox03.reselect.api.ReselectorCompiler;
import it.digifox03.reselect.api.ReselectorUser;
import net.minecraft.util.Identifier;

public class ExampleReselectorUser implements ReselectorUser {
    static final Identifier ZOMBIE_ID = new Identifier("reselect_example", "zombie");

    @Override
    public void onReselectorReload(ReselectorCompiler compiler) {
        Reselectors.zombieReselector = compiler.get(ZOMBIE_ID, Reselectors.ZombieReselector.class);
    }
}
