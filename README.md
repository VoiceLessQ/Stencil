# Stencil

Client-side Fabric mod for Minecraft 26.3. Spawn proofing with ghost blocks.

Stencil shows you where mobs can spawn and helps you fix it. Turn on the overlay and every dark, spawnable spot near you gets a ghost block. Pick any block that stops spawns, hold right click with a stick and walk; the ghosts fill in from your inventory as you go.

Choose a torch or lantern instead and the ghosts turn into a light plan: the fewest lights that leave nothing around you dark enough to spawn, spaced out on open ground and packed in where walls block the glow. Place all puts them down in one go.

## What it does

- **Spawn proof overlay** (`N,X`): a ghost block on every dark spot around you where a mob could spawn. `N,M` switches to Layer mode, which marks every empty block on your own Y level. `N,B` swaps square and circle, `Shift+Up/Down` changes the radius. `spawnProofSingleLayer` keeps a fill one block thick; `spawnProofLowGaps` also marks one-block gaps where spiders fit.
- **Light planning**: choose a torch, lantern, glowstone or any other light-giving block and the ghosts become a plan. Open ground gets a lattice about 19 blocks apart, walls and slopes get extra lights where the glow cannot reach. The plan covers `lightPlanRadius` (32) blocks around you and 16 up and down. `torchMaxSpawnLight` (0) is the highest light level mobs still spawn at.
- **Tools**: only work with the tool item in your main hand (`toolItem`, a stick by default). `Left Ctrl` plus the scroll wheel picks Place blocks, Place all, Replace blocks, Change block, Extend facing side or Mark area; right click runs it. Both place tools pull the block from your inventory (creative conjures it) and keep placing while held. Change block takes the block item in your off hand. Extend facing side uses the plain scroll wheel.
- **Marked area**: with Mark area, right click two opposite corner blocks and the ghosts stay inside that box instead of following you, from the corner blocks up to one block above the higher one. Ghosts show as empty outlines until you choose a block. While you hold the tool, the action bar shows how many blocks the area still needs, how many you carry and how many you are short. Extend facing side then grows or shrinks the box toward where you look, up and down included. Shift and right click clears the mark.
- **Replace blocks**: hold right click on a block to break it and put the chosen block in its place, for example to swap old carpets for slabs. It only touches blocks mobs cannot spawn on, or blocks Stencil placed this session, never containers or other block entities, and only inside the marked area when there is one. In survival the block is mined with what is in your hand, so it takes as long as mining normally does.
- **Reach**: the server only accepts placements within your interaction range, about 4.5 blocks. With op you can set `commandReach` and Stencil raises the range by `/attribute` while the overlay is on (the game caps it at 64).
- **Settings** (`N,C`): tabs, value editors and hotkey capture, saved to `config/stencil.json`. `ghostBlockAlpha` sets ghost opacity, `spawnProofGhostColor` tints them. The same actions are listed under Stencil in the game's Key Binds screen, unbound, for anyone who prefers a single vanilla key.
- **Command**: `/stencil list`, `/stencil get <option>`, `/stencil set <option> <value>` and `/stencil reset <option>` change the same settings from chat, with tab completion for names and values. Client-side, so it works on any server. Hotkeys take the key string (`/stencil set spawnProofToggle N,X`, or `none` to clear); unknown key names and item ids are refused.

Stencil is just a spawn proofing mod at the moment.

See [CHANGELOG.md](CHANGELOG.md) for the version history.
