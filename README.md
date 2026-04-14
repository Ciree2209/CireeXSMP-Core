# 💎 CSMP-CORE

[![Version](https://img.shields.io/badge/Version-1.21.1-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**CSMP-CORE** is a high-performance, all-in-one suite designed for Survival/SMP servers. Built with modern Paper/Spigot APIs and asynchronous database logic, it provides a seamless experience for economy, progression, and player utilities.

CSMP-Core
All-in-one core plugin for survival/SMP servers. Built for Paper 1.21.x with async MySQL, Vault economy, and Adventure API.
Instead of running 10+ separate plugins, this handles everything a custom SMP needs in one jar.
Features
Auction House (/ah)

List items for sale with a 5% listing fee
Buy items from other players (5% tax on sale)
48-hour auto-expiry with collection bin for unsold items
Listing limits scale with rank (3 default → 20 for Legend)
Full async MySQL — no main thread blocking

Admin Shop (/shop)

GUI-based shop with configurable items, prices, and categories
Multi-page navigation
Supports custom NBT/lore items
Vault integrated buy/sell

Rank Progression (/rankup)

6-tier system: Wanderer → Settler → Trader → Merchant → Tycoon → Legend
Requirements per rank: playtime (via PLAY_ONE_MINUTE statistic), money, event wins
Auto-promotes through Vault permissions (works with LuckPerms)
Money is deducted on rankup

Server Events

Chat Quiz — Random math questions, first correct answer wins tokens
Mining Race — Competitive block-breaking event
Events run on a configurable timer (default: every hour)
Winners earn Event Tokens as secondary currency

Event Tokens (/tokens)

Secondary currency separate from Vault economy
Earned through server events
Admin commands: /tokens give, /tokens take, /tokens set
YAML-based storage with auto-save on shutdown

Home System

/sethome [name] — First 2 homes free, then scaling cost ($5k / $10k / $25k)
/home [name] — Defaults to "home" if no name given
/delhome [name] / /homes — Delete and list
Overwriting existing homes is free
Cached in memory on join, async MySQL persistence

Player Warps (/pwarp)

Public warps anyone can visit
$50,000 creation cost
Owner can delete their own warps
MySQL backed with in-memory cache

Teleport Requests

/tpa <player> — Request to teleport to someone
/tpahere <player> — Request someone to teleport to you ($100 cost)
/tpaccept — Accept incoming request

PvP Toggle (/pvp)

Opt-in PvP system for SMP
Combat tagging — can't toggle off during a fight (15s tag)
Configurable cooldown before you can disable PvP again (default: 5 min)

Scoreboard

Dynamic sidebar: rank, balance, tokens, current event, server IP
Updates every second
Per-player scoreboards (no cross-contamination)
Pulls data from Vault permissions and economy

Chat & Tab

Custom chat formatting with rank prefix support
Configurable tab header/footer with online player count

Resource World (/rw)

Random teleport to a designated resource world

Cosmetics (/cosmetics)

Particle trail system via GUI menu

Perks

/hat — Wear any item on your head
/ec — Portable ender chest
/wb — Portable workbench
/nick — Change display name

Technical Details

Database: MySQL with HikariCP connection pooling (prepared statement caching enabled)
Threading: All DB reads/writes run async via BukkitScheduler
API: Paper 1.21.1+ with Adventure components (no legacy ChatColor)
Dependencies: Vault, LuckPerms, economy provider (EssentialsX etc.)
Messages: Centralized messages.yml with placeholder support
Config files: config.yml, database.yml, messages.yml, shop.yml

Setup

Paper 1.21.1+ server
Install Vault + LuckPerms + an economy provider
Drop CSMP-CORE.jar into /plugins
Configure MySQL credentials in database.yml
Restart server — tables are created automatically

Commands
CommandDescriptionPermission/ah [sell <price>]Auction housecsmp.auction/shopAdmin shopcsmp.shop/rankupRank upcsmp.rankup/tokens [give/take/set]Event tokenscsmp.tokens / csmp.tokens.admin/pvpToggle PvPcsmp.pvp.toggle/sethome [name]Set a home—/home [name]Teleport to home—/homesList homes—/delhome [name]Delete home—/pwarp [set/del/name]Player warps—/tpa <player>TPA request—/tpahere <player>TPA here ($100)—/tpacceptAccept TPA—/rwResource worldcsmp.resourceworld/cosmeticsCosmetics menu—/hatWear item as hatcsmp.perks.hat/ecEnder chest—/wbWorkbench—/nick <n>Nickname—

Made by Ciree | Discord: hitreg_69

---

Developed with ❤️ for the Minecraft Community. 
Part of the CireeX High-Performance Suite.
