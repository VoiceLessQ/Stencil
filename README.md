# Stencil

Client-side schematic overlay for Minecraft 26.3 (Fabric). Load a build, see it as a
ghost in the world, and fill it in.

Reads the `.litematic` NBT format so existing schematics work, but the mod itself is a
fresh implementation rather than a fork.

## Building

MC 26.3 needs JDK 25:

```
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

On this machine the user-level `~/.gradle/gradle.properties` pins an older JDK, so pass
it explicitly:

```
./gradlew "-Dorg.gradle.java.home=<jdk-25 path>" build
```

## Status

Scaffold only. Nothing renders yet.
