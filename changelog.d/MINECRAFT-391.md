bump: minor

### Added
- Emerald wallet: always-on consolidation of loose emeralds and emerald blocks across the main inventory, including the hotbar, into a stable 56-64 loose reserve, independent of any backpack. Surplus above 64 packs into blocks and a shortfall below 56 unpacks blocks, at the vanilla 9:1 ratio, with exact value conservation and no change when the reserve is already stable. A conversion that cannot fit in the inventory is deferred rather than ever dropping or duplicating items. The player's selected hotbar slot keeps the loose reserve when feasible, so a selected emerald stack stays usable for trading.
