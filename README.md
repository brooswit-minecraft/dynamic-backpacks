# dynamic-backpacks

Backpacks for Minecraft (NeoForge 1.21.1, Java 21). Mod ID: `dynamicbackpacks`.
MIT licensed. See epic MINECRAFT-66.

Carrying too much loot is risky: armor sets how many inventory slots you can
carry safely, and overloaded players spill whole stacks while moving. Backpacks
replace chest armor to protect cargo.

**Current state: core mechanic, Stick Pack and Leather Backpack.**

- Capacity counts occupied slots of the main inventory only (the hotbar is not
  inventory). One armor point is one safely carried slot.
- Over capacity, walking triggers spill checks (one per `blocksPerSpillCheck`
  blocks traveled): a random main-inventory slot is picked, empty slots
  included, and a hit drops the whole stack. Standing still never spills.
- Packs replace chest armor and give no armor points. Stick Pack: half the
  main inventory is guaranteed safe. Leather Backpack: all of it. A broken
  Leather Backpack becomes a fresh Stick Pack; a broken Stick Pack is gone.

## Building

Requires a JDK 21 with `javac` on `JAVA_HOME`.

```sh
./gradlew build
```
