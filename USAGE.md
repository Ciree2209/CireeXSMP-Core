# CirexxSMP Core - Usage & Setup Guide

## Required Plugins
To ensure full functionality, the following plugins must be installed on your server:

1.  **Vault**: Handles Economy and Chat hookups.
2.  **LuckPerms**: Manages Ranks and Permissions.
3.  **ProtocolLib**: Required for advanced packet handling (NPCs, Cosmetics).
4.  **PlaceholderAPI**: (Optional) For specialized placeholders in chat/scoreboards.
5.  **Citizens**: (Optional) If you plan to use advanced NPCs defined in the plugin.

## Installation
1.  Place `CSMP-CORE.jar` into your server's `plugins` folder.
2.  Ensure all dependencies listed above are also in the `plugins` folder.
3.  Restart the server.
4.  Configure `config.yml` and `messages.yml` as needed.
5.  Set up your database connection in `config.yml`.

---

## Commands

### Economy & Shop
- **/shop**: Opens the main Shop GUI.
- **/ah**: Opens the Auction House (Global view).
- **/ah sell <price>**: Lists the item currently in your hand for the specified price (5% fee applies).
- **/tokens**: View your current Event Token balance.

### Rank System
- **/rankup**: Purchases the next rank in the hierarchy if requirements are met (Money + Playtime/Stats).

### Teleportation & Homes
- **/sethome [name]**: Sets a home at your current location.
- **/delhome [name]**: Deletes a saved home.
- **/home [name]**: Teleports you to a saved home.
- **/homes**: Lists all your saved homes.
- **/pwarp [name]**: Player Warp system.
- **/tpa <player>**: Request to teleport to another player.
- **/tpahere <player>**: Request another player to teleport to you.
- **/tpaccept**: Accept a pending teleport request.
- **/rw**: Random teleport to the Resource World (wipes periodically).

### Player Utils & Cosmetics
- **/pvp**: Toggles your PvP status (ON/OFF).
- **/cosmetics**: Opens the Cosmetics GUI (Particles, etc.).
- **/hat**: Puts the item in your hand on your head.
- **/workbench** (or `/wb`): Opens a portable crafting table.
- **/ec**: Opens your Ender Chest.
- **/nick <name>**: Sets your display nickname (supports color codes).

---

## Permissions
Access to perks is controlled via permission nodes (managed by LuckPerms).

- `csmp.perk.hat`
- `csmp.perk.workbench`
- `csmp.perk.ec`
- `csmp.perk.nick`
- `csmp.auctions.limit.X` (See AuctionManager for specific limits like `tycoon`, `legend`).

## Troubleshooting
- **Database Error**: Ensure your MySQL/SQLite credentials in `config.yml` are correct.
- **"Vault not found"**: Make sure you have both Vault and an Economy provider (like EssentialsX).
