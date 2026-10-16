bump: minor

### Added
- Iron, Gold, Diamond and Netherite Backpacks, plus the Emerald, Glow, Diving and Hopper utility packs, each crafted from a Leather Backpack and one material. All degrade on break into a fresh Leather Backpack.
- Gold/Netherite Backpack: attracts nearby dropped items using a finite pull-force budget that shrinks to zero as the main inventory fills, divided across eligible items by distance.
- Emerald Backpack: dings when ore is found within an expanding search radius (starting at 8 blocks, +8 each time the current radius is fully scanned with no hit, capped at 40), then resets. The scan is spread across ticks at a bounded read rate, using constant-time arithmetic to turn a sample index into a position rather than ever materializing the search volume, and skips unloaded/out-of-build-height positions without reading block state.
- Glow Pack: keeps the wearer glowing by refreshing the vanilla Glowing effect before the previous application expires.
- Diving Pack: once normal air runs out, drowning damage is converted into pack durability damage instead of player health. The durability cost per point of damage is 1:1, pending an explicit design decision on a multiplier (see the spec's open item); direct combat damage is untouched.
- Hopper Pack: while standing over a block with item storage, periodically pushes one carried stack into it using that block's own item-handler capability, so chests/barrels and modded machines each keep their normal insertion rules.
