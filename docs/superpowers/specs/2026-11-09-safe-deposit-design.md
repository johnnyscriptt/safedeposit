# Safe Deposit: plan

## Context

New standalone Fabric mod for MC 26.3. A **Safe Deposit** button on chest screens moves
everything from your inventory into the chest **except slots you have locked**. You lock a
slot by holding a key and clicking it. The goal is unloading after a mining trip in one click
without losing your sword, pickaxe or food. Stardew Chest Buttons' "Add to Existing" only moves
item types the chest already holds; this is the bolder version, with protection.

### Decided by the user
- Standalone mod named **Safe Deposit** (modid `safedeposit`, slug `safe-deposit`, repo
  `johnnyscriptt/safedeposit`, package `io.github.johnnyscriptt.safedeposit`).
- Locking is **hold a key + click a slot**.
- **Client + server**, built the Chest Buttons way: one C2S payload, the server moves the items.

### My calls (raised in chat, no objection)
- Locks belong to the **slot position**, not the item. They're saved in the client config and
  apply on every world and server.
- Only the 36 main and hotbar slots are ever deposited. Armour and offhand are never touched.
- Containers: chest, double chest, barrel, ender chest (`ChestMenu`) and shulker box (`ShulkerBoxMenu`).
- Locks only affect Safe Deposit. Shift-click, drop and sorting stay vanilla.
- Lock key default **Left Alt**, rebindable under Options, Controls, Safe Deposit.
- Accent `#4FC3F7` sky blue on a deep purple `#2A1650` icon background (no sibling uses these).
- On a server without the mod the button still appears, greyed out, with the tooltip "The server
  needs Safe Deposit" (brand rule). Locks still toggle because they're client-side.
- Button sits to the left of the chest panel. If `chestbuttons` is loaded, it moves down
  below Chest Buttons' two buttons (+48 px) so they share a column.

## Build (follow minecraft-mod-studio phases)

