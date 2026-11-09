# Safe Deposit (Fabric mod)

Built and published with the `minecraft-mod-studio` skill; its references hold the brand,
store and upload conventions. This file only records what is specific to this mod.

## Platform and stack
- Windows (PowerShell) build machine. Minecraft 26.3, Fabric Loader 0.19.5, Fabric API
  0.161.0+26.3, Loom 1.18-SNAPSHOT, Gradle 9.7.1, Java 25, Mojang mappings (no remap step).
- JUnit 5 for `core/`; Fabric client gametests in `src/gametest`.

## Commands (run from this folder; the parent folder is not a Gradle project)
- `.\gradlew.bat test`, `.\gradlew.bat build`, `.\gradlew.bat runClientGameTest`.
- Deploy for testing: quit Minecraft, remove the old jar, copy
  `build\libs\safedeposit-<ver>.jar` into `%APPDATA%\.minecraft\mods\`.

## Conventions
- TDD: a failing test is seen before implementation. Pure logic lives in `core/` with no
  Minecraft imports; `src/test` only compiles against `src/main`.
- Accent colour `#4FC3F7`: in-game highlight, caption bar, icon glow.
- Default keys: Left Alt = lock slot (held while clicking a slot inside a screen).
- Store text: no em dashes. `.ps1` string literals stay ASCII.

## Domain notes
- Renamed from "Quick Deposit" on 2026-11-09 before release: that name was already taken on
  Modrinth and CurseForge by other authors' deposit mods.
- Client + server. The client sends only the lock mask; the server re-validates the menu
  (ChestMenu or ShulkerBoxMenu, stillValid, empty cursor), strips mask bits above 35 and
  rate-limits to one request per 5 ticks.
- Locks are inventory indices (0-8 hotbar, 9-35 main), never menu slot numbers, which differ
  per container. Armour (36-39) and offhand (40) are never deposited.
- Key mappings don't update inside screens: SlotLocking reads the physical key
  (`InputConstants.isKeyDown` on the bound key). `InputConstants.Type` is KEYBOARD/MOUSE in 26.3.
- `SlotView.accepts` maps to `Slot.mayPlace`, which is what keeps shulker boxes out of shulker boxes.
- Button shares Stardew Chest Buttons' left column; offset 48 px when `chestbuttons` is loaded.
- Design note: docs/superpowers/specs/2026-11-09-safe-deposit-design.md.

## Upload
- `upload-pack/UPLOAD-GUIDE.md` has every store field. Stores are logged in on the Windows
  Chrome. Modrinth: no AI images, in-game-render icon. CurseForge: ChatGPT icon.
