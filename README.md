# 🏪 Shop Mod - Complete Economy System for Minecraft

[![Release](https://img.shields.io/github/v/release/Uipg9/shop?style=flat-square)](https://github.com/Uipg9/shop/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-green?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-dbd0b4?style=flat-square)](https://fabricmc.net/)

A comprehensive economy mod for Minecraft 1.21.11 (Fabric) featuring a dynamic shop system, banking with investments, stock market, lucky crates, and universal earnings from all gameplay activities.

## ✨ Key Features

### 🛒 Dynamic Shop System
- **17 organized categories** with 100+ items
- **Stock market pricing** - Buy low, sell high!
- **Creative-style browsing** - Access to all Minecraft items
- **Bulk buying** - Purchase items in stacks
- **Smart navigation** - Intuitive category system

### 🏦 Banking & Investments
- **27-slot secure storage** for valuable items
- **Investment system** with daily returns
  - Easy: +10% / -5% | Normal: +25% / -15% | Hard: +50% / -40%
- **Intuitive GUI** with quick buttons ($100, $1K, $10K, $100K, ALL)
- **Automatic daily processing** at dawn

### 📈 Stock Market
- **Dynamic pricing** (0.50x - 2.50x multipliers)
- **4 daily updates** (Dawn, Noon, Dusk, Midnight)
- **Affects all shop items** - Strategic buying opportunities
- **Synced to Minecraft time** - No real-world waiting!

### 🎰 Lucky Crates (Gambling)
- **5 tiers** from cheap to expensive
- **Daily refreshes** with randomized rewards
- **High-risk, high-reward** gameplay
- Money and rare item prizes

### 💰 Universal Earnings
- **Block Breaking**: Ores, crops, and more give money + XP
- **Mob Kills**: Combat rewards scale with difficulty
  - Bosses: $10K - $50K | Regular mobs: $60 - $200
- **Upgrade Integration**: Multiply all earnings
- **Action bar notifications** - Clean, non-intrusive

### ⚡ Upgrade System
- **4 upgrade types**: Income Multiplier, XP Multiplier, Mining Speed, Walking Speed
- **10 levels each** with increasing costs and benefits
- **Stack with all earnings** for maximum profit

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `/shop` | Open the main shop with 17 categories |
| `/bank` | Access banking and investment system |

## 📦 Installation

### Requirements
- **Minecraft**: 1.21.11 (Fabric)
- **Fabric Loader**: 0.18.4+
- **Fabric API**: 0.141.1+1.21.11
- **sgui**: 1.12.0+1.21.11
- **Java**: 21+

### Steps
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download [sgui](https://modrinth.com/mod/sgui) (Simple GUI library)
4. Download latest [Shop Mod release](https://github.com/Uipg9/shop/releases)
5. Place all mods in `.minecraft/mods` folder
6. Launch Minecraft!

## 📊 Shop Categories

<details>
<summary>View all 17 categories</summary>

1. **Building Blocks** - Stone, wood, concrete, glass, etc.
2. **Nature & Farming** - Seeds, saplings, flowers, crops
3. **Redstone & Tech** - Redstone components, rails, hoppers
4. **Combat & Tools** - Weapons, armor, tools (all tiers)
5. **Food & Potions** - Cooked foods, brewing ingredients
6. **Decorative** - Banners, carpets, paintings, pottery
7. **Nether Items** - Netherite, blaze rods, wither skulls
8. **End Items** - Shulker boxes, elytra, end crystals
9. **Ocean Items** - Prismarine, sea lanterns, nautilus shells
10. **Rare Items** - Totems, saddles, music discs, lodestones
11. **Enchantments** - All enchantments at all levels
12. **Mob Spawners** - Every mob type available
13. **Lucky Crates** - 5 gambling tiers
14. **Upgrades** - Income, XP, speed multipliers

</details>

## 💡 How to Play

### Getting Started
1. Use `/shop` to browse items
2. Start earning by mining, farming, or fighting mobs
3. Watch for stock market changes (prices update 4x per day)
4. Invest in the bank for passive income
5. Try lucky crates for bonus rewards!

### Making Money
- **Mining**: Break ores for instant cash ($5 - $200 per block)
- **Combat**: Kill mobs for rewards ($60 - $50,000 for bosses)
- **Farming**: Harvest crops for steady income
- **Investments**: Bank returns based on difficulty
- **Trading**: Buy low during market dips, hold for profit

### Maximizing Profits
1. **Buy upgrades** - Income/XP multipliers stack with everything
2. **Time the market** - Buy during 0.50x, sell during 2.50x periods
3. **Invest wisely** - Higher difficulty = higher potential returns
4. **Complete activities** - All gameplay rewards you!

## 🔧 Technical Details

### Features
- **Persistent data** - Player balances and investments saved automatically
- **Multiplayer ready** - Server-compatible with per-player economies
- **Performance optimized** - Efficient tick handling, no lag
- **Debug logging** - Comprehensive logs for troubleshooting
- **Action bar UI** - Clean notifications, no chat spam

### Statistics
- 10,750+ lines of code
- 46 files across 8 major systems
- 100+ buyable items
- 4 upgrade types with 10 levels each

## 🛠️ Development

### Building from Source

```bash
git clone https://github.com/Uipg9/shop.git
cd shop
gradlew build
```

The compiled JAR will be in `build/libs/shop-1.0.0.jar`

### Development Setup

1. Clone the repository
2. Import as Gradle project in IntelliJ IDEA
3. Run `genSources` Gradle task
4. Use "Minecraft Client" configuration to test

### Project Structure
```
src/main/java/com/shopmod/
├── bank/          # Banking & investment system
├── command/       # Command handlers
├── crates/        # Lucky crate system
├── currency/      # Money management
├── data/          # Persistent data storage
├── economy/       # Stock market & pricing
├── events/        # Block/mob earnings handlers
├── gui/           # Shop & bank GUIs
├── shop/          # Shop categories & items
├── spawner/       # Spawner pickup system
└── upgrades/      # Upgrade system
```

## 🐛 Known Issues

None currently! If you find bugs, please [open an issue](https://github.com/Uipg9/shop/issues).

## 🔮 Planned Features

- [ ] Smelting rewards system
- [ ] Additional gambling mini-games
- [ ] Player-to-player trading
- [ ] Configuration file for customization
- [ ] More upgrade tiers
- [ ] Shop themes/skins
- [ ] Auction house system
- [ ] Quest/achievement integration

## 📝 Changelog

### v1.0.0 (January 2026)
- 🎉 Initial release with complete economy system
- ✅ 17 shop categories with dynamic pricing
- ✅ Banking system with investments
- ✅ Stock market with 4x daily updates
- ✅ Lucky crate gambling
- ✅ Universal earnings (blocks + mobs)
- ✅ Upgrade system with multipliers
- ✅ Action bar notifications
- ✅ Minecraft time-based systems

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## 💬 Support

- **Issues**: [GitHub Issues](https://github.com/Uipg9/shop/issues)
- **Releases**: [GitHub Releases](https://github.com/Uipg9/shop/releases)

---

Made with ❤️ for the Minecraft communitySee `Information/PROJECT_SETUP_GUIDE.txt` for detailed setup instructions.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Planned Features

- 🔍 Item search in shop
- 📊 Detailed statistics and leaderboards  
- 🎯 Daily quests and challenges
- 💎 Shop upgrades and perks
- 🎁 Item bundles and special offers

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

- Built with [Fabric](https://fabricmc.net/)
- Uses [Fabric API](https://github.com/FabricMC/fabric)

## Support

For bugs and feature requests, please use the [GitHub Issues](https://github.com/yourusername/qol-mod/issues) page.
