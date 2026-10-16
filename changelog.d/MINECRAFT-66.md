bump: minor

### Added
- Carrying capacity: only occupied main-inventory slots count (the hotbar never does), and each armor point is one safely carried slot. Over capacity, distance-driven spill checks pick a random main-inventory slot, empty ones included, and drop the whole stack. Standing still never spills. The spill distance and the per-tick cap are configurable (`blocksPerSpillCheck`, `maxChecksPerTick`).
- Stick Pack: a chest-slot pack with no armor that guarantees half of the main inventory is safe. When it breaks it disappears.
- Leather Backpack (Stick Pack + Leather): protects the whole main inventory. When it breaks it is replaced by a fresh Stick Pack.
- Iron, Gold, Diamond and Netherite Backpacks, plus the Emerald, Glow, Diving and Hopper utility packs, each crafted from a Leather Backpack and one material. All degrade on break into a fresh Leather Backpack.
- Gold/Netherite Backpack: attracts nearby dropped items using a finite pull-force budget that shrinks to zero as the main inventory fills, divided across eligible items by distance.
- Emerald Backpack: dings when ore is found within an expanding search radius (8 blocks, +8 every 20 ticks without a hit), then resets.
- Glow Pack: keeps the wearer glowing by refreshing the vanilla Glowing effect before the previous application expires.
- Diving Pack: once normal air runs out, drowning damage is converted into pack durability damage instead of player health. The durability cost per point of damage is 1:1, pending an explicit design decision on a multiplier (see the spec's open item); direct combat damage is untouched.
- Hopper Pack: while standing over a block with item storage, periodically pushes one carried stack into it using that block's own item-handler capability, so chests/barrels and modded machines each keep their normal insertion rules.
