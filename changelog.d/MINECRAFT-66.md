bump: minor

### Added
- Carrying capacity: only occupied main-inventory slots count (the hotbar never does), and each armor point is one safely carried slot. Over capacity, distance-driven spill checks pick a random main-inventory slot, empty ones included, and drop the whole stack. Standing still never spills. The spill distance and the per-tick cap are configurable (`blocksPerSpillCheck`, `maxChecksPerTick`).
- Stick Pack: a chest-slot pack with no armor that guarantees half of the main inventory is safe. When it breaks it disappears.
- Leather Backpack (Stick Pack + Leather): protects the whole main inventory. When it breaks it is replaced by a fresh Stick Pack.
