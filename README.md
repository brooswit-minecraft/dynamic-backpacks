# dynamic-backpacks

Backpacks for Minecraft (NeoForge 1.21.1, Java 21). Mod ID: `dynamicbackpacks`.
MIT licensed. See epic MINECRAFT-66.

Carrying too much loot is risky: armor sets how many inventory slots you can
carry safely, and overloaded players spill whole stacks while moving. Backpacks
replace chest armor to protect cargo.

**Current state: core mechanic plus the full Phase 1 + Phase 2 pack set.**

- Capacity counts occupied slots of the main inventory only (the hotbar is not
  inventory). One armor point is one safely carried slot.
- Over capacity, walking triggers spill checks (one per `blocksPerSpillCheck`
  blocks traveled): a random main-inventory slot is picked, empty slots
  included, and a hit drops the whole stack. Standing still never spills.
- Packs replace chest armor and give no armor points. Stick Pack: half the
  main inventory is guaranteed safe. Leather Backpack and every upgrade:
  all of it. A broken upgraded pack degrades to a fresh Leather Backpack;
  a broken Leather Backpack becomes a fresh Stick Pack; a broken Stick Pack
  is gone.
- Upgrade path: `Stick Pack → Leather Backpack → one material-specialized pack`.
  Iron and Diamond Backpacks add no utility, just durability. Gold and
  Netherite attract nearby dropped items (a finite pull budget that empties
  as the inventory fills). Emerald pings when ore is nearby, searching an
  expanding radius. Glow keeps the wearer glowing. Diving converts drowning
  damage into pack durability once normal air runs out (the durability cost
  per point of damage is a 1:1 placeholder pending a design decision on a
  multiplier). Hopper periodically pushes carried items into whatever
  storage is below the player.
- Item icons for the new packs currently reuse the Leather Backpack texture
  as a placeholder; distinct art is a follow-up.
- Emerald wallet: always on, independent of any backpack. Every tick it
  counts loose emeralds and emerald blocks across the main inventory
  **including the hotbar** (unlike the carry/spill mechanic above, which
  excludes the hotbar) and settles them onto a stable 56-64 loose-emerald
  reserve - packing surplus above 64 into blocks, and unpacking blocks to
  cover a shortfall below 56, at the vanilla 9:1 ratio. The total value
  (loose + 9 x blocks) is conserved exactly, and a conversion that can't
  fit in the inventory is skipped rather than ever dropping or duplicating
  items. Eligible slots are the 36 main-inventory slots (`Inventory.items`,
  hotbar + the 27 non-hotbar slots); armor, offhand, the cursor stack,
  crafting grids and any open container are untouched. The player's
  currently selected hotbar slot keeps the loose reserve when that slot is
  itself eligible, so a selected emerald stack stays usable for trading.

## Building

Requires a JDK 21 with `javac` on `JAVA_HOME`.

```sh
./gradlew build
```