### 1. Scaffold
`python <skill>/scripts/new_mod.py "Safe Deposit" --dir "<minecraft mods>/Safe Deposit"
--accent "#4FC3F7" --description "One click deposits your inventory into a chest, except the slots you lock."
--wrapper-from "<minecraft mods>/Material Checklist"`, environment `*`. Then `git init -b main`,
brand commit identity, first commit. Write `.claude/CLAUDE.md`, the design note at
`docs/superpowers/specs/2026-11-09-safe-deposit-design.md` (this plan's decisions), and a memory
entry `safe-deposit-project` + MEMORY.md line.

### 2. Core, test-first (`core/`, no Minecraft imports, failing JUnit seen first)
- `core/SlotView`: copied from `Stardew Chest Buttons/src/main/java/com/example/chestbuttons/core/SlotView.java`
  **plus** `boolean accepts(Object key)` (needed so a shulker box is never pushed into a shulker box).
- `core/LockedSlots`: immutable 36-bit mask over inventory indices 0..35. `toggle(i)`,
  `isLocked(i)`, `toLong()`, `fromLong(long)` which drops bits above 35, and out-of-range indices are ignored.
- `core/SafeDeposit.run(List<SlotView> chest, List<IndexedSlot> player, LockedSlots locks)`:
  for each unlocked, non-empty player slot, top up matching chest stacks first, then fill empty
  chest slots that `accepts` the item (the same two passes as `core/AddToExisting.java` in Chest
  Buttons, without the "type already present" filter). What doesn't fit stays put.
- `core/config/SafeDepositConfig` { `long lockedMask` } with `clamp()`.
- Tests: `SafeDepositTest` (locked slots untouched; tops up before new stacks; respects
  `accepts`; full chest leaves remainder; mixed max stack sizes 64/16/1), `LockedSlotsTest`
  (toggle, round trip, bits above 35 stripped), config clamp. Reuse `FakeSlot` pattern from
  Chest Buttons tests.

### 3. Server
- `net/SafeDepositPayload(long lockedMask)`, a record with `StreamCodec`, registered with
  `PayloadTypeRegistry.serverboundPlay()` in `SafeDepositMod`.
- `server/DepositHandler`: modelled on `Stardew Chest Buttons/.../server/ContainerActions.java`.
  Requires `player.containerMenu` to be a `ChestMenu` or `ShulkerBoxMenu` and `stillValid`. Splits
  slots by `slot.container == player.getInventory()`, keeps player slots with
  `getContainerSlot()` 0..35, sanitises the mask with `LockedSlots.fromLong`, runs the core,
  then `broadcastChanges()`. Rate limit: 1 request per player per 250 ms.
- `server/SlotViewAdapter` + `StackKey`: copied from Chest Buttons. `accepts` → `slot.mayPlace(stack)`.

### 4. Client
- `SafeDepositClient`: registers the lock `KeyMapping` (category `safedeposit:main`) and loads
  `ConfigIo` (Gson `config/safedeposit.json`, Simple Autoclicker pattern).
- `ui/DepositButton`: `SpriteIconButton` like `ChestButtonWidgets.java`, added in
  `ScreenEvents.AFTER_INIT` for `ContainerScreen`/`ShulkerBoxScreen`. Sends the payload with the
  current mask. Disabled + tooltip when `!ClientPlayNetworking.canSend(TYPE)`. 16 px sprite
  hand-drawn from vanilla parts (chest + down arrow), no AI art.
- `ui/SlotLocking`: on any `AbstractContainerScreen` (inventory screen too), a Fabric screen
  mouse-click hook. If the lock key is physically held (check via the bound key with
  `InputConstants.isKeyDown`, since key mappings don't update inside screens), the hovered slot
  belongs to the player `Inventory` and its index is 0..35: toggle it, save the config, play
  vanilla `UI_BUTTON_CLICK`, and swallow the click. Hovered slot comes from an accessor mixin
  on `AbstractContainerScreen` (copy `AbstractContainerScreenAccessor`, add `hoveredSlot`).
- `render/LockOverlay`: after the screen renders, draw a tint (accent, alpha ~0.22)
  on each locked slot, colour only (the padlock was dropped 2026-11-09 at the user's request) (`leftPos + slot.x`, `topPos + slot.y`). Confirm the 26.3
  hook name with `javap` (scaffold.md, 26.3 API notes).
- Lang: button tooltip "Safe Deposit" + second line "Hold Left Alt and click a slot to lock it"
  (uses the actual bound key name).

### 5. Gametests
- `DepositGameTest` (client gametest): fill the inventory, lock slots 0 and 5, open a chest, press
  the button, assert locked slots kept, everything else in the chest; shulker-in-shulker case.
- Template `StoreShotsGameTest` / `IconRenderGameTest` filled later for the gallery and icon.

### 6. Brand and release (skill phases 5 to 8, each artefact shown to the user)
Icon pair (ChatGPT for CurseForge, in-game render for Modrinth/jar), 5 to 7 captioned gallery
shots (01 = locked hotbar glowing, chest filled), humanized + linted store copy, upload pack.
Repo private; Modrinth submit and first CurseForge upload only after explicit go-ahead.

## Verification
- `.\gradlew.bat test`: core tests green (each seen red first).
- `.\gradlew.bat runClientGameTest`: `DepositGameTest` passes.
- `.\gradlew.bat runServer`: dedicated server boots with no client classes loaded.
- `.\gradlew.bat build`, then quit Minecraft, swap `safedeposit-*.jar` into
  `%APPDATA%\.minecraft\mods\`. The user checks lock/unlock in the inventory and in chests, deposit
  into a chest, double chest, barrel, ender chest and shulker, Alt-click not clashing with
  Litematica/MiniHUD, Iris on/off, Faithful 32x overlay alignment, and Chest Buttons installed
  alongside (stacked column).
