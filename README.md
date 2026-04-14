# 💎 CSMP-CORE

[![Version](https://img.shields.io/badge/Version-1.21.1-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**CSMP-CORE** is a high-performance, all-in-one suite designed for Survival/SMP servers. Built with modern Paper/Spigot APIs and asynchronous database logic, it provides a seamless experience for economy, progression, and player utilities.

---

## 🚀 Key Features

### ⚖️ Professional Auction House (`/ah`)
A global marketplace where players can trade items.
- **Async Processing**: All data operations (MySQL) run off the main thread to ensure 20 TPS.
- **Collection Bin**: Safe item return for expired or cancelled listings.
- **Tax System**: Configurable tax to balance the economy.

### 🛒 Dynamic Admin Shop (`/shop`)
A fully customizable GUI-based shop.
- Support for any item with custom NBT/Lore.
- Integrated with Vault.

### 🏆 Multi-Stage Rankup System (`/rankup`)
Keep players engaged with automated progression.
- **Requirements**: Playtime, Money, and Event Stats (MySQL tracked).
- **Ranks**: Wanderer → Settler → Trader → Merchant → Tycoon → Legend.

### 🏠 Navigation & Utilities
- **Home System**: Multiple homes, set/del/list functionality.
- **TPA System**: Safe player teleport requests (`/tpa`, `/tpahere`).
- **Resource World**: Random teleportation to fresh worlds (`/rw`).
- **Hat & Perks**: `/hat`, portable `/ec` (enderchest), and `/wb` (workbench).

### ⚔️ Combat & PvP Management
- **PvP Toggle**: Allow players to choose their playstyle (`/pvp`).
- **Event Tokens**: Unique secondary currency awarded for event participation.

---

## 🛠️ Technical Overview

- **Database**: Powered by **HikariCP** for high-efficiency connection pooling.
- **UI System**: Clean GUI layouts with multi-page support and interactive elements.
- **Messaging**: Managed via Adventure (MiniMessage-ready) with a central `messages.yml`.
- **API Support**: Deep integration with **Vault** and **LuckPerms**.

---

## 📋 Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/ah` | `csmp.auction` | Open the Auction House |
| `/shop` | `csmp.shop` | Open the Admin Shop |
| `/rankup` | `csmp.rankup` | Move to the next rank |
| `/tokens` | `csmp.tokens` | Check your event tokens |
| `/pvp` | `csmp.pvp.toggle` | Toggle your PvP status |
| `/rw` | `csmp.resourceworld` | Warp to the resource world |
| `/hat` | `csmp.perks.hat` | Wear the item in your hand |

---

## ⚙️ Installation

1. Ensure you are running **Paper 1.21.1+**.
2. Install dependencies: **Vault**, **LuckPerms**, and an Economy provider (e.g., EssentialsX).
3. Drop `CSMP-CORE.jar` into your `/plugins` folder.
4. Configure your MySQL credentials in `database.yml`.
5. Restart your server.

---

## 👨‍💻 Credits

- **Developer**: [Ciree2209](https://github.com/Ciree2209)
- **Discord**: hitreg_69
- **Engine**: Paper/Spigot API
- **License**: MIT

---

Developed with ❤️ for the Minecraft Community. 
Part of the CireeX High-Performance Suite.
