package it.digifox03.reselect.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface ReselectorCompiler {
    /**
     * This method can be called to require a selector of the specified type.
     * An exception will be raised if the required reselector cannot be found
     * with the specified type. The exception must not be caught.
     * <br><br>
     * The reselector type MUST be an interface.
     * The interface MUST have exactly one non-default method
     * and it MUST be annotated with {@link ReselectMethod}.
     * The annotated method must return an {@link Identifier}
     * and it CANNOT have generic parameters or return type.
     *
     * @param id The identifier of the reselector
     * @param type The class of the type of the reselector
     * @param <T> The type of the reselector
     * @return An instance of the requested reselector
     */
    <@NotNull T> T get(@NotNull Identifier id, @NotNull Class<T> type);
}
