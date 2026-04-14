# CSMP-CORE

[![Version](https://img.shields.io/badge/Version-1.21.1-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

All-in-one core plugin for Survival/SMP servers. Built for Paper 1.21.x with async MySQL, Vault economy, and Adventure API.

---

## Features

**Auction House** — Players list items for sale with a 5% listing fee and 48-hour expiry. Unsold items return to a collection bin. Listing limits scale with rank (3 → 20 for Legend). All operations are fully async.

**Admin Shop** — GUI-based shop with multi-page navigation, configurable items/prices, and Vault buy/sell integration.

**Rank Progression** — 6-tier system: Wanderer → Settler → Trader → Merchant → Tycoon → Legend. Requirements per rank: playtime, money, and event wins. Auto-promotes via LuckPerms and deducts money on rankup.

**Server Events** — Chat Quiz (math questions, first correct answer wins tokens) and Mining Race (competitive block-breaking). Events run on a configurable timer (default: hourly).

**Event Tokens** — Secondary currency earned through events. YAML-backed with admin commands to give/take/set balances.

**Home System** — Up to N homes per player with scaling costs ($5k/$10k/$25k). Cached in memory on join, persisted async via MySQL.

**Player Warps** — Public warps with a $50,000 creation cost. MySQL-backed with in-memory cache.

**Teleport Requests** — `/tpa`, `/tpahere` ($100 cost), and `/tpaccept`. Standard request/accept flow.

**PvP Toggle** — Opt-in PvP system with combat tagging (15s). Cooldown before disabling (default: 5 min).

**Scoreboard** — Per-player sidebar showing rank, balance, tokens, active event, and server IP. Updates every second.

**Chat & Tab** — Custom chat formatting with rank prefix support. Configurable tab header/footer with player count.

**Cosmetics** — Particle trail selector via GUI menu.

**Perks** — `/hat`, `/ec` (portable ender chest), `/wb` (portable workbench), `/nick` (display name).

**Resource World** — `/rw` for random teleport into a designated resource world.

---

## Requirements

- Paper **1.21.1+**
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- [LuckPerms](https://luckperms.net/)
- An economy provider (e.g. EssentialsX)
- MySQL database

---

## Installation

1. Drop `CSMP-CORE.jar` into your `/plugins` folder.
2. Start the server once to generate config files, then stop it.
3. Fill in your MySQL credentials in `database.yml`.
4. Restart — tables are created automatically.

---

## Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/ah [sell <price>]` | Auction House | `csmp.auction` |
| `/shop` | Admin Shop | `csmp.shop` |
| `/rankup` | Rank up | `csmp.rankup` |
| `/tokens` | Check token balance | `csmp.tokens` |
| `/tokens give/take/set` | Manage tokens | `csmp.tokens.admin` |
| `/pvp` | Toggle PvP | `csmp.pvp.toggle` |
| `/sethome [name]` | Set a home | — |
| `/home [name]` | Teleport to home | — |
| `/homes` | List homes | — |
| `/delhome [name]` | Delete a home | — |
| `/pwarp [set/del/name]` | Player warps | — |
| `/tpa <player>` | Request teleport | — |
| `/tpahere <player>` | Request player to you | — |
| `/tpaccept` | Accept TPA request | — |
| `/rw` | Resource world | `csmp.resourceworld` |
| `/cosmetics` | Cosmetics menu | — |
| `/hat` | Wear item as hat | `csmp.perks.hat` |
| `/ec` | Portable ender chest | — |
| `/wb` | Portable workbench | — |
| `/nick <name>` | Set nickname | — |

---

## Configuration

| File | Purpose |
| :--- | :--- |
| `config.yml` | General settings (event timers, cooldowns, limits) |
| `database.yml` | MySQL credentials and HikariCP pool settings |
| `messages.yml` | All player-facing messages with placeholder support |
| `shop.yml` | Admin shop items, categories, and prices |

---

## Technical Details

- **Database**: MySQL via HikariCP connection pooling with prepared statement caching.
- **Threading**: All DB reads/writes run async via `BukkitScheduler`.
- **API**: Paper 1.21.1+ with Adventure/MiniMessage components (no legacy `ChatColor`).
- **Dependencies**: Vault, LuckPerms, economy provider.

---

Made by [Ciree] · Discord: `hitreg_69`
