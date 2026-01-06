# QOL Mod - Minecraft Quality of Life Improvements

A Fabric mod for Minecraft 1.21.11 that adds quality of life improvements to enhance your gameplay experience.

## Features

### 🛒 Virtual Shop System (In Development)
- **Command-based shop**: Use `/shop` to access a GUI shop
- **Virtual currency**: Earn and spend money without physical items
- **Buy ANY item**: Creative menu-style access to all Minecraft items
- **Sell items**: Turn your items into money
- **Passive income**: Earn money just by playing
- **Activity rewards**: Get paid for farming, logging, mining, and fishing
- **Configurable pricing**: Customize item values to your liking

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest release from [GitHub Releases](https://github.com/Uipg9/shop/releases)
4. Place both mods in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.4 or newer
- Fabric API 0.141.1+1.21.11 or newer
- Java 21 or newer

## Development

### Building from Source

```bash
git clone https://github.com/Uipg9/shop.git
cd shop
.\gradlew.bat build
```

The compiled JAR will be in `build/libs/`

### Development Environment Setup

1. Clone the repository
2. Import as a Gradle project in IntelliJ IDEA
3. Wait for Gradle sync to complete
4. Run `genSources` Gradle task
5. Use "Minecraft Client" run configuration to test

See `Information/PROJECT_SETUP_GUIDE.txt` for detailed setup instructions.

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
