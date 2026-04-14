# CirexxSMP Core - Features & Commands

## 💰 Economy & Trading
The core of the server's economy, integrated with Vault.
- **Admin Shop** (`/shop`): Buy and sell items through a GUI. Prices configurable in `shop.yml`.
- **Auction House** (`/ah`): Global player marketplace.
  - `/ah`: View active listings (Pages: Infinite).
  - `/ah sell <price>`: Sell the item in your hand (5% Tax).
  - **Collection Bin**: Expired or cancelled items return to your bin.
- **Event Tokens** (`/tokens`): Secondary currency earned from server events.

## 🏆 Ranks & Progression
Automated progression system.
- **Rankup** (`/rankup`): Automatically advance to the next rank if you meet requirements.
  - **Requirements**: Money, Playtime, and Stats (e.g., Event Wins).
  - **Rks**: Wanderer -> Settler -> Trader -> Merchant -> Tycoon -> Legend.

## 🏠 Teleportation & Homes
Essential navigation tools.
- **Homes**: Save personal waypoints.
  - `/sethome [name]`, `/home [name]`, `/delhome [name]`, `/homes`.
- **Teleport Request**: Safe player-to-player teleportation.
  - `/tpa <player>`, `/tpahere <player>`, `/tpaccept`, `/tpdeny`.
- **Resource World** (`/rw`): Randomly teleport to a pristine world for gathering resources.
- **Player Warps** (`/pwarp`): (Work in Progress) User-defined warps.

## 🎭 Cosmetics & Perks
Fun extras for players.
- **Cosmetics GUI** (`/cosmetics`): Equip particle trails and effects.
- **Virtual Tools**:
  - `/wb`: Open a portable crafting table.
  - `/ec`: Open your Ender Chest anywhere.
  - `/hat`: Wear any item as a hat.
  - `/nick <name>`: Change your display name.

## ⚔️ PvP & Stats
- **PvP Toggle** (`/pvp`): Enable or disable PvP combat.
- **Stats Tracking**: Tracks Playtime and Event Wins for rankups.

## 🎉 Server Events
- **Event System**: Automated events like Mining Competitions and Chat Quizzes (managed by admins).
