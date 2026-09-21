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

Early alpha. The schematic side (loading, ghost rendering, filling) is not written yet.
What works today:

- Settings screen (`M,C` by default) with tabs, value editors and hotkey capture.
  Config is saved to `config/stencil.json`; option names match Litematica's, so a
  block lifted from `litematica.json` drops in.
- Spawn proof overlay (`M,X`): ghost blocks on every dark, mob-spawnable spot around
  you. `M,N` switches to Layer mode, which marks every empty block on your own Y level.
  `M,B` toggles square or circle area and `Shift+Up/Down` changes the radius.
  Tools: `Left Ctrl` plus the scroll wheel selects Place blocks, Place all, Change
  block or Extend facing side, and right click runs the selected one. Each tool also
  has its own key (`toolPlaceBlocks`, `toolPlaceAll` = `V`, `toolChangeBlock` = `C`,
  `toolExtendSide` = `Left Alt`) that uses it directly. Change block takes the block
  item in your hand when you right click a ghost. Both place tools pull the block from
  your inventory (creative mode conjures it), so nothing needs to be in hand, and both
  place keys keep placing while held, so a fill grows outward as you walk. Extend uses the wheel. With
  `spawnProofSingleLayer` (on by default) no ghost is offered on top of a block you
  placed or on top of the chosen block, so a fill stays one block thick.
  `spawnProofLowGaps` (on by default) also marks spots with only one block of
  headroom, such as under leaves, where spiders and other short mobs can spawn.

See [CHANGELOG.md](CHANGELOG.md) for the version history.
