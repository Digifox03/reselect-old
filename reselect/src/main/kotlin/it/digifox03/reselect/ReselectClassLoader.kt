package it.digifox03.reselect

internal class ReselectClassLoader(parent: ClassLoader) : ClassLoader(parent) {
    internal fun loadClass(data: ByteArray): Class<*> {
        return defineClass(null, data, 0, data.size)
    }
}
