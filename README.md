# Quick Deposit

A Fabric mod for Minecraft 26.3. One button next to a chest moves everything from your
inventory into it, except the slots you have locked. Lock your sword, pickaxe and food once,
then unload a whole mining trip in one click.

Player-facing details will be in [`upload-pack/copy/description-modrinth.md`](upload-pack/copy/description-modrinth.md).

## Using it

- **Lock a slot:** hold **Left Alt** and click one of your inventory or hotbar slots. It gets a
  blue tint and a padlock. Do it again to unlock. Works in the normal inventory and in any
  container screen. Locks belong to the slot, not the item, and are saved in
  `config/quickdeposit.json`.
- **Deposit:** open a chest, double chest, barrel, ender chest or shulker box and press the
  button to the left of it. Every unlocked slot of the 36 main and hotbar slots empties into
  the container: matching stacks are topped up first, then empty slots are used. Armour and
  offhand are never touched. Whatever doesn't fit stays where it was.

The lock key is rebindable in Options, Controls, under **Quick Deposit**.

## Multiplayer

The mod goes on both sides. The server does the moving (one request, checked and rate
limited server-side), because the client can't move items on its own without dozens of
simulated clicks. On a server without the mod the button is greyed out and says so; locks
still work since they live on your side.

## Playing with Stardew Chest Buttons

Both mods put buttons to the left of the chest. When Chest Buttons is installed, Quick Deposit
sits under its two buttons in the same column.

## Building

Needs JDK 25. From this folder:

```
.\gradlew.bat test                 unit tests (core/)
.\gradlew.bat build                build\libs\quickdeposit-<version>.jar
.\gradlew.bat runClientGameTest    in-game deposit test, screenshots in build\run\clientGameTest\screenshots
.\gradlew.bat runServer            dedicated server (run\server.properties uses port 25599)
```

`build-mod.bat` and `run-client.bat` do the same with a local JDK 25 path (machine-specific,
gitignored).

## Architecture

- `core/`: pure Java, no Minecraft imports, unit-tested. `QuickDeposit` (the two-pass
  deposit), `LockedSlots` (36-bit lock mask), `DepositRules` (rate limit),
  `config/QuickDepositConfig`.
- `src/main`: `QuickDepositPayload` (C2S, carries the lock mask), `server/DepositHandler`
  (validates the open menu, splits container and inventory slots, runs the core), and
  `SlotViewAdapter`/`StackKey` (live slots to the core's view; `accepts` uses `Slot.mayPlace`,
  so shulker boxes never go into shulker boxes).
- `src/client`: lock key, `ui/SlotLocking` (Alt-click hook and padlock overlay),
  `ui/DepositButton`, `config/ConfigIo`.
- `src/gametest`: `DepositGameTest` drives real input (Alt-click, button click) and checks the
  result on the server. Not in the jar.

## License

MIT, see [LICENSE](LICENSE).
