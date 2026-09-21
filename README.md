# Stencil

Client-side Fabric mod for Minecraft 26.3. Spawn proofing with ghost blocks.

## What it does

- **Spawn proof overlay** (`M,X`): a ghost block on every dark spot around you where a mob could spawn. `M,N` switches to Layer mode, which marks every empty block on your own Y level. `M,B` swaps square and circle, `Shift+Up/Down` changes the radius.
- **Light planning**: choose a torch, lantern, glowstone or any other light-giving block and the ghosts become a plan. Open ground gets a lattice about 19 blocks apart, walls and slopes get extra lights where the glow cannot reach. The plan covers `lightPlanRadius` (32) blocks around you and 16 up and down. `torchMaxSpawnLight` (0) is the highest light level mobs still spawn at.
- **Tools**: `Left Ctrl` plus the scroll wheel picks Place blocks, Place all, Change block or Extend facing side; right click runs it. Each tool also has its own key (`V`, `C`, `Left Alt`). Both place tools pull the block from your inventory (creative conjures it) and keep placing while held. Change block takes the block item in your hand.
- **Reach**: the server only accepts placements within your interaction range, about 4.5 blocks. With op you can set `commandReach` and Stencil raises the range by `/attribute` while the overlay is on (the game caps it at 64).
- **Settings** (`M,C`): tabs, value editors and hotkey capture, saved to `config/stencil.json`. `ghostBlockAlpha` sets ghost opacity, `spawnProofGhostColor` tints them. `spawnProofSingleLayer` keeps a fill one block thick; `spawnProofLowGaps` also marks one-block gaps where spiders fit.

Stencil is just a spawn proofing mod at the moment.

See [CHANGELOG.md](CHANGELOG.md) for the version history.

## Modrinth description

Stencil shows you where mobs can spawn and helps you fix it. Turn on the overlay and every dark, spawnable spot near you gets a ghost block. Pick any block that stops spawns, hold the place key and walk; the ghosts fill in from your inventory as you go.

Choose a torch or lantern instead and the ghosts turn into a light plan: the fewest lights that leave nothing around you dark enough to spawn, spaced out on open ground and packed in where walls block the glow. Place all puts them down in one go.

Client-side, Fabric, Minecraft 26.3. Ghost colour and opacity, radius, shape and every hotkey are in the settings screen.

## CurseForge description

Spawn proofing tool for Fabric. Stencil marks every spot around you where a mob could spawn with a ghost block, then places the block you chose there from your inventory while you hold the place key. Pick a torch or lantern and it plans lights for you instead, spaced so no spot stays dark enough to spawn, and Place all sets them in one go.

Client-side only. Works on servers, though placement stays within your normal reach unless you have op. Settings screen with hotkey capture, ghost colour and opacity, area shape and radius.
