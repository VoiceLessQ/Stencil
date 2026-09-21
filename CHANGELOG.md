# Changelog

All notable changes to Stencil are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/). Versions follow
[Semantic Versioning](https://semver.org/); the `-alpha` suffix marks pre-release
research builds.

## [Unreleased]

### Added

- Search box on the settings screen that filters every tab's options by name.
- Hotkeys can be bound to mouse buttons, including side buttons.
- Ghost blocks render as the chosen block model instead of a coloured box.
- Tools: `Left Ctrl` plus wheel selects Place blocks, Place all, Change block
  or Extend facing side; right click runs it. Each tool has its own hotkey
  (`toolPlaceAll` = `V`, `toolChangeBlock` = `C`, `toolExtendSide` = `Left Alt`)
  for direct use. Both place tools take the block from the inventory, or
  conjure it in creative mode.
- `spawnProofSingleLayer` option: never offer a ghost on top of a placed block
  or the chosen block, so fills stay one block thick.
- `spawnProofLowGaps` option: mark one-block-high spots too, where short mobs
  can still spawn.

### Changed

- Hotkey capture: Escape cancels and keeps the old binding, Backspace or Delete
  clears it.

### Fixed

- Dropdown options ignored left click and cycled on right click, because
  Minecraft 26.3 numbers mouse buttons from 1.
- Number, colour and text boxes now commit on Enter or when clicking elsewhere,
  and show the clamped value.
- Dev client on GNOME Wayland had no title bar; the run config now uses X11.

## [0.1.0-alpha] - 2026-09-21

### Added

- Fabric client mod scaffold for Minecraft 26.3 on JDK 25.
- Config system with Litematica-compatible option names, persisted to
  `config/stencil.json`.
- Hotkey system with chorded bindings and duplicate-binding warnings.
- Settings screen with tabs, boolean, cycle, text and hotkey editors; a cancelled
  hotkey capture keeps the previous binding.
- Spawn proof overlay: ghost blocks on mob-spawnable positions in a square or
  circular area around the player, with a Layer mode for filling a flat level.
  Radius is adjustable per mode by hotkey or scroll wheel, and single sides can be
  extended up to the radius limit.

[Unreleased]: https://github.com/VoiceLessQ/Stencil/compare/v0.1.0-alpha...HEAD
[0.1.0-alpha]: https://github.com/VoiceLessQ/Stencil/releases/tag/v0.1.0-alpha
