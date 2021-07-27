package it.digifox03.reselect.api;

public interface ReselectorUser {
    /**
     * This method is called when the reselector resources are reloaded.
     * It is guaranteed to be called at least once before any rendering
     * (with the exception of the loading screen) happens.
     * When this method is called, all reselectors generated from
     * previous calls become invalid.
     * @param compiler Used to require a reselector {@link ReselectorCompiler}
     */
    void onReselectorReload(ReselectorCompiler compiler);
}
