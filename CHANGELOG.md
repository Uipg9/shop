# Changelog

## [Unreleased] - v1.0.26
### Added
- **🌐 Village Web Dashboard**: Revolutionary localhost web interface for village management
  - Access villages at `http://localhost:8080` from any web browser
  - Real-time village overview with level, worker count, daily profit, total value
  - Complete worker management: hire, fire, upgrade all worker types
  - Resource management: view stocks, transfer to trade center, bulk selling
  - Building control: construct buildings directly from web interface
  - Auto-manage toggle with live efficiency display (100% manual, 70% auto)
  - Responsive design works on desktop and mobile devices
  - Auto-refresh every 30 seconds + manual refresh button
  - Similar to minecraft-web-chat but specifically for village system
  - Comprehensive API endpoints for potential integrations
  - Full documentation in `Information/VILLAGE_WEB_DASHBOARD_GUIDE.txt`

### Technical
- Added Javalin 5.6.3 web server framework
- Added Java-WebSocket 1.5.4 for future real-time updates
- Added Jackson 2.15.2 for JSON processing
- Added SLF4J 2.0.7 for proper logging
- Integrated web server lifecycle with Minecraft server start/stop
- CORS configuration for local development
- RESTful API design following best practices

All notable changes to the Shop Mod will be documented in this file.

## [1.0.25] - 2025-01-06

### MASSIVE VILLAGE SYSTEM OVERHAUL

Complete redesign with interconnected resource economy, supply chains, and building mechanics.

### Added
- **Resource Economy**: 10 resource types (Food, Wood, Fish, Ore, Leather, Wool, Tools, Armor, Enchanted, Rare)
- **Worker Supply Chains**: 8 workers with input/output mechanics across 4 tiers
- **Building System**: 10 buildings with daily upkeep (House, Granary, Warehouse, Workshop, Market, etc.)
- **Housing System**: Build unlimited houses for 100+ worker capacity
- **Food & Strikes**: Workers need 1 food/day or go on strike
- **Village Progression**: 4 levels (Hamlet → Village → Town → City)
- **Auto-Manage Mode**: Toggle 70% efficiency auto vs 100% manual
- **Trade Center**: Separate storage with auto-sell functionality
- **Enhanced GUI**: 5 tabs including new comprehensive Guide tab
- **Enhanced /bal**: Shows village economics (salaries, income, profit)

### Changed
- VillageManager completely rewritten for complex economy
- Daily processing now includes village production and trade center auto-sell

## [1.0.24] - 2024-12-31
- Simple village system with passive income from workers
- Worker hire/fire/upgrade mechanics
- Daily salary payments

## [1.0.23] - Previous
- Bank space upgrades, bank returns, enhanced loans, /shop balance command

## [1.0.22] - Previous
- Loan system with daily payments and credit scores

## [1.0.21] - Previous
- Bank system with risky investments

## [1.0.20] - Previous
- Night vision toggle, teleportation system (/sethome, /home)

## [1.0.19] - Previous
- Tree harvester feature (auto-chop logs)

## [1.0.18] - Previous
- Bulk discount system (10+: 5% off, 50+: 10% off, 100+: 15% off)

## [1.0.17] - Previous
- Wallet interest system (10% daily)

## [1.0.15] - Previous
- 300+ shop items added across 5 tiers

## [1.0.10] - Initial Release
- Basic shop system with currency management
